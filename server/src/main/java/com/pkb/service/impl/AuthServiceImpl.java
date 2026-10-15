package com.pkb.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pkb.common.constant.RedisKeys;
import com.pkb.common.exception.BusinessException;
import com.pkb.common.result.ResultCode;
import com.pkb.config.SecurityProperties;
import com.pkb.dto.CaptchaVO;
import com.pkb.dto.InitAdminRequest;
import com.pkb.dto.InitStatusVO;
import com.pkb.dto.LoginRequest;
import com.pkb.dto.LoginVO;
import com.pkb.dto.UserVO;
import com.pkb.entity.User;
import com.pkb.mapper.UserMapper;
import com.pkb.service.AuthService;
import com.pkb.util.CaptchaValidator;
import com.pkb.util.PasswordPolicy;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证与初始化实现（P-011 单账号登录）。
 *
 * <p>验证码与登录失败计数都放 Redis：
 * <ul>
 *   <li>{@code pkb:captcha:{key}} → 答案，TTL 2 分钟，校验后立即删除（一次性）</li>
 *   <li>{@code pkb:login:fail:{phone}} → 失败次数，TTL = 锁定时长（15 分钟）</li>
 * </ul>
 *
 * <p>⚠️ 与 OSC 的差异：**无强制改密、无角色 / 部门 / 职位写入**（这些列已随 PRD 附录 C.3 删除）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** 账号状态：正常 */
    private static final int STATUS_NORMAL = 0;
    /** 账号状态：停用 */
    private static final int STATUS_DISABLED = 1;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final SecurityProperties securityProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final CaptchaValidator captchaValidator;

    @Override
    public CaptchaVO createCaptcha() {
        // 取一个"结果非负"的算术验证码：easy-captcha 的减法可能给出负结果（如 1-2），
        // 手机上输负号很别扭，所以重掷几次（重掷成本极低）
        ArithmeticCaptcha captcha;
        String answer;
        int attempts = 0;
        do {
            captcha = new ArithmeticCaptcha(130, 48);
            // 2 位数运算（如 3+4=?），手机上更好输入
            captcha.setLen(2);
            answer = captcha.text();
            attempts++;
        } while (answer != null && answer.trim().startsWith("-") && attempts < 8);

        String base64 = captcha.toBase64();
        if (!base64.startsWith("data:")) {
            base64 = "data:image/png;base64," + base64;
        }

        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(
                RedisKeys.CAPTCHA_PREFIX + captchaKey,
                answer,
                Duration.ofSeconds(securityProperties.getCaptchaTtlSeconds()));

        return new CaptchaVO(captchaKey, base64);
    }

    @Override
    public InitStatusVO initStatus() {
        Long count = userMapper.selectCount(null);
        return new InitStatusVO(count != null && count > 0);
    }

    @Override
    public void initAdmin(InitAdminRequest request) {
        if (initStatus().initialized()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "系统已初始化，无法重复创建账号");
        }
        String phone = request.phone().trim();
        passwordPolicy.validate(request.password(), phone, null);

        User account = new User();
        account.setPhone(phone);
        account.setPassword(passwordEncoder.encode(request.password()));
        account.setStatus(STATUS_NORMAL);
        userMapper.insert(account);

        log.info("初始化完成，已创建唯一账号：id={}, phone={}", account.getId(), phone);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        captchaValidator.validateAndConsume(request.captchaKey(), request.captchaCode());

        String phone = request.phone().trim();
        checkLocked(phone);

        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, phone));
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_FAILED, "账号不存在，请确认手机号");
        }
        if (Objects.equals(user.getStatus(), STATUS_DISABLED)) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            int remaining = increaseFailAndGetRemaining(phone);
            if (remaining <= 0) {
                throw new BusinessException(ResultCode.ACCOUNT_LOCKED,
                        "尝试次数过多，账号已锁定 " + securityProperties.getLoginLockMinutes() + " 分钟");
            }
            throw new BusinessException(ResultCode.LOGIN_FAILED, "密码错误，还剩 " + remaining + " 次机会");
        }

        clearFail(phone);

        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        log.info("登录成功：userId={}, phone={}", user.getId(), phone);
        return new LoginVO(tokenInfo.getTokenName(), tokenInfo.getTokenValue(), UserVO.from(user));
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    // ------------------------------------------------------------
    // 内部方法
    // ------------------------------------------------------------

    private void checkLocked(String phone) {
        String key = RedisKeys.LOGIN_FAIL_PREFIX + phone;
        int fails = getFailCount(key);
        if (fails >= securityProperties.getLoginMaxFail()) {
            Long minutes = stringRedisTemplate.getExpire(key, TimeUnit.MINUTES);
            long remain = (minutes == null || minutes <= 0)
                    ? securityProperties.getLoginLockMinutes() : minutes;
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED,
                    "尝试次数过多，请 " + remain + " 分钟后重试");
        }
    }

    /** 失败次数 +1，返回剩余可尝试次数（<=0 表示已达锁定阈值） */
    private int increaseFailAndGetRemaining(String phone) {
        String key = RedisKeys.LOGIN_FAIL_PREFIX + phone;
        int max = securityProperties.getLoginMaxFail();
        Long count = stringRedisTemplate.opsForValue().increment(key);
        long current = count == null ? max : count;
        if (current == 1L) {
            // TTL 只在第一次失败时设置：15 分钟内的连续失败累计，不因每次失败而顺延
            stringRedisTemplate.expire(key, Duration.ofMinutes(securityProperties.getLoginLockMinutes()));
        }
        return (int) (max - current);
    }

    private int getFailCount(String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("登录失败计数解析异常，已重置：key={}, value={}", key, value);
            stringRedisTemplate.delete(key);
            return 0;
        }
    }

    private void clearFail(String phone) {
        stringRedisTemplate.delete(RedisKeys.LOGIN_FAIL_PREFIX + phone);
    }
}
