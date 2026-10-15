<script setup>
/**
 * 标签管理（P-007）—— ⬜ **T8 实现，当前为占位**
 *
 * T8 要落地的形态（PRD P-007）：
 *   标签 = `tag.type=0`；与项目**共用一张表**，靠 `type` 区分
 *   字段：名称 / 颜色 / 排序 `sort`；一条笔记可关联多个标签（多选）
 *   删除标签只解除关联，**不删笔记**
 */
</script>

<template>
  <div class="tags">
    <el-empty description="标签管理由 T8 实现（P-007）" />
  </div>
</template>

<style scoped>
.tags {
  padding: var(--space-4);
}
</style>
