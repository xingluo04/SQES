<template>
  <div class="page-view">
    <div class="page-header">
      <h2 class="page-title">班级管理</h2>
      <el-button type="primary" @click="openDialog()">
        <span class="btn-icon">+</span>
        新增班级
      </el-button>
    </div>

    <div class="page-card">
      <div class="filter-bar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索班级名称"
          clearable
          class="filter-input"
          @clear="loadData"
          @keyup.enter="loadData"
        />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="className" label="班级名称" width="160" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="department" label="院系" width="140" />
        <el-table-column prop="teacherName" label="班主任" width="100" />
        <el-table-column prop="studentCount" label="学生数" width="90" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑班级' : '新增班级'"
      width="480px"
      class="form-dialog"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="form.className" placeholder="如：计算机2024-1班" />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="form.grade" placeholder="如：2024级" />
        </el-form-item>
        <el-form-item label="院系">
          <el-input v-model="form.department" placeholder="请输入院系" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getClassPage, createClass, updateClass, deleteClass } from '@/api/class'
import type { ClassInfo } from '@/types/class'
import type { PageResult } from '@/types/api'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref<ClassInfo[]>([])
const total = ref(0)
const formRef = ref<FormInstance>()

const query = reactive({
  current: 1,
  size: 10,
  keyword: '',
})

const form = reactive<Partial<ClassInfo>>({
  id: undefined,
  className: '',
  grade: '',
  department: '',
})

const rules: FormRules = {
  className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  grade: [{ required: true, message: '请输入年级', trigger: 'blur' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await getClassPage(query) as unknown as { data: PageResult<ClassInfo> }
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openDialog(row?: ClassInfo) {
  isEdit.value = !!row
  if (row) Object.assign(form, row)
  dialogVisible.value = true
}

function resetForm() {
  form.id = undefined
  form.className = ''
  form.grade = ''
  form.department = ''
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateClass(form)
      ElMessage.success('更新成功')
    } else {
      await createClass(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: ClassInfo) {
  await ElMessageBox.confirm(`确定删除班级「${row.className}」？`, '提示', { type: 'warning' })
  await deleteClass(row.id)
  ElMessage.success('删除成功')
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

.form-dialog {
  :deep(.el-dialog) {
    background: var(--color-bg-secondary);
    border: 1px solid var(--color-border);
    border-radius: 12px;
  }
  :deep(.el-dialog__header) {
    border-bottom: 1px solid var(--color-border);
  }
  :deep(.el-dialog__title) {
    color: var(--color-text-primary);
    font-family: var(--font-display);
  }
}
</style>
