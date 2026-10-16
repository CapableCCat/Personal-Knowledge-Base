package com.pkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pkb.entity.Attachment;

/**
 * 附件 Mapper。
 *
 * <p>⚠️ 一条笔记可挂多个附件；{@code file_key} 存的是**对象存储 key**，
 * 读取时由 {@code MinioProperties#resolvePublicPrefix} 拼公开前缀。
 */
public interface AttachmentMapper extends BaseMapper<Attachment> {
}
