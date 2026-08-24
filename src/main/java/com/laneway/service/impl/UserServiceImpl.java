package com.laneway.service.impl;

import static com.laneway.utils.RedisConstants.LOGIN_CODE_KEY;
import static com.laneway.utils.RedisConstants.LOGIN_CODE_TTL;
import static com.laneway.utils.RedisConstants.LOGIN_USER_KEY;
import static com.laneway.utils.RedisConstants.LOGIN_USER_TTL;
import static com.laneway.utils.RedisConstants.USER_SIGN_KEY;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laneway.dto.LoginFormDTO;
import com.laneway.dto.Result;
import com.laneway.dto.UserDTO;
import com.laneway.entity.User;
import com.laneway.mapper.UserMapper;
import com.laneway.reliability.RateLimiter;
import com.laneway.reliability.RedisScripts;
import com.laneway.service.IUserService;
import com.laneway.utils.RegexUtils;
import com.laneway.utils.SystemConstants;
import com.laneway.utils.UserHolder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

/**
 * Service implementation.
 *
 * @author Yi Chen
 * @since 2026-08-24
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Resource
  private RateLimiter rateLimiter;

  private static final DefaultRedisScript<Long> CONSUME_CODE =
      RedisScripts.number("consume-code.lua");

  @Override
  public Result sendCode(String phone, HttpSession session) {
    // 1. Validate the phone number.
    if (RegexUtils.isPhoneInvalid(phone)) {
      // 2. Return an error for invalid input.
      return Result.fail("手机号格式错误！");
    }
    if (!rateLimiter.allow("code-phone:" + phone, 1, 60000)) {
      return Result.fail("请一分钟后再获取验证码");
    }
    // 3. Generate a verification code for valid input.
    String code = RandomUtil.randomNumbers(6);

    // 4. Store the verification code.
    stringRedisTemplate
        .opsForValue()
        .set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

    // 5. Send the verification code.
    log.debug("发送短信验证码成功，验证码：{}", code);
    // Return success.
    return Result.ok();
  }

  @Override
  public Result login(LoginFormDTO loginForm, HttpSession session) {
    // 1. Validate the phone number.
    String phone = loginForm.getPhone();
    if (RegexUtils.isPhoneInvalid(phone)) {
      // 2. Return an error for invalid input.
      return Result.fail("手机号格式错误！");
    }
    if (!rateLimiter.allow("login-phone:" + phone, 5, 120000)) {
      return Result.fail("尝试次数过多，请稍后重试");
    }
    String code = loginForm.getCode();
    if (code == null
        || !code.matches("[0-9]{6}")
        || !Long.valueOf(1)
            .equals(
                stringRedisTemplate.execute(
                    CONSUME_CODE, Collections.singletonList(LOGIN_CODE_KEY + phone), code))) {
      return Result.fail("验证码错误或已使用");
    }

    // 4. On a match, find the user by phone: select * from tb_user where phone = ?.
    User user = query().eq("phone", phone).one();

    // 5. Check whether the user exists.
    if (user == null) {
      // 6. Create and save a new user if absent.
      try {
        user = createUserWithPhone(phone);
      } catch (DuplicateKeyException duplicate) {
        user = query().eq("phone", phone).one();
      }
    }

    // 7. Store user details in Redis.
    // 7.1. Generate a random login token.
    String token = UUID.randomUUID().toString(true);
    // 7.2. Convert the User to a HashMap for storage.
    UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
    Map<String, Object> userMap =
        BeanUtil.beanToMap(
            userDTO,
            new HashMap<>(),
            CopyOptions.create()
                .setIgnoreNullValue(true)
                .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
    // 7.3. Store the data.
    String tokenKey = LOGIN_USER_KEY + token;
    stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
    // 7.4. Set the token expiry.
    stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

    // 8. Return the token.
    return Result.ok(token);
  }

  @Override
  public Result signInToday() {
    // 1. Get the currently authenticated user.
    Long userId = UserHolder.getUser().getId();
    // 2. Get the date.
    LocalDateTime now = LocalDateTime.now();
    // 3. Build the key.
    String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
    String key = USER_SIGN_KEY + userId + keySuffix;
    // 4. Get today's day of the month.
    int dayOfMonth = now.getDayOfMonth();
    // 5. Write Redis SETBIT key offset 1.
    stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
    return Result.ok();
  }

  /**
   * Count consecutive check-ins ending today within the current month, not the monthly total.
   *
   * <p>Count consecutive ones from today's least significant bit; return zero if absent and never
   * cross month boundaries.
   */
  @Override
  public Result queryCurrentMonthConsecutiveSignInDays() {
    // 1. Get the currently authenticated user.
    Long userId = UserHolder.getUser().getId();
    // 2. Get the date.
    LocalDateTime now = LocalDateTime.now();
    // 3. Build the key.
    String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
    String key = USER_SIGN_KEY + userId + keySuffix;
    // 4. Get today's day of the month.
    int dayOfMonth = now.getDayOfMonth();
    // 5. Read this month's check-ins through today as a decimal number: BITFIELD sign:5:202608 GET
    // u14 0.
    List<Long> bitmapValues =
        stringRedisTemplate
            .opsForValue()
            .bitField(
                key,
                BitFieldSubCommands.create()
                    .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth))
                    .valueAt(0));
    if (bitmapValues == null || bitmapValues.isEmpty()) {
      // No check-in records.
      return Result.ok(0);
    }
    Long signInBitmap = bitmapValues.get(0);
    if (signInBitmap == null || signInBitmap == 0) {
      return Result.ok(0);
    }
    // 6. Iterate over the bits.
    int consecutiveDays = 0;
    while (true) {
      // 6.1. AND with one to read the last bit and check whether it is zero.
      if ((signInBitmap & 1) == 0) {
        // Zero means no check-in; stop.
        break;
      } else {
        // A set bit means a check-in; increment the count.
        consecutiveDays++;
      }
      // Shift right to discard the last bit and examine the next.
      signInBitmap >>>= 1;
    }
    return Result.ok(consecutiveDays);
  }

  private User createUserWithPhone(String phone) {
    // 1. Create the user.
    User user = new User();
    user.setPhone(phone);
    user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
    // 2. Save the user.
    save(user);
    return user;
  }

  @Override
  public Result logout(String token) {
    if (token == null || token.trim().isEmpty()) {
      return Result.fail("缺少登录令牌");
    }
    stringRedisTemplate.delete(LOGIN_USER_KEY + token);
    UserHolder.removeUser();
    return Result.ok();
  }

  @Override
  public Result queryUserById(Long userId) {
    User user = getById(userId);
    return user == null ? Result.ok() : Result.ok(BeanUtil.copyProperties(user, UserDTO.class));
  }
}
