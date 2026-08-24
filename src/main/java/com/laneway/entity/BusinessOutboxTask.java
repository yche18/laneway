package com.laneway.entity;

import java.sql.Timestamp;
import lombok.Data;

/** Database snapshot of a durable business task; internal processing only. */
@Data
public class BusinessOutboxTask {
  private Long id;
  private String kind;
  private Long aggregateId;
  /** Initiating user; may be null for tasks without a user, such as activity retries. */
  private Long userId;
  /** Last processed follow-row ID for the next feed batch; this is not a follower user ID. */
  private long cursorId;

  private String state;
  private int attempts;
  private int consumeAttempts;
  /** Used only by media deletion tasks; may be null for other events. */
  private String resourcePath;
  /** Event time; feed continuations preserve it so retries do not change ordering. */
  private Timestamp createdAt;
  /** Scheduled execution time for a delayed event; null for ordinary events. */
  private Timestamp deliverAt;
}
