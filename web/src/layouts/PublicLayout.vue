<script setup>
/**
 * 公开场景外壳（登录 / 引导两个页面共用）
 *
 * `meta.bare === true` 时**不渲染页头页脚**，内容区也不限宽 ——
 * 登录页与引导页用它做「全屏背景 + 表单垂直水平居中」。
 *
 * ⚠️ 与 OSC 的差异：删掉了 `LocaleSwitch`（i18n 已删，见清单 D23）；文案改为 PKB。
 */
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { APP_FULL_NAME, APP_NAME } from '@/constants/app'

const route = useRoute()

/** 无壳模式：不显示页头页脚、内容不限宽不限高 */
const bare = computed(() => route.meta?.bare === true)
</script>

<template>
  <div class="public-layout" :class="{ 'is-bare': bare }">
    <header v-if="!bare" class="public-header">
      <span class="public-brand">{{ APP_NAME }}</span>
    </header>

    <main class="public-main" :class="{ 'is-bare': bare }">
      <router-view />
    </main>

    <footer v-if="!bare" class="public-footer">{{ APP_FULL_NAME }} · V0.1</footer>
  </div>
</template>

<style scoped>
.public-layout {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

/* 无壳模式：铺满视口，让页面自己掌控全屏背景与居中 */
.public-layout.is-bare {
  min-height: 100vh;
}

.public-header {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 14px 16px;
  background: var(--bg-surface);
  border-bottom: 1px solid var(--border-color);
}

.public-brand {
  font-size: 16px;
  font-weight: 500;
  color: var(--brand-primary);
}

.public-main {
  flex: 1;
  width: 100%;
  max-width: 560px;
  margin: 0 auto;
  padding: 12px;
  padding-bottom: 40px;
}

/* 无壳模式下取消限宽与内边距，交给页面自己排 */
.public-main.is-bare {
  max-width: none;
  padding: 0;
}
</style>
