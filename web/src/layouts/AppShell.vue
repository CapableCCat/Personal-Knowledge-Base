<script setup>
/**
 * 内部场景统一外壳（PKB V0.1）
 *
 * 全部内部页面共用这一套外壳，**菜单完全由路由表生成**（`router/menu.js`）：
 *   - 桌面（≥1024）：左侧菜单，每项带图标
 *   - 紧凑（768~1023）：侧栏**图标态**，鼠标移入临时展开为浮层；点折叠按钮可固定展开
 *   - 手机（<768）：隐藏侧栏，**导航走页头 ☰ 抽屉**（含全部 4 项）
 *   - 右上角：账号区 hover 出下拉 → 退出登录
 *
 * ⚠️ **手机端没有底部 tabbar**（T4 拍板，方案 A）：
 *   PRD P-010 冻结的「手机底部 3 Tab（最近 / 待整理 / 项目）」是**首页的内容 Tab**
 *   （原文「切 Tab 只换内容区」= 内容级、非路由级），由 **T9 的 HomeView 自建**。
 *   外壳若也占底部，375×812 的屏上会叠两条底栏（吃掉近 15% 高度）。
 *   → **底部那个位置留给 T9**；外壳只管布局与导航。
 *
 * ⚠️ 与 OSC 的关键差异（T1 落地，见清单 §4 T1）：
 *   - **菜单从 11 项砍到 4 项**：首页 / 笔记 / 项目 / 标签
 *   - **删掉 `LocaleSwitch`（i18n）与 `AnnouncementBell`（公告）**：前者整删，后者随业务删除
 *   - **删掉 `MoreGrid` 图标宫格**
 *   - **账号显示改用手机号**：`user` 表已删 `name` / `avatar_url` 列（PRD 附录 C.3）
 *   - **账号下拉只剩「退出登录」**：个人中心随 OSC 业务删除（PRD §1.4 非目标）
 *
 * 滚动模型（PRD D-6，OSC 已踩过的坑，**不可删**）：
 *   `.app-shell` = `height:100% + overflow:hidden`；`.app-body` 与 `.app-main` 的
 *   `min-height:0` 是关键 —— flex 子项默认 `min-height:auto`，不加它内部滚动会失效。
 *   用 `height:100%` 而**不是** `100dvh`（dvh 随地址栏收放变化，会让滚动位置跳）。
 *
 * ⚠️ 用的是本文件专属的 `useLayoutMode`（三档），**不是** `useIsMobile`（只有 <768 一档）。
 */
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  Document,
  Folder,
  HomeFilled,
  Menu as MenuIcon,
  PriceTag
} from '@element-plus/icons-vue'
import { APP_NAME, ROUTE_PATH } from '@/constants/app'
import { useLayoutMode } from '@/composables/useLayoutMode'
import { useUserStore } from '@/stores/user'
import { useRouteMenu } from '@/router/menu'
import NoteDrawer from '@/components/NoteDrawer.vue'

/**
 * 菜单图标映射：路由 `meta.menu.icon` 存的是**组件名字符串**（路由表是纯数据，不该 import 组件），
 * 这里做**显式**映射 —— 显式 import 才能被 tree-shake；漏配时降级为不显示图标（不报错）。
 */
const MENU_ICONS = {
  Document,
  Folder,
  HomeFilled,
  PriceTag
}

const layoutMode = useLayoutMode()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { groups } = useRouteMenu()

const isMobile = computed(() => layoutMode.value === 'mobile')
const isCompact = computed(() => layoutMode.value === 'compact')
const isDesktop = computed(() => layoutMode.value === 'desktop')

const drawerVisible = ref(false)
/** 紧凑档：鼠标移入侧栏时临时展开（浮层，不挤内容） */
const hoverExpand = ref(false)
/** 紧凑档：点折叠按钮**固定**展开（占位推内容）—— 触屏没有 hover，必须有这条出路 */
const pinnedExpand = ref(false)

/** 侧栏是否处于「图标态」 */
const menuCollapsed = computed(
  () => isCompact.value && !hoverExpand.value && !pinnedExpand.value
)
/** 侧栏是否展开（含固定展开与悬停展开） */
const menuExpanded = computed(() => !menuCollapsed.value)

const currentTitle = computed(() => route.meta.title || APP_NAME)
/** 账号显示：单账号无 `name` 列，用手机号 */
const displayName = computed(() => userStore.profile?.phone || '未登录')
/** 头像占位：手机号末两位（无 avatar 列） */
const avatarInitial = computed(() => (userStore.profile?.phone || '?').slice(-2))

