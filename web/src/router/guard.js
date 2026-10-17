import { APP_NAME, ROUTE_PATH } from '@/constants/app'
import { useUserStore } from '@/stores/user'

/**
 * 路由守卫
 *
 * ⚠️ 与 OSC 的关键差异（PRD §2.2 / 清单 D13、D17）：**只判两件事**
 *   1. 系统是否已初始化（未初始化 → 强制引导页）
 *   2. 是否已登录（未登录 → 登录页，带 redirect）
 *
 * **不再有**：角色 / 能力判定（`constants/roles.js` 已整删）、首登强制改密跳转、
 * 公开端落地页开关逻辑（PKB 没有报名页）。
 *
 * 守卫只负责「别让用户点进去白跑一趟」；真正的鉴权在后端拦截器。
 */
export function setupRouterGuard(router) {
  router.beforeEach(async (to) => {
    document.title = to.meta.title ? `${to.meta.title} · ${APP_NAME}` : APP_NAME

    const userStore = useUserStore()

    // 1. 系统初始化状态（会话内只请求一次）
    let initialized = true
    try {
      initialized = await userStore.fetchInitStatus()
    } catch {
      // 后端不可用时保持 true（不强制跳引导页），避免把用户卡死在这一步
    }
    if (!initialized) {
      // 未初始化：只允许待在引导页
      return to.name === 'initAdmin' ? true : { path: ROUTE_PATH.INIT }
    }
    if (to.name === 'initAdmin') {
      // 已初始化：引导页不可再进（不得二次建号）
      // ⚠️ 用**路由名**判断而不是 `to.path === '/init'`：
      //    后者漏掉 `/init/`（带尾斜杠）—— 实测那样会绕过重定向、仍渲染建号表单。
      return { path: ROUTE_PATH.LOGIN }
    }

    // 2. 公开页
    if (to.meta.public) {
      if (to.name === 'login' && userStore.isLoggedIn) {
        return { path: ROUTE_PATH.HOME }
      }
      return true
    }

    // 3. 需要登录
    if (!userStore.isLoggedIn) {
      return { path: ROUTE_PATH.LOGIN, query: { redirect: to.fullPath } }
    }

    return true
  })
}
