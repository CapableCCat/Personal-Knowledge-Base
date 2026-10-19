<script setup>
/**
 * 标签管理页（P-007）—— 复用 `TagManager`（标签 = `tag.type=0`）。
 */
import TagManager from '@/components/TagManager.vue'
</script>

<template>
  <div class="tags-page">
    <TagManager :type="0" noun="标签" />
  </div>
</template>

<style scoped>
.tags-page {
  min-height: 100%;
}
</style>
