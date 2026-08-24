package com.laneway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.laneway.dto.Result;
import com.laneway.entity.Blog;

/**
 * Service interface.
 *
 * @author Yi Chen
 * @since 2026-08-24
 */
public interface IBlogService extends IService<Blog> {

  Result queryHotBlog(Integer current);

  Result queryBlogById(Long id);

  Result likeBlog(Long id);

  Result setBlogLike(Long id, boolean liked);

  Result queryBlogLikes(Long id);

  Result saveBlog(Blog blog);

  Result queryBlogOfFollow(Long max, Integer offset);

  Result queryMyBlog(Integer current);

  Result queryBlogByShop(Long shopId, Integer current);

  Result searchBlogEvidence(Long shopId, String keyword, Integer current);

  Result queryBlogByUserId(Integer current, Long id);
}
