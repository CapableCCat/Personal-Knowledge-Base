package com.pkb.dto;

/**
 * 图形验证码响应。
 *
 * @param captchaKey   验证码标识（登录时原样回传）
 * @param captchaImage 图片（data URL，可直接给 img 的 src）
 */
public record CaptchaVO(String captchaKey, String captchaImage) {
}
