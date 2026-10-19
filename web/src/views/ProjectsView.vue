<script setup>
/**
 * 项目管理页（P-006）—— 复用 `TagManager`（项目 = `tag.type=1`）。
 *
 * <p>与标签管理页的唯一差别是 `type` 与文案；排序、删除、计数等交互完全共用，
 * 避免两页各维护一份实现后漂移。
 */
import TagManager from '@/components/TagManager.vue'
</script>

<template>
  <div class="projects-page">
    <TagManager :type="1" noun="项目" />
  </div>
</template>

<style scoped>
.projects-page {
  min-height: 100%;
}
</style>
