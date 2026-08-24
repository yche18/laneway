package com.laneway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laneway.entity.BlogComments;
import com.laneway.mapper.BlogCommentsMapper;
import com.laneway.service.IBlogCommentsService;
import org.springframework.stereotype.Service;

/**
 * Service implementation.
 *
 * @author Yi Chen
 * @since 2026-08-24
 */
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments>
    implements IBlogCommentsService {}
