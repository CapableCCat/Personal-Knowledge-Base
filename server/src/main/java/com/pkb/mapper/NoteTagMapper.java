package com.pkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pkb.entity.NoteTag;

/**
 * 笔记—标签关联 Mapper（联合主键在**数据库**，实体不声明主键）。
 *
 * <p>⚠️ **T2 实测结论**：MyBatis-Plus 3.5.7 不允许实体有多个 {@code @TableId}
 * （会在启动时抛异常、Mapper 注册失败）。故 {@link NoteTag} 的两个字段都不带
 * {@code @TableId}，联合主键约束由数据库的 {@code PRIMARY KEY (note_id, tag_id)} 负责 ——
 * 这也是「重复关联天然幂等」的来源。
 *
 * <p>⚠️ 使用约束（清单 D28）：
 * <ul>
 *   <li>关联的增删一律**物理操作**（该表无 {@code is_deleted} 列）</li>
 *   <li>解除关联**不要用 {@code deleteById}**（没有主键声明，该方法不可用），
 *       一律走 {@code LambdaQueryWrapper}：
 *       <pre>{@code
 *       noteTagMapper.delete(Wrappers.<NoteTag>lambdaQuery()
 *               .eq(NoteTag::getNoteId, noteId)
 *               .in(NoteTag::getTagId, tagIds));
 *       }</pre></li>
 * </ul>
 */
public interface NoteTagMapper extends BaseMapper<NoteTag> {
}
