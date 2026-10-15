package com.pkb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 安全相关配置（application.yml 的 pkb.security.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "pkb.security")
public class SecurityProperties {

    /** 连续登录失败多少次后锁定 */
    private int loginMaxFail = 5;

    /** 锁定时长（分钟），同时也是失败计数器的 TTL */
    private int loginLockMinutes = 15;

    /** 图形验证码有效期（秒） */
    private int captchaTtlSeconds = 120;

    /** 密码最小长度 */
    private int passwordMinLength = 8;

    /** 密码最大长度（BCrypt 只取前 72 字节，这里限制在合理范围） */
    private int passwordMaxLength = 20;
}
