package com.pkb.dto;

import com.pkb.entity.Tag;

/**
 * 标签 / 项目的**轻量摘要**（嵌在笔记响应里，不带计数、不带时间）。
 *
 * <p>为什么单独一个 VO：笔记详情与列表都要带「项目名 + 标签」，但都不需要
 * {@code noteCount} / {@code createdAt} 这类管理页字段。用同一份数据会让笔记响应
 * 背上一堆用不到的字段，也让「计数」这种昂贵统计被无意中带进列表接口。
 *
 * @param type 0=标签 1=项目（前端据此把「项目」与「标签」分开展示）
 */
public record TagBriefVO(Long id, String name, Integer type, String color) {

    public static TagBriefVO from(Tag tag) {
        if (tag == null) {
            return null;
        }
        return new TagBriefVO(tag.getId(), tag.getName(), tag.getType(), tag.getColor());
    }
}
