<script setup>
/**
 * 标签 / 项目**管理页的共用组件**（P-006 / P-007）。
 *
 * <p>为什么两页共用一个组件：它们除了 `type` 与文案，交互一模一样
 * （新建 / 改名 / 改色 / 上移下移 / 查看笔记 / 删除）。各写一份必然漂移。
 *
 * <p>几个刻意的设计：
 * - **不用 `el-table`**：表格在 375 宽会横向滚动，而管理页也要能在手机上用 →
 *   改成「一行一条、窄屏自动换行」的列表。
 * - **排序用上移 / 下移**，不用拖拽：不引第三方拖拽库，且「整组 id 一起提交」让
 *   服务端把 sort 重写成 0,10,20…，结果确定、可重复。
 * - **删除确认里写明「笔记不会删除」**：这是 PRD P-006/P-007 的硬要求，
 *   用户最怕的是「删项目把笔记一起删了」。
 */
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createTag, deleteTag, listTags, sortTags, updateTag } from '@/api/tag'

const props = defineProps({
  /** 0=标签 1=项目 */
  type: { type: Number, required: true },
  /** 文案用的名词（「标签」/「项目」） */
  noun: { type: String, default: '标签' }
})

const router = useRouter()

const items = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const form = ref({ name: '', color: '', sort: null })

const dialogTitle = computed(() => (editingId.value ? `编辑${props.noun}` : `新建${props.noun}`))
const totalNotes = computed(() => items.value.reduce((sum, item) => sum + (item.noteCount || 0), 0))

async function load() {
  loading.value = true
  try {
    items.value = (await listTags(props.type)) || []
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

onMounted(load)

function openCreate() {
  editingId.value = null
  form.value = { name: '', color: '', sort: null }
  dialogVisible.value = true
}

function openEdit(item) {
  editingId.value = item.id
  form.value = { name: item.name, color: item.color || '', sort: item.sort }
  dialogVisible.value = true
}

async function submit() {
  const name = (form.value.name || '').trim()
  if (!name) {
    ElMessage.warning(`请填写${props.noun}名称`)
    return
  }
  saving.value = true
  try {
    // color 传空串 = 清空颜色（后端把它归一成 null）；sort 不填 = 由后端排到末尾 / 保持不变
    const payload = { name, color: form.value.color || '' }
    if (form.value.sort !== null && form.value.sort !== undefined) {
      payload.sort = form.value.sort
    }
    if (editingId.value) {
      await updateTag(editingId.value, payload)
    } else {
      await createTag({ ...payload, type: props.type })
    }
    ElMessage.success('已保存')
    dialogVisible.value = false
    await load()
  } catch {
    /* 拦截器已提示 */
  } finally {
    saving.value = false
  }
}

/** 上移 / 下移：把整组顺序提给后端，服务端重写成 0,10,20… */
async function move(item, direction) {
  const ids = items.value.map((it) => it.id)
  const index = ids.indexOf(item.id)
  const target = index + direction
  if (target < 0 || target >= ids.length) {
    return
  }
  const next = [...ids]
  ;[next[index], next[target]] = [next[target], next[index]]
  try {
    await sortTags({ type: props.type, ids: next })
    await load()
  } catch {
    /* 拦截器已提示 */
  }
}

/** 点进该标签 / 项目下的笔记列表（笔记页按 query 初始化筛选） */
function viewNotes(item) {
  router.push({
    path: '/notes',
    query: props.type === 1 ? { projectId: item.id } : { tagId: item.id }
  })
}

async function remove(item) {
  const count = item.noteCount || 0
  try {
    await ElMessageBox.confirm(
      `删除${props.noun}「${item.name}」？${count} 条笔记会解除关联，笔记本身不会被删除。`,
      `删除${props.noun}`,
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      }
    )
  } catch {
    return
  }
  try {
    const affected = await deleteTag(item.id)
    ElMessage.success(`已删除，解除了 ${affected ?? count} 条笔记的关联`)
    await load()
  } catch {
    /* 拦截器已提示 */
  }
}
</script>

<template>
  <div class="tag-manager">
    <div class="tag-manager__head">
      <div class="tag-manager__summary">
        共 <strong>{{ items.length }}</strong> 个{{ noun }}
        <span class="tag-manager__sub">关联 {{ totalNotes }} 条笔记（含待整理）</span>
      </div>
      <el-button type="primary" size="small" @click="openCreate">新建{{ noun }}</el-button>
    </div>

    <div v-loading="loading" class="tag-manager__list">
      <el-empty v-if="!loading && items.length === 0" :description="`还没有${noun}，点右上角新建`" />

      <div v-for="(item, index) in items" :key="item.id" class="tag-row">
        <span class="tag-row__dot" :style="{ background: item.color || 'var(--border-color)' }" />
        <span class="tag-row__name" :title="item.name">{{ item.name }}</span>
        <el-tag size="small" type="info" effect="plain">{{ item.noteCount || 0 }} 条</el-tag>

        <div class="tag-row__actions">
          <el-button link size="small" :disabled="index === 0" @click="move(item, -1)">上移</el-button>
          <el-button link size="small" :disabled="index === items.length - 1" @click="move(item, 1)">
            下移
          </el-button>
          <el-button link size="small" @click="viewNotes(item)">查看笔记</el-button>
          <el-button link size="small" @click="openEdit(item)">编辑</el-button>
          <el-button link size="small" type="danger" @click="remove(item)">删除</el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="420px" append-to-body>
      <el-form label-width="64px" @submit.prevent="submit">
        <el-form-item :label="noun + '名称'" required>
          <el-input v-model="form.name" maxlength="64" show-word-limit placeholder="必填，同类型内不重名" />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="form.color" />
          <span class="tag-manager__hint">可不设</span>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :step="10" controls-position="right" />
          <span class="tag-manager__hint">数值小的在前；不填则排在末尾</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.tag-manager {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
}

.tag-manager__head {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-manager__summary {
  font-size: 13px;
  color: var(--text-regular);
}

.tag-manager__sub {
  margin-left: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

.tag-manager__list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 120px;
}

.tag-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 12px;
  background: var(--bg-surface);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-surface);
}

.tag-row__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.tag-row__name {
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.tag-row__actions {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-left: auto;
}

.tag-manager__hint {
  margin-left: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

@media (max-width: 767px) {
  .tag-manager {
    padding: 12px;
  }

  .tag-row__actions {
    margin-left: 0;
    flex-wrap: wrap;
  }
}
</style>
