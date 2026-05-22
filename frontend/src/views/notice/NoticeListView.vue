<template>
  <div class="page-view">
    <div class="page-header">
      <h2 class="page-title">通知公告</h2>
    </div>

    <div class="notice-list" v-loading="loading">
      <div
        v-for="item in notices"
        :key="item.id"
        class="notice-card"
        @click="openDetail(item)"
      >
        <div class="notice-head">
          <el-tag :type="typeTag(item.type)" size="small">{{ typeLabel(item.type) }}</el-tag>
          <span class="notice-time">{{ item.createTime }}</span>
        </div>
        <h3 class="notice-title">{{ item.title }}</h3>
        <p class="notice-preview">{{ stripHtml(item.content).slice(0, 120) }}{{ stripHtml(item.content).length > 120 ? '...' : '' }}</p>
      </div>

      <el-empty v-if="!loading && !notices.length" description="暂无通知公告" />
    </div>

    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadNotices"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :title="currentNotice?.title"
      width="640px"
      destroy-on-close
    >
      <div class="detail-meta">
        <el-tag :type="typeTag(currentNotice?.type)" size="small">{{ typeLabel(currentNotice?.type) }}</el-tag>
        <span class="detail-time">{{ currentNotice?.createTime }}</span>
        <span class="detail-publisher">发布人：{{ currentNotice?.publisherName }}</span>
      </div>
      <div class="detail-content" v-html="currentNotice?.content" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getNoticePage, getNoticeDetail } from '@/api/notice'
import type { Notice } from '@/types/notice'

const loading = ref(false)
const notices = ref<Notice[]>([])
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)
const detailVisible = ref(false)
const currentNotice = ref<Notice | null>(null)

function typeLabel(type?: number) {
  const map: Record<number, string> = { 1: '通知', 2: '公告', 3: '系统' }
  return map[type || 1] || '通知'
}

function typeTag(type?: number) {
  const map: Record<number, string> = { 1: '', 2: 'success', 3: 'warning' }
  return (map[type || 1] || '') as any
}

function stripHtml(html: string) {
  return html?.replace(/<[^>]+>/g, '') || ''
}

async function loadNotices() {
  loading.value = true
  try {
    const res = await getNoticePage({ current: currentPage.value, size: pageSize }) as unknown as { data: { records: Notice[]; total: number } }
    notices.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function openDetail(item: Notice) {
  try {
    const res = await getNoticeDetail(item.id) as unknown as { data: Notice }
    currentNotice.value = res.data
    detailVisible.value = true
  } catch {
    currentNotice.value = item
    detailVisible.value = true
  }
}

onMounted(loadNotices)
</script>

<style scoped lang="scss">
.page-header {
  margin-bottom: 20px;
  .page-title { font-family: var(--font-display); font-size: 22px; color: var(--color-text-primary); }
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-card {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--color-accent-jade);
    background: var(--color-bg-hover);
  }

  .notice-head {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 8px;

    .notice-time {
      font-size: 12px;
      color: var(--color-text-secondary);
      font-family: var(--font-mono);
    }
  }

  .notice-title {
    font-family: var(--font-display);
    font-size: 16px;
    color: var(--color-text-primary);
    margin: 0 0 6px;
  }

  .notice-preview {
    font-size: 13px;
    color: var(--color-text-secondary);
    line-height: 1.6;
    margin: 0;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border);

  .detail-time {
    font-size: 13px;
    color: var(--color-text-secondary);
    font-family: var(--font-mono);
  }

  .detail-publisher {
    font-size: 13px;
    color: var(--color-text-secondary);
  }
}

.detail-content {
  font-size: 14px;
  color: var(--color-text-primary);
  line-height: 1.8;

  :deep(img) {
    max-width: 100%;
    border-radius: 8px;
  }
}
</style>
