<script setup>
import { useRouter } from 'vue-router'
import { ROUTE_PATH } from '@/constants/app'

const router = useRouter()
</script>

<template>
  <div class="not-found">
    <p class="not-found__code">404</p>
    <p class="not-found__text">这个页面不存在，或者链接已经失效。</p>
    <div class="not-found__actions">
      <el-button type="primary" @click="router.replace(ROUTE_PATH.HOME)">回到首页</el-button>
      <el-button @click="router.replace(ROUTE_PATH.LOGIN)">去登录</el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  padding: 80px 16px;
  text-align: center;
}

.not-found__code {
  margin: 0;
  font-size: 48px;
  font-weight: 500;
  color: var(--brand-primary);
}

.not-found__text {
  margin: 8px 0 20px;
  color: var(--text-secondary);
}

.not-found__actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}
</style>
