<template>
  <div class="profile-view">
    <div class="page-header">
      <h2 class="page-title">个人中心</h2>
    </div>

    <div class="profile-grid">
      <section class="profile-panel">
        <div class="panel-header">
          <div
            class="avatar-preview"
            role="button"
            tabindex="0"
            @click="triggerAvatarUpload"
            @keydown.enter.prevent="triggerAvatarUpload"
            @keydown.space.prevent="triggerAvatarUpload"
          >
            <el-avatar :size="64" :src="avatarSrc">
              {{ avatarText }}
            </el-avatar>
            <span class="avatar-edit">✎</span>
            <input
              ref="avatarInputRef"
              type="file"
              accept="image/*"
              class="avatar-input"
              @change="handleAvatarChange"
            />
          </div>
          <div>
            <h3>基本信息</h3>
            <p>{{ roleLabel }}</p>
          </div>
        </div>

        <el-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="profileRules"
          label-width="86px"
          class="profile-form"
        >
          <el-form-item label="用户名">
            <el-input :model-value="userStore.userInfo?.username || ''" disabled />
          </el-form-item>
          <el-form-item label="姓名" prop="realName">
            <el-input v-model="profileForm.realName" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="profileForm.gender">
              <el-radio :value="1">男</el-radio>
              <el-radio :value="0">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="profileSubmitting" @click="handleProfileSubmit">
              保存资料
            </el-button>
          </el-form-item>
        </el-form>
      </section>

      <section class="profile-panel">
        <div class="panel-header compact">
          <div>
            <h3>修改密码</h3>
            <p>修改成功后需要重新登录</p>
          </div>
        </div>

        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="96px"
          class="profile-form"
        >
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入旧密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button type="warning" :loading="passwordSubmitting" @click="handlePasswordSubmit">
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getUserInfo } from '@/api/auth'
import { uploadFile } from '@/api/file'
import { updatePassword, updateProfile } from '@/api/user'
import { useUserStore } from '@/stores/user'
import type { PasswordUpdateRequest, ProfileUpdateRequest, User } from '@/types/user'

const router = useRouter()
const userStore = useUserStore()

const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const avatarInputRef = ref<HTMLInputElement>()
const profileSubmitting = ref(false)
const passwordSubmitting = ref(false)

const profileForm = reactive<ProfileUpdateRequest>({
  realName: '',
  gender: 1,
  phone: '',
  email: '',
  avatar: '',
})

const passwordForm = reactive<PasswordUpdateRequest>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const profileRules: FormRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少 6 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const roleLabel = computed(() => {
  const map: Record<string, string> = {
    admin: '管理员',
    teacher: '教师',
    student: '学生',
    parent: '家长',
  }
  return map[userStore.role] || '用户'
})

const avatarText = computed(() => {
  return (profileForm.realName || userStore.userInfo?.username || '用户').slice(0, 1)
})

const avatarSrc = computed(() => {
  if (!profileForm.avatar) return undefined
  if (/^https?:\/\//i.test(profileForm.avatar)) return profileForm.avatar
  if (import.meta.env.DEV && profileForm.avatar.startsWith('/uploads/')) {
    return `http://localhost:8089${profileForm.avatar}`
  }
  return profileForm.avatar
})

function fillProfileForm(user: User) {
  profileForm.realName = user.realName || ''
  profileForm.gender = user.gender ?? 1
  profileForm.phone = user.phone || ''
  profileForm.email = user.email || ''
  profileForm.avatar = user.avatar || ''
}

async function refreshUserInfo() {
  const res = await getUserInfo() as unknown as { data: User }
  userStore.setUserInfo(res.data)
  fillProfileForm(res.data)
}

async function handleProfileSubmit() {
  const valid = await profileFormRef.value?.validate().catch(() => false)
  if (!valid) return

  profileSubmitting.value = true
  try {
    await updateProfile(profileForm)
    await refreshUserInfo()
    ElMessage.success('个人资料已更新')
  } finally {
    profileSubmitting.value = false
  }
}

function triggerAvatarUpload() {
  avatarInputRef.value?.click()
}

async function handleAvatarChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    input.value = ''
    return
  }

  profileSubmitting.value = true
  try {
    const res = await uploadFile(file) as unknown as { data: string }
    profileForm.avatar = res.data
    await updateProfile(profileForm)
    await refreshUserInfo()
    ElMessage.success('头像已上传')
  } finally {
    profileSubmitting.value = false
    input.value = ''
  }
}

async function handlePasswordSubmit() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  passwordSubmitting.value = true
  try {
    await updatePassword(passwordForm)
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/login')
  } finally {
    passwordSubmitting.value = false
  }
}

onMounted(async () => {
  if (userStore.userInfo) {
    fillProfileForm(userStore.userInfo)
  }
  await refreshUserInfo()
})
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  .page-title {
    font-family: var(--font-display);
    font-size: 22px;
    color: var(--color-text-primary);
  }
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 420px);
  gap: 20px;
  align-items: start;
}

.profile-panel {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 20px;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--color-border);

  &.compact {
    min-height: 82px;
  }

  h3 {
    margin: 0 0 6px;
    font-size: 18px;
    color: var(--color-text-primary);
  }

  p {
    margin: 0;
    font-size: 13px;
    color: var(--color-text-secondary);
  }
}

.avatar-preview {
  position: relative;
  flex: 0 0 auto;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  cursor: pointer;

  &:focus-visible {
    outline: 2px solid var(--color-accent-jade);
    outline-offset: 4px;
  }
}

.avatar-edit {
  position: absolute;
  right: -2px;
  bottom: -2px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--color-accent-jade);
  color: #fff;
  border: 2px solid var(--color-bg-secondary);
  font-size: 12px;
  line-height: 1;
}

.avatar-input {
  display: none;
}

.profile-form {
  max-width: 560px;
}

@media (max-width: 960px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .profile-form {
    max-width: none;
  }
}
</style>
