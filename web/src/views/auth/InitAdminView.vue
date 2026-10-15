<script setup>
/**
 * 引导页（P-011）：首启创建**唯一账号**
 *
 * 仅当 user 表为空时可进入；创建成功后跳登录页。
 *
 * ⚠️ 与 OSC 的差异（PRD §2.2 / 清单 D14）：
 *   - **无姓名字段**（单账号不需要；后端 `InitAdminRequest` 也已删除该字段）
 *   - **不触发强制改密** —— 密码就是本人当场设置的
 *   - 控件改用 Element Plus（与登录页一致）
 */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { initAdmin } from '@/api/auth'
import { ROUTE_PATH } from '@/constants/app'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const formRef = ref(null)
const form = reactive({ phone: '', password: '', confirmPassword: '' })

const rules = {
  phone: [
    { required: true, message: '请填写手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请填写密码', trigger: 'blur' },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d)\S{8,20}$/,
      message: '8~20 位，须同时包含字母和数字且不含空格',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

async function onSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await initAdmin({ phone: form.phone, password: form.password })
    ElMessage.success('账号创建成功，请登录')
    userStore.markInitialized()
    await router.replace(ROUTE_PATH.LOGIN)
  } catch {
    // 提示由 axios 拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="init">
    <div class="init__card">
      <div class="init__brand">
        <h1 class="init__title">初始化 PKB</h1>
        <p class="init__sub">第一次使用，请创建唯一的账号</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="onSubmit"
      >
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="将作为登录账号" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="8~20 位，须同时含字母和数字"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入密码"
            @keyup.enter="onSubmit"
          />
        </el-form-item>

        <el-button
          type="primary"
          class="init__submit"
          :loading="loading"
          native-type="submit"
          @click="onSubmit"
        >
          创建账号
        </el-button>
      </el-form>

      <p class="init__tip">
        该接口只在系统未初始化时可用；已建账号后会被后端拒绝，避免重复创建。
      </p>
    </div>
  </div>
</template>

<style scoped>
.init {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: var(--space-6) var(--space-4);
  background-image: var(--login-bg-image), var(--login-bg-gradient);
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
}

.init__card {
  width: 100%;
  max-width: 380px;
  padding: var(--space-6);
  background: var(--bg-surface);
  border-radius: var(--radius-surface);
  box-shadow: var(--shadow-popup);
}

.init__brand {
  margin-bottom: var(--space-4);
  text-align: center;
}

.init__title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.init__sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.init__submit {
  width: 100%;
  margin-top: var(--space-2);
}

.init__tip {
  margin: var(--space-4) 0 0;
  font-size: 12px;
  line-height: 1.8;
  color: var(--text-secondary);
}

@media (max-width: 480px) {
  .init {
    background-attachment: scroll;
  }
}
</style>
