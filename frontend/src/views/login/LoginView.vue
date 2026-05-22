<template>
  <AuthLayout mode="login">
    <template #form>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <div class="form-item-anim" style="--delay: 0.3s">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              prefix-icon="User"
            />
          </el-form-item>
        </div>

        <div class="form-item-anim" style="--delay: 0.45s">
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
        </div>

        <div class="form-item-anim" style="--delay: 0.6s">
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              <span class="btn-text">登 录</span>
              <span class="btn-glow" />
            </el-button>
          </el-form-item>
        </div>
      </el-form>

      <div class="login-footer form-item-anim" style="--delay: 0.75s">
        <span class="footer-text">还没有账号？</span>
        <el-link type="primary" :underline="false" @click="router.push('/register')">
          注册新用户
        </el-link>
      </div>
    </template>
  </AuthLayout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import AuthLayout from '@/components/AuthLayout.vue'
import type { LoginRequest } from '@/types/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<LoginRequest>({
  username: (router.currentRoute.value.query.username as string) || '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login(form) as unknown as { code: number; data: { token: string; userId: number; username: string; realName: string; role: string; avatar: string | null }; message: string }
    userStore.setToken(res.data.token)
    userStore.setUserInfo({
      id: res.data.userId,
      username: res.data.username,
      realName: res.data.realName,
      role: res.data.role as any,
      avatar: res.data.avatar,
      phone: null,
      email: null,
      gender: null,
      status: 1,
      createTime: '',
      updateTime: '',
    })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {
    // error already handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
/* ── 入场动画 ── */
.form-item-anim {
  opacity: 0;
  transform: translateY(24px);
  animation: fadeSlideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) var(--delay, 0s) forwards;
}

@keyframes fadeSlideUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ── 输入框 ── */
.login-form {
  :deep(.el-input__wrapper) {
    background: rgba(27, 40, 56, 0.5);
    border: 1px solid rgba(42, 58, 74, 0.6);
    border-radius: 10px;
    box-shadow: none;
    transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
    padding: 4px 12px;

    &:hover {
      border-color: rgba(77, 182, 172, 0.4);
      background: rgba(27, 40, 56, 0.7);
      box-shadow: 0 0 0 1px rgba(77, 182, 172, 0.1);
    }

    &.is-focus {
      border-color: var(--color-accent-jade);
      background: rgba(27, 40, 56, 0.8);
      box-shadow:
        0 0 0 3px rgba(77, 182, 172, 0.15),
        0 0 20px rgba(77, 182, 172, 0.08);
    }
  }

  :deep(.el-input__prefix) {
    color: var(--color-text-secondary);
    transition: color 0.3s ease;
  }

  :deep(.el-input__wrapper.is-focus .el-input__prefix) {
    color: var(--color-accent-jade);
  }

  :deep(.el-input__inner) {
    &::placeholder {
      transition: color 0.3s ease;
    }
  }

  :deep(.el-input__wrapper.is-focus .el-input__inner::placeholder) {
    color: rgba(77, 182, 172, 0.5);
  }
}

/* ── 按钮 ── */
.login-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-family: var(--font-body);
  letter-spacing: 6px;
  background: linear-gradient(135deg, #4DB6AC 0%, #3A9D91 50%, #2E8B82 100%);
  color: #fff;
  position: relative;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);

  .btn-text {
    position: relative;
    z-index: 1;
  }

  .btn-glow {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
    border-radius: 50%;
    transform: translate(-50%, -50%);
    transition: width 0.5s ease, height 0.5s ease;
  }

  &:hover {
    background: linear-gradient(135deg, #5CC4BA 0%, #4DB6AC 50%, #3DA29A 100%);
    box-shadow:
      0 4px 24px rgba(77, 182, 172, 0.4),
      0 0 40px rgba(77, 182, 172, 0.15);
    transform: translateY(-2px);

    .btn-glow {
      width: 300px;
      height: 300px;
    }
  }

  &:active {
    transform: translateY(0) scale(0.98);
    box-shadow: 0 2px 12px rgba(77, 182, 172, 0.3);
  }
}

/* ── 底部链接 ── */
.login-footer {
  text-align: center;
  margin-top: 24px;

  .footer-text {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin-right: 4px;
  }
}
</style>
