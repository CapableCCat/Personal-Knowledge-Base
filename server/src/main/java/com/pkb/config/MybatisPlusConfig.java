package com.pkb.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置：时间字段自动填充 + 内置分页插件。
 *
 * <p>⚠️ 与 OSC 的差异：**删掉了 created_by / updated_by 的填充**。
 * PRD 附录 C.3 已拍板删除这两列（单账号系统里「谁改的」恒等于自己，无信息量）；
 * 而 MyBatis-Plus 的 {@code strictInsertFill} 找不到对应属性会抛异常，
 * 故填充器必须与实体字段严格同步。见《开发任务点清单》§6 D12。
 */
@Slf4j
@Configuration
public class MybatisPlusConfig {

    /** 分页插件（V0.1 体量用 MP 内置分页） */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 时间字段自动填充：created_at / updated_at。
     *
     * <p>⚠️ **每个字段都先判 `hasSetter`**：T1 实测过 {@code strictInsertFill} 在实体缺少
     * 对应属性时会抛异常（当时是删了审计列却忘了删填充逻辑）。
     * T8 新增的 {@code note_tag} 两个字段都没有、{@code tag} 没有 {@code updatedAt} ——
     * 用「有才填」的写法，新增实体就不必回头改这里。
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();
                if (metaObject.hasSetter("createdAt")) {
                    strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
                }
                if (metaObject.hasSetter("updatedAt")) {
                    strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                if (metaObject.hasSetter("updatedAt")) {
                    strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
                }
            }
        };
    }
}
