<script setup>
/**
 * 笔记表单 —— **全站唯一的一份**（PRD D-7：桌面抽屉与手机全屏页必须复用它）。
 *
 * <p>设计要点：
 * - **自带取数**：父组件只告诉它「哪个 id」，它自己拉详情（父组件不必关心接口）
 * - **新建态**：`noteId` 为 null / 'new' → 新建；**保存成功后把内部 id 切到新值**，
 *   否则再点一次保存会重复创建
 * - **未保存拦截**：对外暴露 `isDirty`，由抽屉的 `before-close` 与全屏页的路由守卫做二次确认
 * - **格式只读**：`content_format` 创建后不可切换（铁律①）。**只有新建态**给格式选择，
 *   保存后只剩只读徽标（T7）
 * - **编辑器按格式分流**（T7）：`0=md` → `MarkdownEditor`；`1=html` → `RichTextEditor`。
 *   ⚠️ 两个编辑器都**懒加载**（defineAsyncComponent）—— 它们加起来是数 MB，
 *   首页 / 列表页不该为「打开一条笔记」付这个首屏成本。
 * - **XSS**：html 提交前过 `sanitizeHtml`，md 渲染产物在编辑器内部过
 *   `sanitizeMarkdownHtml`（见 `MarkdownEditor.vue` 与 `utils/sanitizeHtml.js`）
 * - **项目 / 标签**（T8）：项目**单选**（`tag.type=1`，可清空）、标签**多选**（`tag.type=0`）；
 *   保存时随主请求一起提交（后端在同一事务里做「全量替换」），不做两次请求
 */
import { computed, defineAsyncComponent, nextTick, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  archiveNote,
  createNote,
  deleteNote,
  getNote,
  pinNote,
  unarchiveNote,
  updateNote
} from '@/api/note'
import { listTags } from '@/api/tag'
import { useLayoutMode } from '@/composables/useLayoutMode'
import { sanitizeHtml } from '@/utils/sanitizeHtml'

const MarkdownEditor = defineAsyncComponent(() => import('@/components/MarkdownEditor.vue'))
const RichTextEditor = defineAsyncComponent(() => import('@/components/RichTextEditor.vue'))

const props = defineProps({
  /** 笔记 id；null / 'new' 表示新建 */
  noteId: { type: [String, Number], default: null }
})

const emit = defineEmits(['saved', 'deleted'])

const layoutMode = useLayoutMode()

const currentId = ref(null)
const loading = ref(false)
const saving = ref(false)
const removing = ref(false)

/** 编辑器高度：抽屉里给固定值；手机全屏页给 vh，避免把保存按钮挤出屏幕 */
const editorHeight = computed(() => (layoutMode.value === 'mobile' ? '46vh' : '380px'))

/** 项目选项（type=1）与标签选项（type=0）—— 一次请求拿全，靠 type 分流 */
const projects = ref([])
const tagOptions = ref([])

function emptyForm() {
  return { title: '', content: '', contentFormat: 0, pinned: 0, status: 0, projectId: null, tagIds: [] }
}

const form = ref(emptyForm())
const snapshot = ref(serialize(form.value))

const isNew = computed(() => currentId.value === null)
const formatLabel = computed(() => (form.value.contentFormat === 1 ? 'HTML' : 'Markdown'))
const statusLabel = computed(() => (form.value.status === 1 ? '已归档' : '待整理'))
const isDirty = computed(() => serialize(form.value) !== snapshot.value)

/** id 列表排序：脏检查要稳定，选择顺序不该被当成「改了」 */
function sortIds(ids) {
  return [...(ids || [])].filter((id) => id !== null && id !== undefined).sort((a, b) => a - b)
}

/** 只序列化「可编辑且参与脏检查」的字段（顺序固定，保证比较稳定） */
function serialize(f) {
  return JSON.stringify({
    title: f.title,
    content: f.content,
    contentFormat: f.contentFormat,
    pinned: f.pinned,
    status: f.status,
    projectId: f.projectId ?? null,
    tagIds: sortIds(f.tagIds)
  })
}

/** 拉取项目 / 标签选项（管理页改完回来会重新挂载表单，所以每次打开都取一次最新） */
async function loadOptions() {
  try {
    const all = await listTags()
    projects.value = (all || []).filter((tag) => tag.type === 1)
    tagOptions.value = (all || []).filter((tag) => tag.type === 0)
  } catch {
    // 拦截器已提示；选项拿不到只是不能选，不影响正文编辑
  }
}

