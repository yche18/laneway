package com.laneway.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laneway.analytics.BehaviorEvent;
import com.laneway.analytics.KafkaBehaviorTransport;
import com.laneway.entity.BusinessOutboxTask;
import com.laneway.mapper.BehaviorMapper;
import com.laneway.mapper.VoucherOrderMapper;
import com.laneway.messaging.DeliveryRetryPolicy;
import com.laneway.service.IBehaviorJournalService;
import com.laneway.utils.FailureMessages;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class BehaviorJournalServiceImpl implements IBehaviorJournalService {

  @Resource
  private BehaviorMapper behaviorMapper;

  @Resource
  private VoucherOrderMapper voucherOrderMapper;

  private final ObjectMapper json;
  private final TransactionTemplate transactions;
  private final ObjectProvider<KafkaBehaviorTransport> transport;
  private final AtomicLong pending = new AtomicLong();
  private final AtomicLong review = new AtomicLong();

  @Value("${laneway.analytics.enabled:false}")
  private boolean enabled;

  public BehaviorJournalServiceImpl(
      ObjectMapper json,
      TransactionTemplate transactions,
      ObjectProvider<KafkaBehaviorTransport> transport) {
    this.json = json;
    this.transactions = transactions;
    this.transport = transport;
  }

  @Autowired
  public void bindMetrics(MeterRegistry registry) {
    registry.gauge("laneway.analytics.outbox.pending", pending);
    registry.gauge("laneway.analytics.outbox.review", review);
  }

  /**
   * Create an analytics event from the durable order snapshot and write the analytics outbox in the
   * current transaction.
   *
   * <p>Reuse the source task ID on replay; current voucher pricing or ownership must not replace
   * historical snapshots.
   */
  @Override
  public void appendOrderBehaviorEvent(BusinessOutboxTask source) {
    if (!enabled) {
      return;
    }
    if (!TransactionSynchronizationManager.isActualTransactionActive()) {
      throw new IllegalStateException("订单分析事件需要业务事务");
    }
    List<Map<String, Object>> rows =
        voucherOrderMapper.selectBehaviorSnapshot(source.getAggregateId());
    if (rows.isEmpty() || rows.get(0).get("shop_id_snapshot") == null) {
      return;
    }
    Map<String, Object> order = rows.get(0);
    BehaviorEvent event =
        new BehaviorEvent(
            "order:" + source.getId(),
            ((Number) order.get("shop_id_snapshot")).longValue(),
            ((Number) order.get("user_id")).longValue(),
            "ORDER_PAID".equals(source.getKind()) ? "PAID" : "ORDER",
            source.getCreatedAt().getTime());
    try {
      behaviorMapper.insertOutboxEvent(
          event.getEventId(), event.getShopId(), json.writeValueAsString(event));
    } catch (JsonProcessingException bad) {
      throw new IllegalArgumentException("订单分析事件编码失败", bad);
    }
  }

  @Override
  public void recordShopVisit(long shopId, Long userId) {
    submitShopVisitEvent(shopId, userId);
  }

  /**
   * Submit a visit tracking event and return its ID for later persistence checks.
   *
   * <p>Visits use best-effort delivery; an ID confirms collection was initiated, not Kafka
   * acknowledgement or persistence.
   *
   * <p>Return null when analytics is disabled.
   */
  @Override
  public String submitShopVisitEvent(long shopId, Long userId) {
    if (enabled) {
      String eventId = UUID.randomUUID().toString();
      transport
          .getObject()
          .publishVisitBestEffort(
              new BehaviorEvent(eventId, shopId, userId, "VISIT", System.currentTimeMillis()));
      return eventId;
    }
    return null;
  }

  @Scheduled(fixedDelay = 1000)
  /**
   * Claim and deliver a batch of durable analytics events; network delivery follows the claim
   * transaction commit.
   *
   * <p>Write SENT only after Kafka acknowledgement; retries preserve the event ID for idempotent
   * consumption.
   */
  @Override
  public void relayAnalyticsOutboxBatch() {
    if (!enabled) {
      return;
    }
    String owner = UUID.randomUUID().toString();
    List<Map<String, Object>> rows =
        transactions.execute(
            tx -> {
              List<Map<String, Object>> batch = behaviorMapper.selectClaimableEvents();
              for (Map<String, Object> row : batch) {
                behaviorMapper.claimEvent(owner, (String) row.get("event_id"));
              }
              return batch;
            });
    for (Map<String, Object> row : rows) {
      try {
        if (behaviorMapper.renewLease((String) row.get("event_id"), owner) == 0) {
          continue;
        }
        transport
            .getObject()
            .publishAndAwaitAcknowledgement(
                json.readValue(row.get("payload").toString(), BehaviorEvent.class));
        behaviorMapper.markSent((String) row.get("event_id"), owner);
      } catch (Exception failure) {
        int attempts = ((Number) row.get("attempts")).intValue() + 1;
        behaviorMapper.recordSendFailure(
            attempts >= DeliveryRetryPolicy.MAX_SEND_ATTEMPTS ? "REVIEW" : "PENDING",
            attempts,
            FailureMessages.summarize(failure),
            DeliveryRetryPolicy.delaySeconds(attempts),
            (String) row.get("event_id"),
            owner);
      }
    }
  }

  @Scheduled(fixedDelay = 60000)
  @Override
  public void redriveMissingAnalyticsEvents() {
    if (!enabled) {
      return;
    }
    behaviorMapper.redriveMissingEvents();
  }

  @Scheduled(fixedDelay = 10000)
  @Override
  public void refreshMetrics() {
    if (!enabled) {
      return;
    }
    pending.set(behaviorMapper.countPendingEvents());
    review.set(behaviorMapper.countReviewEvents());
  }
}
