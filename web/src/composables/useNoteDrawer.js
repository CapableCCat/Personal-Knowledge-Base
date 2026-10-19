import { ref } from 'vue'

/**
 * 笔记抽屉的**模块级单例状态**。
 *
 * <p>为什么放在 composable 而不是某个页面里：桌面端「打开笔记」用的是抽屉，
 * 而唤起抽屉的入口不止一个 —— 笔记列表（T6）与首页（T9）都要用。
 * 状态放模块级，页面只需 `open(id)`，抽屉外壳挂在 `AppShell` 上即可。
 *
 * <p>`version`：数据变更版本号。抽屉里保存 / 删除后 `bump()` 一下，
 * 列表页 watch 它来刷新 —— 避免父子组件之间为了「刷新一下」再传一堆事件。
 */
const visible = ref(false)
const noteId = ref(null)
const version = ref(0)

export function useNoteDrawer() {
  /** 打开抽屉；传 null 表示新建 */
  function open(id = null) {
    noteId.value = id
    visible.value = true
  }

  function close() {
    visible.value = false
    noteId.value = null
  }

  /** 数据有变更（保存 / 删除）→ 让列表重新拉一次 */
  function bump() {
    version.value += 1
  }

  return { visible, noteId, version, open, close, bump }
}
