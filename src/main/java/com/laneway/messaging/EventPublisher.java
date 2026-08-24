package com.laneway.messaging;

public interface EventPublisher {
  void publish(BusinessEvent event);
}
