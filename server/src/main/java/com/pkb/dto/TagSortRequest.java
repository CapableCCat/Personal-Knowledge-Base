package com.pkb.dto;

import java.util.List;

/**
 * 重排同一类型下的标签 / 项目。
 *
 * <p>为什么用「整组 id 顺序」而不是「只传两个被交换的 sort」：
 * sort 是普通整数，两两交换在并发/重复点击下容易撞成同值；整组提交由服务端
 * 按顺序重写成 `0, 10, 20…`，结果**总是确定的**，前端也只需把当前可见顺序发上来。
 *
 * @param type 0=标签 / 1=项目（服务端会校验 ids 全部属于该类型）
 * @param ids  该类型下**当前全部** id，按目标顺序排列
 */
public record TagSortRequest(Integer type, List<Long> ids) {
}
