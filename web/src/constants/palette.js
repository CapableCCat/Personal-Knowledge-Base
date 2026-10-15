/**
 * 图表与 JS 侧配色 · 单一出处（T21）
 *
 * ⚠️ **为什么需要单独一份常量，而不是直接用 CSS 变量**：
 * ECharts 的颜色是**画在 canvas 上的**，读不到 `var(--brand-primary)`；
 * 组件的 JS prop（如 Vant 图标色）同理。所以凡是"JS 里要色值"的地方都从这里取，
 * 保证换主题时**只有一处需要同步**。
 *
 * ⚠️ **与 `styles/index.scss` 的对应关系（改主色时两处一起改）**：
 *   BRAND.primary ↔ --brand-primary（另：scss 里还有 6 个 EP 派生色要重算）
 *   BRAND.accent  ↔ --brand-accent
 *   NEUTRAL.*     ↔ --text-* / --border-* / --bg-*
 */

/** 品牌色（与 styles/index.scss 保持一致） */
export const BRAND = {
  /** 品牌主色 #3557F4 —— 实测自 Logo 主体文字 */
  primary: '#3557f4',
  /** 强调/动效色 #00D4FF —— 青蓝，只用于高亮，不进常规组件 */
  accent: '#00d4ff'
}

/** #RRGGBB → rgba(r,g,b,alpha)；用于渐变、半透明填充（避免手写 rgba 与主色脱节） */
export function hexToRgba(hex, alpha = 1) {
  const value = hex.replace('#', '')
  const full =
    value.length === 3
      ? value
          .split('')
          .map((c) => c + c)
          .join('')
      : value
  const r = parseInt(full.slice(0, 2), 16)
  const g = parseInt(full.slice(2, 4), 16)
  const b = parseInt(full.slice(4, 6), 16)
  return `rgba(${r},${g},${b},${alpha})`
}

/**
 * 分类色板：饼图 / 多系列柱状的默认取色顺序。
 * 首色 = 品牌主色（保证图表与 UI 同源），第 7 位用强调青蓝点缀。
 */
export const CATEGORY_PALETTE = [
  BRAND.primary,
  '#67c23a',
  '#e6a23c',
  '#f56c6c',
  '#909399',
  '#9c27b0',
  BRAND.accent,
  '#ff9800'
]

/** 单系列（柱状 / 折线 / 条形）的填充色 */
export const SERIES_COLOR = BRAND.primary

/**
 * 语义色（成功 / 警告 / 危险 / 中性）。
 * 与 Element Plus 的 `--el-color-success|warning|danger|info` **同值** ——
 * 那边给 CSS 用，这边给 **JS 侧**用（如 Vant 图标的 color prop），保证同一语义同一个色。
 */
export const SEMANTIC = {
  success: '#67c23a',
  warning: '#e6a23c',
  danger: '#f56c6c',
  info: '#909399'
}

/**
 * 地图热力连续色阶（浅 → 深）。
 * 由品牌主色推出来：末档比主色深一档，让高值省份更跳出来。
 */
export const MAP_RAMP = ['#e7ebfe', '#9aabfa', BRAND.primary, '#253dab']

/** 图表通用中性色（坐标轴 / 网格 / 地图底）—— 与 scss 的中性灰阶同值 */
export const NEUTRAL = {
  axisLabel: '#606266',
  axisLabelMuted: '#909399',
  splitLine: '#f0f2f5',
  textPrimary: '#303133',
  mapArea: '#f7f8fa',
  mapBorder: '#dcdfe6',
  /** 地图 hover 高亮底（暖色，刻意区别于品牌蓝，避免与热力色混淆） */
  mapHighlight: '#ffe58f'
}
