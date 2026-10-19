package com.pkb.service;

import com.pkb.dto.TagCreateRequest;
import com.pkb.dto.TagSortRequest;
import com.pkb.dto.TagUpdateRequest;
import com.pkb.dto.TagVO;

import java.util.List;

/**
 * 标签 / 项目管理（P-006 / P-007）—— 两者共用 `tag` 表，靠 {@code type} 区分。
 *
 * <p>⚠️ 三条与数据一致性相关的约定：
 * <ol>
 *   <li>**名称同类型内不重复**：库层面没有唯一索引（清单 D26），由本层查重兜住；</li>
 *   <li>**删除只解关联、不删笔记**：先物理删掉 `note_tag` 里的关联行，再逻辑删除 tag；</li>
 *   <li>**计数口径 = 全部状态（含待整理）**：PRD #12 于 2026-10-19 拍板。</li>
 * </ol>
 */
public interface TagService {

    /**
     * 列表（管理页 / 笔记表单的选项）。
     *
     * @param type 0=标签 / 1=项目 / null=两类都返回（前端表单一次拿全）
     */
    List<TagVO> list(Integer type);

    TagVO create(TagCreateRequest request);

    TagVO update(Long id, TagUpdateRequest request);

    /**
     * 删除标签 / 项目：解除全部关联 + 逻辑删除。
     *
     * @return 被解除关联的笔记数（前端用于「影响 N 条笔记」提示的复核）
     */
    int delete(Long id);

    /** 按给定顺序重排同一类型下的全部条目（顺序落库成 0,10,20…） */
    List<TagVO> sort(TagSortRequest request);
}
