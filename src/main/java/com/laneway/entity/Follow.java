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
@TableName("tb_follow")
public class Follow implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Primary key. */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /** User ID. */
  private Long userId;

  /** Associated user ID. */
  private Long followUserId;

  /** Creation time. */
  private LocalDateTime createTime;
}
