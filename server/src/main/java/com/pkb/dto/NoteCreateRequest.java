package com.pkb.dto;

/**
 * 新建笔记请求（P-001 快速记录 / P-005 待整理闭环）。
 *
 * <p>所有字段都可空，但**标题与正文不能同时为空**（Service 层校验 → 40000）。
 *
 * @param title         标题（超 255 字会被**截断**入库，全文仍进 content —— 见 PRD P-001）
 * @param content       正文原文
 * @param contentFormat 格式：0=md（默认）/ 1=html；**创建后不可切换**（铁律①）
 * @param associations  项目 / 标签（T8，可选）：**带了就全量建立，不带就不建**
 *                      —— 与笔记在同一个事务里，避免「笔记建好了、标签没挂上」
 */
public record NoteCreateRequest(
        String title,
        String content,
        Integer contentFormat,
        NoteAssociationsRequest associations
) {
}
