import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

/**
 * 外壳的**三档布局断点**（T23）。
 *
 * 与 `useIsMobile` 的区别（**别混用，职责不同**）：
 *   - `useIsMobile`：只管「<768px」一件事，给**页面内容**用（表格还是卡片列表）；
 *     有 13 处页面在依赖它，语义不能动。
 *   - `useLayoutMode`：给**外壳（AppShell）**用，多出中间的「紧凑档」，决定
 *     侧栏展开 / 图标态 / 底部 tabbar。
 *
 * 三档（《V1.0 收尾需求》§4.5）：
 *   ≥1024px         desktop  左侧菜单完整展开（图标 + 文字）
 *   768~1023px      compact  左侧菜单折叠为**图标态**，悬停临时展开（或点折叠按钮固定展开）
 *   <768px          mobile   底部 tabbar，隐藏左侧菜单
 *
 * ⚠️ `<768` 与 `useIsMobile` 的断点**保持一致**（同为 768），只是这里多切了一刀。
 */

/** 紧凑档下界：< 这个宽度就是手机布局（与 `useIsMobile` 同值） */
export const MOBILE_MAX_WIDTH = 768

/** 桌面档下界：≥ 这个宽度侧栏完整展开 */
export const DESKTOP_MIN_WIDTH = 1024

function resolveMode(width) {
  if (width < MOBILE_MAX_WIDTH) {
    return 'mobile'
  }
  if (width < DESKTOP_MIN_WIDTH) {
    return 'compact'
  }
  return 'desktop'
}

/**
 * 响应式返回当前布局档位。
 *
 * @returns {import('vue').ComputedRef<'desktop'|'compact'|'mobile'>}
 */
export function useLayoutMode() {
  const width = ref(typeof window === 'undefined' ? DESKTOP_MIN_WIDTH : window.innerWidth)
  const mode = computed(() => resolveMode(width.value))

  let mqlMobile = null
  let mqlDesktop = null
  const sync = () => {
    width.value = window.innerWidth
  }

  onMounted(() => {
    sync()
    // 两条 matchMedia 各守一个边界；任一变化都重算（比监听 resize 省）
    mqlMobile = window.matchMedia(`(max-width: ${MOBILE_MAX_WIDTH - 1}px)`)
    mqlDesktop = window.matchMedia(`(min-width: ${DESKTOP_MIN_WIDTH}px)`)
    mqlMobile.addEventListener('change', sync)
    mqlDesktop.addEventListener('change', sync)
    // 兜底：某些环境（如 CDP 改视口）不一定派发上面两条的变化事件
    window.addEventListener('resize', sync)
  })

  onBeforeUnmount(() => {
    if (mqlMobile) mqlMobile.removeEventListener('change', sync)
    if (mqlDesktop) mqlDesktop.removeEventListener('change', sync)
    window.removeEventListener('resize', sync)
  })

  return mode
}
