package com.laneway.service;

import com.laneway.messaging.BusinessEvent;

/** Business processing service. */
public interface IBusinessEventService {
  void consume(BusinessEvent event, String group);

  boolean recordConsumptionFailure(
      BusinessEvent event,
      String group,
      String brokerId,
      String payload,
      RuntimeException failure,
      int deliveries);
}
