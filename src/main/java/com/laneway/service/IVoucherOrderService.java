package com.laneway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.laneway.dto.Result;
import com.laneway.entity.VoucherOrder;

/**
 * Service interface.
 *
 * @author Yi Chen
 * @since 2026-08-24
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

  /**
   * Conditional stock deduction and order insertion share a transaction; the user lock covers
   * commit.
   */
  void persistSeckill(VoucherOrder order);

  Result seckillVoucher(Long voucherId);

  Result orderOrdinaryVoucher(Long voucherId);

  Result queryMyOrder(Long orderId);

  Result queryMyVoucherOrder(Long voucherId);

  Result cancelUnpaidOrdinaryOrder(Long orderId);

  Result payMockOrder(Long orderId, Long expectedPayCents);

  Result queryMyOrders(Integer page, boolean heldOnly);
}
