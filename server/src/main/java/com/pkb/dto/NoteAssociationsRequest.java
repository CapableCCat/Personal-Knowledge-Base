package com.pkb.dto;

import java.util.List;

/**
 * 笔记与「项目 / 标签」的关联（P-006 / P-007）。
 *
 * <p>⚠️ **语义是「全量替换」，不是增量**（T8 定稿）：
 * <ul>
 *   <li>请求里**带了这个对象** → 以它为准：项目 = {@code projectId}（null 表示不挂项目），
 *       标签 = {@code tagIds}（null / 空数组表示没有标签）</li>
 *   <li>请求里**没带**（null）→ 完全不动关联（老调用方 / T5 测试不受影响）</li>
 * </ul>
 * 全量替换让「一条笔记只能有一个项目」这条铁律**在实现上天然成立**：
 * 服务端先清空该笔记的关联再按请求重建，不存在「新增第二个项目」的路径。
 *
 * @param projectId 项目 id（必须是 `tag.type=1`）；null = 不挂项目
 * @param tagIds    标签 id 列表（必须都是 `tag.type=0`，重复会被去重）
 */
public record NoteAssociationsRequest(Long projectId, List<Long> tagIds) {
}
