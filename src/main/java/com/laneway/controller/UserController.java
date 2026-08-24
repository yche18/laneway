package com.laneway.controller;

import com.laneway.dto.LoginFormDTO;
import com.laneway.dto.Result;
import com.laneway.dto.UserDTO;
import com.laneway.service.IUserInfoService;
import com.laneway.service.IUserService;
import com.laneway.utils.UserHolder;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller.
 *
 * @author Yi Chen
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

  @Resource
  private IUserService userService;

  @Resource
  private IUserInfoService userInfoService;

  /** Send a phone verification code. */
  @PostMapping("code")
  public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
    // Send and store the SMS verification code.
    return userService.sendCode(phone, session);
  }

  /**
   * Log in.
   *
   * @param loginForm phone number with a verification code or password
   */
  @PostMapping("/login")
  public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session) {
    // Authenticate the user.
    return userService.login(loginForm, session);
  }

  /**
   * Log out.
   *
   * @return empty result
   */
  @PostMapping("/logout")
  public Result logout(@RequestHeader(value = "authorization", required = false) String token) {
    return userService.logout(token);
  }

  @GetMapping("/me")
  public Result me() {
    // Return the currently authenticated user.
    UserDTO user = UserHolder.getUser();
    return Result.ok(user);
  }

  @GetMapping("/info/{id}")
  public Result info(@PathVariable("id") Long userId) {
    return userInfoService.queryUserInfo(userId);
  }

  @GetMapping("/{id}")
  public Result queryUserById(@PathVariable("id") Long userId) {
    return userService.queryUserById(userId);
  }

  @PostMapping("/sign")
  public Result signInToday() {
    return userService.signInToday();
  }

  @GetMapping("/sign/count")
  public Result queryCurrentMonthConsecutiveSignInDays() {
    return userService.queryCurrentMonthConsecutiveSignInDays();
  }
}
