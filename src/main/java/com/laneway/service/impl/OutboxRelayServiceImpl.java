package com.laneway.service.impl;

import com.laneway.entity.BusinessOutboxTask;
import com.laneway.enums.OutboxState;
import com.laneway.mapper.BusinessOutboxMapper;
import com.laneway.mapper.SeckillReservationMapper;
import com.laneway.messaging.BusinessEvent;
import com.laneway.messaging.DeliveryRetryPolicy;
import com.laneway.messaging.EventPublisher;
import com.laneway.service.IBusinessEventService;
import com.laneway.service.IOutboxRelayService;
import com.laneway.utils.FailureMessages;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
public class OutboxRelayServiceImpl implements IOutboxRelayService {

  @Resource
  private BusinessOutboxMapper businessOutboxMapper;

  @Resource
  private SeckillReservationMapper seckillReservationMapper;

  private final TransactionTemplate transactions;
  private final ObjectProvider<EventPublisher> publishers;
  private final IBusinessEventService consumer;
  private final MeterRegistry metrics;
  private final AtomicLong pending = new AtomicLong();
  private final AtomicLong review = new AtomicLong();
  private final AtomicLong inflight = new AtomicLong();

  @Value("${laneway.mq.enabled:false}")
  private boolean mqEnabled;

  @Value("${laneway.mq.lease-seconds:60}")
  private int leaseSeconds;

  @Value("${laneway.mq.relay-batch-size:20}")
  private int batchSize;

  @Value("${laneway.mq.redelivery-seconds:300}")
  private int redeliverySeconds;

  public OutboxRelayServiceImpl(
      TransactionTemplate transactions,
      ObjectProvider<EventPublisher> publishers,
      IBusinessEventService consumer,
      MeterRegistry metrics) {
    this.transactions = transactions;
    this.publishers = publishers;
    this.consumer = consumer;
    this.metrics = metrics;
    metrics.gauge("laneway.outbox.pending", pending);
    metrics.gauge("laneway.outbox.review", review);
    metrics.gauge("laneway.outbox.inflight", inflight);
  }

  /**
   * Claim a batch of deliverable tasks and commit their leases in a short transaction.
   *
   * <p>SKIP LOCKED lets workers claim different tasks; send after commit to avoid holding row locks
   * during network calls.
   */
  @Override
  public List<BusinessOutboxTask> claimOutboxBatch(String leaseOwner) {
    return transactions.execute(
        tx -> {
          List<BusinessOutboxTask> tasks =
              businessOutboxMapper.selectClaimableEvents(Math.max(1, Math.min(100, batchSize)));
          for (BusinessOutboxTask task : tasks) {
            businessOutboxMapper.claimEvent(leaseOwner, effectiveLeaseSeconds(), task.getId());
          }
          return tasks;
        });
  }

  /**
   * Claim and deliver one outbox batch without guaranteeing that all pending tasks are drained.
   *
   * <p>Each batch uses a distinct lease owner; owner predicates prevent expired or overlapping
   * workers from overwriting newer results.
   */
  @Override
  public void relayOutboxBatch() {
    // A distinct batch owner prevents overlapping calls in one process from overwriting each other
    // after lease expiry.
    String leaseOwner = UUID.randomUUID().toString();
    for (BusinessOutboxTask task : claimOutboxBatch(leaseOwner)) {
      sendClaimedTask(task, leaseOwner);
    }
  }

  private void sendClaimedTask(BusinessOutboxTask task, String leaseOwner) {
    long started = System.nanoTime();
    try {
      if (businessOutboxMapper.renewLease(effectiveLeaseSeconds(), task.getId(), leaseOwner) == 0) {
        return;
      }
      if (deliverOutboxTask(task, leaseOwner)) {
        metrics.counter("laneway.mq.sends", "result", "success").increment();
      }
    } catch (RuntimeException failure) {
      recordSendFailure(task, leaseOwner, failure);
      metrics.counter("laneway.mq.sends", "result", "retry").increment();
      log.warn("事件 {} 投递失败，将按原事件号重试: {}", task.getId(), FailureMessages.summarize(failure));
    } finally {
      metrics.timer("laneway.mq.send").record(System.nanoTime() - started, TimeUnit.NANOSECONDS);
    }
  }

  private boolean deliverOutboxTask(BusinessOutboxTask task, String leaseOwner) {
    BusinessEvent event = BusinessEvent.from(task);
    if (mqEnabled) {
      publishers.getObject().publish(event);
      // Fast consumers may already finish; owner and state predicates prevent overwriting DONE with
      // SENT.
      businessOutboxMapper.markSent(task.getId(), leaseOwner);
      return true;
    }
    if (event.getDeliverAt() != null && event.getDeliverAt() > System.currentTimeMillis()) {
      businessOutboxMapper.deferDeadline(task.getId(), leaseOwner);
      return false;
    }
    consumer.consume(event, BusinessEventServiceImpl.resolveConsumerGroup(event.getKind()));
    return true;
  }

  private void recordSendFailure(
      BusinessOutboxTask task, String leaseOwner, RuntimeException failure) {
    int attempts = task.getAttempts() + 1;
    boolean needsReview = attempts >= DeliveryRetryPolicy.MAX_SEND_ATTEMPTS;
    String reason = FailureMessages.summarize(failure);
    transactions.execute(
        tx -> {
          int changed =
              businessOutboxMapper.recordSendFailure(
                  attempts,
                  DeliveryRetryPolicy.delaySeconds(attempts),
                  needsReview ? OutboxState.REVIEW.name() : OutboxState.PENDING.name(),
                  reason,
                  task.getId(),
                  leaseOwner);
          if (changed > 0 && needsReview && "SECKILL_FULFILL".equals(task.getKind())) {
            seckillReservationMapper.markProcessingForReview(reason, task.getAggregateId());
          }
          return null;
        });
  }

  private int effectiveLeaseSeconds() {
    return Math.max(10, leaseSeconds);
  }

  @Scheduled(fixedDelay = 60000)
  /**
   * Requeue sent events that remain unconsumed beyond the observation window, preserving the
   * original event ID.
   *
   * <p>Do not redeliver future close events yet; the inbox and business idempotency rules handle
   * duplicate consumption.
   */
  @Override
  public void redriveUnconsumedEvents() {
    int age = Math.max(30, redeliverySeconds);
    // Future deadlines are expected to remain unconsumed. Redrive only after their due time.
    businessOutboxMapper.redriveUnconsumedEvents(-age, -age);
  }

  @Scheduled(fixedDelay = 10000)
  @Override
  public void refreshMetrics() {
    pending.set(businessOutboxMapper.countPendingEvents());
    review.set(businessOutboxMapper.countReviewEvents());
    inflight.set(businessOutboxMapper.countDueSentEvents());
  }
}
