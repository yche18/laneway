package com.laneway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.laneway.dto.Result;
import com.laneway.entity.UserInfo;

/**
 * Service interface.
 *
 * @author Yi Chen
 * @since 2026-08-24
 */
public interface IUserInfoService extends IService<UserInfo> {

  Result queryUserInfo(Long userId);
}