onMounted(loadOptions)

/**
 * 未保存修改的确认框。
 * @returns {Promise<boolean>} true = 可以丢弃修改继续
 */
async function confirmDiscard() {
  if (!isDirty.value) return true
  try {
    await ElMessageBox.confirm('有未保存的修改，确定要丢弃吗？', '提示', {
      type: 'warning',
      confirmButtonText: '丢弃修改',
      cancelButtonText: '继续编辑'
    })
    return true
  } catch {
    return false
  }
}

async function load() {
  const raw = props.noteId
  const isCreate = raw === null || raw === undefined || raw === '' || raw === 'new'

  if (isCreate) {
    currentId.value = null
    form.value = emptyForm()
    snapshot.value = serialize(form.value)
    return
  }

  loading.value = true
  try {
    const note = await getNote(raw)
    currentId.value = note.id
    form.value = {
      title: note.title || '',
      content: note.content || '',
      contentFormat: note.contentFormat ?? 0,
      pinned: note.pinned ?? 0,
      status: note.status ?? 0,
      projectId: note.projectId ?? null,
      tagIds: sortIds((note.tags || []).map((tag) => tag.id))
    }
    snapshot.value = serialize(form.value)
  } catch {
    // 提示已由 axios 拦截器统一处理；这里把 id 复位，避免表单停留在半加载状态
    currentId.value = null
  } finally {
    loading.value = false
  }
}

watch(() => props.noteId, load, { immediate: true })

/**
 * 新建态切换格式（只有新建态有这个入口，见模板）。
 *
 * <p>md 与 html 是两种**互不兼容的源码形态**：把 `**加粗**` 直接当 html 存进去，
 * 详情页会原样显示成星号文本。所以切换格式必须清空正文，且**正文非空时先确认**。
 *
 * <p>⚠️ 这里刻意**不用 v-model**，而是 `:model-value` + `@update:model-value`：
 * v-model 会在 `@change` 之前就把值改掉，于是「确认框还没选、编辑器已经换过去了」——
 * 结果 md 正文被当成 html 喂给富文本编辑器（T7 实测踩到）。单向绑定下，
 * 用户点「取消」时格式根本没变，编辑器也不会换。
 */
async function onFormatChange(next) {
  if (next === form.value.contentFormat) {
    return
  }
  if (!form.value.content.trim()) {
    form.value.contentFormat = next
    return
  }
  try {
    await ElMessageBox.confirm(
      '切换格式会清空当前正文（两种格式的源码不兼容），确定吗？',
      '切换格式',
      {
        type: 'warning',
        confirmButtonText: '清空并切换',
        cancelButtonText: '取消'
      }
    )
  } catch {
    return
  }
  form.value.content = ''
  form.value.contentFormat = next
}

/**
 * 编辑器初始化时的「重新序列化」不算用户编辑 —— 接受它作为新基线。
 *
 * <p>背景：wangEditor 会把入库的 HTML 重新序列化一遍，字符串可能与库里存的不完全一致；
 * 不处理的话一打开 html 笔记就显示「有未保存的修改」（T7 实测踩到）。
 * 具体静默期逻辑见 `RichTextEditor.vue` 的 `armSettle`。
 */
function onContentNormalized(value) {
  if (value === form.value.content) return
  form.value.content = value
  snapshot.value = serialize(form.value)
}

