package com.pkb.util;

import com.pkb.common.exception.BusinessException;
import com.pkb.common.result.ResultCode;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;

/**
 * 上传图片的三层校验（PKB：笔记正文内嵌图片；**图片专用**）。
 *
 * <p>⚠️ **PKB 的附件不限于图片**（P-009 要支持 pdf / doc / xlsx / zip 等），
 * 那类通用附件在 T11 另写按「扩展名 + MIME 白名单 + 可识别类型的魔数」的校验，
 * **不要**套用本类（本类只认 jpg / png）。
 *
 * <p>为什么必须三层：扩展名和 {@code Content-Type} 都是客户端说了算的，
 * 把 {@code .exe} 改名成 {@code .png}、或把 Content-Type 伪造成 {@code image/png} 都很容易，
 * 所以最后一道**文件头魔数**才是真正的判据。
 *
 * <p>抽取动机：头像与公告配图当年限制完全一致（jpg/png、≤2MB、同三层校验），
 * 各写一份必然漂移 —— 宁可让复用方多传一个 {@code label}（用于拼错误文案）。
 */
public final class ImageValidator {

    /** 单张图片上限 2MB（与 spring.servlet.multipart 配置一致） */
    public static final long MAX_SIZE = 2L * 1024 * 1024;

    private static final Map<String, String> ALLOWED_EXT = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png");

    /**
     * 校验通过后的图片内容。
     *
     * @param ext         归一化后的扩展名（jpg / jpeg / png）
     * @param contentType 写对象存储时用的 Content-Type
     * @param bytes       文件字节（已经读进内存，调用方直接拿去上传）
     */
    public record ValidatedImage(String ext, String contentType, byte[] bytes) {
    }

    /**
     * 校验上传的图片，不通过直接抛业务异常。
     *
     * @param file  上传文件
     * @param label 文案主语，如「头像」「图片」→「头像仅支持 jpg / png 格式」
     */
    public static ValidatedImage validate(MultipartFile file, String label) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ResultCode.PARAM_ERROR, label + "大小不能超过 2MB");
        }

        String ext = extensionOf(file.getOriginalFilename());
        if (ext == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, label + "仅支持 jpg / png 格式");
        }
        String declaredType = file.getContentType();
        if (StringUtils.hasText(declaredType) && !ALLOWED_EXT.get(ext).equalsIgnoreCase(declaredType)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, label + "格式与文件类型不一致，请重新选择图片");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "读取上传文件失败，请重试");
        }
        if (!matchesMagic(bytes, ext)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件内容不是有效的图片，请重新选择");
        }
        return new ValidatedImage(ext, ALLOWED_EXT.get(ext), bytes);
    }

    /** 取扩展名；非 jpg/jpeg/png 一律返回 null */
    public static String extensionOf(String filename) {
        if (!StringUtils.hasText(filename)) {
            return null;
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return null;
        }
        String ext = filename.substring(dot + 1).toLowerCase(Locale.ROOT);
        return ALLOWED_EXT.containsKey(ext) ? ext : null;
    }

    /** 文件头魔数：JPEG = FF D8 FF，PNG = 89 50 4E 47 0D 0A 1A 0A */
    public static boolean matchesMagic(byte[] bytes, String ext) {
        if (bytes == null) {
            return false;
        }
        if ("png".equals(ext)) {
            byte[] magic = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
            if (bytes.length < magic.length) {
                return false;
            }
            for (int i = 0; i < magic.length; i++) {
                if (bytes[i] != magic[i]) {
                    return false;
                }
            }
            return true;
        }
        return bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF;
    }

    private ImageValidator() {
    }
}
