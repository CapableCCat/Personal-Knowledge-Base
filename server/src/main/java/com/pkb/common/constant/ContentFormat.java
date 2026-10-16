package com.pkb.common.constant;

/**
 * 笔记内容格式（`note.content_format`）。
 *
 * <p>⚠️ **铁律①（PRD 冻结）**：一条笔记一种格式 —— 新建时选定、**之后不切换**、默认 md。
 * 已否决「双列镜像存 md + html」（双份存储易漂）与「只存 md 实时转」（转换有损）。
 *
 * <p>因此 T5 的笔记更新接口**必须显式拦截** `content_format` 字段：
 * 即使前端传了也不生效（建议直接拒绝该字段的更新请求）。
 */
public enum ContentFormat {

    /** Markdown（默认） */
    MD(0, "md"),

    /** 富文本 HTML */
    HTML(1, "html");

    private final int code;
    private final String label;

    ContentFormat(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    /** 数据库默认值对应的格式 */
    public static ContentFormat defaultFormat() {
        return MD;
    }

    public static ContentFormat of(Integer code) {
        if (code == null) {
            return defaultFormat();
        }
        for (ContentFormat value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return defaultFormat();
    }
}
