package com.pkb.common.constant;

/**
 * 标签类型（`tag.type`）—— 标签与项目**共用一张表**，靠本字段区分。
 *
 * <p>⚠️ **铁律②（PRD 冻结）**：**项目 = {@link #PROJECT}**。
 * 表结构允许一条笔记关联多个项目，但 **UI 层约束为单个**（详情页项目是单选下拉、标签多选）。
 * 因此 **T8 的后端关联写入必须兜底**：一条笔记已有关联的 `type=1` 标签时
 * **替换而非新增** —— 否则数据层会出现「一条笔记多个项目」的脏数据（UI 单选挡不住直接调接口）。
 */
public enum TagType {

    /** 标签（可多选） */
    TAG(0, "标签"),

    /** 项目（UI 单选） */
    PROJECT(1, "项目");

    private final int code;
    private final String label;

    TagType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static TagType of(Integer code) {
        if (code == null) {
            return TAG;
        }
        for (TagType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return TAG;
    }
}
