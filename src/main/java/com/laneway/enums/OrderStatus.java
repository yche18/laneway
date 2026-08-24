package com.laneway.enums;

/**
 * Database order status; PAID also means a free voucher was claimed and does not prove money was
 * collected.
 */
public enum OrderStatus {
  UNPAID(1),
  PAID(2),
  USED(3),
  CANCELLED(4),
  REFUNDING(5),
  REFUNDED(6);

  private final int code;

  OrderStatus(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public boolean matches(Integer value) {
    return value != null && value == code;
  }
}
