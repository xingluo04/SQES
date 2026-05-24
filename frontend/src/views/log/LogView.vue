<template>
  <div class="page-view">
    <div class="page-header">
      <h2 class="page-title">操作日志</h2>
      <el-button type="danger" @click="openCleanDialog">
        清理日志
      </el-button>
    </div>

    <div class="page-card">
      <div class="filter-bar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索操作内容 / 用户名"
          clearable
          class="filter-input"
          @clear="loadData"
          @keyup.enter="loadData"
        />
        <el-select
          v-model="query.module"
          placeholder="模块"
          clearable
          class="filter-select"
          @change="loadData"
        >
          <el-option v-for="item in moduleOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select
          v-model="query.result"
          placeholder="结果"
          clearable
          class="filter-select"
          @change="loadData"
        >
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAIL" />
        </el-select>
        <el-date-picker
          v-model="timeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="filter-time"
          @change="handleTimeChange"
        />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="module" label="模块" width="110" />
        <el-table-column prop="username" label="操作人" width="100" />
        <el-table-column prop="operation" label="操作内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="method" label="请求方法" width="90">
          <template #default="{ row }">
            <el-tag
              :type="methodTagType(row.method)"
              size="small"
            >{{ row.method }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP 地址" width="140" />
        <el-table-column prop="result" label="结果" width="90">
          <template #default="{ row }">
            <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ row.result === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="100" />
        <el-table-column prop="createTime" label="操作时间" width="170" />
        <el-table-column label="详情" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-pagination">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="日志详情" width="640px" class="log-detail-dialog">
      <el-descriptions class="log-detail-descriptions" :column="2" border>
        <el-descriptions-item label="模块">{{ currentLog?.module }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog?.username }}</el-descriptions-item>
        <el-descriptions-item label="操作内容">{{ currentLog?.operation }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog?.method }}</el-descriptions-item>
        <el-descriptions-item label="IP 地址">{{ currentLog?.ip }}</el-descriptions-item>
        <el-descriptions-item label="结果">
          {{ currentLog?.result === 'SUCCESS' ? '成功' : '失败' }}
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentLog?.costTime ?? 0 }} ms</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentLog?.createTime }}</el-descriptions-item>
      </el-descriptions>
      <div class="detail-block">
        <div class="detail-title">参数摘要</div>
        <pre>{{ formatJson(currentLog?.params) }}</pre>
      </div>
      <div v-if="currentLog?.errorMsg" class="detail-block">
        <div class="detail-title">错误信息</div>
        <pre>{{ currentLog.errorMsg }}</pre>
      </div>
    </el-dialog>

    <el-dialog v-model="cleanVisible" title="清理日志" width="420px">
      <el-form label-width="96px">
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="cleanBeforeTime"
            type="datetime"
            placeholder="选择截止时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cleanVisible = false">取消</el-button>
        <el-button type="danger" :disabled="!cleanBeforeTime" @click="handleClean">
          确认清理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLogPage, cleanLogs } from '@/api/log'
import type { LogPageQuery, OperationLog } from '@/types/log'
import type { PageResult } from '@/types/api'

const loading = ref(false)
const tableData = ref<OperationLog[]>([])
const total = ref(0)
const timeRange = ref<[string, string] | null>(null)
const detailVisible = ref(false)
const cleanVisible = ref(false)
const currentLog = ref<OperationLog>()
const cleanBeforeTime = ref('')

const moduleOptions = ['用户管理', '学生管理', '班级管理', '评价管理', '数据导入', '聚类分析', '通知管理', '操作日志', '系统操作']

const query = reactive<LogPageQuery>({
  current: 1,
  size: 10,
  keyword: '',
  module: '',
  result: '',
  startTime: '',
  endTime: '',
})

function methodTagType(method: string) {
  const map: Record<string, string> = {
    GET: 'success',
    POST: '',
    PUT: 'warning',
    DELETE: 'danger',
  }
  return map[method] as any || 'info'
}

async function loadData() {
  query.current = query.current || 1
  loading.value = true
  try {
    const res = await getLogPage(query) as unknown as { data: PageResult<OperationLog> }
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleTimeChange(value: [string, string] | null) {
  query.startTime = value?.[0] || ''
  query.endTime = value?.[1] || ''
  query.current = 1
  loadData()
}

function resetQuery() {
  query.current = 1
  query.size = 10
  query.keyword = ''
  query.module = ''
  query.result = ''
  query.startTime = ''
  query.endTime = ''
  timeRange.value = null
  loadData()
}

function openDetail(row: OperationLog) {
  currentLog.value = row
  detailVisible.value = true
}

function formatJson(value?: string) {
  if (!value) return ''
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

function openCleanDialog() {
  cleanBeforeTime.value = ''
  cleanVisible.value = true
}

async function handleClean() {
  if (!cleanBeforeTime.value) return
  await ElMessageBox.confirm(`确定清理 ${cleanBeforeTime.value} 之前的操作日志？此操作不可恢复。`, '警告', { type: 'warning' })
  await cleanLogs(cleanBeforeTime.value)
  ElMessage.success('日志已清理')
  cleanVisible.value = false
  loadData()
}

onMounted(loadData)
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

.page-card {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 20px;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;

  .filter-input {
    width: 240px;
  }

  .filter-select {
    width: 140px;
  }

  .filter-time {
    width: 360px;
  }
}

.page-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.detail-block {
  margin-top: 16px;

  .detail-title {
    margin-bottom: 8px;
    color: var(--color-text-primary);
    font-weight: 600;
  }

  pre {
    max-height: 260px;
    overflow: auto;
    margin: 0;
    padding: 14px;
    border-radius: 8px;
    background: #111c28;
    border: 1px solid #3a4f65;
    color: #f3eee8;
    font-size: 13px;
    line-height: 1.7;
    white-space: pre-wrap;
    word-break: break-word;
  }
}

:global(.log-detail-dialog .el-dialog__body) {
  color: #f8f3ec !important;
}

:global(.log-detail-dialog .log-detail-descriptions) {
  --el-text-color-primary: #f8f3ec;
  --el-text-color-regular: #f8f3ec;
  --el-text-color-secondary: #d7e3ee;
  --el-fill-color-light: #172536;
  --el-border-color-lighter: #4b6178;
}

:global(.log-detail-dialog .log-detail-descriptions .el-descriptions__label) {
  min-width: 86px;
  color: #d7e3ee !important;
  background-color: #172536 !important;
  font-weight: 700;
}

:global(.log-detail-dialog .log-detail-descriptions .el-descriptions__content) {
  color: #fff8ef !important;
  background-color: #23384d !important;
  font-weight: 500;
}

:global(.log-detail-dialog .log-detail-descriptions .el-descriptions__body),
:global(.log-detail-dialog .log-detail-descriptions .el-descriptions__body table) {
  background-color: #23384d !important;
}

:global(.log-detail-dialog .log-detail-descriptions .el-descriptions__body td),
:global(.log-detail-dialog .log-detail-descriptions .el-descriptions__body th) {
  border-color: #4b6178 !important;
}
</style>
