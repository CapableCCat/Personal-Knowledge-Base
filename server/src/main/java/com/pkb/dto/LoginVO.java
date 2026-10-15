package com.pkb.dto;

/**
 * 登录成功响应。
 *
 * @param tokenName  Header 名（pkb-token），与后端 sa-token.token-name 一致
 * @param tokenValue token 值，前端存本地并在后续请求 Header 里回传
 * @param user       当前账号信息
 */
public record LoginVO(String tokenName, String tokenValue, UserVO user) {
}
