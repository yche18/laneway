package com.laneway.controller;

import com.laneway.dto.Result;
import com.laneway.entity.Blog;
import com.laneway.service.IBlogService;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller.
 *
 * @author Yi Chen
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

  @Resource
  private IBlogService blogService;

  @PostMapping
  public Result saveBlog(@RequestBody Blog blog) {
    return blogService.saveBlog(blog);
  }

  @PutMapping("/like/{id}")
  public Result likeBlog(@PathVariable("id") Long id) {
    return blogService.likeBlog(id);
  }

  @PutMapping("/like/{id}/state")
  public Result setLike(@PathVariable("id") Long id, @RequestParam boolean liked) {
    return blogService.setBlogLike(id, liked);
  }

  @GetMapping("/of/me")
  public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
    return blogService.queryMyBlog(current);
  }

  @GetMapping("/hot")
  public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
    return blogService.queryHotBlog(current);
  }

  /** Shop-specific review lookup for the external Agent integration. */
  @GetMapping("/of/shop")
  public Result queryBlogByShop(
      @RequestParam("shopId") Long shopId,
      @RequestParam(value = "current", defaultValue = "1") Integer current) {
    return blogService.queryBlogByShop(shopId, current);
  }

  /** Read-only evidence lookup by shop and title or body keywords. */
  @GetMapping("/search")
  public Result searchBlogEvidence(
      @RequestParam("shopId") Long shopId,
      @RequestParam("keyword") String keyword,
      @RequestParam(value = "current", defaultValue = "1") Integer current) {
    return blogService.searchBlogEvidence(shopId, keyword, current);
  }

  @GetMapping("/{id}")
  public Result queryBlogById(@PathVariable("id") Long id) {
    return blogService.queryBlogById(id);
  }

  @GetMapping("/likes/{id}")
  public Result queryBlogLikes(@PathVariable("id") Long id) {
    return blogService.queryBlogLikes(id);
  }

  @GetMapping("/of/user")
  public Result queryBlogByUserId(
      @RequestParam(value = "current", defaultValue = "1") Integer current,
      @RequestParam("id") Long id) {
    return blogService.queryBlogByUserId(current, id);
  }

  @GetMapping("/of/follow")
  public Result queryBlogOfFollow(
      @RequestParam("lastId") Long max,
      @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
    return blogService.queryBlogOfFollow(max, offset);
  }
}
