package com.pkb.util;

import com.pkb.common.constant.RedisKeys;
import com.pkb.common.exception.BusinessException;
import com.pkb.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 图形验证码校验（一次性）：PKB 目前只有登录（P-011）使用。
 *
 * <p>无论对错都立即作废，避免同一张验证码被重复使用（防重放）。
 */
@Component
@RequiredArgsConstructor
public class CaptchaValidator {

    private final StringRedisTemplate stringRedisTemplate;

    public void validateAndConsume(String captchaKey, String captchaCode) {
        if (!StringUtils.hasText(captchaKey) || !StringUtils.hasText(captchaCode)) {
            throw new BusinessException(ResultCode.CAPTCHA_INVALID, "请填写验证码");
        }
        String redisKey = RedisKeys.CAPTCHA_PREFIX + captchaKey;
        String answer = stringRedisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.hasText(answer)) {
            throw new BusinessException(ResultCode.CAPTCHA_INVALID, "验证码已过期，请点击图片刷新");
        }
        stringRedisTemplate.delete(redisKey);
        if (!answer.equalsIgnoreCase(captchaCode.trim())) {
            throw new BusinessException(ResultCode.CAPTCHA_INVALID);
        }
    }
}
