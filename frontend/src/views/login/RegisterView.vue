<template>
  <AuthLayout mode="register">
    <template #form>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="register-form"
        label-position="top"
        @keyup.enter="handleRegister"
      >
        <div class="form-item-anim" style="--delay: 0.3s">
          <div class="form-row">
            <el-form-item label="用户名" prop="username" class="form-item">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                size="large"
              />
            </el-form-item>

            <el-form-item label="姓名" prop="realName" class="form-item">
              <el-input
                v-model="form.realName"
                placeholder="请输入真实姓名"
                size="large"
              />
            </el-form-item>
          </div>
        </div>

        <div class="form-item-anim" style="--delay: 0.42s">
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              size="large"
              show-password
            />
          </el-form-item>
        </div>

        <div class="form-item-anim" style="--delay: 0.54s">
          <el-form-item label="角色" prop="role">
            <el-radio-group v-model="form.role" class="role-group">
              <el-radio value="student">
                <span class="role-icon">🎓</span>
                <span>学生</span>
              </el-radio>
              <el-radio value="teacher">
                <span class="role-icon">👨‍🏫</span>
                <span>教师</span>
              </el-radio>
              <el-radio value="parent">
                <span class="role-icon">👪</span>
                <span>家长</span>
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </div>

        <div class="form-item-anim" style="--delay: 0.66s">
          <div class="form-row">
            <el-form-item label="手机号" class="form-item">
              <el-input
                v-model="form.phone"
                placeholder="选填"
                size="large"
              />
            </el-form-item>

            <el-form-item label="邮箱" class="form-item">
              <el-input
                v-model="form.email"
                placeholder="选填"
                size="large"
              />
            </el-form-item>
          </div>
        </div>

        <div class="form-item-anim" style="--delay: 0.78s">
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="register-btn"
              :loading="loading"
              @click="handleRegister"
            >
              <span class="btn-text">注 册</span>
              <span class="btn-glow" />
            </el-button>
          </el-form-item>
        </div>
      </el-form>

      <div class="register-footer form-item-anim" style="--delay: 0.9s">
        <span class="footer-text">已有账号？</span>
        <el-link type="primary" :underline="false" @click="router.push('/login')">
          返回登录
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
import { register } from '@/api/auth'
import AuthLayout from '@/components/AuthLayout.vue'
import type { RegisterRequest } from '@/types/user'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<RegisterRequest>({
  username: '',
  password: '',
  realName: '',
  role: '',
  phone: '',
  email: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push({ path: '/login', query: { username: form.username } })
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
.register-form {
  :deep(.el-form-item__label) {
    color: var(--color-text-secondary);
    font-size: 13px;
    padding-bottom: 4px;
  }

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

  :deep(.el-input__wrapper.is-focus .el-input__prefix) {
    color: var(--color-accent-jade);
  }

  :deep(.el-radio__input.is-checked .el-radio__inner) {
    background-color: var(--color-accent-jade);
    border-color: var(--color-accent-jade);
  }

  :deep(.el-radio__input.is-checked + .el-radio__label) {
    color: var(--color-accent-jade);
  }

  :deep(.el-radio__inner) {
    background: var(--color-bg-primary);
    border-color: var(--color-border);
  }
}

.form-row {
  display: flex;
  gap: 16px;

  .form-item {
    flex: 1;
  }
}

.role-group {
  display: flex;
  gap: 0;
  width: 100%;

  :deep(.el-radio) {
    flex: 1;
    margin-right: 0;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(27, 40, 56, 0.5);
    border: 1px solid rgba(42, 58, 74, 0.6);
    border-radius: 10px;
    transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
    padding: 0 12px;

    &:hover {
      border-color: rgba(77, 182, 172, 0.4);
      background: rgba(27, 40, 56, 0.7);
      transform: translateY(-1px);
    }

    &.is-checked {
      background: rgba(77, 182, 172, 0.12);
      border-color: var(--color-accent-jade);
      box-shadow: 0 0 12px rgba(77, 182, 172, 0.15);
      transform: translateY(-1px);
    }

    & + .el-radio {
      margin-left: 12px;
    }
  }

  :deep(.el-radio__label) {
    display: flex;
    align-items: center;
    gap: 6px;
    color: var(--color-text-primary);
    font-size: 14px;
    padding-left: 6px;
  }

  :deep(.el-radio__input) {
    display: none;
  }
}

.role-icon {
  font-size: 16px;
}

/* ── 按钮 ── */
.register-btn {
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

.register-footer {
  text-align: center;
  margin-top: 20px;

  .footer-text {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin-right: 4px;
  }
}
</style>
