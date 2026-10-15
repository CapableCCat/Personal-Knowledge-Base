<script setup>
/**
 * 项目管理（P-006）—— ⬜ **T8 实现，当前为占位**
 *
 * T8 要落地的形态（PRD P-006，已冻结）：
 *   项目 = `tag.type=1`；字段：名称 / 颜色 / 排序 `sort`；列表显示每个项目下的笔记数
 *   ⚠️ **后端关联写入必须兜底**：一条笔记已有关联 `type=1` 标签时**替换而非新增**，
 *      否则数据层会出现「一条笔记多个项目」的脏数据（UI 单选挡不住直接调接口）。
 */
</script>

<template>
  <div class="projects">
    <el-empty description="项目管理由 T8 实现（P-006）" />
  </div>
</template>

<style scoped>
.projects {
  padding: var(--space-4);
}
</style>
