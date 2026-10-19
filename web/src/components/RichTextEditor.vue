<script setup>
/**
 * 富文本编辑器（P-004 的 `html` 格式笔记正文）
 *
 * 选型 @wangeditor-next/editor：原版 wangEditor 已停维护，社区 fork 仍在持续更新；
 * 中文文档与开箱即用的工具栏最省事。
 *
 * ⚠️ 与 OSC 的差异（T1 落地）：**图片上传改为可注入的 prop**。
 *    OSC 版本硬依赖 `api/announcement`（已随公告业务删除），会把构建打断。
 *    现在由调用方传入 `uploadImage` 函数（返回图片 URL 的 Promise）；
 *    不传则不显示图片上传入口 —— 这样组件不再耦合任何业务 API。
 *
 * <p>T7 起本组件真正接入了 `NoteForm`（`content_format=1` 的笔记）。
 * ⚠️ **图片上传仍归 T11**：目前谁也没传 `uploadImage`，工具栏里的图片入口是隐藏的。
 *
 * ⚠️ 拿到的 HTML 一律经 `sanitizeHtml` 清洗后再交给父组件（提交前的那一道）；
 *    渲染时还会再清一次，见 `utils/sanitizeHtml.js`。
 *    ⚠️ **md 格式笔记编译出的 HTML 同样要过 DOMPurify**（见清单 D6 / PRD P-004），
 *    那条链路由 `MarkdownEditor.vue` 的 `sanitize` 钩子负责（用的是 markdown 档）。
 */
import { onBeforeUnmount, ref, shallowRef, watch } from 'vue'
import '@wangeditor-next/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor-next/editor-for-vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '请输入笔记内容…' },
  height: { type: String, default: '340px' },
  /**
   * 可选：图片上传函数 `(file) => Promise<string>`（resolve 图片可访问 URL）。
   * 不传则不显示图片上传入口（T7 接上 MinIO 上传通道后再传）。
   */
  uploadImage: { type: Function, default: null }
})

const emit = defineEmits(['update:modelValue', 'normalize'])

/** 编辑器实例必须用 shallowRef：深层响应式会代理掉编辑器内部结构 */
const editorRef = shallowRef(null)
const valueHtml = ref(props.modelValue || '')

/**
 * 初始化「静默期」。
 *
 * <p>wangEditor 初始化时会把自己的内容按 Slate 重新序列化一遍（解析入库的 HTML → 再生成），
 * 结果字符串**可能与后端存的不完全一致**。如果直接把它当成用户输入，
 * 一打开 html 笔记就会立刻显示「有未保存的修改」，关闭时还会莫名弹确认框（T7 实测踩到）。
 *
 * <p>处理：编辑器创建后先静默 250ms，期间的变化不向上 emit；静默期结束时把编辑器
 * 认下来的 HTML 作为**新基线**交给父组件（`normalize` 事件），父组件据此重置脏检查快照。
 */
const SETTLE_MS = 250
let live = false
let settleTimer = null

/**
 * 「这个 HTML 算不算空」。
 *
 * <p>必须单独判一次：wangEditor 对空内容会给 `<p><br></p>`（Slate 的空段落），
 * 它是**非空字符串**。若直接把它当正文交回父组件，空表单会在
 * 「md → 富文本 → md」切换后变成 `<p><br></p>` 的 md 源码（T7 实测踩到）。
 * 只贴图（有 `<img>`）的笔记仍然算「有内容」（PRD P-004 允许）。
 */
function isEmptyHtml(html) {
  if (!html) return true
  if (/<img\b/i.test(html)) return false
  return html.replace(/<[^>]*>/g, '').replace(/&nbsp;/gi, ' ').trim().length === 0
}

/** 进入静默期：结束后把编辑器认下来的 HTML 作为新基线交给父组件 */
function armSettle() {
  clearTimeout(settleTimer)
  live = false
  settleTimer = setTimeout(() => {
    live = true
    const editor = editorRef.value
    if (!editor) return
    const raw = editor.getHtml()
    const next = isEmptyHtml(raw) ? '' : raw
    if (next !== props.modelValue) {
      emit('normalize', next)
    }
  }, SETTLE_MS)
}

