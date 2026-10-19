<script setup>
/**
 * Markdown 编辑器（P-004 的 `md` 格式笔记正文）—— **全站唯一的一份**。
 *
 * <p>选型 `md-editor-v3`（PRD 第十二章 #1，2026-10-17 拍板）：Vue3 原生组件，底层是
 * markdown-it + CodeMirror 6，表格 / 代码块 / 列表开箱可用；关键是它提供
 * `sanitize(html) => html` 钩子（渲染后、插入 DOM 前），正好用来挂 DOMPurify。
 *
 * <p><b>三条必须自己兜住的事（否则会静默降级）：</b>
 * <ol>
 *   <li>**CDN 本地化**：md-editor-v3 默认把 highlight.js / prettier / screenfull /
 *       katex / mermaid 当 CDN 资源懒加载（`https://unpkg.com/...`）。本项目单机自用、
 *       可能完全离线 —— 不本地化就会出现「代码块永不上色 + 控制台一堆网络错误」。
 *       做法：注入本地 `highlight.js` 实例（给了 `instance` 后它不再插 CDN 的 `<link>`），
 *       其余会走 CDN 的能力（prettier / 全屏 / 公式 / 图表）全部关掉、工具栏也摘掉。</li>
 *   <li>**md 产物必须过 DOMPurify**（PRD P-004）：走 `:sanitize="sanitizeMarkdownHtml"`。
 *       ⚠️ 注意用的是 **markdown 档**，不是富文本档 —— 富文本档禁 `class`，会把
 *       `hljs-*`（高亮色）与 `md-editor-*`（代码块外壳）一起剥掉。</li>
 *   <li>**原始 HTML 默认转义**：markdown-it 的 `html` 保持默认关闭，md 里写的
 *       `<script>` / `<div>` 只会当文本显示（D46）。需要富文本请用 html 格式笔记。</li>
 * </ol>
 */
import { config, MdEditor } from 'md-editor-v3'
import hljs from 'highlight.js/lib/common'
import 'highlight.js/styles/atom-one-light.css'
import 'md-editor-v3/lib/style.css'
import { sanitizeMarkdownHtml } from '@/utils/sanitizeHtml'

/**
 * 全局一次性配置：把代码高亮换成**本地** highlight.js 实例。
 *
 * <p>为什么在模块顶层调用：`config` 是全局单例配置，组件被多次挂载也只该执行一次；
 * ESM 模块只求值一次，正好满足。`deepMerge` 对 `instance` 这类键是直接赋值（不深拷贝），
 * 所以 hljs 实例不会被代理破坏。
 */
config({ editorExtensions: { highlight: { instance: hljs } } })

defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '正文支持 Markdown（表格、代码块、列表…）' },
  /** 编辑器整体高度（抽屉里给固定值，手机上给 vh） */
  height: { type: String, default: '360px' },
  /** 唯一 id：同一页面挂多份时避免 DOM id 冲突 */
  editorId: { type: String, default: 'pkb-md-editor' }
})

const emit = defineEmits(['update:modelValue'])

/**
 * 明确排除的工具栏项（逐个都有理由，别凭感觉加回来）：
 * - `image`           图片上传归 T11（附件），本轮不给入口，避免存进不可用的地址
 * - `mermaid`/`katex` 会从 CDN 拉资源（离线必坏）
 * - `prettier`        格式化按钮，CDN 资源
 * - `pageFullscreen`/`fullscreen` 依赖 screenfull（CDN 资源）
 * - `htmlPreview`     会把 md 里的原始 HTML 渲染出来 —— 与「默认转义」策略冲突，且是 XSS 面
 * - `catalog`         V0.1 不需要目录
 * - `github`          指向作者仓库，无意义
 * - `save`            表单自己有「保存」按钮，避免两个「保存」语义（且它只写 localStorage 草稿）
 */
const TOOLBARS_EXCLUDE = [
  'image',
  'mermaid',
  'katex',
  'prettier',
  'pageFullscreen',
  'fullscreen',
  'htmlPreview',
  'catalog',
  'github',
  'save'
]
</script>

<template>
  <MdEditor
    :id="editorId"
    :model-value="modelValue"
    :placeholder="placeholder"
    :style="{ height }"
    theme="light"
    language="zh-CN"
    :sanitize="sanitizeMarkdownHtml"
    :toolbars-exclude="TOOLBARS_EXCLUDE"
    :footers="['markdownTotal']"
    :preview="false"
    :no-upload-img="true"
    :no-prettier="true"
    :no-mermaid="true"
    :no-katex="true"
    :no-echarts="true"
    @update:model-value="(value) => emit('update:modelValue', value)"
  />
</template>

<style scoped>
/* 让编辑器在抽屉 / 全屏页里贴住容器宽度即可，外观交给 md-editor 自带样式 */
:deep(.md-editor) {
  width: 100%;
}

@media (max-width: 767px) {
  /*
   * 375 宽下工具栏默认是「单行 + 横向滚动」，后面的预览 / 撤销 / 重做被藏到屏幕外，
   * 手机上又没有可见滚动条 → 用户根本发现不了（T7 实测：scrollWidth 659 / 容器 345）。
   * 改成换行展示后工具栏 3 行、所有按钮可达，整页仍无横向滚动。
   */
  :deep(.md-editor-toolbar-wrapper) {
    overflow-x: visible;
  }

  :deep(.md-editor-toolbar),
  :deep(.md-editor-toolbar-left) {
    flex-wrap: wrap;
    height: auto;
  }
}
</style>
