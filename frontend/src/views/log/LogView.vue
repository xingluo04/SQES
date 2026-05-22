<template>
  <div class="page-view">
    <div class="page-header">
      <h2 class="page-title">操作日志</h2>
      <el-button type="danger" @click="handleClear">
        清空日志
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
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
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
        <el-table-column prop="params" label="参数" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="操作时间" width="170" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLogPage, clearLogs } from '@/api/log'
import type { OperationLog } from '@/types/log'
import type { PageResult } from '@/types/api'

const loading = ref(false)
const tableData = ref<OperationLog[]>([])
const total = ref(0)

const query = reactive({
  current: 1,
  size: 10,
  keyword: '',
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
  loading.value = true
  try {
    const res = await getLogPage(query) as unknown as { data: PageResult<OperationLog> }
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function handleClear() {
  await ElMessageBox.confirm('确定清空所有操作日志？此操作不可恢复。', '警告', { type: 'warning' })
  await clearLogs()
  ElMessage.success('日志已清空')
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
  gap: 12px;
  margin-bottom: 16px;

  .filter-input {
    width: 240px;
  }
}

.page-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