/** 保存（新建或更新） */
async function save() {
  if (!form.value.title.trim() && !form.value.content.trim()) {
    ElMessage.warning('请填写标题或正文')
    return
  }
  saving.value = true
  try {
    // html 提交前先过一道 DOMPurify（渲染前那道在 RichTextEditor / sanitizeHtml 里）；
    // md 是源码，**不能清洗**（会破坏 Markdown 语法），它的清洗在渲染产物上做。
    const isHtml = form.value.contentFormat === 1
    const content = isHtml ? sanitizeHtml(form.value.content) : form.value.content

    // 项目 / 标签随主请求一起提交（后端同一事务里「全量替换」）——
    // 刻意不拆成两次请求：拆了就会出现「正文存了、标签没存」的半边成功（PRD NFR）。
    const associations = {
      projectId: form.value.projectId ?? null,
      tagIds: sortIds(form.value.tagIds)
    }

    let saved
    if (isNew.value) {
      saved = await createNote({
        title: form.value.title,
        content,
        contentFormat: form.value.contentFormat,
        associations
      })
      // ⚠️ 关键：切到已创建 id，否则再点保存会重复创建
      currentId.value = saved.id
      ElMessage.success('已保存到待整理')
    } else {
      saved = await updateNote(currentId.value, {
        title: form.value.title,
        content,
        // 传相同格式：后端允许；传不同会被 40000 拒绝
        contentFormat: form.value.contentFormat,
        pinned: form.value.pinned,
        associations
      })
      ElMessage.success('已保存')
    }
    form.value = {
      title: saved.title || '',
      // 用**后端返回的内容**回填：html 已在服务端过了 jsoup（可能与本地的略有差异），
      // 不同步回来的话脏检查会立刻变红
      content: saved.content || '',
      contentFormat: saved.contentFormat ?? form.value.contentFormat,
      pinned: saved.pinned ?? 0,
      status: saved.status ?? 0,
      projectId: saved.projectId ?? null,
      tagIds: sortIds((saved.tags || []).map((tag) => tag.id))
    }
    // 等编辑器把新值同步进 DOM 再取快照，避免编辑器自身的规范化触发一次假「未保存」
    await nextTick()
    snapshot.value = serialize(form.value)
    emit('saved', saved)
  } catch {
    // 失败提示由拦截器处理；保持 dirty 状态，让用户能重试
  } finally {
    saving.value = false
  }
}

/** 归档 / 取回 */
async function toggleArchive() {
  if (isNew.value) {
    ElMessage.warning('请先保存，再归档')
    return
  }
  try {
    const next = form.value.status === 1
      ? await unarchiveNote(currentId.value)
      : await archiveNote(currentId.value)
    form.value.status = next.status
    form.value.pinned = next.pinned ?? form.value.pinned
    snapshot.value = serialize(form.value)
    ElMessage.success(next.status === 1 ? '已归档' : '已取回待整理')
    emit('saved', next)
  } catch {
    /* 拦截器已提示 */
  }
}

/** 置顶 / 取消置顶 */
async function togglePin() {
  if (isNew.value) {
    ElMessage.warning('请先保存，再置顶')
    return
  }
  try {
    const next = await pinNote(currentId.value, form.value.pinned === 1 ? 0 : 1)
    form.value.pinned = next.pinned ?? 0
    snapshot.value = serialize(form.value)
    ElMessage.success(form.value.pinned === 1 ? '已置顶' : '已取消置顶')
    emit('saved', next)
  } catch {
    /* 拦截器已提示 */
  }
}