function iconOf(name) {
  return MENU_ICONS[name] || null
}

/** 抽屉点某一项 → 关抽屉（跳转由 el-menu 的 router 模式完成） */
function onMenuSelect() {
  drawerVisible.value = false
}

/** 页头折叠按钮：手机档打开抽屉；紧凑档固定展开/收起侧栏 */
function onToggleMenu() {
  if (isMobile.value) {
    drawerVisible.value = true
    return
  }
  pinnedExpand.value = !pinnedExpand.value
  hoverExpand.value = false
}

/** 切档时复位临时状态，避免「在紧凑档固定展开、拉宽到桌面后状态残留」 */
watch(layoutMode, () => {
  hoverExpand.value = false
  pinnedExpand.value = false
})

/** 切路由时收起手机抽屉 */
watch(
  () => route.fullPath,
  () => {
    drawerVisible.value = false
  }
)

onMounted(() => {
  // 占位：保留挂载钩子，后续首页三列滚动需要时在此加监听
})

onBeforeUnmount(() => {
  // 占位：与 onMounted 对称
})

/** 右上角账号下拉 */
async function onAccountCommand(command) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    } catch {
      return
    }
    await userStore.logout()
    router.replace({ path: ROUTE_PATH.LOGIN })
  }
}
</script>

<template>
  <div class="app-shell">
    <!-- 桌面 / 紧凑：左侧菜单 -->
    <aside
      v-if="!isMobile"
      class="app-aside"
      :class="{
        'is-compact': isCompact,
        'is-expanded': isCompact && menuExpanded,
        'is-pinned': isCompact && pinnedExpand
      }"
      @mouseenter="hoverExpand = true"
      @mouseleave="hoverExpand = false"
    >
      <div class="app-brand">
        <span class="app-brand__text">{{ APP_NAME }}</span>
        <!-- 紧凑档图标态：品牌名缩成标记，省空间 -->
        <span v-if="menuCollapsed" class="app-brand__mark">P</span>
      </div>
      <el-menu :default-active="route.path" router class="app-menu">
        <el-menu-item-group v-for="group in groups" :key="group.key" :title="group.label">
          <el-menu-item v-for="item in group.items" :key="item.path" :index="item.path">
            <el-icon v-if="iconOf(item.icon)"><component :is="iconOf(item.icon)" /></el-icon>
            <span class="app-menu__label">{{ item.label }}</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-menu>
    </aside>

    <div class="app-body">
      <header class="app-header">
        <button
          v-if="!isDesktop"
          class="app-menu-toggle"
          type="button"
          :aria-label="isMobile ? '打开菜单' : '展开或收起菜单'"
          @click="onToggleMenu"
        >
          <el-icon :size="20"><MenuIcon /></el-icon>
        </button>
        <span class="app-title">{{ currentTitle }}</span>
        <div class="app-actions">
          <el-dropdown trigger="hover" @command="onAccountCommand">
            <span class="app-account">
              <el-avatar :size="26">{{ avatarInitial }}</el-avatar>
              <span class="app-user">{{ displayName }}</span>
              <el-icon class="app-account__caret"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="app-main">
        <router-view />
      </main>
    </div>

    <!--
      手机档：页头 ☰ → 全部菜单。
      ⚠️ 底部**不放**导航（T4 拍板）：那个位置留给 T9 首页的内容 Tab（PRD P-010）。
    -->
    <el-drawer v-model="drawerVisible" direction="ltr" size="240px" :with-header="false">
      <div class="app-drawer__brand">{{ APP_NAME }}</div>
      <el-menu :default-active="route.path" router class="app-drawer__menu" @select="onMenuSelect">
        <el-menu-item v-for="item in groups.flatMap((g) => g.items)" :key="item.path" :index="item.path">
          <el-icon v-if="iconOf(item.icon)"><component :is="iconOf(item.icon)" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-drawer>

    <!--
      笔记抽屉（桌面端）挂在外壳上，而不是某个页面里 ——
      这样笔记列表（T6）与首页（T9）都能用 `useNoteDrawer().open(id)` 唤起，
      不用各自维护一份抽屉。手机端不走它（走 `/note/:id` 全屏页）。
    -->
    <NoteDrawer />
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  /* 紧凑档悬停展开的浮层要相对这里定位 */
  position: relative;
  /*
   * **撑满视口高度 + 内部各自滚动** —— 顶栏固定、侧栏与内容各滚各的。
   * ⚠️ 用 `height: 100%`（沿 html/body/#app 的高度链）而**不是** `100dvh`：
   *    手机浏览器地址栏收放时 dvh 会变化，容器高度跟着变 → 内容重排、滚动位置跳。
   */
  height: 100%;
  overflow: hidden;
}

