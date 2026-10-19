import request from './request'

/**
 * 标签 / 项目接口（对应后端 `TagController`，全部需登录）。
 *
 * ⚠️ 两者共用 `tag` 表，靠 `type` 区分：0=标签（多选）、1=项目（单选）。
 */

/** 列表：type 不传 = 两类都返回（笔记表单一次拿全）；管理页各传 0 / 1 */
export const listTags = (type) =>
  request.get('/tag', { params: type === undefined || type === null ? {} : { type } })

/** 新建（type 必填，建后不可改） */
export const createTag = (data) => request.post('/tag', data)

/** 改名 / 改色 / 改排序（type 不可变；`color: ''` 表示清空颜色） */
export const updateTag = (id, data) => request.put(`/tag/${id}`, data)

/** 重排：传该类型下**全部** id 的目标顺序 */
export const sortTags = (data) => request.put('/tag/sort', data)

/** 删除（解除全部关联 + 逻辑删除）；返回被解除关联的笔记数 */
export const deleteTag = (id) => request.delete(`/tag/${id}`)
