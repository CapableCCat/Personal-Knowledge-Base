<script setup>
/**
 * 笔记全屏页（P-003，手机端主用的详情页）。
 *
 * <p>与桌面抽屉**复用同一份 `NoteForm`** —— 这是 PRD D-7 的硬要求，
 * 否则两处字段与校验一定会漂。
 *
 * <p>路由 `/note/new` 是**新建态**（手机端没有抽屉，用整页来建）。
 *
 * <p>未保存离开：用 `onBeforeRouteLeave` 拦一道（与抽屉的 `before-close` 等价）。
 */
import { computed, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import NoteForm from '@/components/NoteForm.vue'
import { useNoteDrawer } from '@/composables/useNoteDrawer'

const route = useRoute()
const router = useRouter()
const { bump } = useNoteDrawer()
const formRef = ref(null)

const rawId = computed(() => route.params.id)
const isNew = computed(() => rawId.value === 'new')
// ⚠️ 不要跟应用页头（route.meta.title = 「笔记详情」）用同一个词，否则一行里出现两次「笔记详情」。
const heading = computed(() => (isNew.value ? '新建笔记' : '编辑笔记'))

/** 保存后：新建态要换成真实 id 的路由，否则刷新会回到空白新建页 */
function onSaved(note) {
  bump()
  if (note?.id && isNew.value) {
    router.replace(`/note/${note.id}`)
  }
}

function onDeleted() {
  bump()
  router.replace('/notes')
}

async function goBack() {
  router.back()
}

/** 未保存离开拦截 */
onBeforeRouteLeave(async () => {
  if (!formRef.value?.isDirty) return true
  try {
    await ElMessageBox.confirm('有未保存的修改，确定要离开吗？', '提示', {
      type: 'warning',
      confirmButtonText: '离开',
      cancelButtonText: '继续编辑'
    })
    return true
  } catch {
    return false
  }
})
</script>

<template>
  <div class="note-detail">
    <el-page-header class="note-detail__head" :content="heading" @back="goBack" />
    <NoteForm
      ref="formRef"
      :note-id="isNew ? 'new' : rawId"
      @saved="onSaved"
      @deleted="onDeleted"
    />
  </div>
</template>

<style scoped>
.note-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px 14px 20px;
}

.note-detail__head {
  flex: none;
}
</style>
