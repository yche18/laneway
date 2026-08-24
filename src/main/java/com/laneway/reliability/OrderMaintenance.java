package com.laneway.reliability;

import com.laneway.service.IOrderLifecycleService;
import javax.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderMaintenance {
  @Resource
  private IOrderLifecycleService orderLifecycleService;

  @Scheduled(fixedDelay = 60000)
  public void expireOrdinaryOrders() {
    orderLifecycleService.expireOverdueOrders();
  }
}
