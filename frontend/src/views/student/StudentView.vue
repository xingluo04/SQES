<template>
  <div class="page-view">
    <div class="page-header">
      <h2 class="page-title">学生管理</h2>
      <el-button type="primary" @click="openDialog()">
        <span class="btn-icon">+</span>
        新增学生
      </el-button>
    </div>

    <div class="page-card">
      <div class="filter-bar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索姓名 / 学号"
          clearable
          class="filter-input"
          @clear="loadData"
          @keyup.enter="loadData"
        />
        <el-select v-model="query.classId" placeholder="按班级筛选" clearable @change="loadData">
          <el-option v-for="c in classList" :key="c.id" :label="c.className" :value="c.id" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="className" label="班级" width="140" />
        <el-table-column prop="gender" label="性别" width="70">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="enrollmentYear" label="入学年份" width="100" />
        <el-table-column prop="parentName" label="家长" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
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
      :title="isEdit ? '编辑学生' : '新增学生'"
      width="520px"
      class="form-dialog"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select v-model="form.classId" placeholder="请选择班级">
            <el-option v-for="c in classList" :key="c.id" :label="c.className" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="入学年份" prop="enrollmentYear">
          <el-input-number v-model="form.enrollmentYear" :min="2000" :max="2030" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
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
import { getStudentPage, createStudent, updateStudent, deleteStudent } from '@/api/student'
import { getClassList } from '@/api/class'
import type { Student } from '@/types/student'
import type { ClassInfo } from '@/types/class'
import type { PageResult } from '@/types/api'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref<Student[]>([])
const classList = ref<ClassInfo[]>([])
const total = ref(0)
const formRef = ref<FormInstance>()

const query = reactive({
  current: 1,
  size: 10,
  keyword: '',
  classId: undefined as number | undefined,
})

const form = reactive({
  id: undefined as number | undefined,
  studentNo: '',
  realName: '',
  classId: undefined as number | undefined,
  gender: 1 as number,
  enrollmentYear: new Date().getFullYear(),
  phone: '',
  email: '',
})

const rules: FormRules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  enrollmentYear: [{ required: true, message: '请输入入学年份', trigger: 'blur' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await getStudentPage(query) as unknown as { data: PageResult<Student> }
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadClasses() {
  const res = await getClassList() as unknown as { data: ClassInfo[] }
  classList.value = res.data
}

function openDialog(row?: Student) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, { ...row, gender: row.gender ?? 1 })
  }
  dialogVisible.value = true
}

function resetForm() {
  form.id = undefined
  form.studentNo = ''
  form.realName = ''
  form.classId = undefined
  form.gender = 1
  form.enrollmentYear = new Date().getFullYear()
  form.phone = ''
  form.email = ''
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateStudent(form)
      ElMessage.success('更新成功')
    } else {
      await createStudent(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: Student) {
  await ElMessageBox.confirm(`确定删除学生「${row.realName}」？`, '提示', { type: 'warning' })
  await deleteStudent(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadData()
  loadClasses()
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
