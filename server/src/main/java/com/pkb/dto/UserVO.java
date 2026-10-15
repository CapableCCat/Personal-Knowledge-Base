package com.pkb.dto;

import com.pkb.entity.User;

import java.time.LocalDateTime;

/**
 * 账号信息出参（绝不含密码）。
 *
 * <p>单账号系统无「他人视角」，故 OSC 的 {@code masked()} 脱敏重载已删。
 */
public record UserVO(
        Long id,
        String phone,
        Integer status,
        LocalDateTime createdAt
) {

    public static UserVO from(User user) {
        if (user == null) {
            return null;
        }
        return new UserVO(
                user.getId(),
                user.getPhone(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
