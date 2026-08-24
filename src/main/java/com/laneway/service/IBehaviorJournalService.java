package com.laneway.service;

import com.laneway.entity.BusinessOutboxTask;

/** Register and deliver visit tracking and durable order analytics events. */
public interface IBehaviorJournalService {
  void appendOrderBehaviorEvent(BusinessOutboxTask source);

  void recordShopVisit(long shopId, Long userId);

  /**
   * Return a tracking event ID, not a delivery or persistence acknowledgement; null when tracking
   * is disabled.
   */
  String submitShopVisitEvent(long shopId, Long userId);

  void relayAnalyticsOutboxBatch();

  void redriveMissingAnalyticsEvents();

  void refreshMetrics();
}
