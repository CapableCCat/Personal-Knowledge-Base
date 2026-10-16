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
 * 附件（表 `attachment`）—— 一条笔记可挂**多个**。
 *
 * <p>字段依据：PRD V0.1 §5.1 + §6.3。
 *
 * <p>⚠️ <b>PKB 的附件不止图片</b>（PRD P-009 明确要求支持 pdf / doc / xlsx / zip 等）：
 * OSC 的 {@link com.pkb.util.ImageValidator} 是**图片专用**（只认 jpg / png），
 * T11 的通用附件校验需另写「扩展名 + MIME 白名单 + 可识别类型的魔数校验」，**不要套用**它。
 *
 * <p>⚠️ {@code fileKey} 存的是**对象存储 key**（如 {@code attachments/202610/xxx.pdf}），
 * **不是完整 URL**；读取时才拼公开前缀（{@code MinioProperties#resolvePublicPrefix}）——
 * 这样换域名 / 换端口时历史附件不会集体失效。
 *
 * <p>字段与 `server/sql/01_schema.sql` 一一对应，新增字段请同步建表脚本。
 */
@Data
@TableName("attachment")
public class Attachment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属笔记 id */
    private Long noteId;

    /** 对象存储 key（**非完整 URL**） */
    private String fileKey;

    /** 原始文件名（展示与下载时用） */
    private String fileName;

    /** 字节数 */
    private Long size;

    /** MIME 类型（可空） */
    private String mime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer isDeleted;
}
