<script setup>
/**
 * 首页工作台（P-010 / P-001 快速记录入口）—— T9。
 *
 * <p><b>三块内容：</b>
 * 1. **快速记录**：顶部输入框，回车即建一条 `status=0` 的笔记，**不跳页**；右侧按钮切格式（默认 md）。
 * 2. **状态条**：待整理 / 已归档 / 项目 / 本周 +N，四项都可点，跳到对应列表并带筛选。
 * 3. **看板**：桌面三列（项目 / 最近 / 待整理）**各自滚动**；手机三 Tab（最近 / 待整理 / 项目），
 *    切 Tab **只换内容区**（不换路由、不刷新）。
 *
 * <p><b>滚动模型（T4 实测修正，D34，别照抄 PRD 里的旧说法）：</b>
 * 外壳 `.app-main` 是整页的滚动容器；首页要「三列各自滚」，就必须让首页**占满** `.app-main`
 * 且自己不滚（`height:100%` + `min-height:0`），再给每一列的 body 单独 `overflow-y:auto`。
 * ⚠️ 只堆 `min-height:0` 而不给 `overflow` 是**无效**的 —— 这是 OSC 时期最大的传说。
 *
 * <p>⚠️ 手机底部 3 Tab 是**本页自建的内容 Tab**（D33）：外壳不渲染底栏，底部位置留在这里。
 */
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createNote, getNoteStats, pageNotes } from '@/api/note'
import { listTags } from '@/api/tag'
import { useLayoutMode } from '@/composables/useLayoutMode'
import { useNoteDrawer } from '@/composables/useNoteDrawer'

const router = useRouter()
const layoutMode = useLayoutMode()
const { open: openDrawer, version } = useNoteDrawer()

const isMobile = computed(() => layoutMode.value === 'mobile')

// ------------------------------------------------------------
// 快速记录（P-001）
// ------------------------------------------------------------
const FORMAT_KEY = 'pkb_home_format'

const quickText = ref('')
/** 格式只影响**此后新建**的笔记，不动任何已有笔记（PRD P-001） */
const quickFormat = ref(Number(sessionStorage.getItem(FORMAT_KEY)) === 1 ? 1 : 0)
const creating = ref(false)
const shaking = ref(false)
/** 中文输入法组词期间按回车**不能**当成提交（PRD §9.3 #4 的 IME 坑） */
const composing = ref(false)
const inputRef = ref(null)

function toggleFormat() {
  quickFormat.value = quickFormat.value === 1 ? 0 : 1
  sessionStorage.setItem(FORMAT_KEY, String(quickFormat.value))
}

function shake() {
  shaking.value = true
  window.setTimeout(() => {
    shaking.value = false
  }, 400)
}

async function submitQuick() {
  if (creating.value) {
    return
  }
  const text = quickText.value.trim()
  if (!text) {
    // PRD P-001：空输入回车不创建，输入框轻微抖动提示（不弹错误框）
    shake()
    return
  }
  creating.value = true
  try {
    // 输入的文字**同时**作为 title 与 content（title 由后端截断到 255，全文进 content）
    await createNote({ title: text, content: text, contentFormat: quickFormat.value })
    quickText.value = ''
    ElMessage.success('已存入待整理')
    await refreshAll()
  } catch {
    // 失败时**保留输入内容**，由拦截器提示；这里绝不 clear（PRD P-001 分支）
  } finally {
    creating.value = false
  }
  // ⚠️ 必须等 `creating` 复位后再聚焦：提交中输入框是 `disabled`，
  //    对 disabled 元素调 focus() 是**无效**的（T9 实测抓到，「保持焦点」会失效）。
  await nextTick()
  inputRef.value?.focus?.()
}

/** 回车：组词中不提交，避免中文输入法「选字回车」被当成提交 */
function onEnter(event) {
  if (composing.value || event.isComposing) {
    return
  }
  submitQuick()
}

// ------------------------------------------------------------
// 数据：状态条 + 三列
// ------------------------------------------------------------
const stats = ref({ inbox: 0, archived: 0, projects: 0, weekAdded: 0 })

const recent = ref([])
const inboxNotes = ref([])
const projects = ref([])

