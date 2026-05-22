<template>
  <div class="page-view">
    <div class="page-header">
      <h2 class="page-title">数据导入</h2>
    </div>

    <div class="import-card">
      <h3 class="card-title">Excel 数据导入</h3>
      <p class="card-desc">上传 Excel 文件（.xlsx），系统将自动解析并导入综测数据。请确保文件格式正确，第一行为表头。</p>

      <div class="form-row">
        <span class="form-label">学年</span>
        <el-input v-model="academicYear" placeholder="如 2023-2024" clearable class="year-input" />
      </div>

      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        :on-exceed="handleExceed"
        drag
        class="upload-area"
      >
        <div class="upload-inner">
          <span class="upload-icon">📄</span>
          <p class="upload-text">将 Excel 文件拖到此处，或 <em>点击上传</em></p>
          <p class="upload-hint">仅支持 .xlsx / .xls 格式</p>
        </div>
      </el-upload>

      <div class="form-actions">
        <el-button type="primary" size="large" :loading="importing" :disabled="!selectedFile" @click="handleImport">
          开始导入
        </el-button>
        <el-button size="large" @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 导入结果 -->
    <div v-if="resultData" class="result-card" :class="resultData.success ? 'success' : 'fail'">
      <h3 class="result-title">{{ resultData.success ? '导入成功' : '导入失败' }}</h3>
      <div class="result-stats">
        <div class="stat-item" v-if="resultData.totalRows !== undefined">
          <span class="stat-num">{{ resultData.totalRows }}</span>
          <span class="stat-label">总行数</span>
        </div>
        <div class="stat-item" v-if="resultData.successRows !== undefined">
          <span class="stat-num jade">{{ resultData.successRows }}</span>
          <span class="stat-label">成功</span>
        </div>
        <div class="stat-item" v-if="resultData.failRows !== undefined">
          <span class="stat-num vermillion">{{ resultData.failRows }}</span>
          <span class="stat-label">失败</span>
        </div>
      </div>
      <p v-if="resultData.message" class="result-msg">{{ resultData.message }}</p>
      <div v-if="resultData.errors?.length" class="error-list">
        <p class="error-title">错误详情：</p>
        <ul>
          <li v-for="(err, i) in resultData.errors" :key="i">{{ err }}</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadFile, UploadInstance } from 'element-plus'
import { importExcel } from '@/api/file'

const uploadRef = ref<UploadInstance>()
const academicYear = ref('')
const selectedFile = ref<File | null>(null)
const importing = ref(false)
const resultData = ref<{
  success: boolean
  totalRows?: number
  successRows?: number
  failRows?: number
  message?: string
  errors?: string[]
} | null>(null)

function handleFileChange(file: UploadFile) {
  selectedFile.value = file.raw || null
}

function handleFileRemove() {
  selectedFile.value = null
}

function handleExceed() {
  ElMessage.warning('只能上传一个文件，请先移除已选文件')
}

async function handleImport() {
  if (!academicYear.value?.trim()) return ElMessage.warning('请输入学年')
  if (!/^\d{4}-\d{4}$/.test(academicYear.value.trim())) return ElMessage.warning('学年格式应为 YYYY-YYYY')
  if (!selectedFile.value) return ElMessage.warning('请选择 Excel 文件')

  importing.value = true
  resultData.value = null
  try {
    const res = await importExcel(selectedFile.value, academicYear.value.trim()) as unknown as {
      data: { totalRows?: number; successRows?: number; failRows?: number; message?: string; errors?: string[] }
    }
    resultData.value = { success: true, ...res.data }
    ElMessage.success('导入完成')
  } catch (err: any) {
    resultData.value = {
      success: false,
      message: err?.message || '导入失败',
    }
  } finally {
    importing.value = false
  }
}

function handleReset() {
  academicYear.value = ''
  selectedFile.value = null
  resultData.value = null
  uploadRef.value?.clearFiles()
}
</script>

<style scoped lang="scss">
.page-header {
  margin-bottom: 20px;
  .page-title { font-family: var(--font-display); font-size: 22px; color: var(--color-text-primary); }
}

.import-card {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;

  .card-title {
    font-family: var(--font-display);
    font-size: 16px;
    color: var(--color-text-primary);
    margin-bottom: 8px;
    padding-left: 10px;
    border-left: 3px solid var(--color-accent-jade);
  }

  .card-desc {
    font-size: 13px;
    color: var(--color-text-secondary);
    line-height: 1.6;
    margin: 0 0 20px;
  }

  .form-row {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;

    .form-label {
      font-size: 14px;
      color: var(--color-text-primary);
      white-space: nowrap;
    }

    .year-input { width: 220px; }
  }

  .form-actions {
    display: flex;
    gap: 12px;
    margin-top: 20px;
  }
}

.upload-area {
  :deep(.el-upload) {
    width: 100%;
  }

  :deep(.el-upload-dragger) {
    width: 100%;
    background: rgba(27, 40, 56, 0.4);
    border: 2px dashed var(--color-border);
    border-radius: 10px;
    padding: 40px 20px;
    transition: all 0.2s;

    &:hover {
      border-color: var(--color-accent-jade);
      background: rgba(77, 182, 172, 0.05);
    }
  }
}

.upload-inner {
  text-align: center;

  .upload-icon {
    font-size: 40px;
    display: block;
    margin-bottom: 12px;
  }

  .upload-text {
    font-size: 14px;
    color: var(--color-text-primary);
    margin: 0 0 6px;

    em {
      color: var(--color-accent-jade);
      font-style: normal;
      cursor: pointer;
    }
  }

  .upload-hint {
    font-size: 12px;
    color: var(--color-text-secondary);
    margin: 0;
  }
}

.result-card {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 20px 24px;

  &.success { border-left: 4px solid #81C784; }
  &.fail { border-left: 4px solid #E57373; }

  .result-title {
    font-family: var(--font-display);
    font-size: 16px;
    color: var(--color-text-primary);
    margin: 0 0 16px;
  }

  .result-stats {
    display: flex;
    gap: 32px;
    margin-bottom: 12px;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .stat-num {
        font-family: var(--font-mono);
        font-size: 28px;
        font-weight: 700;
        color: var(--color-text-primary);

        &.jade { color: var(--color-accent-jade); }
        &.vermillion { color: var(--color-accent-vermilion); }
      }

      .stat-label {
        font-size: 12px;
        color: var(--color-text-secondary);
        margin-top: 4px;
      }
    }
  }

  .result-msg {
    font-size: 13px;
    color: var(--color-text-secondary);
    margin: 8px 0 0;
  }

  .error-list {
    margin-top: 12px;
    padding: 12px 16px;
    background: rgba(229, 115, 115, 0.08);
    border: 1px solid rgba(229, 115, 115, 0.2);
    border-radius: 8px;

    .error-title {
      font-size: 13px;
      font-weight: 600;
      color: var(--color-accent-vermilion);
      margin: 0 0 8px;
    }

    ul {
      margin: 0;
      padding-left: 18px;

      li {
        font-size: 13px;
        color: var(--color-text-secondary);
        line-height: 1.8;
      }
    }
  }
}
</style>
