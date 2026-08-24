package com.laneway.service;

import com.laneway.dto.Result;
import com.laneway.entity.VoucherOrder;

/** Persist, deliver, and manually reconcile flash-sale requests. */
public interface ISeckillService {
  String STREAM = "stream.orders";
  String GROUP = "g1";

  void fulfillReservationFromMessage(long orderId);

  /**
   * Lock the reservation in the order transaction; false means a terminal state already prevents
   * another stock deduction.
   */
  boolean lockReservationForFulfillment(VoucherOrder order);

  void markReservationSuccessful(long orderId);

  String queryReservationState(long orderId);

  Result queryReservationStatus(long orderId, long userId);

  Result publishActivity(long voucherId);

  /**
   * Recovery after manual reconciliation, with admission checks distinct from initial publication.
   */
  Result recoverActivity(long voucherId);

  Result retryReservation(long orderId);

  Result markReservationFailedAndCompensateRedis(long orderId);

  /** Generate a reconciliation report without automatically repairing stock. */
  Result queryInventoryReconciliationReport(long voucherId);

  Result queryReservationsForReview();
}
