package com.pkb.controller;

import com.pkb.common.result.Result;
import com.pkb.dto.TagCreateRequest;
import com.pkb.dto.TagSortRequest;
import com.pkb.dto.TagUpdateRequest;
import com.pkb.dto.TagVO;
import com.pkb.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签 / 项目接口（P-006 / P-007）—— 两者共用 `tag` 表，靠 `type` 区分。
 *
 * <p>⚠️ 本组接口**全部需要登录**（不在 `SaTokenConfig` 白名单里）。
 *
 * <p>⚠️ 路由顺序：`PUT /tag/sort` 与 `PUT /tag/{id}` 并存 —— Spring 会优先匹配
 * 字面量路径 `sort`，所以不会被 `{id}` 抢走；但**别把 `{id}` 改成 `/{id:\\d+}`**，
 * 那会引入不必要的复杂度。
 */
@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 列表。
     *
     * @param type 0=标签 / 1=项目 / 不传=两类都返回（笔记表单一次拿全，少一次往返）
     */
    @GetMapping
    public Result<List<TagVO>> list(@RequestParam(required = false) Integer type) {
        return Result.ok(tagService.list(type));
    }

    @PostMapping
    public Result<TagVO> create(@RequestBody TagCreateRequest request) {
        return Result.ok(tagService.create(request), "已创建");
    }

    /** 改名 / 改色 / 改排序（`type` 不可变） */
    @PutMapping("/{id}")
    public Result<TagVO> update(@PathVariable Long id, @RequestBody TagUpdateRequest request) {
        return Result.ok(tagService.update(id, request), "已更新");
    }

    /** 按给定顺序重排该类型下的全部条目 */
    @PutMapping("/sort")
    public Result<List<TagVO>> sort(@RequestBody TagSortRequest request) {
        return Result.ok(tagService.sort(request), "已排序");
    }

    /**
     * 删除（解除全部关联 + 逻辑删除）。
     *
     * @return 被解除关联的笔记数 —— 前端二次确认里的「影响 N 条笔记」用它复核
     */
    @DeleteMapping("/{id}")
    public Result<Integer> delete(@PathVariable Long id) {
        int affected = tagService.delete(id);
        return Result.ok(affected, "已删除，解除了 " + affected + " 条笔记的关联");
    }
}
