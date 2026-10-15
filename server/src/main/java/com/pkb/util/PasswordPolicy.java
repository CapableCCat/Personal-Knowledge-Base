package com.pkb.util;

import com.pkb.common.exception.BusinessException;
import com.pkb.common.result.ResultCode;
import com.pkb.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 密码强度校验（规则见《开发任务点清单》§6 D32）。
 *
 * <p>规则：长度 8~20 位；必须同时包含字母与数字；不得含空格；
 * 不得与手机号相同；不得与旧密码相同。
 */
@Component
@RequiredArgsConstructor
public class PasswordPolicy {

    private final SecurityProperties securityProperties;

    /**
     * @param rawPassword  待校验的新密码（明文）
     * @param phone        用户手机号，可为 null
     * @param oldPassword  旧密码（明文），可为 null（初始化/重置场景无需传）
     */
    public void validate(String rawPassword, String phone, String oldPassword) {
        int min = securityProperties.getPasswordMinLength();
        int max = securityProperties.getPasswordMaxLength();

        if (!StringUtils.hasText(rawPassword)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请填写密码");
        }
        if (rawPassword.length() < min || rawPassword.length() > max) {
            throw new BusinessException(ResultCode.PARAM_ERROR,
                    "密码长度需为 " + min + "~" + max + " 位");
        }
        if (rawPassword.contains(" ")) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "密码不能包含空格");
        }
        boolean hasLetter = rawPassword.chars().anyMatch(Character::isLetter);
        boolean hasDigit = rawPassword.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "密码必须同时包含字母和数字");
        }
        if (phone != null && rawPassword.equals(phone)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "密码不能与手机号相同");
        }
        if (oldPassword != null && rawPassword.equals(oldPassword)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "新密码不能与旧密码相同");
        }
    }
}
