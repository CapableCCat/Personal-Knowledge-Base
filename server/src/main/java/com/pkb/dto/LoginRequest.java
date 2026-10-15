package com.pkb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 登录请求（F-002）。
 */
public record LoginRequest(

        @NotBlank(message = "请填写手机号")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @NotBlank(message = "请填写密码")
        String password,

        @NotBlank(message = "验证码已失效，请点击图片刷新")
        String captchaKey,

        @NotBlank(message = "请填写验证码")
        String captchaCode
) {
}
