package com.pkb.controller;

import com.pkb.common.result.Result;
import com.pkb.dto.UserVO;
import com.pkb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前账号接口（需登录）。
 *
 * <p>与 OSC 的差异：改密 / 资料 / 学号 / 头像全部随业务删除，只剩「读当前账号」。
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 当前登录账号信息（不含密码） */
    @GetMapping("/current")
    public Result<UserVO> current() {
        return Result.ok(userService.current());
    }
}
