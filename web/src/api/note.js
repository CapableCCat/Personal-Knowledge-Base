import request from './request'

/**
 * 笔记接口（对应后端 `NoteController`，全部需登录）。
 *
 * ⚠️ 归档 / 取回是**两个显式端点**（不是 PATCH status）—— 后端刻意这么设计，
 * 调用方不可能传进非法状态。
 */

/** 新建（默认 inbox / 未置顶 / md） */
export const createNote = (data) => request.post('/note', data)

/** 详情（含全文） */
export const getNote = (id) => request.get(`/note/${id}`)

/**
 * 列表（分页 + 状态筛选；**不返回正文**，只有 excerpt）。
 * @param {{page?:number,size?:number,status?:number|null}} params
 */
export const pageNotes = (params) => request.get('/note/page', { params })

/** 更新标题 / 正文 / 置顶（格式不可改） */
export const updateNote = (id, data) => request.put(`/note/${id}`, data)

/** 归档（幂等） */
export const archiveNote = (id) => request.post(`/note/${id}/archive`)

/** 取回待整理（幂等） */
export const unarchiveNote = (id) => request.post(`/note/${id}/unarchive`)

/** 置顶开关 */
export const pinNote = (id, pinned) => request.put(`/note/${id}/pinned`, { pinned })

/** 逻辑删除 */
export const deleteNote = (id) => request.delete(`/note/${id}`)
