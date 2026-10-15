import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { STORAGE_KEY } from '@/constants/app'
import * as authApi from '@/api/auth'
import * as userApi from '@/api/user'

function readCachedProfile() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY.PROFILE) || 'null')
  } catch {
    return null
  }
}

/**
 * 当前账号 + 系统初始化状态（单账号系统，只有一个账号）。
 *
 * ⚠️ 与 OSC 的差异（PRD §2.2 / 清单 D13、D14）：
 *   - **没有 `needChangePassword`** —— 强制改密已整删
 *   - **没有角色 / 能力判断** —— `constants/roles.js` 已整删
 *   - profile 只有 `{ id, phone, status, createdAt }`
 */
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(STORAGE_KEY.TOKEN) || '')
  const profile = ref(readCachedProfile())

  /** 系统是否已初始化；null 表示还没查过 */
  const initialized = ref(null)
  let initStatusPromise = null

  const isLoggedIn = computed(() => !!token.value)

  function setToken(value) {
    token.value = value || ''
    if (token.value) {
      localStorage.setItem(STORAGE_KEY.TOKEN, token.value)
    } else {
      localStorage.removeItem(STORAGE_KEY.TOKEN)
    }
  }

  function setProfile(value) {
    profile.value = value || null
    if (profile.value) {
      localStorage.setItem(STORAGE_KEY.PROFILE, JSON.stringify(profile.value))
    } else {
      localStorage.removeItem(STORAGE_KEY.PROFILE)
    }
  }

  function clear() {
    setToken('')
    setProfile(null)
  }

  /** 查询初始化状态（整个会话只真正请求一次） */
  async function fetchInitStatus() {
    if (initialized.value !== null) {
      return initialized.value
    }
    if (!initStatusPromise) {
      initStatusPromise = authApi
        .getInitStatus()
        .then((data) => {
          initialized.value = !!data?.initialized
          return initialized.value
        })
        .catch((error) => {
          initStatusPromise = null
          throw error
        })
    }
    return initStatusPromise
  }

  function markInitialized() {
    initialized.value = true
  }

  async function login(payload) {
    const data = await authApi.login(payload)
    setToken(data.tokenValue)
    setProfile(data.user)
    return data
  }

  async function fetchCurrent() {
    const data = await userApi.getCurrentUser()
    setProfile(data)
    return data
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // 忽略：本地登录态照样清掉
    }
    clear()
  }

  return {
    token,
    profile,
    initialized,
    isLoggedIn,
    setToken,
    setProfile,
    clear,
    fetchInitStatus,
    markInitialized,
    login,
    fetchCurrent,
    logout
  }
})
