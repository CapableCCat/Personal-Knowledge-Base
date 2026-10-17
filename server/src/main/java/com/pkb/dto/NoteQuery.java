package com.pkb.dto;

/**
 * 笔记列表查询参数。
 *
 * @param page   页码，从 1 开始（默认 1）
 * @param size   每页条数，1~100（默认 20）
 * @param status 状态筛选：0=待整理 / 1=已归档 / **null=全部**
 */
public record NoteQuery(Integer page, Integer size, Integer status) {
}
