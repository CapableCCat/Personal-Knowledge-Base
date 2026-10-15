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
 * 账号（表 user）—— **单账号系统，表中只有 1 行**。
 *
 * <p>字段级依据：PRD 附录 C.3（2026-10-09 拍板）。由 OSC 的 25 列精简为 **7 列**；
 * 删除的 18 列全部是社团业务字段（成员档案 / 权限推导 / 飞书 / 生源地 / 审计人）。
 *
 * <p>⚠️ 两处与 OSC 的关键差异：
 * <ul>
 *   <li>**无 `activated_at`** —— 它的唯一用途是「首登强制改密」判定，该逻辑已整删（D14）</li>
 *   <li>**无 `created_by` / `updated_by`** —— 单账号下恒等于自己，无信息量</li>
 * </ul>
 *
 * <p>字段与 `server/sql/01_schema.sql` 一一对应，新增字段请同步建表脚本。
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 手机号：业务主键，全局唯一，同时是登录账号 */
    private String phone;

    /** BCrypt 密文 */
    private String password;

    /** 状态 0正常 1停用（单账号自用，保留作为兜底开关） */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
