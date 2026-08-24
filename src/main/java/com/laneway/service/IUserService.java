package com.laneway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.laneway.dto.LoginFormDTO;
import com.laneway.dto.Result;
import com.laneway.entity.User;
import javax.servlet.http.HttpSession;

/**
 * Service interface.
 *
 * @author Yi Chen
 * @since 2026-08-24
 */
public interface IUserService extends IService<User> {

  Result sendCode(String phone, HttpSession session);

  Result login(LoginFormDTO loginForm, HttpSession session);

  Result signInToday();

  Result queryCurrentMonthConsecutiveSignInDays();

  Result logout(String token);

  Result queryUserById(Long userId);
}
