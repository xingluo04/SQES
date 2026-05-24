<template>
  <div class="layout">
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-brand" @click="router.push('/dashboard')">
        <span class="brand-icon">素质</span>
        <span v-show="!isCollapsed" class="brand-text">评价系统</span>
      </div>

      <el-menu
        :default-active="route.path"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
      >
        <el-menu-item index="/dashboard">
          <span class="menu-icon">📊</span>
          <template #title>数据看板</template>
        </el-menu-item>

        <el-menu-item v-if="isAdmin" index="/user">
          <span class="menu-icon">👤</span>
          <template #title>用户管理</template>
        </el-menu-item>

        <el-menu-item v-if="isAdmin || isTeacher" index="/student">
          <span class="menu-icon">🎓</span>
          <template #title>学生管理</template>
        </el-menu-item>

        <el-menu-item v-if="isAdmin || isTeacher" index="/class">
          <span class="menu-icon">🏫</span>
          <template #title>班级管理</template>
        </el-menu-item>

        <el-sub-menu v-if="isAdmin || isTeacher" index="evaluation">
          <template #title>
            <span class="menu-icon">📝</span>
            <span>评价管理</span>
          </template>
          <el-menu-item index="/evaluation/moral">德育评价</el-menu-item>
          <el-menu-item index="/evaluation/academic">智育评价</el-menu-item>
          <el-menu-item index="/evaluation/physical">体育评价</el-menu-item>
          <el-menu-item index="/evaluation/art">美育评价</el-menu-item>
          <el-menu-item index="/evaluation/practice">劳动评价</el-menu-item>
          <el-menu-item index="/evaluation/comprehensive">综合评价</el-menu-item>
          <el-menu-item v-if="isAdmin || isTeacher" index="/evaluation/import">数据导入</el-menu-item>
        </el-sub-menu>

        <el-menu-item v-if="isAdmin || isTeacher" index="/cluster">
          <span class="menu-icon">🔬</span>
          <template #title>聚类分析</template>
        </el-menu-item>

        <el-menu-item v-if="isStudent" index="/evaluation/my">
          <span class="menu-icon">📝</span>
          <template #title>我的评价</template>
        </el-menu-item>

        <el-menu-item v-if="isParent" index="/evaluation/child">
          <span class="menu-icon">👶</span>
          <template #title>子女评价</template>
        </el-menu-item>

        <el-menu-item v-if="isAdmin" index="/notice">
          <span class="menu-icon">📢</span>
          <template #title>通知管理</template>
        </el-menu-item>

        <el-menu-item v-if="isTeacher || isStudent || isParent" index="/notice-list">
          <span class="menu-icon">📢</span>
          <template #title>通知公告</template>
        </el-menu-item>

        <el-menu-item v-if="isAdmin" index="/log">
          <span class="menu-icon">📋</span>
          <template #title>操作日志</template>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-toggle" @click="isCollapsed = !isCollapsed">
        <span>{{ isCollapsed ? '»' : '«' }}</span>
      </div>
    </aside>

    <div class="main-wrapper">
      <header class="topbar">
        <div class="topbar-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="topbar-right">
          <span class="user-name">{{ userStore.userInfo?.realName || '用户' }}</span>
          <el-dropdown @command="handleCommand">
            <span class="user-role">
              {{ roleLabel }}
              <span class="dropdown-arrow">▼</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserInfo } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapsed = ref(false)

const isAdmin = computed(() => userStore.role === 'admin')
const isTeacher = computed(() => userStore.role === 'teacher')
const isStudent = computed(() => userStore.role === 'student')
const isParent = computed(() => userStore.role === 'parent')

const roleLabel = computed(() => {
  const map: Record<string, string> = {
    admin: '管理员',
    teacher: '教师',
    student: '学生',
    parent: '家长',
  }
  return map[userStore.role] || '用户'
})

onMounted(async () => {
  if (!userStore.userInfo) {
    try {
      const res = await getUserInfo() as unknown as { data: any }
      userStore.setUserInfo(res.data)
    } catch {
      // handled by interceptor
    }
  }
})

function handleCommand(cmd: string) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  display: flex;
  flex-direction: column;
  width: 240px;
  background: var(--color-bg-sidebar);
  border-right: 1px solid var(--color-border);
  transition: width 0.25s ease;
  overflow: hidden;

  &.collapsed {
    width: 64px;
  }
}

.sidebar-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 56px;
  padding: 0 16px;
  cursor: pointer;
  border-bottom: 1px solid var(--color-border);
  gap: 8px;

  .brand-icon {
    font-family: var(--font-display);
    font-size: 20px;
    font-weight: 700;
    color: var(--color-accent-jade);
    white-space: nowrap;
  }

  .brand-text {
    font-family: var(--font-display);
    font-size: 18px;
    font-weight: 700;
    color: var(--color-text-primary);
    white-space: nowrap;
  }
}

.el-menu {
  flex: 1;
  border-right: none;
  padding: 8px 0;
  overflow-y: auto;

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 44px;
    line-height: 44px;
    margin: 2px 8px;
    border-radius: 8px;

    &:hover {
      background: var(--color-bg-hover);
    }

    &.is-active {
      background: rgba(77, 182, 172, 0.12);
      color: var(--color-accent-jade);
    }
  }

  :deep(.el-sub-menu .el-menu-item) {
    padding-left: 56px !important;
    height: 40px;
    line-height: 40px;
    font-size: 13px;
  }
}

.menu-icon {
  margin-right: 8px;
  font-size: 16px;
}

.sidebar-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  border-top: 1px solid var(--color-border);
  cursor: pointer;
  color: var(--color-text-secondary);
  font-size: 14px;
  transition: background 0.2s;

  &:hover {
    background: var(--color-bg-hover);
    color: var(--color-accent-jade);
  }
}

.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 24px;
  background: var(--color-bg-secondary);
  border-bottom: 1px solid var(--color-border);
  flex-shrink: 0;
}

.topbar-left {
  :deep(.el-breadcrumb__inner) {
    color: var(--color-text-secondary);
  }
  :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
    color: var(--color-text-primary);
  }
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;

  .user-name {
    font-size: 14px;
    color: var(--color-text-primary);
  }

  .user-role {
    font-size: 13px;
    color: var(--color-accent-jade);
    cursor: pointer;

    .dropdown-arrow {
      margin-left: 4px;
      font-size: 10px;
      opacity: 0.6;
    }
  }
}

.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: var(--color-bg-primary);
}
</style>
