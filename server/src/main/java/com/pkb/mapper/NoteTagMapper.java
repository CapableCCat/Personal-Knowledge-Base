package com.pkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pkb.dto.TagCountRow;
import com.pkb.entity.NoteTag;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
 *
 * <p>T8 追加两条聚合查询（管理页计数 / 列表筛选）。⚠️ 一律 join `note` 并过滤
 * `is_deleted = 0` —— 逻辑删除的笔记不该出现在任何计数与筛选里。
 */
public interface NoteTagMapper extends BaseMapper<NoteTag> {

    /**
     * 按标签分组统计笔记数（**口径：全部状态，含待整理** —— PRD #12 拍板）。
     *
     * <p>一次分组查询拿全表计数，避免管理页「每个标签查一次」的 N+1。
     */
    @Select("SELECT nt.tag_id AS tagId, COUNT(*) AS total "
            + "FROM note_tag nt "
            + "JOIN note n ON n.id = nt.note_id AND n.is_deleted = 0 "
            + "GROUP BY nt.tag_id")
    List<TagCountRow> countNotesByTag();

    /**
     * 找出**同时拥有全部给定标签**的笔记 id（AND 语义，PRD P-002）。
     *
     * <p>`HAVING COUNT(DISTINCT tag_id) = 标签数` 是 AND 的经典写法：
     * 只要少一个标签，计数就对不上。标签 id 由服务层校验为数字，不存在注入面。
     *
     * @param tagIds 标签 id（非空、已去重）
     * @param size   标签个数（必须等于 tagIds.size()）
     */
    @Select("<script>"
            + "SELECT nt.note_id FROM note_tag nt "
            + "WHERE nt.tag_id IN "
            + "<foreach collection='tagIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "GROUP BY nt.note_id "
            + "HAVING COUNT(DISTINCT nt.tag_id) = #{size}"
            + "</script>")
    List<Long> findNoteIdsHavingAllTags(@Param("tagIds") List<Long> tagIds, @Param("size") int size);
}
