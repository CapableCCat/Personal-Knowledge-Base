package com.pkb.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 接口文档（Knife4j / springdoc）配置，仅 dev 环境生效。
 *
 * <p>访问入口：http://127.0.0.1:8080/doc.html
 */
@Configuration
@Profile("dev")
public class OpenApiConfig {

    @Bean
    public OpenAPI pkbOpenApi() {
        return new OpenAPI().info(new Info()
                .title("PKB 个人知识库 API")
                .version("0.1.0")
                .description("个人知识库（Personal Knowledge Base）· V0.1 后端接口（仅 dev 环境暴露）"));
    }
}
