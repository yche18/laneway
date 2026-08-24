package com.laneway.service;

import java.sql.Timestamp;

/** Write business events and notify caches after transaction commit. */
public interface IBusinessOutboxService {
  void appendBusinessEvent(String kind, long aggregateId, Long userId);

  void appendMediaDeletionEvent(String path, long userId);

  void appendShopCacheInvalidation(long shopId);

  void appendOrderExpirationEvent(long orderId, Long userId, Timestamp deadline);

  void relayOutboxBatch();
}
