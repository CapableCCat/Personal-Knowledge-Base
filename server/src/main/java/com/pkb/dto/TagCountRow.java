package com.pkb.dto;

/**
 * `note_tag` 按标签分组的计数行（T8 管理页用）。
 *
 * @param tagId 标签 / 项目 id
 * @param total 该标签下**未删除**的笔记数（口径：全部状态，见 PRD #12）
 */
public record TagCountRow(Long tagId, Long total) {
}
