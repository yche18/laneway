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
 * Flash-sale voucher table, with a one-to-one relationship to vouchers.
 *
 * @author Yi Chen
 * @since 2026-08-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_seckill_voucher")
public class SeckillVoucher implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Associated voucher ID. */
  @TableId(value = "voucher_id", type = IdType.INPUT)
  private Long voucherId;

  /** Stock. */
  private Integer stock;

  /** Creation time. */
  private LocalDateTime createTime;

  /** Effective time. */
  private LocalDateTime beginTime;

  /** Expiration time. */
  private LocalDateTime endTime;

  /** Last update time. */
  private LocalDateTime updateTime;
}
