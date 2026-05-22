<template>
  <div class="page-view">
    <div class="page-header">
      <h2 class="page-title">智育测评</h2>
      <el-button type="primary" @click="openDialog()">
        <span class="btn-icon">+</span>
        新增评价
      </el-button>
    </div>

    <div class="page-card">
      <div class="filter-bar">
        <el-input v-model="query.keyword" placeholder="搜索学号 / 姓名" clearable class="filter-input" @clear="loadData" @keyup.enter="loadData" />
        <el-input v-model="query.academicYear" placeholder="学年 如 2023-2024" clearable class="filter-input" @clear="loadData" @keyup.enter="loadData" />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="studentNo" label="学号" width="110" />
        <el-table-column prop="studentName" label="姓名" width="90" />
        <el-table-column prop="academicYear" label="学年" width="120" />
        <el-table-column prop="weightedAvgScore" label="加权均分" width="100" align="center" />
        <el-table-column prop="academicBonusSubtotal" label="奖励小计" width="90" align="center" />
        <el-table-column prop="academicDeduction" label="扣分" width="70" align="center">
          <template #default="{ row }">
            <span style="color: #E57373">{{ row.academicDeduction ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="80" align="center">
          <template #default="{ row }">
            <span class="score-highlight">{{ row.totalScore ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="evaluatorName" label="评价人" width="90" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-pagination">
        <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑智育评价' : '新增智育评价'" width="600px" class="form-dialog" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="form-row">
          <el-form-item label="学生" prop="studentId">
            <el-select v-model="form.studentId" filterable remote reserve-keyword placeholder="输入学号或姓名搜索" :remote-method="searchStudents" :loading="studentLoading" :disabled="isEdit" style="width: 100%">
              <el-option v-for="s in studentOptions" :key="s.id" :label="`${s.studentNo} - ${s.realName}`" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="学年" prop="academicYear">
            <el-input v-model="form.academicYear" placeholder="如 2023-2024" />
          </el-form-item>
        </div>
        <el-divider content-position="left">成绩录入</el-divider>
        <div class="form-grid">
          <el-form-item label="加权均分"><el-input-number v-model="form.weightedAvgScore" :min="0" :max="100" :step="0.5" controls-position="right" /></el-form-item>
          <el-form-item label="智育加分"><el-input-number v-model="form.academicBonus" :min="0" :max="100" :step="0.5" controls-position="right" /></el-form-item>
          <el-form-item label="其他"><el-input-number v-model="form.academicOther" :min="0" :max="100" :step="0.5" controls-position="right" /></el-form-item>
          <el-form-item label="扣分"><el-input-number v-model="form.academicDeduction" :min="0" :max="100" :step="0.5" controls-position="right" /></el-form-item>
        </div>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="备注信息" />
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
import { getAcademicPage, saveAcademic, deleteAcademic } from '@/api/evaluation'
import { getStudentPage } from '@/api/student'
import type { AcademicEvaluation, EvaluationQuery } from '@/types/evaluation'
import type { Student } from '@/types/student'
import type { PageResult } from '@/types/api'

const loading = ref(false)
const submitting = ref(false)
const studentLoading = ref(false)
const studentOptions = ref<Student[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref<AcademicEvaluation[]>([])
const total = ref(0)
const formRef = ref<FormInstance>()

const query = reactive<EvaluationQuery>({ current: 1, size: 10, keyword: '', academicYear: '' })
const form = reactive<Partial<AcademicEvaluation>>({})

const rules: FormRules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  academicYear: [{ required: true, message: '请输入学年', trigger: 'blur' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAcademicPage(query) as unknown as { data: PageResult<AcademicEvaluation> }
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function searchStudents(keyword: string) {
  if (!keyword) return
  studentLoading.value = true
  try {
    const res = await getStudentPage({ current: 1, size: 50, keyword }) as unknown as { data: PageResult<Student> }
    studentOptions.value = res.data.records
  } finally {
    studentLoading.value = false
  }
}

function openDialog(row?: AcademicEvaluation) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, row)
    if (row.studentId && row.studentNo && row.studentName) {
      studentOptions.value = [{ id: row.studentId, studentNo: row.studentNo, realName: row.studentName } as any]
    }
  } else {
    Object.assign(form, {
      id: undefined, studentId: undefined, academicYear: '',
      weightedAvgScore: 0, academicBonus: 0, academicOther: 0,
      academicDeduction: 0, remark: '',
    })
  }
  dialogVisible.value = true
}

function resetForm() {
  Object.keys(form).forEach(k => (form as any)[k] = undefined)
  studentOptions.value = []
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await saveAcademic(form)
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: AcademicEvaluation) {
  await ElMessageBox.confirm('确定删除该智育评价记录？', '提示', { type: 'warning' })
  await deleteAcademic(row.id!)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.page-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;
  .page-title { font-family: var(--font-display); font-size: 22px; color: var(--color-text-primary); }
}
.page-card { background: var(--color-bg-secondary); border: 1px solid var(--color-border); border-radius: 12px; padding: 20px; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; .filter-input { width: 200px; } }
.page-pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.form-dialog {
  :deep(.el-dialog) { background: var(--color-bg-secondary); border: 1px solid var(--color-border); border-radius: 12px; }
  :deep(.el-dialog__header) { border-bottom: 1px solid var(--color-border); }
  :deep(.el-dialog__title) { color: var(--color-text-primary); font-family: var(--font-display); }
}
.form-row { display: flex; gap: 20px; .el-form-item { flex: 1; } }
.form-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 0 20px; }
.score-highlight { color: var(--color-accent-jade); font-family: var(--font-mono); font-weight: 600; }
</style>
