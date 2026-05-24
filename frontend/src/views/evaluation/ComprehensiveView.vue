<template>
  <div class="page-view">
    <div class="page-header">
      <h2 class="page-title">综合素质评价</h2>
    </div>

    <div class="page-card">
      <div class="filter-bar">
        <el-input v-model="query.keyword" placeholder="搜索学号 / 姓名" clearable class="filter-input" @clear="loadData" @keyup.enter="loadData" />
        <el-input v-model="query.academicYear" placeholder="学年 如 2023-2024" clearable class="filter-input" @clear="loadData" @keyup.enter="loadData" />
        <el-select v-model="query.classId" placeholder="按班级筛选" clearable class="filter-input" @change="loadData">
          <el-option v-for="c in classList" :key="c.id" :label="c.className" :value="c.id" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="studentNo" label="学号" width="110" />
        <el-table-column prop="studentName" label="姓名" width="90" />
        <el-table-column prop="className" label="班级" width="130" />
        <el-table-column prop="academicYear" label="学年" width="110" />
        <el-table-column prop="moralScore" label="德育" width="70" align="center">
          <template #default="{ row }"><span class="score-dim">{{ row.moralScore }}</span></template>
        </el-table-column>
        <el-table-column prop="academicScore" label="智育" width="70" align="center">
          <template #default="{ row }"><span class="score-dim">{{ row.academicScore }}</span></template>
        </el-table-column>
        <el-table-column prop="physicalScore" label="体育" width="70" align="center">
          <template #default="{ row }"><span class="score-dim">{{ row.physicalScore }}</span></template>
        </el-table-column>
        <el-table-column prop="artScore" label="美育" width="70" align="center">
          <template #default="{ row }"><span class="score-dim">{{ row.artScore }}</span></template>
        </el-table-column>
        <el-table-column prop="practiceScore" label="劳动" width="70" align="center">
          <template #default="{ row }"><span class="score-dim">{{ row.practiceScore }}</span></template>
        </el-table-column>
        <el-table-column prop="totalScore" label="综合总分" width="100" align="center">
          <template #default="{ row }">
            <span class="score-highlight">{{ row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="clusterName" label="聚类分组" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.clusterName" :type="clusterTagType(row.clusterName)" size="small">{{ row.clusterName }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-pagination">
        <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="学生综合素质详情" width="780px" class="form-dialog">
      <div v-if="detailData" class="detail-content">
        <div class="detail-info">
          <div class="info-item"><span class="label">姓名</span><span class="value">{{ detailData.studentName }}</span></div>
          <div class="info-item"><span class="label">学号</span><span class="value">{{ detailData.studentNo }}</span></div>
          <div class="info-item"><span class="label">班级</span><span class="value">{{ detailData.className }}</span></div>
          <div class="info-item"><span class="label">学年</span><span class="value">{{ detailData.academicYear }}</span></div>
          <div class="info-item"><span class="label">综合总分</span><span class="value score-highlight">{{ detailData.totalScore }}</span></div>
          <div v-if="detailData.clusterName" class="info-item"><span class="label">聚类分组</span><el-tag :type="clusterTagType(detailData.clusterName)" size="small">{{ detailData.clusterName }}</el-tag></div>
        </div>

        <div class="detail-radar">
          <v-chart :option="radarOption" autoresize style="height: 320px" />
        </div>

        <div v-if="suggestionData" class="detail-suggestion">
          <!-- 总结 -->
          <div class="suggestion-summary">
            <span class="summary-icon">📋</span>
            <p>{{ suggestionData.summary }}</p>
          </div>

          <!-- 优势维度 -->
          <div v-if="suggestionData.strengths?.length" class="suggestion-section">
            <h4 class="section-title strengths-title">优势维度</h4>
            <div class="dim-cards">
              <div v-for="s in suggestionData.strengths" :key="s.dimKey" class="dim-card strength">
                <div class="dim-header">
                  <span class="dim-label">{{ s.dimLabel }}</span>
                  <el-tag size="small" type="success">{{ s.levelLabel }}</el-tag>
                </div>
                <div class="dim-score">
                  <span class="score-val">{{ s.score }}</span>
                  <span class="score-unit">分</span>
                  <span class="score-rank">排名 {{ s.rank }}/{{ s.total }}</span>
                </div>
                <div class="dim-bar">
                  <div class="bar-track"><div class="bar-fill strength" :style="{ width: Math.min(s.score, 100) + '%' }" /></div>
                </div>
                <p class="dim-suggestion">{{ s.suggestion }}</p>
              </div>
            </div>
          </div>

          <!-- 薄弱维度 -->
          <div v-if="suggestionData.weaknesses?.length" class="suggestion-section">
            <h4 class="section-title weakness-title">待提升维度</h4>
            <div class="dim-cards">
              <div v-for="w in suggestionData.weaknesses" :key="w.dimKey" class="dim-card weakness">
                <div class="dim-header">
                  <span class="dim-label">{{ w.dimLabel }}</span>
                  <el-tag size="small" :type="w.level === 'warning' ? 'danger' : 'warning'">{{ w.levelLabel }}</el-tag>
                </div>
                <div class="dim-score">
                  <span class="score-val">{{ w.score }}</span>
                  <span class="score-unit">分</span>
                  <span class="score-rank">排名 {{ w.rank }}/{{ w.total }}</span>
                </div>
                <div class="dim-bar">
                  <div class="bar-track"><div class="bar-fill weakness" :style="{ width: Math.min(w.score, 100) + '%' }" /></div>
                </div>
                <p class="dim-suggestion">{{ w.suggestion }}</p>
              </div>
            </div>
          </div>

          <!-- 行动建议 -->
          <div v-if="suggestionData.actionItems?.length" class="suggestion-section">
            <h4 class="section-title action-title">行动建议</h4>
            <ol class="action-list">
              <li v-for="(item, i) in suggestionData.actionItems" :key="i">{{ item }}</li>
            </ol>
          </div>

          <!-- 群体特征 -->
          <div v-if="suggestionData.typicality" class="suggestion-section">
            <h4 class="section-title typicality-title">群体特征</h4>
            <div class="typicality-box">
              <span class="typicality-label">{{ suggestionData.typicality.label }}</span>
              <span class="typicality-desc">{{ suggestionData.typicality.description }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="showGrowth">查看成长趋势</el-button>
      </template>
    </el-dialog>

    <!-- 成长趋势弹窗 -->
    <el-dialog v-model="growthVisible" title="成长趋势" width="750px" class="form-dialog">
      <div v-if="growthData.length">
        <v-chart :option="growthOption" autoresize style="height: 360px" />
      </div>
      <el-empty v-else description="暂无历史数据" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { RadarChart, LineChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getComprehensivePage, getComprehensiveDetail, getComprehensiveGrowth } from '@/api/evaluation'
import { getClassList } from '@/api/class'
import type { ComprehensiveEvaluation, EvaluationQuery } from '@/types/evaluation'
import type { ClassInfo } from '@/types/class'
import type { PageResult } from '@/types/api'

use([RadarChart, LineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const chartColors = ['#4DB6AC', '#E57373', '#FFB74D', '#80CBC4', '#BCAAA4']
const tagTypes = ['primary', 'success', 'warning', 'danger', 'info'] as const

function clusterTagType(name: string) {
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = ((hash << 5) - hash + name.charCodeAt(i)) | 0
  return tagTypes[Math.abs(hash) % tagTypes.length]
}

const loading = ref(false)
const tableData = ref<ComprehensiveEvaluation[]>([])
const total = ref(0)
const classList = ref<ClassInfo[]>([])

const detailVisible = ref(false)
const detailData = ref<ComprehensiveEvaluation | null>(null)

const growthVisible = ref(false)
const growthData = ref<ComprehensiveEvaluation[]>([])

const suggestionData = computed(() => {
  if (!detailData.value?.suggestion) return null
  try {
    return JSON.parse(detailData.value.suggestion)
  } catch {
    return null
  }
})

const query = reactive<EvaluationQuery>({
  current: 1,
  size: 10,
  keyword: '',
  academicYear: '',
})

const radarOption = computed(() => {
  if (!detailData.value) return {}
  const d = detailData.value
  return {
    color: chartColors,
    tooltip: {},
    radar: {
      indicator: [
        { name: '德育', max: 100 },
        { name: '智育', max: 100 },
        { name: '体育', max: 100 },
        { name: '美育', max: 100 },
        { name: '劳动', max: 100 },
      ],
      shape: 'polygon',
      splitNumber: 4,
      axisName: { color: '#9CA8B7', fontSize: 13 },
      splitLine: { lineStyle: { color: '#2A3A4A' } },
      splitArea: { areaStyle: { color: ['rgba(77,182,172,0.02)', 'rgba(77,182,172,0.06)'] } },
      axisLine: { lineStyle: { color: '#2A3A4A' } },
    },
    series: [{
      type: 'radar',
      data: [{
        value: [d.moralScore, d.academicScore, d.physicalScore, d.artScore, d.practiceScore],
        name: d.studentName,
        areaStyle: { color: 'rgba(77,182,172,0.2)' },
        lineStyle: { color: '#4DB6AC', width: 2 },
        itemStyle: { color: '#4DB6AC' },
      }],
    }],
  }
})

const growthOption = computed(() => {
  if (!growthData.value.length) return {}
  const sorted = [...growthData.value].sort((a, b) => a.academicYear.localeCompare(b.academicYear))
  const years = sorted.map(d => d.academicYear)
  const dims = ['moralScore', 'academicScore', 'physicalScore', 'artScore', 'practiceScore'] as const
  const labels = ['德育', '智育', '体育', '美育', '劳动']
  return {
    color: chartColors,
    tooltip: { trigger: 'axis' },
    legend: { data: labels, textStyle: { color: '#9CA8B7' }, top: 0 },
    grid: { left: 50, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: years, axisLabel: { color: '#9CA8B7' }, axisLine: { lineStyle: { color: '#2A3A4A' } } },
    yAxis: { type: 'value', axisLabel: { color: '#9CA8B7' }, splitLine: { lineStyle: { color: '#233040' } } },
    series: dims.map((dim, i) => ({
      name: labels[i],
      type: 'line',
      data: sorted.map(d => (d as any)[dim]),
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
    })),
  }
})

async function loadData() {
  loading.value = true
  try {
    const res = await getComprehensivePage(query) as unknown as { data: PageResult<ComprehensiveEvaluation> }
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

async function showDetail(row: ComprehensiveEvaluation) {
  try {
    const res = await getComprehensiveDetail({ studentId: row.studentId, academicYear: row.academicYear }) as unknown as { data: ComprehensiveEvaluation }
    detailData.value = res.data
  } catch {
    detailData.value = row
  }
  detailVisible.value = true
}

async function showGrowth() {
  if (!detailData.value) return
  const res = await getComprehensiveGrowth(detailData.value.studentId) as unknown as { data: ComprehensiveEvaluation[] }
  growthData.value = Array.isArray(res.data) ? res.data : []
  growthVisible.value = true
}

onMounted(() => {
  loadData()
  loadClasses()
})
</script>

<style scoped lang="scss">
.page-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;
  .page-title { font-family: var(--font-display); font-size: 22px; color: var(--color-text-primary); }
}
.page-card { background: var(--color-bg-secondary); border: 1px solid var(--color-border); border-radius: 12px; padding: 20px; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; .filter-input { width: 180px; } }
.page-pagination { display: flex; justify-content: flex-end; margin-top: 16px; }

.form-dialog {
  :deep(.el-dialog) { background: var(--color-bg-secondary); border: 1px solid var(--color-border); border-radius: 12px; }
  :deep(.el-dialog__header) { border-bottom: 1px solid var(--color-border); }
  :deep(.el-dialog__title) { color: var(--color-text-primary); font-family: var(--font-display); }
  :deep(.el-dialog__body) { max-height: 75vh; overflow-y: auto; }
}

.score-highlight { color: var(--color-accent-jade); font-family: var(--font-mono); font-weight: 600; }
.score-dim { font-family: var(--font-mono); font-size: 13px; }
.text-muted { color: var(--color-text-secondary); }

.detail-content {
  .detail-info {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px 24px;
    margin-bottom: 20px;

    .info-item {
      display: flex;
      align-items: center;
      gap: 8px;

      .label {
        color: var(--color-text-secondary);
        font-size: 13px;
        min-width: 56px;
      }

      .value {
        color: var(--color-text-primary);
        font-size: 14px;
      }
    }
  }

  .detail-radar {
    margin-bottom: 20px;
    background: rgba(27, 40, 56, 0.5);
    border-radius: 8px;
    padding: 12px;
  }

  .detail-suggestion {

    .suggestion-summary {
      display: flex;
      align-items: flex-start;
      gap: 10px;
      background: rgba(77, 182, 172, 0.08);
      border: 1px solid rgba(77, 182, 172, 0.2);
      border-radius: 8px;
      padding: 14px 16px;
      margin-bottom: 16px;

      .summary-icon { font-size: 18px; line-height: 1.6; }

      p {
        color: var(--color-text-primary);
        font-size: 14px;
        line-height: 1.6;
        margin: 0;
      }
    }

    .suggestion-section {
      margin-bottom: 16px;

      &:last-child { margin-bottom: 0; }

      .section-title {
        font-family: var(--font-display);
        font-size: 14px;
        margin-bottom: 10px;
        padding-left: 10px;
        border-left: 3px solid;

        &.strengths-title { color: #81C784; border-color: #81C784; }
        &.weakness-title { color: #FFB74D; border-color: #FFB74D; }
        &.action-title { color: var(--color-accent-jade); border-color: var(--color-accent-jade); }
        &.typicality-title { color: #90CAF9; border-color: #90CAF9; }
      }
    }

    .dim-cards {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .dim-card {
      background: rgba(27, 40, 56, 0.6);
      border: 1px solid var(--color-border);
      border-radius: 8px;
      padding: 12px 14px;

      &.strength { border-left: 3px solid #81C784; }
      &.weakness { border-left: 3px solid #FFB74D; }

      .dim-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 8px;

        .dim-label {
          font-weight: 600;
          font-size: 14px;
          color: var(--color-text-primary);
        }
      }

      .dim-score {
        display: flex;
        align-items: baseline;
        gap: 4px;
        margin-bottom: 8px;

        .score-val {
          font-family: var(--font-mono);
          font-size: 22px;
          font-weight: 700;
          color: var(--color-accent-jade);
        }

        .score-unit {
          font-size: 12px;
          color: var(--color-text-secondary);
        }

        .score-rank {
          margin-left: auto;
          font-size: 12px;
          font-family: var(--font-mono);
          color: var(--color-text-secondary);
        }
      }

      .dim-bar {
        margin-bottom: 8px;

        .bar-track {
          height: 6px;
          background: rgba(42, 58, 74, 0.8);
          border-radius: 3px;
          overflow: hidden;

          .bar-fill {
            height: 100%;
            border-radius: 3px;
            transition: width 0.6s ease;

            &.strength { background: linear-gradient(90deg, #4DB6AC, #81C784); }
            &.weakness { background: linear-gradient(90deg, #FFB74D, #E57373); }
          }
        }
      }

      .dim-suggestion {
        font-size: 13px;
        color: var(--color-text-secondary);
        line-height: 1.5;
        margin: 0;
        padding-top: 6px;
        border-top: 1px solid rgba(42, 58, 74, 0.5);
      }
    }

    .action-list {
      margin: 0;
      padding-left: 20px;

      li {
        color: var(--color-text-primary);
        font-size: 13px;
        line-height: 1.8;

        &::marker {
          color: var(--color-accent-jade);
          font-weight: 600;
        }
      }
    }

    .typicality-box {
      display: flex;
      align-items: center;
      gap: 12px;
      background: rgba(27, 40, 56, 0.6);
      border: 1px solid var(--color-border);
      border-radius: 8px;
      padding: 12px 14px;

      .typicality-label {
        background: rgba(144, 202, 249, 0.15);
        color: #90CAF9;
        font-size: 13px;
        font-weight: 600;
        padding: 2px 10px;
        border-radius: 4px;
        white-space: nowrap;
      }

      .typicality-desc {
        font-size: 13px;
        color: var(--color-text-secondary);
        line-height: 1.5;
      }
    }
  }
}
</style>
