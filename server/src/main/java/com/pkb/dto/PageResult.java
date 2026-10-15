package com.pkb.dto;

import java.util.List;

/**
 * 通用分页出参（列表接口统一用它，避免直接暴露 MP 的分页对象）。
 */
public record PageResult<T>(long total, long page, long size, List<T> records) {

    public static <T> PageResult<T> of(long total, long page, long size, List<T> records) {
        return new PageResult<>(total, page, size, records);
    }
}
