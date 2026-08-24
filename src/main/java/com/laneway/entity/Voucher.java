package com.laneway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("tb_voucher")
public class Voucher implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Primary key. */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /** Shop ID. */
  private Long shopId;

  /** Voucher title. */
  private String title;

  /** Subtitle. */
  private String subTitle;

  /** Usage rules. */
  private String rules;

  /** Payment amount. */
  private Long payValue;

  /** Deduction amount. */
  private Long actualValue;

  /** Voucher type. */
  private Integer type;

  /** Voucher type. */
  private Integer status;
  /** Stock. */
  @TableField(exist = false)
  private Integer stock;

  /** Effective time. */
  @TableField(exist = false)
  private LocalDateTime beginTime;

  /** Expiration time. */
  @TableField(exist = false)
  private LocalDateTime endTime;

  /** Order eligibility computed using the service clock; response-only, not persisted. */
  @TableField(exist = false)
  private Boolean orderable;

  /** Creation time. */
  private LocalDateTime createTime;

  /** Last update time. */
  private LocalDateTime updateTime;
}
