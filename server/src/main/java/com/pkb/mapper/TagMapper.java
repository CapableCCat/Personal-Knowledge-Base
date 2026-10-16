package com.pkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pkb.entity.Tag;

/**
 * 标签 / 项目 Mapper（两者共用 `tag` 表，靠 {@code type} 区分）。
 *
 * <p>⚠️ 名称唯一性由**应用层查重**保证（清单 D26），库层面没有唯一索引 ——
 * 新建 / 改名时（T8）必须查「同名且未逻辑删除」的标签。
 */
public interface TagMapper extends BaseMapper<Tag> {
}
