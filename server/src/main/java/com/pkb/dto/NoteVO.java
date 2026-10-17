package com.pkb.dto;

import com.pkb.entity.Note;

import java.time.LocalDateTime;

/**
 * 笔记详情出参（**含全文**）。
 *
 * <p>列表接口不用它 —— 列表刻意不传正文（见 {@link NoteListItemVO}）。
 */
public record NoteVO(
        Long id,
        String title,
        String content,
        Integer contentFormat,
        Integer status,
        Integer pinned,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static NoteVO from(Note note) {
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
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
