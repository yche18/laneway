package com.laneway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laneway.dto.Result;
import com.laneway.entity.UserInfo;
import com.laneway.mapper.UserInfoMapper;
import com.laneway.service.IUserInfoService;
import org.springframework.stereotype.Service;

/**
 * Service implementation.
 *
 * @author Yi Chen
 * @since 2026-08-24
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
    implements IUserInfoService {

  @Override
  public Result queryUserInfo(Long userId) {
    UserInfo info = getById(userId);
    if (info == null) {
      return Result.ok();
    }
    info.setCreateTime(null);
    info.setUpdateTime(null);
    return Result.ok(info);
  }
}
