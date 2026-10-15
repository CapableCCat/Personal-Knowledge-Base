package com.pkb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * MinIO 对象存储配置（P-009 附件）。
 *
 * <p>凭据为空时视为「未启用」：应用照常启动，只有附件上传接口会返回友好错误，
 * 避免本地没起 MinIO 就整个服务起不来（沿用 OSC 的可降级思路）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "pkb.minio")
public class MinioProperties {

    private String endpoint = "http://127.0.0.1:9000";

    private String accessKey;

    private String secretKey;

    private String bucket = "pkb";

    /** 对外公开地址前缀；留空则回退为 {@code endpoint/bucket} */
    private String publicUrl;

    /** 是否已配置可用凭据 */
    public boolean configured() {
        return StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey);
    }

    /** 对象公开访问前缀（末尾不带 /） */
    public String resolvePublicPrefix() {
        String prefix = StringUtils.hasText(publicUrl)
                ? publicUrl.trim()
                : endpoint.trim() + "/" + bucket;
        return prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
    }
}
