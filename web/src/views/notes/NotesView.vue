<script setup>
/**
 * 笔记列表（P-002）。
 *
 * <p>设计取舍：
 * - **统一卡片流**（不区分桌面表格 / 手机卡片）：少一套实现，且天然不会横向滚动
 * - **不传正文**：列表接口只给 `excerpt`，点开才拉全文（PRD P-002）
 * - **打开方式按档位分流**（PRD D-7）：桌面 → 抽屉；手机 → 全屏路由 `/note/:id`
 * - **创建**：桌面 → 抽屉（新建态）；手机 → `/note/new`
 *
 * <p>刷新时机：自己操作后 `load()`；抽屉里改了数据后靠 `useNoteDrawer().version` 触发。
 */
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { StarFilled } from '@element-plus/icons-vue'
import { archiveNote, deleteNote, pageNotes, pinNote, unarchiveNote } from '@/api/note'
import { useLayoutMode } from '@/composables/useLayoutMode'
import { useNoteDrawer } from '@/composables/useNoteDrawer'

const router = useRouter()
const layoutMode = useLayoutMode()
const { open: openDrawer, version } = useNoteDrawer()

/**
 * 状态筛选。
 * ⚠️ 用字符串哨兵 `'all'` 而不是 `null` —— `el-radio-group` 拿 `modelValue === value` 比较，
 * 而 `null` 在 EP 内部会被当成「未选中」，导致默认进页面时**三个按钮都没高亮**
 * （T6 放大截图才发现：功能能点，但看不出当前筛选）。
 */
const STATUS_ALL = 'all'
const status = ref(STATUS_ALL)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const items = ref([])
const loading = ref(false)

/** 提交给后端的 status：'all' → null（后端约定 null = 全部） */
const statusParam = computed(() => (status.value === STATUS_ALL ? null : status.value))

async function load() {
  loading.value = true
  try {
    const data = await pageNotes({ page: page.value, size: size.value, status: statusParam.value })
    items.value = data.records || []
    total.value = data.total || 0
  } catch {
    // 拦截器已提示；保持旧列表，避免闪成空白
  } finally {
    loading.value = false
  }
}

onMounted(load)

// 筛选变化 → 回到第 1 页（若本来就在第 1 页则直接重载）
watch(status, () => {
  if (page.value !== 1) {
    page.value = 1
  } else {
    load()
  }
})
watch(page, load)
// 抽屉里保存 / 删除后刷新
watch(version, load)

/** 打开某条笔记 */
function openNote(note) {
  if (layoutMode.value === 'mobile') {
    router.push(`/note/${note.id}`)
  } else {
    openDrawer(note.id)
  }
}

/** 新建 */
function createNew() {
  if (layoutMode.value === 'mobile') {
    router.push('/note/new')
  } else {
    openDrawer(null)
  }
}

/** 置顶 / 取消置顶（行内操作，不打开详情） */
async function onTogglePin(note) {
  try {
    await pinNote(note.id, note.pinned === 1 ? 0 : 1)
    ElMessage.success(note.pinned === 1 ? '已取消置顶' : '已置顶')
    await load()
  } catch {
    /* 拦截器已提示 */
  }
}

/** 归档 / 取回 */
async function onToggleArchive(note) {
  try {
    if (note.status === 1) {
      await unarchiveNote(note.id)
      ElMessage.success('已取回待整理')
    } else {
      await archiveNote(note.id)
      ElMessage.success('已归档')
    }
    await load()
  } catch {
    /* 拦截器已提示 */
  }
}

/** 删除 */
async function onDelete(note) {
  try {
    await ElMessageBox.confirm('删除后不可恢复（V0.1 没有回收站），确定删除吗？', '删除笔记', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await deleteNote(note.id)
    ElMessage.success('已删除')
    // 删掉当前页最后一条时回退一页，避免停在空页
    if (items.value.length === 1 && page.value > 1) {
      page.value -= 1
    } else {
      await load()
    }
  } catch {
    /* 拦截器已提示 */
  }
}

function formatLabel(note) {
  return note.contentFormat === 1 ? 'HTML' : 'Markdown'
}

function formatTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}
</script>

