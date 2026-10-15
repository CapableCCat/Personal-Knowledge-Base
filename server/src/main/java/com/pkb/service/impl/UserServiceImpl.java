package com.pkb.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.pkb.common.exception.BusinessException;
import com.pkb.common.result.ResultCode;
import com.pkb.dto.UserVO;
import com.pkb.entity.User;
import com.pkb.mapper.UserMapper;
import com.pkb.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 当前账号相关实现（P-011）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** 账号状态：停用 */
    private static final int STATUS_DISABLED = 1;

    private final UserMapper userMapper;

    @Override
    public UserVO current() {
        return UserVO.from(requireCurrentUser());
    }

    // ------------------------------------------------------------
    // 内部方法
    // ------------------------------------------------------------

    /** 取当前登录账号，并做存在性 / 停用校验 */
    private User requireCurrentUser() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "账号不存在或已被删除");
        }
        if (Objects.equals(user.getStatus(), STATUS_DISABLED)) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        return user;
    }
}
