package com.pkb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 笔记—标签 / 项目关联（表 `note_tag`）—— **联合主键**。
 *
 * <p>字段依据：PRD V0.1 §5.1 + §6.3。
 *
 * <h3>⚠️ 为什么两个字段都**没有** {@code @TableId}</h3>
 * <p><b>实测结论（T2 验证器跑出来的）</b>：MyBatis-Plus **3.5.7 不允许一个实体有多个
 * {@code @TableId}** —— 加了两个会在启动时直接抛
 * {@code MybatisPlusException: @TableId can't more than one in Class}，
 * 连 Mapper 都注册不上（整个应用起不来）。
 *
 * <p>而 MP 也没有为「真正的复合主键」提供开箱支持，所以本实体刻意**不声明主键**：
 * <ul>
 *   <li>联合主键约束由**数据库**负责（{@code PRIMARY KEY (note_id, tag_id)}）——
 *       重复关联会撞主键，这是「关联天然幂等」的来源</li>
 *   <li>本表只用 {@code insert} / {@code selectList} / 条件 {@code delete}，
 *       **不用也不需要** {@code deleteById} / {@code updateById}
 *       （那两个方法在没有主键声明时不可用，见下）</li>
 * </ul>
 *
 * <h3>使用约束（T8 遵守）</h3>
 * <ul>
 *   <li>建立关联 = {@code insert} 一行</li>
 *   <li>解除关联 = **条件物理删除**（该表无 {@code is_deleted} 列，见清单 D28）：
 *       <pre>{@code
 *       noteTagMapper.delete(Wrappers.<NoteTag>lambdaQuery()
 *               .eq(NoteTag::getNoteId, noteId)
 *               .in(NoteTag::getTagId, tagIds));
 *       }</pre></li>
 *   <li>⚠️ **禁止调用** {@code deleteById} / {@code updateById} —— 没有主键声明，
 *       这两个方法不可用（会报找不到主键）</li>
 * </ul>
 *
 * <p>⚠️ 表层面**不限制**一条笔记只能有一个 {@code type=1} 的项目 ——
 * 这个约束在**后端写入逻辑**里兜底（铁律②）。
 */
@Data
@TableName("note_tag")
public class NoteTag {

    /** 笔记 id（联合主键之一，约束在数据库） */
    private Long noteId;

    /** 标签 / 项目 id（联合主键之一，约束在数据库） */
    private Long tagId;
}
