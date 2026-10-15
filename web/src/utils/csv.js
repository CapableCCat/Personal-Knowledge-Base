/**
 * CSV 导出（T7 初始密码清单 → T13 导入结果复用，故抽成公共工具）
 *
 * 两个细节都是有原因的：
 *   1. **开头加 BOM（\uFEFF）**：不加的话 Excel 双击打开会按本地编码解，中文全是乱码
 *   2. **字段一律加双引号并转义内部引号**：姓名/原因里出现逗号、换行、引号时不会串列
 */

/** 把二维数组拼成 CSV 文本 */
export function buildCsv(rows) {
  return (
    '\uFEFF' +
    rows
      .map((cells) => cells.map((cell) => `"${String(cell ?? '').replace(/"/g, '""')}"`).join(','))
      .join('\r\n')
  )
}

/**
 * 触发浏览器下载一个 CSV 文件。
 *
 * @param {string} filename 文件名（调用方负责带上日期，如 `初始密码清单_2026-09-26.csv`）
 * @param {Array<Array<string|number>>} rows 首行当表头
 */
export function downloadCsv(filename, rows) {
  const blob = new Blob([buildCsv(rows)], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

/** 今天（YYYY-MM-DD），用于文件名 */
export function today() {
  return new Date().toISOString().slice(0, 10)
}