<template>
  <div class="notes">
    <!-- 工具条：状态筛选 + 新建 -->
    <div class="notes__toolbar">
      <el-radio-group v-model="status" size="small">
        <el-radio-button :value="STATUS_ALL">全部</el-radio-button>
        <el-radio-button :value="0">待整理</el-radio-button>
        <el-radio-button :value="1">已归档</el-radio-button>
      </el-radio-group>

      <div class="notes__toolbar-right">
        <span class="notes__count">共 {{ total }} 条</span>
        <el-button type="primary" size="small" @click="createNew">新建笔记</el-button>
      </div>
    </div>

    <!-- 列表 -->
    <div v-loading="loading" class="notes__list">
      <el-empty v-if="!loading && items.length === 0" description="还没有笔记，点「新建笔记」写第一条" />

      <article
        v-for="note in items"
        :key="note.id"
        class="note-card"
        role="button"
        tabindex="0"
        @click="openNote(note)"
        @keydown.enter="openNote(note)"
      >
        <header class="note-card__head">
          <h3 class="note-card__title">
            <el-icon v-if="note.pinned === 1" class="note-card__pin"><StarFilled /></el-icon>
            {{ note.title || '（无标题）' }}
          </h3>
          <div class="note-card__badges">
            <el-tag size="small" type="info" effect="plain">{{ formatLabel(note) }}</el-tag>
            <el-tag size="small" :type="note.status === 1 ? 'success' : 'warning'" effect="light">
              {{ note.status === 1 ? '已归档' : '待整理' }}
            </el-tag>
          </div>
        </header>

        <p class="note-card__excerpt">{{ note.excerpt || '（无内容）' }}</p>

        <footer class="note-card__foot">
          <time class="note-card__time">{{ formatTime(note.updatedAt) }}</time>
          <div class="note-card__actions" @click.stop>
            <el-button link size="small" @click="onTogglePin(note)">
              {{ note.pinned === 1 ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button link size="small" @click="onToggleArchive(note)">
              {{ note.status === 1 ? '取回' : '归档' }}
            </el-button>
            <el-button link size="small" type="danger" @click="onDelete(note)">删除</el-button>
          </div>
        </footer>
      </article>
    </div>

    <!-- 分页 -->
    <div v-if="total > size" class="notes__pager">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        background
        small
      />
    </div>
  </div>
</template>

<style scoped>
.notes {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  /* 卡片流不设横向滚动 */
  max-width: 100%;
}

.notes__toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.notes__toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

.notes__count {
  font-size: 12px;
  color: var(--text-secondary);
}

.notes__list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 120px;
}

.note-card {
  padding: 12px 14px;
  background: var(--bg-surface);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-surface);
  cursor: pointer;
  transition: border-color var(--motion-fast) var(--motion-ease);
}

.note-card:hover,
.note-card:focus-visible {
  border-color: var(--brand-primary);
  outline: none;
}

.note-card__head {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px;
}

.note-card__title {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin: 0;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

.note-card__pin {
  font-size: 13px;
  color: var(--el-color-warning);
}

.note-card__badges {
  display: flex;
  gap: 6px;
  margin-left: auto;
}

.note-card__excerpt {
  margin: 6px 0 8px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-regular);
  /* 摘要最多显示 2 行，避免长文本把卡片撑得很高 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.note-card__foot {
  display: flex;
  align-items: center;
  gap: 8px;
}

.note-card__time {
  font-size: 12px;
  color: var(--text-secondary);
}

.note-card__actions {
  display: flex;
  gap: 4px;
  margin-left: auto;
}

.notes__pager {
  display: flex;
  justify-content: center;
  padding: 4px 0 8px;
}

@media (max-width: 767px) {
  .notes {
    padding: 12px;
  }

  .note-card__actions :deep(.el-button) {
    padding: 4px 2px;
  }
}
</style>
