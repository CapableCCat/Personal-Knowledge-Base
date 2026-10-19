package com.pkb.dto;

/**
 * 更新标签 / 项目（改名 / 改色 / 改排序）。
 *
 * <p>⚠️ **`type` 刻意不在请求里** —— 标签与项目是两种语义，建后不可互转
 * （转过去历史关联的含义就变了）；要换类型请删了重建。
 *
 * <p>字段为 null 表示「本次不改」（与笔记更新同一套约定）。
 */
public record TagUpdateRequest(String name, String color, Integer sort) {
}
