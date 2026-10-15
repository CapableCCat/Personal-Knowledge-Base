package com.pkb.controller;

import com.pkb.common.result.Result;
import com.pkb.dto.CaptchaVO;
import com.pkb.dto.InitAdminRequest;
import com.pkb.dto.InitStatusVO;
import com.pkb.dto.LoginRequest;
import com.pkb.dto.LoginVO;
import com.pkb.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证与初始化接口（本组接口全部免登录，见 SaTokenConfig 白名单）。
 *
 * <p>与 OSC 的差异：无「改密」端点（强制改密已删）。
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 图形验证码（算术型） */
    @GetMapping("/captcha")
    public Result<CaptchaVO> captcha() {
        return Result.ok(authService.createCaptcha());
    }

    /** 系统是否已初始化；前端据此决定是否强制跳引导页 */
    @GetMapping("/init-status")
    public Result<InitStatusVO> initStatus() {
        return Result.ok(authService.initStatus());
    }

    /** 创建唯一账号（仅未初始化时可用） */
    @PostMapping("/init-admin")
    public Result<Void> initAdmin(@Valid @RequestBody InitAdminRequest request) {
        authService.initAdmin(request);
        return Result.ok(null, "账号创建成功，请登录");
    }

    /** 登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request), "登录成功");
    }

    /** 登出 */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok(null, "已退出登录");
    }
}
