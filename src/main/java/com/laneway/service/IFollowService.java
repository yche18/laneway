package com.laneway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.laneway.dto.Result;
import com.laneway.entity.Follow;

/**
 * Service interface.
 *
 * @author Yi Chen
 * @since 2026-08-24
 */
public interface IFollowService extends IService<Follow> {

  Result follow(Long followUserId, Boolean isFollow);

  Result isFollow(Long followUserId);

  Result followCommons(Long id);
}
