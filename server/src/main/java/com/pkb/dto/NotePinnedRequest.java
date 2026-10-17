package com.pkb.dto;

/**
 * 置顶开关请求。
 *
 * @param pinned 0=取消置顶 / 1=置顶（其余值 → 40000）
 */
public record NotePinnedRequest(Integer pinned) {
}
