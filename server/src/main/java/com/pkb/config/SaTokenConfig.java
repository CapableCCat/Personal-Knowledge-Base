package com.pkb.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.pkb.common.exception.BusinessException;
import com.pkb.common.result.ResultCode;
import com.pkb.entity.User;
import com.pkb.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Objects;

/**
 * Sa-Token 鉴权配置。
 *
 * <p>拦截器做两件事：
 * <ol>
 *   <li>校验登录态（Header 里的 pkb-token）</li>
 *   <li>停用兜底：status=1 的账号一律拦下，使「停用」立即生效</li>
 * </ol>
 *
 * <p>⚠️ 与 OSC 的差异（PRD §2.2 / 清单 D13、D17）：
 * <ul>
 *   <li>**无角色 / 权限判定** —— 单账号无权限矩阵，OSC 的 {@code StpInterfaceImpl} 已删</li>
 *   <li>**无首登强制改密兜底** —— 该逻辑整删（D14）</li>
 *   <li>白名单**只剩登录链路所需** —— OSC 的 `/dict/*`、`/recruit/*`、`/feedback/*` 公开读全部作废</li>
 * </ul>
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

    /** 账号状态：停用 */
    private static final int STATUS_DISABLED = 1;

    private final UserMapper userMapper;

    /** 免登录白名单 */
    public static final String[] WHITE_LIST = {
            "/health",
            "/error",
            // 登录与初始化（初始化接口在业务层再判断"是否已初始化"）
            "/auth/captcha",
            "/auth/init-status",
            "/auth/init-admin",
            "/auth/login",
            // dev 接口文档（prod 下 knife4j / springdoc 已整体关闭）
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/favicon.ico"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 1. 登录态
                    StpUtil.checkLogin();

                    // 2. 停用兜底：库里把账号置为停用后，已登录的 token 立即失效
                    //    （登录入口本身也会拦，见 AuthServiceImpl；这里覆盖"停用前已登录"的情况）
                    User current = userMapper.selectById(StpUtil.getLoginIdAsLong());
                    if (current == null) {
                        throw new BusinessException(ResultCode.UNAUTHORIZED, "账号不存在或已被删除");
                    }
                    if (Objects.equals(current.getStatus(), STATUS_DISABLED)) {
                        throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
                    }
                }))
                .addPathPatterns("/**")
                .excludePathPatterns(WHITE_LIST);
    }
}
