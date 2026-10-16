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
 * 标签 / 项目（表 `tag`）—— 两者**共用一张表**，靠 {@link #type} 区分。
 *
 * <p>字段依据：PRD V0.1 §5.1 + §6.3。
 *
 * <p>⚠️ <b>铁律②</b>：**项目 = {@code type=1}**。表结构允许一条笔记关联多个项目，
 * 但 UI 约束为单个；后端关联写入必须兜底（替换而非新增）—— 见
 * {@link com.pkb.common.constant.TagType}。
 *
 * <p>⚠️ <b>名称唯一性走应用层查重（清单 D26）</b>：
 * 库层面**故意不建** `UNIQUE(name, type)` —— 因为 {@code isDeleted} 与唯一索引天然冲突
 * （逻辑删除的标签仍占用同名，之后新建同名会撞键）。所以 T8 在新建 / 改名时
 * 必须查「同名且未逻辑删除」的标签。
 *
 * <p>字段与 `server/sql/01_schema.sql` 一一对应，新增字段请同步建表脚本。
 */
@Data
@TableName("tag")
public class Tag {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 名称（同类型内不重复 —— 由**应用层**保证，见类注释） */
    private String name;

    /** 类型 0=标签 1=项目 —— 见 {@link com.pkb.common.constant.TagType} */
    private Integer type;

    /** 颜色（十六进制，如 #3557F4；可空） */
    private String color;

    /** 排序（小的在前） */
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer isDeleted;
}
