import request from './request'

/**
 * 健康检查（T1 起后端提供，免鉴权）
 * @returns {Promise<{app:string, profile:string, version:string, time:string, uptime:string,
 *   components: Record<string, {status:string, latencyMs:number, detail?:string}>}>}
 */
export const getHealth = () => request.get('/health')
