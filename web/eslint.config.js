import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import configPrettier from 'eslint-config-prettier'
import globals from 'globals'

// eslint-plugin-vue 10 的 flat 预设命名在不同小版本间有差异，这里做兼容取值
const vueEssential = pluginVue.configs['flat/essential'] ?? pluginVue.configs.essential ?? []

export default [
  { ignores: ['dist/**', 'node_modules/**', '*.min.js'] },
  js.configs.recommended,
  ...vueEssential,
  configPrettier,
  {
    files: ['**/*.{js,vue}'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: {
        ...globals.browser,
        ...globals.node
      }
    },
    rules: {
      // ElMessage / showToast 等由 unplugin-auto-import 按需注入，ESLint 无法感知
      'no-undef': 'off',
      // App.vue、main.js 等单文件场景不强制多词命名
      'vue/multi-word-component-names': 'off',
      'no-unused-vars': ['warn', { argsIgnorePattern: '^_' }]
    }
  }
]
