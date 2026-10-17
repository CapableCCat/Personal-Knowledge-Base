import { computed } from 'vue'
import { useRouter } from 'vue-router'

/**
 * 菜单**从路由表生成**（单一出处：加一个页面 = 只改路由表一处）
 *
 * 每条内部路由在 `meta.menu` 里声明自己：
 *   `{ label, order, group, icon }`
 *     - `label` 菜单文案（缺省用 `meta.title`）
 *     - `order` **组内**排序（小的在前）
 *     - `group` 所属分组 key（见 `MENU_GROUPS`；缺省归入「其他」）
 *     - `icon`  Element Plus 图标组件名（字符串，由 `AppShell` 统一取）
 *
 * ⚠️ 与 OSC 的关键差异（PRD §2.2）：**没有 `capability` 字段**。
 *    PKB 是单账号系统，不存在「谁能看到哪一项」，登录即可见全部菜单；
 *    OSC 的 `constants/roles.js` 已整删，不再做任何能力过滤。
 *
 * ⚠️ `icon` 存的是**组件名字符串**而不是组件本身 —— 路由表是纯数据，不该 import 组件；
 *    由 `AppShell` 统一从图标库取，取不到时降级为不显示图标（不报错）。
 */

/** 分组定义（`order` 决定组的先后；组内各项按 `meta.menu.order` 排） */
export const MENU_GROUPS = [{ key: 'main', label: '知识库', order: 10 }]

/** 未声明 group 的项落到这里（不静默丢弃，方便发现漏配） */
const FALLBACK_GROUP = { key: 'other', label: '其他', order: 999 }

/**
 * ⚠️ **没有 `tabbarItems`**（T4 移除，方案 A）。
 *
 * <p>外壳不再渲染底部 tabbar —— PRD P-010 冻结的「手机底部 3 Tab（最近 / 待整理 / 项目）」
 * 是**首页的内容 Tab**（原文「切 Tab 只换内容区」= 内容级、非路由级），
 * 由 **T9 的 `HomeView` 自建**，不复用菜单数据。
 *
 * <p>手机端的外壳导航走页头 ☰ 抽屉（`AppShell` 里直接用 `groups` 拍平）。
 */

export function useRouteMenu() {
  const router = useRouter()

  /** 全部菜单项（**扁平**，按「组 order → 组内 order」排序） */
  const items = computed(() =>
    router
      .getRoutes()
      .filter((record) => record.meta && record.meta.menu)
      .map((record) => {
        const meta = record.meta.menu
        const group = MENU_GROUPS.find((g) => g.key === meta.group) || FALLBACK_GROUP
        return {
          path: record.path,
          label: meta.label || record.meta.title || record.path,
          icon: meta.icon || '',
          order: meta.order ?? 999,
          groupKey: group.key,
          groupLabel: group.label,
          groupOrder: group.order
        }
      })
      .sort((a, b) =>
        a.groupOrder !== b.groupOrder ? a.groupOrder - b.groupOrder : a.order - b.order
      )
  )

  /** 分组结构（**空组不渲染**）—— 桌面左侧菜单与移动端抽屉共用这一份 */
  const groups = computed(() => {
    const map = new Map()
    for (const item of items.value) {
      if (!map.has(item.groupKey)) {
        map.set(item.groupKey, {
          key: item.groupKey,
          label: item.groupLabel,
          order: item.groupOrder,
          items: []
        })
      }
      map.get(item.groupKey).items.push({
        path: item.path,
        label: item.label,
        icon: item.icon
      })
    }
    return [...map.values()].sort((a, b) => a.order - b.order)
  })

  return { groups, items }
}
