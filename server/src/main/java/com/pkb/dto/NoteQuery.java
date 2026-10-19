package com.pkb.dto;

import java.util.List;

/**
 * 笔记列表查询参数。
 *
 * <p>筛选组合语义（PRD P-002 / P-010）：
 * <ul>
 *   <li>{@code status}：0=待整理 / 1=已归档 / null=全部</li>
 *   <li>{@code projectId}：项目（单选，`tag.type=1`）</li>
 *   <li>{@code tagIds}：标签（多选，**AND 语义** —— 必须同时命中全部标签）</li>
 *   <li>{@code weekOnly}：只看**本周新建**（`created_at` ≥ 本周一 00:00，PRD #11 口径）</li>
 * </ul>
 * 各类筛选可自由组合；切换筛选时前端负责把页码重置为 1。
 *
 * @param page      页码，从 1 开始（默认 1）
 * @param size      每页条数，1~100（默认 20）
 * @param status    状态筛选
 * @param projectId 项目筛选（单个项目 id）
 * @param tagIds    标签筛选（多个标签 id，AND）
 * @param weekOnly  只看本周新建（首页状态条「本周 +N」点进来的入口）
 */
public record NoteQuery(
        Integer page,
        Integer size,
        Integer status,
        Long projectId,
        List<Long> tagIds,
        Boolean weekOnly
) {
}
