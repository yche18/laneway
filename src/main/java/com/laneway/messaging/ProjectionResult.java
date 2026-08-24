package com.laneway.messaging;

/** Projection result; CONTINUE means this batch succeeded and the next task must be persisted. */
public enum ProjectionResult {
  COMPLETE,
  CONTINUE
}
