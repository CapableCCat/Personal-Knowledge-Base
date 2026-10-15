<script setup>
/**
 * 登录页（P-011）：手机号 + 密码 + 算术图形验证码
 *
 * 全屏背景 + 卡片居中，靠路由 `meta.bare` 去掉页头页脚与限宽。
 * 背景值集中在 `styles/index.scss` 的 `--login-bg-image` / `--login-bg-gradient`：
 * 素材到位后只改那一行就能换成真背景图，本文件不用动。
 *
 * ⚠️ 与 OSC 的差异：
 *   - **无强制改密跳转**（单账号密码由本人设置，见清单 D14）
 *   - **无报名页 / 查询页互链**（PKB 没有公开端）
 *   - **无联通性检查卡**（`HealthCheckCard` 已随业务删除，见清单 D23）
 *   - 登录控件改用 Element Plus（单账号自用，桌面与手机都要顺）
 */
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha } from '@/api/auth'
import { ROUTE_PATH } from '@/constants/app'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const captchaImage = ref('')
const formRef = ref(null)
const form = reactive({ phone: '', password: '', captchaKey: '', captchaCode: '' })

const rules = {
  phone: [
    { required: true, message: '请填写手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请填写密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请填写验证码', trigger: 'blur' }]
}

/** 刷新验证码（点击图片也可刷新） */
async function refreshCaptcha() {
  form.captchaCode = ''
  try {
    const data = await getCaptcha()
    form.captchaKey = data.captchaKey
    captchaImage.value = data.captchaImage
  } catch {
    // 失败提示由 axios 拦截器统一处理
  }
}

async function onSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({ ...form })
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ROUTE_PATH.HOME
    await router.replace(redirect)
  } catch {
    // 验证码是一次性的，失败后必须换一张
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(refreshCaptcha)
</script>

<template>
  <div class="login">
    <div class="login__card">
      <div class="login__brand">
        <h1 class="login__title">PKB 个人知识库</h1>
        <p class="login__sub">Personal Knowledge Base · V0.1</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="login__form"
        @submit.prevent="onSubmit"
      >
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="请输入 11 位手机号" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>

        <el-form-item label="验证码" prop="captchaCode">
          <div class="login__captcha-row">
            <el-input
              v-model="form.captchaCode"
              maxlength="6"
              placeholder="请输入计算结果"
              @keyup.enter="onSubmit"
            />
            <div class="login__captcha-box" title="看不清？点击刷新" @click="refreshCaptcha">
              <img v-if="captchaImage" class="login__captcha" :src="captchaImage" alt="图形验证码" />
              <span v-else class="login__captcha-loading">加载中…</span>
            </div>
          </div>
        </el-form-item>

        <el-button
          type="primary"
          class="login__submit"
          :loading="loading"
          native-type="submit"
          @click="onSubmit"
        >
          登录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
/*
 * 全屏背景 + 卡片居中。
 * 背景值来自 styles/index.scss 的 token：--login-bg-image（素材到位后换这张图）/ --login-bg-gradient。
 * 两层顺序不能反：图在上、渐变在下 —— 这样图没配（none）时自然只剩渐变。
 */
.login {
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

.login__card {
  width: 100%;
  max-width: 380px;
  padding: var(--space-6);
  background: var(--bg-surface);
  border-radius: var(--radius-surface);
  box-shadow: var(--shadow-popup);
}

.login__brand {
  margin-bottom: var(--space-4);
  text-align: center;
}

.login__title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.login__sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.login__form {
  margin-top: var(--space-2);
}

.login__captcha-row {
  display: flex;
  gap: 8px;
  align-items: center;
  width: 100%;
}

/* 验证码：固定宽度容器，图片按高度缩放，避免撑破输入行 */
.login__captcha-box {
  display: flex;
  flex: none;
  align-items: center;
  justify-content: center;
  width: 96px;
  height: 32px;
  overflow: hidden;
  cursor: pointer;
}

.login__captcha {
  height: 32px;
  max-width: 96px;
  border-radius: 4px;
}

.login__captcha-loading {
  font-size: 12px;
  color: var(--text-secondary);
}

.login__submit {
  width: 100%;
  margin-top: var(--space-2);
}

@media (max-width: 480px) {
  .login {
    /* 手机上不要固定背景，避免部分浏览器滚动时的重绘抖动 */
    background-attachment: scroll;
  }
}
</style>
