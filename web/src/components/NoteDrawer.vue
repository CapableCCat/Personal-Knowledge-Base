<script setup>
/**
 * 桌面端笔记抽屉（~720px）。
 *
 * <p>**为什么挂在 `AppShell` 而不是列表页里**：唤起抽屉的入口不止一个 ——
 * 笔记列表（T6）与首页（T9）都要用。挂在外壳上，任何内部页 `useNoteDrawer().open(id)` 即可。
 *
 * <p>⚠️ `el-drawer` 收起是 `v-show`（`display:none`），元素仍在 DOM 里 ——
 * 判断「是否已关闭」要用 `!!el.offsetParent`，不要用 `!!document.querySelector('.el-drawer')`。
 *
 * <p>`destroy-on-close`：关掉即销毁 `NoteForm`，脏状态自然清零，不会串到下一条笔记。
 */
import { computed, ref } from 'vue'
import { useNoteDrawer } from '@/composables/useNoteDrawer'
import NoteForm from './NoteForm.vue'

const { visible, noteId, bump, close } = useNoteDrawer()
const formRef = ref(null)

/**
 * ⚠️ **必须保留可见的关闭入口**：早先图省事用了 `:with-header="false"`，
 * 结果抽屉只能靠 Esc 或点遮罩关闭，用户找不到出口（T6 截图评审发现）。
 */
const title = computed(() => (noteId.value ? '笔记' : '新建笔记'))

/** 关闭前拦一道：有未保存修改则二次确认 */
async function beforeClose(done) {
  const ok = await formRef.value?.confirmDiscard?.()
  if (ok === false) return
  done()
}

function onSaved() {
  // 保存后不关抽屉（方便继续改），只让列表刷新
  bump()
}

function onDeleted() {
  bump()
  close()
}
</script>

<template>
  <el-drawer
    v-model="visible"
    :title="title"
    size="720px"
    destroy-on-close
    :before-close="beforeClose"
    class="note-drawer"
  >
    <NoteForm ref="formRef" :note-id="noteId" @saved="onSaved" @deleted="onDeleted" />
  </el-drawer>
</template>

<style scoped>
.note-drawer :deep(.el-drawer__body) {
  padding: 16px 20px 24px;
}
</style>
