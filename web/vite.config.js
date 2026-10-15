import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver, VantResolver } from 'unplugin-vue-components/resolvers'

// 后端 origin 只用于本地开发代理；线上由 Nginx 同源反代 /api，无需改前端代码
const BACKEND_ORIGIN = 'http://127.0.0.1:8080'

export default defineConfig({
  plugins: [
    vue(),
    // Element Plus / Vant 的函数式 API（ElMessage、showToast 等）按需自动引入
    AutoImport({
      resolvers: [ElementPlusResolver({ importStyle: false }), VantResolver({ importStyle: false })],
      dts: false
    }),
    // 组件按需引入：Element Plus、Vant，以及 src/components 下的本地组件
    // 样式统一在 main.js 引整包 CSS（见该文件注释），故这里关闭按组件引样式
    Components({
      dirs: ['src/components'],
      resolvers: [ElementPlusResolver({ importStyle: false }), VantResolver({ importStyle: false })],
      dts: false
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    host: '127.0.0.1',
    open: false,
    proxy: {
      // 后端接口不带 /api 前缀（见《开发任务点清单》§6 D12），故代理时剥掉
      '/api': {
        target: BACKEND_ORIGIN,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  build: {
    outDir: 'dist',
    chunkSizeWarningLimit: 1200
  }
})
