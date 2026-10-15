package com.pkb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 引导页创建**唯一账号**请求（P-011）。
 *
 * <p>与 OSC 的差异：无「姓名」字段（单账号不需要），不触发强制改密。
 */
public record InitAdminRequest(

        @NotBlank(message = "请填写手机号")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @NotBlank(message = "请填写密码")
        String password
) {
}
