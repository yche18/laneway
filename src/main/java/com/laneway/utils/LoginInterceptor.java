package com.laneway.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    // 1. Check whether ThreadLocal contains an authenticated user.
    if (UserHolder.getUser() == null) {
      // No user: reject the request and set the status code.
      response.setStatus(401);
      // Reject the request.
      return false;
    }
    // An authenticated user may proceed.
    return true;
  }
}
