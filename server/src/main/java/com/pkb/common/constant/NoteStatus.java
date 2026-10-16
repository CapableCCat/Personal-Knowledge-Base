package com.pkb.common.constant;

/**
 * 笔记状态（`note.status`）—— **待整理闭环**的状态定义（PRD P-005，核心闭环）。
 *
 * <p>闭环：`写（不管分类）→ INBOX 待整理 → 清理归类 → ARCHIVED 已归档`。
 * 新建笔记一律 {@link #INBOX}。
 */
public enum NoteStatus {

    /** 待整理（inbox）—— 新建默认 */
    INBOX(0, "待整理"),

    /** 已归档（archived）—— 正式库 */
    ARCHIVED(1, "已归档");

    private final int code;
    private final String label;

    NoteStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    /** 新建笔记的默认状态 */
    public static NoteStatus defaultStatus() {
        return INBOX;
    }

    public static NoteStatus of(Integer code) {
        if (code == null) {
            return defaultStatus();
        }
        for (NoteStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return defaultStatus();
    }
}