const loadingRecent = ref(false)
const loadingInbox = ref(false)
const loadingProjects = ref(false)

/** 手机档当前 Tab（内容级切换，不改路由） */
const MOBILE_TABS = [
  { key: 'recent', label: '最近' },
  { key: 'inbox', label: '待整理' },
  { key: 'projects', label: '项目' }
]
const mobileTab = ref('recent')

const COLUMN_SIZE = 20

async function loadStats() {
  try {
    stats.value = await getNoteStats()
  } catch {
    /* 拦截器已提示 */
  }
}

async function loadRecent() {
  loadingRecent.value = true
  try {
    const data = await pageNotes({ page: 1, size: COLUMN_SIZE })
    recent.value = data.records || []
  } catch {
    /* 拦截器已提示 */
  } finally {
    loadingRecent.value = false
  }
}

async function loadInbox() {
  loadingInbox.value = true
  try {
    const data = await pageNotes({ page: 1, size: COLUMN_SIZE, status: 0 })
    inboxNotes.value = data.records || []
  } catch {
    /* 拦截器已提示 */
  } finally {
    loadingInbox.value = false
  }
}

async function loadProjects() {
  loadingProjects.value = true
  try {
    projects.value = (await listTags(1)) || []
  } catch {
    /* 拦截器已提示 */
  } finally {
    loadingProjects.value = false
  }
}

async function refreshAll() {
  await Promise.all([loadStats(), loadRecent(), loadInbox(), loadProjects()])
}

onMounted(refreshAll)
// 抽屉里保存 / 删除 / 归档后刷新（与笔记列表同一套机制）
watch(version, refreshAll)

// ------------------------------------------------------------
// 状态条与跳转
// ------------------------------------------------------------
const statusItems = computed(() => [
  { key: 'inbox', label: '待整理', value: stats.value.inbox, to: { path: '/notes', query: { status: '0' } } },
  { key: 'archived', label: '已归档', value: stats.value.archived, to: { path: '/notes', query: { status: '1' } } },
  { key: 'projects', label: '项目', value: stats.value.projects, to: { path: '/projects' } },
  // 「本周」= 本周一 00:00 起算的本周新建数（PRD #11 拍板）
  { key: 'week', label: '本周', value: '+' + stats.value.weekAdded, to: { path: '/notes', query: { week: '1' } } }
])

function goStatus(item) {
  router.push(item.to)
}

function goProject(project) {
  router.push({ path: '/notes', query: { projectId: project.id } })
}

function goAllNotes(status) {
  router.push({ path: '/notes', query: status === null ? {} : { status: String(status) } })
}

/** 打开笔记：桌面走抽屉，手机走全屏路由（D-7，与列表页一致） */
function openNote(note) {
  if (isMobile.value) {
    router.push(`/note/${note.id}`)
  } else {
    openDrawer(note.id)
  }
}

function formatTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(5, 16)
}

/** 手机 Tab 当前展示的笔记列表 */
const mobileNotes = computed(() => (mobileTab.value === 'inbox' ? inboxNotes.value : recent.value))
</script>

