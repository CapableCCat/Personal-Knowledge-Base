package com.pkb.service;

import com.pkb.dto.NoteCreateRequest;
import com.pkb.dto.NoteListItemVO;
import com.pkb.dto.NoteQuery;
import com.pkb.dto.NoteUpdateRequest;
import com.pkb.dto.NoteVO;
import com.pkb.dto.PageResult;

/**
 * 笔记 CRUD（P-001 快速记录 / P-005 待整理闭环 / P-002 列表的后端部分）。
 *
 * <p>⚠️ 铁律③：**所有写操作都必须经本接口** —— `content_text` 的同步只在这里发生，
 * 任何绕过 Service 直接操作 Mapper 的写法都会让搜索静默漏数据。
 *
 * <p>⚠️ 铁律①：`content_format` 创建后不可切换（更新接口显式拒绝）。
 */
public interface NoteService {

    /** 新建（默认 inbox / 未置顶 / md） */
    NoteVO create(NoteCreateRequest request);

    /** 详情（含全文）；不存在或已删除 → 40400 */
    NoteVO detail(Long id);

    /** 列表（分页 + 状态筛选；**排序固定** `pinned DESC, updated_at DESC`；**不返回正文**） */
    PageResult<NoteListItemVO> page(NoteQuery query);

    /** 更新标题 / 正文 / 置顶（整体提交语义；格式不可改） */
    NoteVO update(Long id, NoteUpdateRequest request);

    /** 归档（inbox → archived，**幂等**） */
    NoteVO archive(Long id);

    /** 取回（archived → inbox，**幂等**） */
    NoteVO unarchive(Long id);

    /** 置顶开关（0/1） */
    NoteVO pin(Long id, Integer pinned);

    /** 逻辑删除（`is_deleted=1`，行仍在库里） */
    void delete(Long id);
}
