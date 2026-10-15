package com.pkb;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PKB 个人知识库（Personal Knowledge Base）后端启动类。
 *
 * <p>单用户自用「第二大脑」· V0.1（存得进、找得到）。
 */
@SpringBootApplication
@MapperScan("com.pkb.mapper")
public class PkbServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PkbServerApplication.class, args);
    }
}
