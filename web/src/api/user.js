import request from './request'

/**
 * 当前登录账号信息（不含密码）。
 *
 * ⚠️ 与 OSC 的差异：改密 / 资料 / 学号 / 头像接口随业务删除，
 *    单账号系统只剩「读当前账号」。
 */
export const getCurrentUser = () => request.get('/user/current')
