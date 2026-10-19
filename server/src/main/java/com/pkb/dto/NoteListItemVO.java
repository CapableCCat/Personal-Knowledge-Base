package com.pkb.dto;

import com.pkb.entity.Note;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记列表项（**不含正文** —— PRD P-002：列表接口不传全文）。
 *
 * @param excerpt 摘要：由 `content_text` 截断而来（纯文本，最多 200 字）
 * @param project 所属项目（`tag.type=1`，最多一个；null = 未挂项目）
 * @param tags    标签（`tag.type=0`，按名称排序 —— 展示截断（如 3 个 + `+n`）由前端决定）
 */
public record NoteListItemVO(
        Long id,
        String title,
        String excerpt,
        Integer contentFormat,
        Integer status,
        Integer pinned,
        TagBriefVO project,
        List<TagBriefVO> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /** 摘要最大长度 */
    public static final int EXCERPT_MAX = 200;

    public static NoteListItemVO from(Note note) {
        return from(note, null, List.of());
    }

    public static NoteListItemVO from(Note note, TagBriefVO project, List<TagBriefVO> tags) {
        if (note == null) {
            return null;
        }
        return new NoteListItemVO(
                note.getId(),
                note.getTitle(),
                excerptOf(note.getContentText()),
                note.getContentFormat(),
                note.getStatus(),
                note.getPinned(),
                project,
                tags == null ? List.of() : tags,
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }

    private static String excerptOf(String contentText) {
        if (contentText == null || contentText.isBlank()) {
            return "";
        }
        String text = contentText.trim();
        return text.length() <= EXCERPT_MAX ? text : text.substring(0, EXCERPT_MAX) + "…";
    }
}
