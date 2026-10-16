package com.pkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pkb.entity.Note;

/**
 * 笔记 Mapper（由启动类上的 {@code @MapperScan("com.pkb.mapper")} 注册）。
 *
 * <p>⚠️ 本 Mapper **不承载 {@code content_text} 的计算逻辑** ——
 * 铁律③要求它必须与 {@code content} 同步，故统一由
 * {@link com.pkb.common.ContentTextBuilder} 在 Service 层收口，
 * **禁止在这里或调用方散落写**（漏同步 = 搜索漏数据，且不报错）。
 */
public interface NoteMapper extends BaseMapper<Note> {
}
