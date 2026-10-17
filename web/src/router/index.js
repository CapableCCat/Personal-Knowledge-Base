import { createRouter, createWebHistory } from 'vue-router'
import { setupRouterGuard } from './guard'

/**
 * 路由表（PRD §8.2 页面地图）
 *
 * 外壳只有两套：
 *   - 公开场景：`PublicLayout`（登录 / 引导）—— 页面上不得出现任何登录后才有的元素
 *   - 内部场景：`AppShell`（其余全部）—— 登录后共用同一套外壳，
 *     菜单按 `meta.menu` 从本表生成（见 `./menu.js`）
 *
 * meta 说明：
 *   title  页面标题（也作文档标题）
 *   task   该页面由哪个任务点实现（便于追账）
 *   menu   内部导航声明：`{ label, order, group, icon }`
 *          - 不写 `menu` 的路由不出现在菜单里（公开页、详情页、404）
 *          - ⚠️ **没有 `capability`** —— PKB 是单账号系统，无权限矩阵（PRD §2.2），
 *            OSC 的 `constants/roles.js` 已整删，登录即可见全部菜单
 *
 * ⚠️ V0.1 的页面在 T4~T11 逐个落地；T1 阶段先把「空壳」跑通：
 *    首页 / 登录 / 引导 / 404 可用，其余页面是占位实现。
 */
const routes = [
  // ---------- 公开场景 ----------
  // ⚠️ 每个公开页**各自**挂 PublicLayout，父路径就是页面自己的路径（子路由 path 为空）。
  //    绝对**不能**再把它们放进一个 `path: '/'` 的父记录里 —— 那会与下面 AppShell 的
  //    `path: '/'` 撞车：Vue Router 会取**先声明**的那个记录来匹配 `/`，
  //    于是 `/` 渲染成「PublicLayout + 空 router-view」= 有页头页脚但内容全空。
  //    （2026-10-17 用户截图抓到的真实 bug，见清单 §4 T4 实现结果 ⑥。）
  {
    path: '/login',
    component: () => import('@/layouts/PublicLayout.vue'),
    children: [
      {
        path: '',
        name: 'login',
        component: () => import('@/views/auth/LoginView.vue'),
        // bare：登录页全屏背景 + 表单居中，故不显示页头页脚、内容不限宽
        meta: { title: '登录', public: true, bare: true, task: 'T3' }
      }
    ]
  },
  {
    path: '/init',
    component: () => import('@/layouts/PublicLayout.vue'),
    children: [
      {
        path: '',
        name: 'initAdmin',
        component: () => import('@/views/auth/InitAdminView.vue'),
        meta: { title: '初始化', public: true, bare: true, task: 'T3' }
      }
    ]
  },

  // ---------- 内部场景（统一外壳） ----------
  {
    path: '/',
    component: () => import('@/layouts/AppShell.vue'),
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('@/views/HomeView.vue'),
        meta: {
          title: '首页',
          task: 'T9',
          menu: { order: 10, group: 'main', icon: 'HomeFilled' }
        }
      },
      {
        path: 'notes',
        name: 'notes',
        component: () => import('@/views/notes/NotesView.vue'),
        meta: {
          title: '笔记',
          task: 'T6',
          menu: { order: 20, group: 'main', icon: 'Document' }
        }
      },
      {
        path: 'note/:id',
        name: 'noteDetail',
        component: () => import('@/views/notes/NoteDetailView.vue'),
        // 详情页不进菜单（桌面走 el-drawer，这张路由主要给手机端用）
        meta: { title: '笔记详情', task: 'T6' }
      },
      {
        path: 'projects',
        name: 'projects',
        component: () => import('@/views/ProjectsView.vue'),
        meta: {
          title: '项目',
          task: 'T8',
          menu: { order: 30, group: 'main', icon: 'Folder' }
        }
      },
      {
        path: 'tags',
        name: 'tags',
        component: () => import('@/views/TagsView.vue'),
        meta: {
          title: '标签',
          task: 'T8',
          menu: { order: 40, group: 'main', icon: 'PriceTag' }
        }
      }
    ]
  },

  // ---------- 兜底 ----------
  {
    path: '/404',
    name: 'notFound',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: '页面不存在', public: true }
  },
  { path: '/:pathMatch(.*)*', redirect: '/404' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

setupRouterGuard(router)

export default router
