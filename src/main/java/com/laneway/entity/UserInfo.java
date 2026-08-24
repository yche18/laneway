package com.laneway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
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
@TableName("tb_user_info")
public class UserInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Primary key and user ID. */
  @TableId(value = "user_id", type = IdType.AUTO)
  private Long userId;

  /** City name. */
  private String city;

  /** Personal introduction, at most 128 characters. */
  private String introduce;

  /** Follower count. */
  private Integer fans;

  /** Number of followed users. */
  private Integer followee;

  /** Gender: 0 male, 1 female. */
  private Boolean gender;

  /** Birthday. */
  private LocalDate birthday;

  /** Points. */
  private Integer credits;

  /** Membership level from 0 to 9; zero means no membership. */
  private Boolean level;

  /** Creation time. */
  private LocalDateTime createTime;

  /** Last update time. */
  private LocalDateTime updateTime;
}
