import * as echarts from 'echarts/core'
import { BarChart, LineChart, MapChart, PieChart } from 'echarts/charts'
import {
  GeoComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
  VisualMapComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

/**
 * ECharts 按需引入（T15 看板）
 *
 * 只注册本项目真正用到的图表与组件（柱/折线/饼/地图 + geo/visualMap/tooltip/legend/title/grid），
 * 而不是 `import 'echarts'` 整包 —— 整包 min 约 1MB，按需后能省掉大半。
 * 新增图表类型时记得在这里补注册，否则运行时会报「未注册的图表类型」。
 */
echarts.use([
  BarChart,
  LineChart,
  MapChart,
  PieChart,
  GeoComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
  VisualMapComponent,
  CanvasRenderer
])

const CHINA_MAP_NAME = 'china'

let chinaMapPromise = null

/**
 * 加载并注册中国地图（**含台湾省 / 香港特别行政区 / 澳门特别行政区 + 九段线**）。
 *
 * <p>地图数据放在 `public/map/china.json`，**同源静态加载**：① 不进 JS 包，只有看板页会拉；
 * ② 浏览器缓存；③ 断网/内网环境也能用（答辩现场不依赖任何在线地图服务，也就不需要 key）。
 *
 * <p>数据来源：国内公开的省级行政区划矢量数据（阿里云 DataV.GeoAtlas 的 100000_full），
 * 34 个省级区划，未包含任何境外地图服务或在线瓦片底图。
 *
 * @returns {Promise<boolean>} 是否注册成功
 */
export function ensureChinaMap() {
  if (!chinaMapPromise) {
    chinaMapPromise = fetch('/map/china.json')
      .then((response) => {
        if (!response.ok) {
          throw new Error(`地图数据加载失败：HTTP ${response.status}`)
        }
        return response.json()
      })
      .then((geoJson) => {
        echarts.registerMap(CHINA_MAP_NAME, geoJson)
        return true
      })
      .catch((error) => {
        // 失败不抛给调用方：看板其余图表照常展示，只有地图显示空态
        console.error('[osc] 中国地图注册失败，地区分布图将不可用：', error)
        chinaMapPromise = null
        return false
      })
  }
  return chinaMapPromise
}

export { CHINA_MAP_NAME }
export default echarts
