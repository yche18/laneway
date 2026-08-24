package com.laneway.service.impl;

import com.laneway.dto.Result;
import com.laneway.entity.BusinessOutboxTask;
import com.laneway.enums.ReservationState;
import com.laneway.mapper.BehaviorMapper;
import com.laneway.mapper.BusinessOutboxMapper;
import com.laneway.mapper.MessageMapper;
import com.laneway.mapper.SeckillReservationMapper;
import com.laneway.service.IOperationsService;
import com.laneway.service.ISeckillService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class OperationsServiceImpl implements IOperationsService {

  @Resource
  private BehaviorMapper behaviorMapper;

  @Resource
  private BusinessOutboxMapper businessOutboxMapper;

  @Resource
  private MessageMapper messageMapper;

  @Resource
  private SeckillReservationMapper seckillReservationMapper;

  @Resource
  private ISeckillService coordinator;

  @Override
  public Result queryReservationsForReview() {
    return coordinator.queryReservationsForReview();
  }

  @Override
  public Result querySeckillDeadLetters() {
    return Result.ok(seckillReservationMapper.selectDeadLetters());
  }

  @Override
  public Result queryUnfinishedBusinessEvents() {
    return Result.ok(businessOutboxMapper.selectUnfinishedEvents());
  }

  @Override
  public Result retryBusinessEvent(long eventId) {
    BusinessOutboxTask task = businessOutboxMapper.selectReviewEvent(eventId);
    if (task != null && "SECKILL_FULFILL".equals(task.getKind())) {
      long orderId = task.getAggregateId();
      if (ReservationState.REVIEW.matches(coordinator.queryReservationState(orderId))) {
        return coordinator.retryReservation(orderId);
      }
    }
    return businessOutboxMapper.retryReviewEvent(eventId) > 0
        ? Result.ok()
        : Result.fail("任务不在待核对状态");
  }

  @Override
  public Result queryMessageFailures() {
    return Result.ok(messageMapper.selectFailures());
  }

  @Override
  public Result queryUnsentAnalyticsEvents() {
    return Result.ok(behaviorMapper.selectUnsentEvents());
  }

  @Override
  public Result queryAnalyticsFailures() {
    return Result.ok(behaviorMapper.selectFailures());
  }

  @Override
  public Result retryAnalyticsEvent(String eventId) {
    return behaviorMapper.retryReviewEvent(eventId) > 0 ? Result.ok() : Result.fail("事件不在待核对状态");
  }

  @Override
  public Result queryInventoryReconciliationReport(long voucherId) {
    return coordinator.queryInventoryReconciliationReport(voucherId);
  }

  @Override
  public Result publishActivity(long voucherId) {
    return coordinator.publishActivity(voucherId);
  }

  @Override
  public Result recoverActivity(long voucherId) {
    return coordinator.recoverActivity(voucherId);
  }

  @Override
  public Result retryReservation(long orderId) {
    return coordinator.retryReservation(orderId);
  }

  @Override
  public Result markReservationFailedAndCompensateRedis(long orderId) {
    return coordinator.markReservationFailedAndCompensateRedis(orderId);
  }
}