/** 删除（二次确认） */
async function remove() {
  if (isNew.value) {
    ElMessage.warning('尚未保存，无需删除')
    return
  }
  try {
    await ElMessageBox.confirm('删除后不可恢复（V0.1 没有回收站），确定删除吗？', '删除笔记', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  removing.value = true
  try {
    await deleteNote(currentId.value)
    ElMessage.success('已删除')
    emit('deleted', currentId.value)
  } catch {
    /* 拦截器已提示 */
  } finally {
    removing.value = false
  }
}

// 暴露给父组件：抽屉的 before-close / 全屏页的路由守卫要用
defineExpose({ isDirty, save, confirmDiscard })
</script>

<template>
  <div v-loading="loading" class="note-form">
    <!-- 元信息条：格式（新建态可选 / 已建只读徽标） + 状态 + 置顶开关 -->
    <div class="note-form__meta">
      <!-- ⚠️ 格式选择**只在新建态**出现：content_format 建后不可切换（铁律①，后端也会拦） -->
      <el-radio-group
        v-if="isNew"
        :model-value="form.contentFormat"
        size="small"
        class="note-form__format"
        @update:model-value="onFormatChange"
      >
        <el-radio-button :value="0">Markdown</el-radio-button>
        <el-radio-button :value="1">富文本</el-radio-button>
      </el-radio-group>
      <el-tag v-else size="small" type="info" effect="plain">{{ formatLabel }}</el-tag>

      <el-tag size="small" :type="form.status === 1 ? 'success' : 'warning'" effect="light">
        {{ statusLabel }}
      </el-tag>
      <span v-if="isNew" class="note-form__hint">新建后默认进入「待整理」</span>

      <div class="note-form__meta-right">
        <el-tooltip content="置顶后会在列表最前" placement="top">
          <el-button
            size="small"
            :type="form.pinned === 1 ? 'primary' : 'default'"
            :disabled="isNew"
            @click="togglePin"
          >
            {{ form.pinned === 1 ? '已置顶' : '置顶' }}
          </el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- 标题 -->
    <el-input
      v-model="form.title"
      class="note-form__title"
      maxlength="255"
      show-word-limit
      placeholder="标题（可留空）"
    />

    <!-- 项目（单选，可清空） + 标签（多选）—— T8。项目是 tag.type=1，标签是 tag.type=0 -->
    <div class="note-form__taxonomy">
      <div class="note-form__field">
        <span class="note-form__field-label">项目</span>
        <el-select
          v-model="form.projectId"
          class="note-form__project"
          size="small"
          clearable
          filterable
          placeholder="不挂项目"
        >
          <el-option v-for="item in projects" :key="item.id" :label="item.name" :value="item.id">
            <span class="note-form__option-dot" :style="{ background: item.color || 'var(--border-color)' }" />
            {{ item.name }}
          </el-option>
        </el-select>
      </div>

      <div class="note-form__field note-form__field--grow">
        <span class="note-form__field-label">标签</span>
        <el-select
          v-model="form.tagIds"
          class="note-form__tags"
          size="small"
          multiple
          filterable
          clearable
          collapse-tags
          collapse-tags-tooltip
          :max-collapse-tags="3"
          placeholder="可多选；超过 50 个可直接搜索"
        >
          <el-option v-for="item in tagOptions" :key="item.id" :label="item.name" :value="item.id">
            <span class="note-form__option-dot" :style="{ background: item.color || 'var(--border-color)' }" />
            {{ item.name }}
          </el-option>
        </el-select>
      </div>
    </div>

    <!-- 正文：按 contentFormat 分流（T7）。两个编辑器都是懒加载，首屏不背它们的体积。 -->
    <MarkdownEditor
      v-if="form.contentFormat !== 1"
      v-model="form.content"
      :height="editorHeight"
      editor-id="pkb-note-md-editor"
    />
    <RichTextEditor
      v-else
      v-model="form.content"
      :height="editorHeight"
      placeholder="正文（富文本）—— 支持加粗、列表、表格等"
      @normalize="onContentNormalized"
    />

    <!-- 操作区 -->
    <div class="note-form__actions">
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      <el-button :disabled="isNew" @click="toggleArchive">
        {{ form.status === 1 ? '取回待整理' : '归档' }}
      </el-button>
      <el-button type="danger" plain :loading="removing" :disabled="isNew" @click="remove">
        删除
      </el-button>
      <span class="note-form__dirty">{{ isDirty ? '有未保存的修改' : '' }}</span>
    </div>
  </div>
</template>

<style scoped>
.note-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}

.note-form__meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.note-form__meta-right {
  margin-left: auto;
}

.note-form__hint {
  font-size: 12px;
  color: var(--text-secondary);
}

.note-form__title :deep(.el-input__inner) {
  font-size: 16px;
  font-weight: 500;
}

.note-form__format {
  flex-shrink: 0;
}

/* 项目 / 标签一排：桌面并排，窄屏自动换行（标签那一栏吃掉剩余宽度） */
.note-form__taxonomy {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.note-form__field {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.note-form__field--grow {
  flex: 1 1 240px;
}

.note-form__field-label {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.note-form__project {
  width: 160px;
}

.note-form__tags {
  flex: 1;
  min-width: 0;
}

.note-form__option-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  margin-right: 6px;
  border-radius: 50%;
  vertical-align: middle;
}

.note-form__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.note-form__dirty {
  margin-left: auto;
  font-size: 12px;
  color: var(--el-color-warning);
}

@media (max-width: 767px) {
  /* 375 宽下「格式选择 + 状态 + 置顶」放不下一行：换行，别撑出横向滚动 */
  .note-form__meta {
    flex-wrap: wrap;
  }

  .note-form__hint {
    display: none;
  }
}
</style>
