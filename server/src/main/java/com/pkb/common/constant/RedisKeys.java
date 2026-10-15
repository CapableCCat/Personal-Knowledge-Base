package com.pkb.common.constant;

/**
 * Redis key 前缀（统一加 pkb: 前缀，避免与 Sa-Token 自身的 key 冲突）。
 */
public final class RedisKeys {

    /** 图形验证码：pkb:captcha:{captchaKey} → 答案，校验后立即删除 */
    public static final String CAPTCHA_PREFIX = "pkb:captcha:";

    /** 登录失败计数：pkb:login:fail:{phone} → 次数，TTL = 锁定时长 */
    public static final String LOGIN_FAIL_PREFIX = "pkb:login:fail:";

    private RedisKeys() {
    }
}
