package com.pkb.dto;

/**
 * 新建标签 / 项目。
 *
 * @param name  名称（必填，同类型内不重复；服务层 trim + 最长 64）
 * @param type  类型：0=标签 / 1=项目（必填 —— 刻意不设默认值，避免把项目误建成标签）
 * @param color 颜色（可选，`#RRGGBB` / `#RGB`）
 * @param sort  排序（可选；不传则排在同类型末尾 = 当前最大 sort + 10）
 */
public record TagCreateRequest(String name, Integer type, String color, Integer sort) {
}
