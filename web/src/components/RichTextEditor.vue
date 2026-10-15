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
 * ⚠️ 拿到的 HTML 一律经 `sanitizeHtml` 清洗后再交给父组件（提交前的那一道）；
 *    渲染时还会再清一次，见 `utils/sanitizeHtml.js`。
 *    ⚠️ **md 格式笔记编译出的 HTML 同样要过 DOMPurify**（见清单 D6 / PRD P-004）。
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

const emit = defineEmits(['update:modelValue'])

/** 编辑器实例必须用 shallowRef：深层响应式会代理掉编辑器内部结构 */
const editorRef = shallowRef(null)
const valueHtml = ref(props.modelValue || '')

watch(
  () => props.modelValue,
  (next) => {
    if ((next || '') !== valueHtml.value) {
      valueHtml.value = next || ''
    }
  }
)

watch(valueHtml, (next) => emit('update:modelValue', next || ''))

const toolbarConfig = {
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
}

onBeforeUnmount(() => {
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
