package com.laneway.utils;

import static com.laneway.utils.RedisConstants.LOGIN_USER_KEY;
import static com.laneway.utils.RedisConstants.LOGIN_USER_TTL;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.laneway.dto.UserDTO;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

public class RefreshTokenInterceptor implements HandlerInterceptor {

  private StringRedisTemplate stringRedisTemplate;

  public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    // 1. Read the token from the request header.
    String token = request.getHeader("authorization");
    if (StrUtil.isBlank(token)) {
      return true;
    }
    // 2. Load the user from Redis using the token.
    String key = LOGIN_USER_KEY + token;
    Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
    // 3. Check whether the user exists.
    if (userMap.isEmpty()) {
      return true;
    }
    // 5. Convert the hash data to UserDTO.
    UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
    // 6. Store the existing user in ThreadLocal.
    UserHolder.saveUser(userDTO);
    // 7. Refresh the token expiry.
    stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.MINUTES);
    // 8. Allow the request.
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
      throws Exception {
    // Clear the user context.
    UserHolder.removeUser();
  }
}
