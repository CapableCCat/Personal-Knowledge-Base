package com.pkb.service;

import com.pkb.dto.UserVO;

/**
 * 当前账号相关（P-011）。
 *
 * <p>单账号系统只有「读当前账号」一件事；个人中心 / 头像 / 学号 / 改密
 * 全部随 OSC 业务一并删除（PRD §1.4 非目标）。
 */
public interface UserService {

    /** 当前登录账号信息 */
    UserVO current();
}
