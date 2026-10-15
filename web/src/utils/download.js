/**
 * 通用文件下载（T13 Excel 模板 / 各处 CSV 共用）
 */

/** 触发浏览器保存一个 Blob */
export function saveBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

/**
 * 从「本该是文件、实际是错误 JSON」的 Blob 里读出错误文案。
 *
 * 为什么需要：下载类接口用 `responseType: 'blob'`，后端返回业务错误时（如 40100/40300）
 * axios 拦截器看到的是 Blob（没有 code 字段）会当成功放行 —— 这里补一道识别，
 * 免得用户拿到一个内容是 `{"code":40300,...}` 的"假 xlsx"。
 *
 * @returns {Promise<string|null>} 是错误体就返回 message，否则 null
 */
export async function readBlobMessage(blob) {
  if (!blob || !blob.type || !blob.type.includes('json')) {
    return null
  }
  try {
    const body = JSON.parse(await blob.text())
    return body && body.message ? body.message : '下载失败，请稍后重试'
  } catch {
    return '下载失败，请稍后重试'
  }
}
