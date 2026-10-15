<script setup>
/**
 * 笔记全屏页（P-003，手机端主用的详情页）—— ⬜ **T6 实现，当前为占位**
 *
 * T6 要落地的形态（PRD P-003，已冻结）：
 *   桌面（≥1024）：`el-drawer`（~720px）
 *   手机（<768） ：本页 `/note/:id`（独立全屏路由）
 *   ⚠️ **两处必须复用同一份表单组件**，不允许各写一套（否则字段与校验会漂）。
 *
 *   ⚠️ 验证「drawer 已关闭」要用 `!!el.offsetParent` —— el-drawer 关闭是 `v-show`（display:none），
 *      元素仍在 DOM 里，`!!document.querySelector('.el-drawer')` 永远为真（OSC 踩过）。
 */
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <div class="note-detail">
    <el-page-header :content="`笔记详情（id=${route.params.id}）`" @back="$router.back()" />
    <el-empty description="笔记详情由 T6 实现（P-003）" />
  </div>
</template>

<style scoped>
.note-detail {
  padding: var(--space-4);
}
</style>
