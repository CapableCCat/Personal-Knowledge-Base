package com.pkb.controller;

import com.pkb.common.result.Result;
import com.pkb.dto.NoteCreateRequest;
import com.pkb.dto.NoteListItemVO;
import com.pkb.dto.NotePinnedRequest;
import com.pkb.dto.NoteQuery;
import com.pkb.dto.NoteUpdateRequest;
import com.pkb.dto.NoteVO;
import com.pkb.dto.PageResult;
import com.pkb.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 笔记接口（P-001 快速记录 / P-002 列表 / P-005 待整理闭环）。
 *
 * <p>⚠️ 本组接口**全部需要登录**（不在 `SaTokenConfig` 白名单里）。
 *
 * <p>⚠️ 归档 / 取回刻意做成**两个显式端点**（而不是一个带 status 参数的 PATCH）：
 * 这样调用方不可能传进非法状态，语义也更贴近「归档」「取回」两个动作。
 */
@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /** 新建（默认 inbox、未置顶、md） */
    @PostMapping
    public Result<NoteVO> create(@RequestBody NoteCreateRequest request) {
        return Result.ok(noteService.create(request), "已保存");
    }

    /** 列表（分页 + 状态筛选；不含正文，只返回摘要） */
    @GetMapping("/page")
    public Result<PageResult<NoteListItemVO>> page(NoteQuery query) {
        return Result.ok(noteService.page(query));
    }

    /** 详情（含全文） */
    @GetMapping("/{id}")
    public Result<NoteVO> detail(@PathVariable Long id) {
        return Result.ok(noteService.detail(id));
    }

    /** 更新标题 / 正文 / 置顶（格式不可改） */
    @PutMapping("/{id}")
    public Result<NoteVO> update(@PathVariable Long id, @RequestBody NoteUpdateRequest request) {
        return Result.ok(noteService.update(id, request), "已更新");
    }

    /** 归档（待整理 → 已归档，幂等） */
    @PostMapping("/{id}/archive")
    public Result<NoteVO> archive(@PathVariable Long id) {
        return Result.ok(noteService.archive(id), "已归档");
    }

    /** 取回（已归档 → 待整理，幂等） */
    @PostMapping("/{id}/unarchive")
    public Result<NoteVO> unarchive(@PathVariable Long id) {
        return Result.ok(noteService.unarchive(id), "已取回待整理");
    }

    /** 置顶开关 */
    @PutMapping("/{id}/pinned")
    public Result<NoteVO> pin(@PathVariable Long id, @RequestBody NotePinnedRequest request) {
        return Result.ok(noteService.pin(id, request.pinned()), "已更新置顶");
    }

    /** 逻辑删除（行仍在库里，V0.1 无回收站） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noteService.delete(id);
        return Result.ok(null, "已删除");
    }
}
