package com.laneway.entity;

import lombok.Data;

/** Durable flash-sale eligibility; replay must preserve order, user, and voucher identity. */
@Data
public class SeckillReservation {
  private Long orderId;
  private Long voucherId;
  private Long userId;
  private String state;
  private int attempts;
  private String reason;
}
