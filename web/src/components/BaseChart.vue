<script setup>
/**
 * ECharts 薄封装（T15 看板）
 *
 * 只做四件事：初始化 → 响应 option 变化 → 跟随容器尺寸 resize → 卸载时销毁。
 * 刻意不封装成"配置 DSL"：各图表 option 的差异太大，包一层反而更难改。
 */
import { onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import echarts from '@/utils/echarts'

const props = defineProps({
  option: { type: Object, required: true },
  height: { type: String, default: '300px' }
})

const container = ref(null)
/** 实例必须 shallowRef：深层响应式会代理掉 ECharts 内部结构 */
const chart = shallowRef(null)
let observer = null

function render() {
  if (chart.value) {
    // notMerge=true：数据换了以后旧的 series 不该残留
    chart.value.setOption(props.option, true)
  }
}

onMounted(() => {
  chart.value = echarts.init(container.value)
  render()
  // 侧边菜单收起 / 窗口缩放 / 窄屏切布局时容器宽度会变，图表得跟着 resize
  if (typeof ResizeObserver !== 'undefined') {
    observer = new ResizeObserver(() => chart.value && chart.value.resize())
    observer.observe(container.value)
  }
})

watch(() => props.option, render, { deep: true })

onBeforeUnmount(() => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
  chart.value?.dispose()
  chart.value = null
})
</script>

<template>
  <div ref="container" class="base-chart" :style="{ height }" />
</template>

<style scoped>
.base-chart {
  width: 100%;
}
</style>
