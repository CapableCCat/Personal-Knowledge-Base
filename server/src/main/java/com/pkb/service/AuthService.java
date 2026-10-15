package com.pkb.service;

import com.pkb.dto.CaptchaVO;
import com.pkb.dto.InitAdminRequest;
import com.pkb.dto.InitStatusVO;
import com.pkb.dto.LoginRequest;
import com.pkb.dto.LoginVO;

/**
 * 认证与初始化（P-011 单账号登录）。
 *
 * <p>与 OSC 的差异：**无强制改密、无角色**；{@code initAdmin} 的语义是「创建唯一账号」。
 */
public interface AuthService {

    /** 生成算术图形验证码（答案存 Redis，校验后作废） */
    CaptchaVO createCaptcha();

    /** 系统是否已初始化（user 表是否有记录） */
    InitStatusVO initStatus();

    /** 创建唯一账号（仅在未初始化时可用） */
    void initAdmin(InitAdminRequest request);

    /** 手机号 + 密码 + 验证码登录 */
    LoginVO login(LoginRequest request);

    /** 登出当前登录态 */
    void logout();
}