<template>
  <div class="home" :class="{ 'is-mobile': isMobile }">
    <!-- ① 快速记录：桌面与手机都常驻在顶部（不随内容区滚动） -->
    <div class="home__composer">
      <el-input
        ref="inputRef"
        v-model="quickText"
        class="home__quick"
        :class="{ 'is-shaking': shaking }"
        :disabled="creating"
        maxlength="2000"
        placeholder="记点什么，回车即存入「待整理」"
        @keydown.enter.prevent="onEnter"
        @compositionstart="composing = true"
        @compositionend="composing = false"
      >
        <template #append>
          <el-button class="home__format" :disabled="creating" @click="toggleFormat">
            {{ quickFormat === 1 ? '富文本' : 'Markdown' }}
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- ② 状态条：四项都可点（手机档横滑） -->
    <div class="home__status" :class="{ 'is-mobile': isMobile }">
      <button
        v-for="item in statusItems"
        :key="item.key"
        class="home__status-item"
        type="button"
        @click="goStatus(item)"
      >
        <span class="home__status-label">{{ item.label }}</span>
        <strong class="home__status-value">{{ item.value }}</strong>
      </button>
    </div>

    <!-- ③-桌面：三列各自滚动 -->
    <div v-if="!isMobile" class="home__columns">
      <section class="home-column">
        <header class="home-column__head">
          <span class="home-column__title">项目</span>
          <router-link class="home-column__more" to="/projects">全部</router-link>
        </header>
        <div v-loading="loadingProjects" class="home-column__body">
          <el-empty v-if="!loadingProjects && projects.length === 0" description="还没有项目" :image-size="56" />
          <button
            v-for="project in projects"
            :key="project.id"
            class="home-project"
            type="button"
            @click="goProject(project)"
          >
            <span class="home-project__dot" :style="{ background: project.color || 'var(--border-color)' }" />
            <span class="home-project__name">{{ project.name }}</span>
            <span class="home-project__count">{{ project.noteCount || 0 }}</span>
          </button>
        </div>
      </section>

      <section class="home-column">
        <header class="home-column__head">
          <span class="home-column__title">最近</span>
          <button class="home-column__more home-column__more--btn" type="button" @click="goAllNotes(null)">
            全部
          </button>
        </header>
        <div v-loading="loadingRecent" class="home-column__body">
          <el-empty v-if="!loadingRecent && recent.length === 0" description="还没有笔记" :image-size="56" />
          <button
            v-for="note in recent"
            :key="note.id"
            class="home-note"
            type="button"
            @click="openNote(note)"
          >
            <span class="home-note__title">{{ note.title || '（无标题）' }}</span>
            <span class="home-note__meta">
              <span v-if="note.status === 1" class="home-note__badge">已归档</span>
              <time>{{ formatTime(note.updatedAt) }}</time>
            </span>
          </button>
        </div>
      </section>

      <section class="home-column">
        <header class="home-column__head">
          <span class="home-column__title">待整理</span>
          <button class="home-column__more home-column__more--btn" type="button" @click="goAllNotes(0)">
            全部
          </button>
        </header>
        <div v-loading="loadingInbox" class="home-column__body">
          <el-empty
            v-if="!loadingInbox && inboxNotes.length === 0"
            description="待整理已清空"
            :image-size="56"
          />
          <button
            v-for="note in inboxNotes"
            :key="note.id"
            class="home-note"
            type="button"
            @click="openNote(note)"
          >
            <span class="home-note__title">{{ note.title || '（无标题）' }}</span>
            <span class="home-note__meta">
              <time>{{ formatTime(note.updatedAt) }}</time>
            </span>
          </button>
        </div>
      </section>
    </div>

    <!-- ③-手机：内容区 + 底部 3 Tab -->
    <template v-else>
      <div
        v-loading="mobileTab === 'projects' ? loadingProjects : mobileTab === 'inbox' ? loadingInbox : loadingRecent"
        class="home__mobile-body"
      >
        <template v-if="mobileTab === 'projects'">
          <el-empty v-if="projects.length === 0" description="还没有项目" :image-size="56" />
          <button
            v-for="project in projects"
            :key="project.id"
            class="home-project"
            type="button"
            @click="goProject(project)"
          >
            <span class="home-project__dot" :style="{ background: project.color || 'var(--border-color)' }" />
            <span class="home-project__name">{{ project.name }}</span>
            <span class="home-project__count">{{ project.noteCount || 0 }}</span>
          </button>
        </template>
        <template v-else>
          <el-empty
            v-if="mobileNotes.length === 0"
            :description="mobileTab === 'inbox' ? '待整理已清空' : '还没有笔记'"
            :image-size="56"
          />
          <button
            v-for="note in mobileNotes"
            :key="note.id"
            class="home-note"
            type="button"
            @click="openNote(note)"
          >
            <span class="home-note__title">{{ note.title || '（无标题）' }}</span>
            <span class="home-note__meta">
              <span v-if="note.status === 1" class="home-note__badge">已归档</span>
              <time>{{ formatTime(note.updatedAt) }}</time>
            </span>
          </button>
        </template>
      </div>

      <nav class="home__tabs">
        <button
          v-for="tab in MOBILE_TABS"
          :key="tab.key"
          class="home__tab"
          :class="{ 'is-active': mobileTab === tab.key }"
          type="button"
          @click="mobileTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </nav>
    </template>
  </div>
