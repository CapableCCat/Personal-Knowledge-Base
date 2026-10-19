package com.pkb.dto;

/**
 * 首页状态条出参（P-010）。
 *
 * <p>四项口径（全部 **`is_deleted=0`**）：
 * <ul>
 *   <li>{@code inbox} —— `status=0` 的笔记数（待整理）</li>
 *   <li>{@code archived} —— `status=1` 的笔记数（已归档）</li>
 *   <li>{@code projects} —— `tag.type=1` 的项目数</li>
 *   <li>{@code weekAdded} —— **本周新建**的笔记数（按 `created_at`，不区分状态）</li>
 * </ul>
 *
 * <p>⚠️ 「本周」口径已拍板（PRD #11，2026-10-19）：**从本周一 00:00 起算**。
 * 四项一次请求返回，避免首页打 4 个接口。
 */
public record NoteStatsVO(long inbox, long archived, long projects, long weekAdded) {
}
