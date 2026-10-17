package com.pkb.dto;

/**
 * 更新笔记请求（整体提交语义）。
 *
 * <p>字段为 null 表示「本次不改」（不是清空）；要清空传空串。
 *
 * <p>⚠️ {@code contentFormat} 只允许**与现值相同**：
 * 非空且不同 → Service 抛 40000「笔记格式创建后不可修改」（铁律①）。
 * 刻意**不静默忽略** —— 静默会让调用方以为改成功了。
 */
public record NoteUpdateRequest(String title, String content, Integer contentFormat, Integer pinned) {
}
