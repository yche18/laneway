package com.laneway.enums;

/** Stable state values shared by durable reservations and Redis request projections. */
public enum ReservationState {
  PROCESSING,
  SUCCESS,
  REVIEW,
  FAILED;

  public boolean matches(String value) {
    return name().equals(value);
  }
}
