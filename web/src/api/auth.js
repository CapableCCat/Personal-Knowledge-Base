import request from './request'

/** 图形验证码（算术型） */
export const getCaptcha = () => request.get('/auth/captcha')

/** 系统是否已初始化（决定是否强制跳引导页） */
export const getInitStatus = () => request.get('/auth/init-status')

/** 创建唯一账号（仅未初始化时可用） */
export const initAdmin = (data) => request.post('/auth/init-admin', data)

/** 登录：手机号 + 密码 + 验证码 */
export const login = (data) => request.post('/auth/login', data)

/** 登出 */
export const logout = () => request.post('/auth/logout')
