package com.laneway.service;

import com.laneway.entity.BusinessOutboxTask;
import java.util.List;

/**
 * Claim and deliver durable tasks under leases; network delivery follows the claim transaction
 * commit.
 */
public interface IOutboxRelayService {
  List<BusinessOutboxTask> claimOutboxBatch(String leaseOwner);

  void relayOutboxBatch();

  void redriveUnconsumedEvents();

  void refreshMetrics();
}