watch(
  () => props.modelValue,
  (next) => {
    if ((next || '') !== valueHtml.value) {
      valueHtml.value = next || ''
      // 换了一条笔记（同一个组件实例复用）时同样要静默一次
      armSettle()
    }
  }
)

watch(valueHtml, (next) => {
  if (live) emit('update:modelValue', next || '')
})

/**
 * 工具栏**显式白名单**（不用默认全量工具栏）。
 *
 * <p>为什么必须收窄（T7 实测）：后端 `HtmlSanitizer` 是白名单清洗，`style` 只放行
 * `text-align / text-indent / color / background-color`，`class` / `id` 一律剥掉。
 * 默认工具栏里的「字号 / 字体 / 行高 / 缩进 / 波浪线 / 着重号 / 格式刷」产生的正是
 * 这些被剥掉的样式 —— 用户编辑时看到变化、**保存后再打开全没了**，
 * 属于 PRD NFR「绝不允许『以为存了其实没存』」明令禁止的体验。
 *
 * <p>所以这里只留「保存后仍然存活」的菜单：加粗 / 斜体 / 下划线 / 删除线 / 颜色 /
 * 列表 / 对齐 / 链接 / 表格 / 行内代码 / 代码块 / 分割线 / 撤销重做。
 * 图片上传归 T11（`uploadImage` 未注入时同样不出现），视频与全屏不需要。
 */
const TOOLBAR_KEYS = [
  'headerSelect',
  'blockquote',
  '|',
  'bold',
  'underline',
  'italic',
  'through',
  'color',
  'bgColor',
  'clearStyle',
  '|',
  'bulletedList',
  'numberedList',
  '|',
  'justifyLeft',
  'justifyCenter',
  'justifyRight',
  '|',
  'insertLink',
  'insertTable',
  'code',
  'codeBlock',
  'divider',
  '|',
  'undo',
  'redo'
]

const toolbarConfig = {
  toolbarKeys: TOOLBAR_KEYS,
  excludeKeys: ['group-video', 'insertVideo', 'uploadVideo', 'fullScreen']
}

const editorConfig = {
  placeholder: props.placeholder,
  MENU_CONF: {}
}

// 只有调用方提供了上传函数，才开放图片上传入口
if (typeof props.uploadImage === 'function') {
  editorConfig.MENU_CONF.uploadImage = {
    maxFileSize: 2 * 1024 * 1024,
    allowedFileTypes: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
    customUpload(file, insertFn) {
      props
        .uploadImage(file)
        .then((url) => insertFn(url, file.name || '图片', url))
        .catch(() => {})
    }
  }
} else {
  toolbarConfig.excludeKeys = [...toolbarConfig.excludeKeys, 'group-image', 'insertImage', 'uploadImage']
}

function handleCreated(editor) {
  editorRef.value = editor
  armSettle()
}

onBeforeUnmount(() => {
  clearTimeout(settleTimer)
  // 不销毁会在弹窗反复开关时堆积监听，官方要求显式 destroy
  editorRef.value?.destroy()
})
</script>

<template>
  <div class="rich-editor">
    <Toolbar class="rich-editor__toolbar" :editor="editorRef" :default-config="toolbarConfig" mode="default" />
    <Editor
      v-model="valueHtml"
      class="rich-editor__body"
      :style="{ height }"
      :default-config="editorConfig"
      mode="default"
      @on-created="handleCreated"
    />
  </div>
</template>

<style scoped>
.rich-editor {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: var(--brand-radius);
  overflow: hidden;
}

.rich-editor__toolbar {
  border-bottom: 1px solid #ebeef5;
}

.rich-editor__body {
  overflow-y: auto;
}

/* 工具栏下拉面板要压在 el-dialog（z-index 2000+）之上，否则会被弹窗盖住 */
.rich-editor :deep(.w-e-drop-panel),
.rich-editor :deep(.w-e-modal) {
  z-index: 3000;
}

.rich-editor :deep(.w-e-bar-item-menus-container) {
  z-index: 3000;
}
</style>
