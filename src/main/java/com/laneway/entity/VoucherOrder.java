package com.laneway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Yi Chen
 * @since 2026-08-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_voucher_order")
public class VoucherOrder implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Primary key. */
  @TableId(value = "id", type = IdType.INPUT)
  private Long id;

  /** User placing the order. */
  private Long userId;

  /** Purchased voucher ID. */
  private Long voucherId;

  /**
   * Amount due at creation, in cents; legacy null values fall back to the current voucher price.
   */
  private Long payValueSnapshot;
  /** Voucher title at creation; later renames must not alter this snapshot. */
  private String titleSnapshot;
  /** Usage rules at creation. */
  private String rulesSnapshot;
  /** Voucher type at creation: 0 regular, 1 flash sale. */
  private Integer voucherTypeSnapshot;
  /**
   * Payment deadline for a regular unpaid order; may be null for free vouchers or orders without
   * timeout closure.
   */
  private LocalDateTime expireAt;
  /** Shop at creation, used to attribute order behavior analytics. */
  private Long shopIdSnapshot;

  /** Payment method: 1 balance, 2 Alipay, 3 WeChat. */
  private Integer payType;

  /** Order status: 1 unpaid, 2 paid, 3 redeemed, 4 cancelled, 5 refunding, 6 refunded. */
  private Integer status;

  /** Order placement time. */
  private LocalDateTime createTime;

  /** Payment time. */
  private LocalDateTime payTime;

  /** Redemption time. */
  private LocalDateTime useTime;

  /** Refund time. */
  private LocalDateTime refundTime;

  /** Last update time. */
  private LocalDateTime updateTime;
}