</template>

<style scoped>
/*
 * 首页占满 `.app-main` 且**自己不再滚**（整页滚动交给三列 / 手机内容区）。
 * height:100% + min-height:0 是「内部滚动」的前提（D34）。
 */
.home {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  min-height: 0;
  padding: 16px;
  box-sizing: border-box;
}

/* ---------- 快速记录 ---------- */
.home__composer {
  flex: none;
}

.home__quick :deep(.el-input__wrapper) {
  border-radius: var(--radius-control);
}

.home__format {
  min-width: 88px;
}

/* 空输入回车：轻微抖动（不弹错误框） */
.home__quick.is-shaking {
  animation: home-shake 0.4s;
}

@keyframes home-shake {
  0%,
  100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-4px);
  }
  75% {
    transform: translateX(4px);
  }
}

/* ---------- 状态条 ---------- */
.home__status {
  display: flex;
  flex: none;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.home__status-item {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  padding: 4px 12px;
  font-size: 13px;
  color: var(--text-regular);
  background: var(--bg-surface);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-pill);
  cursor: pointer;
  transition: border-color var(--motion-fast) var(--motion-ease);
}

.home__status-item:hover {
  border-color: var(--brand-primary);
  color: var(--brand-primary);
}

.home__status-value {
  font-size: 15px;
  color: var(--text-primary);
}

/* 手机档：状态条横滑（不换行、不撑高） */
.home__status.is-mobile {
  flex-wrap: nowrap;
  overflow-x: auto;
  padding-bottom: 2px;
}

.home__status.is-mobile::-webkit-scrollbar {
  display: none;
}

/* ---------- 桌面三列：各列自带滚动 ---------- */
.home__columns {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  flex: 1;
  /* 关键：让三列的高度被「剩余空间」确定，否则列内 overflow 不生效 */
  min-height: 0;
}

.home-column {
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: var(--bg-surface);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-surface);
  overflow: hidden;
}

.home-column__head {
  display: flex;
  flex: none;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-bottom: 1px solid var(--border-color);
}

.home-column__title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.home-column__more {
  font-size: 12px;
  color: var(--brand-primary);
  text-decoration: none;
}

.home-column__more--btn {
  padding: 0;
  background: none;
  border: none;
  cursor: pointer;
}

/* **这一条才是「各列自己滚」的承重件**（D34） */
.home-column__body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* ---------- 列内条目 ---------- */
.home-project,
.home-note {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 10px;
  text-align: left;
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--radius-control);
  cursor: pointer;
}

.home-project:hover,
.home-note:hover {
  background: var(--el-fill-color-light, #f5f7fa);
  border-color: var(--border-color);
}

.home-project__dot {
  flex: none;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.home-project__name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  font-size: 13px;
  color: var(--text-primary);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.home-project__count {
  flex: none;
  font-size: 12px;
  color: var(--text-secondary);
}

.home-note {
  flex-direction: column;
  align-items: stretch;
  gap: 2px;
}

.home-note__title {
  overflow: hidden;
  font-size: 13px;
  color: var(--text-primary);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.home-note__meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--text-secondary);
}

.home-note__badge {
  padding: 0 4px;
  color: var(--el-color-success);
  border: 1px solid currentColor;
  border-radius: 3px;
}

/* ---------- 手机档 ---------- */
.home.is-mobile {
  gap: 8px;
  padding: 10px 10px 0;
}

.home__mobile-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-bottom: 8px;
}

/* 底部 3 Tab：本页自建（D33，外壳不占底部） */
.home__tabs {
  display: flex;
  flex: none;
  gap: 0;
  margin: 0 -10px;
  padding: 4px 10px calc(4px + env(safe-area-inset-bottom, 0px));
  background: var(--bg-surface);
  border-top: 1px solid var(--border-color);
}

.home__tab {
  flex: 1;
  padding: 8px 0;
  font-size: 13px;
  color: var(--text-regular);
  background: none;
  border: none;
  cursor: pointer;
}

.home__tab.is-active {
  font-weight: 500;
  color: var(--brand-primary);
}
</style>
