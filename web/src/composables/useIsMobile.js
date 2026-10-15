import { onBeforeUnmount, onMounted, ref } from 'vue'

/** 移动端断点：<768px 视为移动端（PRD 要求报名页/查询页/审核列表页移动第一体验） */
export const MOBILE_MAX_WIDTH = 768

/**
 * 响应式判断当前是否移动端视口。
 * 管理端页面用它切换「桌面表格 / 移动卡片列表」两套布局。
 *
 * @returns {import('vue').Ref<boolean>}
 */
export function useIsMobile() {
  const isMobile = ref(typeof window !== 'undefined' ? window.innerWidth < MOBILE_MAX_WIDTH : false)
  let mql = null
  const onChange = (event) => {
    isMobile.value = event.matches
  }

  onMounted(() => {
    mql = window.matchMedia(`(max-width: ${MOBILE_MAX_WIDTH - 1}px)`)
    isMobile.value = mql.matches
    mql.addEventListener('change', onChange)
  })

  onBeforeUnmount(() => {
    if (mql) mql.removeEventListener('change', onChange)
  })

  return isMobile
}
