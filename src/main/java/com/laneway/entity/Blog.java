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
@TableName("tb_blog")
public class Blog implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Primary key. */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;
  /** Merchant ID. */
  private Long shopId;
  /** User ID. */
  private Long userId;
  /** User icon. */
  @TableField(exist = false)
  private String icon;
  /** User name. */
  @TableField(exist = false)
  private String name;
  /** Whether the current user has liked this post. */
  @TableField(exist = false)
  private Boolean isLike;

  /** Title. */
  private String title;

  /** Up to nine shop-visit photos, separated by commas. */
  private String images;

  /** Shop-visit description. */
  private String content;

  /** Like count. */
  private Integer liked;

  /** Comment count. */
  private Integer comments;

  /** Creation time. */
  private LocalDateTime createTime;

  /** Last update time. */
  private LocalDateTime updateTime;
}
