<script setup>
/**
 * 根组件：渲染当前路由对应的布局（布局由路由表决定），
 * 并给整个应用套上 Element Plus 的中文语言配置。
 *
 * `el-config-provider` 负责把 locale 下发给该组件树下的所有 EP 组件 ——
 * 不配的话内置文案是英文（OK / Cancel）。
 *
 * ⚠️ 与 OSC 的差异：**i18n 已删**（`stores/locale.js` + `LocaleSwitch.vue` 整删，见清单 D23）。
 *    自用单账号系统不需要中英切换，故直接写死中文 locale，不再有切换状态。
 */
import zhCn from 'element-plus/es/locale/lang/zh-cn'
</script>

<template>
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>
