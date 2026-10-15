/**
 * 应用级常量
 *
 * ⚠️ 本文件是前端的**根依赖**（request.js / 守卫 / AppShell / 登录页等都从这里取），
 *    改一处即全站生效 —— 改名 / 换 Header / 加路由都优先改这里。
 */

export const APP_NAME = 'PKB 个人知识库'
export const APP_FULL_NAME = 'PKB 个人知识库（Personal Knowledge Base）'

/** Sa-Token 的 Header 名，必须与后端 application.yml 的 sa-token.token-name 一致 */
export const TOKEN_HEADER = 'pkb-token'

/** 本地存储 key */
export const STORAGE_KEY = {
  TOKEN: 'pkb_token',
  PROFILE: 'pkb_profile'
}

/** 路由 path */
export const ROUTE_PATH = {
  /** 首页工作台（桌面三列 / 手机 3 Tab） */
  HOME: '/',
  /** 笔记列表 */
  NOTES: '/notes',
  /** 笔记全屏页（手机端主用的详情页；桌面是一个没有后缀的独立路由） */
  NOTE_DETAIL: '/note/:id',
  /** 标签管理 */
  TAGS: '/tags',
  /** 项目管理 */
  PROJECTS: '/projects',
  /** 登录页 */
  LOGIN: '/login',
  /** 引导页（首启建唯一账号） */
  INIT: '/init',
  /** 404 */
  NOT_FOUND: '/404'
}
