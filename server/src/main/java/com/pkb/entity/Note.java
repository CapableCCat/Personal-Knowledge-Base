package com.pkb.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记（表 `note`）。
 *
 * <p>字段依据：PRD V0.1 §5.1 + §6.1。
 *
 * <p><b>三条铁律里，本表占两条</b>：
 * <ul>
 *   <li><b>①一条笔记一种格式</b>：{@code contentFormat} 新建时选定（默认 md）、**之后不切换**。
 *       更新接口必须显式拦截该字段 —— 见 {@link com.pkb.common.constant.ContentFormat}</li>
 *   <li><b>③{@code contentText} 是搜索专用列</b>：md 存轻量去标记文本、html 由 jsoup 剥标签得到。
 *       **搜索只打 `title + content_text`，永不打 `content`**。
 *       ⚠️ 漏同步 = 搜索漏数据，且**不报错**（PRD 风险 R1）——
 *       故新增 / 更新必须经 {@link com.pkb.common.ContentTextBuilder} 统一收口，禁止 Mapper 层散落写</li>
 * </ul>
 *
 * <p>排序口径（冻结）：{@code ORDER BY pinned DESC, updated_at DESC}。
 *
 * <p>⚠️ {@code updatedAt} 是**业务字段**（参与排序）：归档 / 取回也会改动它
 * （PRD P-005 已登记该行为待确认）。
 *
 * <p>字段与 `server/sql/01_schema.sql` 一一对应，新增字段请同步建表脚本。
 */
@Data
@TableName("note")
public class Note {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题（允许空字符串，UI 显示占位文案） */
    private String title;

    /** 正文原文；格式由 {@link #contentFormat} 决定 */
    private String content;

    /** 格式 0=md 1=html —— 建后**不可切换**（铁律①） */
    private Integer contentFormat;

    /**
     * **搜索专用纯文本**（铁律③）。
     *
     * <p>md → 轻量去标记；html → jsoup 剥标签。必须与 {@link #content} 同步生成。
     */
    private String contentText;

    /** 状态 0=inbox 待整理 1=archived 已归档 —— 见 {@link com.pkb.common.constant.NoteStatus} */
    private Integer status;

    /** 置顶 0否 1是 —— 参与排序（用户明确要求保留） */
    private Integer pinned;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
