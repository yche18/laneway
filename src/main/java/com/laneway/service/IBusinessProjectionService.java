package com.laneway.service;

import com.laneway.entity.BusinessOutboxTask;
import com.laneway.messaging.ProjectionResult;

/**
 * Project durable facts into caches, resources, or subsequent tasks; callers own message identity
 * and transactions.
 */
public interface IBusinessProjectionService {
  /**
   * CONTINUE means this batch succeeded with more batches pending; exceptions retain retry
   * responsibility.
   */
  ProjectionResult project(BusinessOutboxTask task);
}
