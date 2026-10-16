package com.pkb.config;

import com.pkb.common.ContentTextBuilder;
import com.pkb.util.HtmlSanitizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@code content_text} 生成器的装配。
 *
 * <p>用 {@code @Bean} 显式装配而**不是**给实现类打 {@code @Component}，原因有二：
 * <ol>
 *   <li>实现类 {@link ContentTextBuilder.Default} 是接口内部的嵌套类，
 *       用注解扫描容易让「谁是实现」变得隐晦；显式装配一眼可见</li>
 *   <li>它依赖 {@link HtmlSanitizer}（html 分支要剥标签）——
 *       依赖关系写在方法签名里，改依赖时不会被漏掉</li>
 * </ol>
 *
 * <p>⚠️ 本类只负责装配，**不含任何计算逻辑**。去标记规则属 PRD 第十二章 #13（归 T5），
 * 届时只改 {@code ContentTextBuilder.Default}，本文件不受影响。
 */
@Configuration
public class ContentTextConfig {

    @Bean
    public ContentTextBuilder contentTextBuilder(HtmlSanitizer htmlSanitizer) {
        return new ContentTextBuilder.Default(htmlSanitizer);
    }
}
