package com.pkb.dto;

/**
 * 系统初始化状态（P-011：首次启动若无账号，则强制引导创建）。
 *
 * @param initialized user 表是否已有记录
 */
public record InitStatusVO(boolean initialized) {
}
