<script setup>
/**
 * 笔记列表（P-002）—— ⬜ **T6 实现，当前为占位**
 *
 * T6 要落地的形态（PRD P-002）：
 *   展示：标题 / 摘要（content_text 截断，纯文本）/ 格式徽标 / 项目名 / 标签 / 状态 / 置顶 / 更新时间
 *   筛选：状态（全部 / 待整理 / 已归档）、项目（单选）、标签（多选）
 *   排序：`ORDER BY pinned DESC, updated_at DESC`（冻结口径）
 *   分页：默认每页 20；手机档为卡片流、无横向滚动
 */
</script>

<template>
  <div class="notes">
    <el-empty description="笔记列表由 T6 实现（P-002）" />
  </div>
</template>

<style scoped>
.notes {
  padding: var(--space-4);
}
</style>
