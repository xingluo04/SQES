import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { public: true, noTransition: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/RegisterView.vue'),
    meta: { public: true, noTransition: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '数据看板' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/ProfileView.vue'),
        meta: { title: '个人中心' },
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/UserView.vue'),
        meta: { title: '用户管理', roles: ['admin'] },
      },
      {
        path: 'student',
        name: 'Student',
        component: () => import('@/views/student/StudentView.vue'),
        meta: { title: '学生管理', roles: ['admin', 'teacher'] },
      },
      {
        path: 'class',
        name: 'Class',
        component: () => import('@/views/class/ClassView.vue'),
        meta: { title: '班级管理', roles: ['admin', 'teacher'] },
      },
      {
        path: 'evaluation/moral',
        name: 'MoralEvaluation',
        component: () => import('@/views/evaluation/MoralView.vue'),
        meta: { title: '德育评价', roles: ['admin', 'teacher'] },
      },
      {
        path: 'evaluation/academic',
        name: 'AcademicEvaluation',
        component: () => import('@/views/evaluation/AcademicView.vue'),
        meta: { title: '智育评价', roles: ['admin', 'teacher'] },
      },
      {
        path: 'evaluation/physical',
        name: 'PhysicalEvaluation',
        component: () => import('@/views/evaluation/PhysicalView.vue'),
        meta: { title: '体育评价', roles: ['admin', 'teacher'] },
      },
      {
        path: 'evaluation/art',
        name: 'ArtEvaluation',
        component: () => import('@/views/evaluation/ArtView.vue'),
        meta: { title: '美育评价', roles: ['admin', 'teacher'] },
      },
      {
        path: 'evaluation/practice',
        name: 'PracticeEvaluation',
        component: () => import('@/views/evaluation/PracticeView.vue'),
        meta: { title: '劳动评价', roles: ['admin', 'teacher'] },
      },
      {
        path: 'evaluation/comprehensive',
        name: 'ComprehensiveEvaluation',
        component: () => import('@/views/evaluation/ComprehensiveView.vue'),
        meta: { title: '综合评价', roles: ['admin', 'teacher'] },
      },
      {
        path: 'evaluation/import',
        name: 'EvaluationImport',
        component: () => import('@/views/evaluation/ImportView.vue'),
        meta: { title: '数据导入', roles: ['admin', 'teacher'] },
      },
      {
        path: 'evaluation/my',
        name: 'MyEvaluation',
        component: () => import('@/views/evaluation/MyEvaluationView.vue'),
        meta: { title: '我的评价', roles: ['student'] },
      },
      {
        path: 'evaluation/child',
        name: 'ChildEvaluation',
        component: () => import('@/views/evaluation/ChildEvaluationView.vue'),
        meta: { title: '子女评价', roles: ['parent'] },
      },
      {
        path: 'cluster',
        name: 'Cluster',
        component: () => import('@/views/cluster/ClusterView.vue'),
        meta: { title: '聚类分析', roles: ['admin', 'teacher'] },
      },
      {
        path: 'notice',
        name: 'Notice',
        component: () => import('@/views/notice/NoticeView.vue'),
        meta: { title: '通知管理', roles: ['admin'] },
      },
      {
        path: 'notice-list',
        name: 'NoticeList',
        component: () => import('@/views/notice/NoticeListView.vue'),
        meta: { title: '通知公告', roles: ['student', 'parent', 'teacher'] },
      },
      {
        path: 'log',
        name: 'Log',
        component: () => import('@/views/log/LogView.vue'),
        meta: { title: '操作日志', roles: ['admin'] },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.public) {
    next()
  } else if (!token) {
    next('/login')
  } else {
    const roles = to.meta.roles as string[] | undefined
    if (roles?.length) {
      const userRole = localStorage.getItem('role') || ''
      if (!roles.includes(userRole)) {
        next('/dashboard')
        return
      }
    }
    next()
  }
})

export default router
