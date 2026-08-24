package com.laneway.service.impl;

import com.laneway.entity.BusinessOutboxTask;
import com.laneway.enums.OutboxState;
import com.laneway.mapper.BusinessOutboxMapper;
import com.laneway.mapper.MessageMapper;
import com.laneway.mapper.SeckillReservationMapper;
import com.laneway.messaging.BusinessEvent;
import com.laneway.messaging.BusinessEventType;
import com.laneway.messaging.DeliveryRetryPolicy;
import com.laneway.messaging.ProjectionResult;
import com.laneway.service.IBusinessEventService;
import com.laneway.service.IBusinessProjectionService;
import com.laneway.utils.FailureMessages;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class BusinessEventServiceImpl implements IBusinessEventService {

  @Resource
  private BusinessOutboxMapper businessOutboxMapper;

  @Resource
  private MessageMapper messageMapper;

  @Resource
  private SeckillReservationMapper seckillReservationMapper;

  private final TransactionTemplate transactions;
  private final IBusinessProjectionService projector;
  private final MeterRegistry metrics;

  public BusinessEventServiceImpl(
      TransactionTemplate transactions,
      IBusinessProjectionService projector,
      MeterRegistry metrics) {
    this.transactions = new TransactionTemplate(transactions.getTransactionManager());
    this.transactions.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    this.projector = projector;
    this.metrics = metrics;
  }

  public static String resolveConsumerGroup(String kind) {
    return BusinessEventType.from(kind).getConsumerGroup();
  }

  @Override
  public void consume(BusinessEvent event, String group) {
    if (event.getSchemaVersion() != 1
        || event.getEventId() <= 0
        || event.getKind() == null
        || !group.equals(resolveConsumerGroup(event.getKind()))) {
      throw new IllegalArgumentException("事件版本、事件号或消费组无效");
    }
    long started = System.nanoTime();
    try {
      transactions.execute(
          tx -> {
            consumeLockedTask(event, group);
            return null;
          });
    } finally {
      metrics
          .timer("laneway.mq.consume", "group", group)
          .record(System.nanoTime() - started, TimeUnit.NANOSECONDS);
    }
  }

  private void consumeLockedTask(BusinessEvent event, String group) {
    // Lock only this event; effects, inbox entry, and task completion still commit in the same
    // transaction.
    BusinessOutboxTask task = businessOutboxMapper.selectEventForUpdate(event.getEventId());
    if (task == null) {
      throw new IllegalArgumentException("消息没有对应的持久化事件");
    }
    if (!event.getKind().equals(task.getKind())
        || event.getAggregateId() != task.getAggregateId()) {
      throw new IllegalArgumentException("消息内容与持久化事件不一致");
    }
    if (messageMapper.countInboxEvent(group, event.getEventId()) > 0) {
      businessOutboxMapper.markDuplicateDone(event.getEventId());
      metrics.counter("laneway.mq.consumptions", "group", group, "result", "duplicate").increment();
      return;
    }
    if (OutboxState.REVIEW.matches(task.getState())) {
      throw new IllegalStateException("事件等待人工复核");
    }
    if (projector.project(task) == ProjectionResult.CONTINUE) {
      appendFeedContinuation(task);
    }
    messageMapper.insertInboxEvent(group, event.getEventId());
    businessOutboxMapper.markConsumed(event.getEventId());
    metrics.counter("laneway.mq.consumptions", "group", group, "result", "success").increment();
  }

  private void appendFeedContinuation(BusinessOutboxTask task) {
    // Advance the cursor for this batch; persist subsequent batches separately with the original
    // publication time.
    long cursor = businessOutboxMapper.selectCursor(task.getId());
    businessOutboxMapper.insertFeedContinuation(
        task.getKind(), task.getAggregateId(), task.getUserId(), cursor, task.getCreatedAt());
  }

  /**
   * Record consumption failure and decide whether the broker may acknowledge this message.
   *
   * @return true if the task is complete or its failure is durably quarantined; false if redelivery
   *     is required. Invalid payloads are quarantined by broker identity and cannot use eventId to
   *     mark another real task
   *     REVIEW.
   */
  @Override
  public boolean recordConsumptionFailure(
      BusinessEvent event,
      String group,
      String brokerId,
      String payload,
      RuntimeException failure,
      int deliveries) {
    return Boolean.TRUE.equals(
        transactions.execute(
            tx -> {
              boolean valid = false;
              if (event != null && event.getEventId() > 0) {
                BusinessOutboxTask task =
                    businessOutboxMapper.selectEventForUpdate(event.getEventId());
                if (task != null) {
                  try {
                    valid =
                        event.getSchemaVersion() == 1
                            && group.equals(resolveConsumerGroup(event.getKind()))
                            && Objects.equals(event.getKind(), task.getKind())
                            && event.getAggregateId() == task.getAggregateId();
                  } catch (RuntimeException invalid) {
                    valid = false;
                  }
                  if (valid && OutboxState.DONE.matches(task.getState())) {
                    return true;
                  }
                  if (valid) {
                    businessOutboxMapper.recordConsumeFailure(
                        FailureMessages.summarize(failure), event.getEventId());
                  }
                }
              }
              if (valid && deliveries < DeliveryRetryPolicy.MAX_CONSUME_ATTEMPTS) {
                return false;
              }
              String key = valid ? "outbox:" + event.getEventId() : "broker:" + brokerId;
              messageMapper.insertFailure(
                  group,
                  key,
                  valid ? event.getEventId() : null,
                  brokerId,
                  payload,
                  FailureMessages.summarize(failure));
              if (valid) {
                businessOutboxMapper.markReview(event.getEventId());
              }
              if (valid && "SECKILL_FULFILL".equals(event.getKind())) {
                if (seckillReservationMapper.markProcessingForReview(
                        FailureMessages.summarize(failure), event.getAggregateId())
                    > 0) {
                  seckillReservationMapper.insertDeadLetter(
                      "mq:outbox:" + event.getEventId(),
                      event.getAggregateId(),
                      payload,
                      FailureMessages.summarize(failure));
                }
              }
              metrics
                  .counter("laneway.mq.consumptions", "group", group, "result", "quarantined")
                  .increment();
              return true;
            }));
  }
}
