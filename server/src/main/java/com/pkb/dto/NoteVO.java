package com.pkb.dto;

import com.pkb.entity.Note;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记详情出参（**含全文**）。
 *
 * <p>列表接口不用它 —— 列表刻意不传正文（见 {@link NoteListItemVO}）。
 *
 * @param projectId 所属项目 id（`tag.type=1`；null = 未挂项目）—— **UI 单选，最多一个**
 * @param tags      标签列表（`tag.type=0`，可多个，按名称排序方便展示）
 */
public record NoteVO(
        Long id,
        String title,
        String content,
        Integer contentFormat,
        Integer status,
        Integer pinned,
        Long projectId,
        List<TagBriefVO> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /** 不带关联的兜底构造（关联由 Service 层单独装配；见 NoteServiceImpl） */
    public static NoteVO from(Note note) {
        return from(note, null, List.of());
    }

    public static NoteVO from(Note note, Long projectId, List<TagBriefVO> tags) {
        if (note == null) {
            return null;
        }
        return new NoteVO(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getContentFormat(),
                note.getStatus(),
                note.getPinned(),
                projectId,
                tags == null ? List.of() : tags,
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
