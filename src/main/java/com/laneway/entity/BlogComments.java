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
@TableName("tb_blog_comments")
public class BlogComments implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Primary key. */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /** User ID. */
  private Long userId;

  /** Shop-visit post ID. */
  private Long blogId;

  /** Associated top-level comment ID, or zero for a top-level comment. */
  private Long parentId;

  /** ID of the comment being replied to. */
  private Long answerId;

  /** Reply content. */
  private String content;

  /** Like count. */
  private Integer liked;

  /** Status: 0 normal, 1 reported, 2 hidden. */
  private Boolean status;

  /** Creation time. */
  private LocalDateTime createTime;

  /** Last update time. */
  private LocalDateTime updateTime;
}