.app-aside {
  display: flex;
  flex-direction: column;
  width: 208px;
  flex: none;
  background: var(--bg-surface);
  border-right: 1px solid var(--border-color);
}

/* ---------- 紧凑档（768~1023px）：图标态 ---------- */
.app-aside.is-compact {
  width: 64px;
  transition: width var(--motion-fast) var(--motion-ease);
}

.app-aside.is-compact.is-expanded {
  width: 208px;
}

/*
 * 悬停展开用**浮层**：只改宽度会让右侧内容跟着抖动。
 * 「点折叠按钮固定展开」时留在文档流里（is-pinned），因为那是用户的明确意图、可以挤内容。
 */
.app-aside.is-compact.is-expanded:not(.is-pinned) {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 30;
  box-shadow: var(--shadow-popup);
}

/* 图标态：隐藏文字，图标居中；组标题退化成一条分隔线，保留分组感 */
.app-aside.is-compact:not(.is-expanded) :deep(.app-menu__label) {
  display: none;
}

.app-aside.is-compact:not(.is-expanded) :deep(.el-menu-item) {
  justify-content: center;
  padding-left: 0 !important;
  padding-right: 0 !important;
}

.app-aside.is-compact:not(.is-expanded) .app-brand__text {
  display: none;
}

.app-aside.is-compact.is-expanded .app-brand__mark {
  display: none;
}

.app-brand {
  display: flex;
  align-items: center;
  padding: 16px;
  font-size: 15px;
  font-weight: 500;
  color: var(--brand-primary);
  white-space: nowrap;
}

.app-brand__mark {
  font-size: 18px;
  font-weight: 600;
}

.app-menu {
  flex: 1;
  overflow-y: auto;
  border-right: none;
}

/* 紧凑档图标态：组标题的留白太占高度，压成一条细分隔线 */
.app-aside.is-compact:not(.is-expanded) :deep(.el-menu-item-group__title) {
  height: 1px;
  margin: 6px 14px;
  padding: 0;
  overflow: hidden;
  font-size: 0;
  line-height: 0;
  background: var(--border-color);
}

.app-body {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
  /* min-height: 0 必须有：flex 子项默认 min-height: auto，内容一长它会被撑高，
     那样内部滚动就失效了 */
  min-height: 0;
}

.app-header {
  display: flex;
  align-items: center;
  gap: 12px;
  /* flex: none —— 顶栏是固定高度条，不能被内容挤压 */
  flex: none;
  padding: 10px 16px;
  background: var(--bg-surface);
  border-bottom: 1px solid var(--border-color);
}

.app-menu-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 6px;
  color: var(--text-regular);
  background: none;
  border: none;
  border-radius: var(--radius-control);
  cursor: pointer;
}

.app-menu-toggle:hover {
  background: var(--el-fill-color-light, #f5f7fa);
  color: var(--brand-primary);
}

.app-title {
  font-size: 15px;
  font-weight: 500;
}

.app-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

/* 账号区：hover 触发下拉（触发器本身也要有 hover 反馈） */
.app-account {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 6px;
  border-radius: var(--radius-pill);
  cursor: pointer;
  outline: none;
}

.app-account:hover,
.app-account:focus-visible {
  background: var(--el-fill-color-light, #f5f7fa);
}

.app-user {
  font-size: 13px;
  color: var(--text-regular);
}

.app-account__caret {
  font-size: 12px;
  color: var(--text-secondary);
}

.app-main {
  flex: 1;
  min-width: 0;
  /* **内容在这里滚**（顶栏与侧栏不跟着动）。min-height: 0 是关键 */
  min-height: 0;
  overflow-y: auto;
  /*
   * ⚠️ 这里**不再**需要为底部 tabbar 留 `padding-bottom`（T4 已移除底栏，
   *    底部位置归 T9 首页自己的内容 Tab 管理）。若将来重新引入底栏，必须同时补回来。
   */
}

.app-drawer__brand {
  padding: 16px;
  font-size: 15px;
  font-weight: 500;
  color: var(--brand-primary);
}

.app-drawer__menu {
  border-right: none;
}
</style>
