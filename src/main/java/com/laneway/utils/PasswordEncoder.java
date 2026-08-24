package com.laneway.utils;

import cn.hutool.core.util.RandomUtil;
import java.nio.charset.StandardCharsets;
import org.springframework.util.DigestUtils;

public class PasswordEncoder {

  public static String encode(String password) {
    // Generate a salt.
    String salt = RandomUtil.randomString(20);
    // Hash the password.
    return encode(password, salt);
  }

  private static String encode(String password, String salt) {
    // Hash the password.
    return salt
        + "@"
        + DigestUtils.md5DigestAsHex((password + salt).getBytes(StandardCharsets.UTF_8));
  }

  public static Boolean matches(String encodedPassword, String rawPassword) {
    if (encodedPassword == null || rawPassword == null) {
      return false;
    }
    if (!encodedPassword.contains("@")) {
      throw new RuntimeException("密码格式不正确！");
    }
    String[] arr = encodedPassword.split("@");
    // Extract the salt.
    String salt = arr[0];
    // Compare hashes.
    return encodedPassword.equals(encode(rawPassword, salt));
  }
}
