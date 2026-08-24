package com.laneway.enums;

/** Durable task status; SENT confirms delivery, while DONE confirms completed consumption. */
public enum OutboxState {
  PENDING,
  SENDING,
  SENT,
  DONE,
  REVIEW;

  public boolean matches(String value) {
    return name().equals(value);
  }
}
