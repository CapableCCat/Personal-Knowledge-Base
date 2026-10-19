package com.pkb.dto;

import java.time.LocalDateTime;

/**
 * 标签 / 项目（管理页出参）。
 *
 * @param type      0=标签 1=项目
 * @param noteCount 关联的**未删除**笔记数 —— 口径见 PRD #12（2026-10-19 拍板）：
 *                  **全部（含待整理）**，不区分 status；删除确认提示的「影响条数」用同一个数
 */
public record TagVO(
        Long id,
        String name,
        Integer type,
        String color,
        Integer sort,
        Long noteCount,
        LocalDateTime createdAt
) {
}
