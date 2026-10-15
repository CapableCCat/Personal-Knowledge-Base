package com.pkb.controller.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * /health 响应体。
 *
 * @param app        应用名
 * @param profile    当前环境
 * @param version    应用版本
 * @param time       服务器时间
 * @param uptime     进程已运行时长
 * @param components 各下游组件状态（redis / mysql）
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HealthVO(
        String app,
        String profile,
        String version,
        String time,
        String uptime,
        Map<String, Component> components
) {

    /**
     * 单个组件状态。
     *
     * @param status    UP / DOWN
     * @param latencyMs 探测耗时（毫秒）
     * @param detail    失败原因（仅 dev 回显，公网环境为 null 且不序列化）
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Component(String status, long latencyMs, String detail) {

        public static Component up(long latencyMs) {
            return new Component("UP", latencyMs, null);
        }

        public static Component down(long latencyMs, String detail) {
            return new Component("DOWN", latencyMs, detail);
        }
    }
}
