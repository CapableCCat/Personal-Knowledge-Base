import DOMPurify from 'dompurify'

/**
 * 富文本清洗（前端侧，PRD F-008「富文本安全：前后端双重」）
 *
 * 两道关口，职责不同、**都不能省**：
 *   1. **提交前**（`sanitizeHtml` 用于表单提交）—— 别把明显不该存的东西发给后端，减少往返；
 *   2. **渲染前**（`sanitizeHtml` 用于 `v-html`）—— 库里的内容不一定是本系统写的
 *      （历史数据、直连数据库导入），进浏览器前必须再过一遍。
 *
 * 真正的权威判定在**后端**（`util/HtmlSanitizer.java`）：那边的白名单还包含
 * 「图片必须是本站对象存储地址」这类前端拿不到的信息。本文件的白名单与后端**尽力对齐**，
 * 标签/属性集合保持一致，差异只在域名级校验上（前端不做，交给后端）。
 *
 * `v-html` 直接渲染用户输入是 Vue 里最典型的 XSS 入口，所以本文件是全站唯一
 * 允许把富文本交给 `v-html` 的地方（组件里只 import 这里的 `sanitizeHtml`）。
 */

/** 与后端 HtmlSanitizer 的标签白名单对齐 */
export const ALLOWED_TAGS = [
  // 块级
  'p', 'div', 'br', 'hr', 'blockquote', 'pre',
  'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
  // 行内
  'span', 'strong', 'b', 'em', 'i', 'u', 's', 'del', 'ins',
  'sub', 'sup', 'mark', 'small', 'code',
  // 列表
  'ul', 'ol', 'li',
  // 链接与图片
  'a', 'img',
  // 表格
  'table', 'thead', 'tbody', 'tfoot', 'tr', 'td', 'th', 'caption',
  // 图文
  'figure', 'figcaption'
]

/** 与后端对齐的属性白名单（不含 class / id / data-*：避免外部样式污染本站布局） */
export const ALLOWED_ATTR = [
  'href', 'title', 'target', 'rel',
  'src', 'alt', 'width', 'height',
  'colspan', 'rowspan', 'start',
  'style'
]

/** 危险容器：连同内容一起丢弃（否则 <script>alert(1)</script> 会留下满屏的 alert(1) 文本） */
const DANGEROUS_TAGS = [
  'script', 'style', 'iframe', 'frame', 'frameset', 'object', 'embed', 'applet',
  'form', 'input', 'button', 'select', 'textarea', 'option',
  'svg', 'math', 'template', 'noscript', 'noembed', 'noframes',
  'link', 'meta', 'base', 'title', 'head'
]

/** 允许保留的 CSS 属性（与后端一致，只放排版相关） */
const ALLOWED_STYLE_PROPS = ['text-align', 'text-indent', 'color', 'background-color']

const SAFE_STYLE_VALUE = /^(#[0-9a-fA-F]{3,8}|rgba?\([\d\s,.%]+\)|\d{1,4}(\.\d{1,2})?(px|em|rem|%)|left|center|right|justify)$/

/** style 逐个声明过滤：值不合规就整条丢弃 */
function filterStyle(value) {
  const kept = String(value || '')
    .split(';')
    .map((item) => item.trim())
    .filter(Boolean)
    .map((item) => {
      const idx = item.indexOf(':')
      if (idx < 0) return null
      const prop = item.slice(0, idx).trim().toLowerCase()
      const val = item.slice(idx + 1).trim()
      if (!ALLOWED_STYLE_PROPS.includes(prop)) return null
      if (!SAFE_STYLE_VALUE.test(val.toLowerCase())) return null
      return `${prop}: ${val}`
    })
    .filter(Boolean)
  return kept.join('; ')
}

// 钩子只注册一次（模块级）：只在本文件导出的 sanitizeHtml 调用链上生效
DOMPurify.addHook('afterSanitizeAttributes', (node) => {
  if (node.tagName === 'A') {
    if (node.hasAttribute('href')) {
      node.setAttribute('target', '_blank')
      // 防反向 tabnabbing：新窗口拿不到 window.opener
      node.setAttribute('rel', 'noopener noreferrer')
    } else {
      node.removeAttribute('target')
    }
  }
  if (node.hasAttribute('style')) {
    const style = filterStyle(node.getAttribute('style'))
    if (style) {
      node.setAttribute('style', style)
    } else {
      node.removeAttribute('style')
    }
  }
})

const PURIFY_CONFIG = {
  ALLOWED_TAGS,
  ALLOWED_ATTR,
  // data-* 与 aria-* 一并关掉：编辑器不需要，留着只会给绕过留缝隙
  ALLOW_DATA_ATTR: false,
  ALLOW_ARIA_ATTR: false,
  ALLOW_UNKNOWN_PROTOCOLS: false,
  // 去掉 data: （可承载 SVG/脚本载荷）；相对路径（announcements/… 这种对象 key）仍然放行
  ALLOWED_URI_REGEXP: /^(?:(?:https?|mailto|tel):|[^a-z]|[a-z+.-]+(?:[^a-z+.\-:]|$))/i,
  FORBID_TAGS: DANGEROUS_TAGS,
  FORBID_CONTENTS: DANGEROUS_TAGS,
  // 剥标签时保留内部文本（正文别被吃掉），危险容器的内容已由 FORBID_CONTENTS 单独处理
  KEEP_CONTENT: true
}

/**
 * 清洗富文本 HTML。
 *
 * @param {string} html 原始 HTML
 * @returns {string} 清洗后的 HTML（入参为空时返回空串）
 */
export function sanitizeHtml(html) {
  if (!html) return ''
  return DOMPurify.sanitize(String(html), PURIFY_CONFIG)
}

/**
 * 清洗后的内容是否「有东西可看」—— 与后端 `HtmlSanitizer.hasVisibleContent` 同口径。
 *
 * 只贴一张海报、一个字不写的公告也是合法内容，所以不能只看纯文本。
 *
 * @param {string} html 已清洗或未清洗的 HTML
 */
export function hasVisibleContent(html) {
  if (!html) return false
  const container = document.createElement('div')
  container.innerHTML = sanitizeHtml(html)
  if (container.querySelector('img')) return true
  return (container.textContent || '').trim().length > 0
}
