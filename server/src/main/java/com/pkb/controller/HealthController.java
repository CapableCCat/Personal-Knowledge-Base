package com.pkb.controller;

import com.pkb.common.result.Result;
import com.pkb.controller.vo.HealthVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 健康检查（免鉴权，见 SaTokenConfig 白名单）。
 *
 * <p>设计取舍：下游（Redis / MySQL）不可用时，HTTP 与 {@code code} 仍为 200
 * —— 200 表示"应用进程自身存活"，真实状态放在 {@code data.components} 里。
 * 这样建库之前也能干净地验收 Redis 连通性，建库后 mysql 自动转 UP。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class HealthController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DETAIL_MAX_LENGTH = 160;

    private final RedisConnectionFactory redisConnectionFactory;
    private final DataSource dataSource;

    @Value("${spring.application.name:pkb-server}")
    private String appName;

    @Value("${spring.profiles.active:dev}")
    private String profile;

    @Value("${pkb.app.version:unknown}")
    private String version;

    @Value("${pkb.health.expose-detail:true}")
    private boolean exposeDetail;

    @GetMapping("/health")
    public Result<HealthVO> health() {
        Map<String, HealthVO.Component> components = new LinkedHashMap<>();
        components.put("redis", checkRedis());
        components.put("mysql", checkMysql());

        HealthVO health = new HealthVO(
                appName,
                profile,
                version,
                LocalDateTime.now().format(TIME_FORMATTER),
                formatUptime(),
                components);
        return Result.ok(health);
    }

    private HealthVO.Component checkRedis() {
        long start = System.nanoTime();
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.ping();
            return HealthVO.Component.up(elapsedMs(start));
        } catch (Exception e) {
            long cost = elapsedMs(start);
            log.warn("Redis 健康检查失败（{}ms）：{}", cost, e.getMessage());
            return HealthVO.Component.down(cost, detail(e));
        }
    }

    private HealthVO.Component checkMysql() {
        long start = System.nanoTime();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
            return HealthVO.Component.up(elapsedMs(start));
        } catch (Exception e) {
            long cost = elapsedMs(start);
            log.warn("MySQL 健康检查失败（{}ms）：{}", cost, e.getMessage());
            return HealthVO.Component.down(cost, detail(e));
        }
    }

    private long elapsedMs(long startNano) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNano);
    }

    /** 只回状态、不回详情时返回 null，避免泄漏内部信息 */
    private String detail(Exception e) {
        if (!exposeDetail) {
            return null;
        }
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String message = root.getMessage();
        if (message == null || message.isBlank()) {
            message = root.getClass().getSimpleName();
        }
        return message.length() > DETAIL_MAX_LENGTH ? message.substring(0, DETAIL_MAX_LENGTH) : message;
    }

    private String formatUptime() {
        long seconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (days > 0) {
            return days + "d" + hours + "h" + minutes + "m";
        }
        if (hours > 0) {
            return hours + "h" + minutes + "m" + secs + "s";
        }
        if (minutes > 0) {
            return minutes + "m" + secs + "s";
        }
        return secs + "s";
    }
}
