<template>
  <div class="page-view">
    <div class="page-header">
      <h2 class="page-title">子女综合素质评价</h2>
    </div>

    <div v-if="loadingChildren" class="loading-box" v-loading="true" />

    <template v-else-if="children.length">
      <!-- 子女选择 -->
      <div class="child-tabs">
        <div
          v-for="c in children"
          :key="c.id"
          class="child-tab"
          :class="{ active: selectedChild?.id === c.id }"
          @click="selectChild(c)"
        >
          <span class="child-name">{{ c.realName }}</span>
          <span class="child-no">{{ c.studentNo }}</span>
        </div>
      </div>

      <div v-if="loadingEval" class="loading-box" v-loading="true" />

      <template v-else-if="evaluations.length">
        <!-- 学年选择 -->
        <div class="year-tabs">
          <div
            v-for="e in evaluations"
            :key="e.academicYear"
            class="year-tab"
            :class="{ active: selectedYear === e.academicYear }"
            @click="selectedYear = e.academicYear"
          >
            {{ e.academicYear }}
          </div>
        </div>

        <template v-if="currentEval">
          <!-- 概览卡片 -->
          <div class="overview-cards">
            <div class="overview-card">
              <span class="card-label">综合总分</span>
              <span class="card-value jade">{{ currentEval.totalScore }}</span>
            </div>
            <div class="overview-card">
              <span class="card-label">德育</span>
              <span class="card-value">{{ currentEval.moralScore }}</span>
            </div>
            <div class="overview-card">
              <span class="card-label">智育</span>
              <span class="card-value">{{ currentEval.academicScore }}</span>
            </div>
            <div class="overview-card">
              <span class="card-label">体育</span>
              <span class="card-value">{{ currentEval.physicalScore }}</span>
            </div>
            <div class="overview-card">
              <span class="card-label">美育</span>
              <span class="card-value">{{ currentEval.artScore }}</span>
            </div>
            <div class="overview-card">
              <span class="card-label">劳动</span>
              <span class="card-value">{{ currentEval.practiceScore }}</span>
            </div>
          </div>

          <!-- 雷达图 + 成长趋势 -->
          <div class="chart-row">
            <div class="chart-card">
              <h3 class="chart-title">五维雷达图</h3>
              <v-chart :option="radarOption" autoresize style="height: 320px" />
            </div>
            <div class="chart-card">
              <h3 class="chart-title">成长趋势</h3>
              <v-chart :option="growthOption" autoresize style="height: 320px" />
            </div>
          </div>

          <!-- 聚类信息 -->
          <div v-if="currentEval.clusterName" class="cluster-info">
            <span class="cluster-label">聚类分组</span>
            <el-tag type="info" size="large">{{ currentEval.clusterName }}</el-tag>
          </div>

          <!-- 评语建议 -->
          <div v-if="suggestionData" class="suggestion-section">
            <h3 class="section-title">评语建议</h3>
            <div class="suggestion-summary">
              <p>{{ suggestionData.summary }}</p>
            </div>

            <div v-if="suggestionData.strengths?.length" class="dim-group">
              <h4 class="group-title strengths">优势维度</h4>
              <div v-for="s in suggestionData.strengths" :key="s.dimKey" class="dim-item strength">
                <div class="dim-head">
                  <span class="dim-name">{{ s.dimLabel }}</span>
                  <el-tag size="small" type="success">{{ s.levelLabel }}</el-tag>
                </div>
                <div class="dim-score-row">
                  <span class="dim-score">{{ s.score }}分</span>
                  <span class="dim-rank">排名 {{ s.rank }}/{{ s.total }}</span>
                </div>
                <div class="dim-bar"><div class="bar-fill strength" :style="{ width: Math.min(s.score, 100) + '%' }" /></div>
                <p class="dim-tip">{{ s.suggestion }}</p>
              </div>
            </div>

            <div v-if="suggestionData.weaknesses?.length" class="dim-group">
              <h4 class="group-title weakness">待提升维度</h4>
              <div v-for="w in suggestionData.weaknesses" :key="w.dimKey" class="dim-item weakness">
                <div class="dim-head">
                  <span class="dim-name">{{ w.dimLabel }}</span>
                  <el-tag size="small" :type="w.level === 'warning' ? 'danger' : 'warning'">{{ w.levelLabel }}</el-tag>
                </div>
                <div class="dim-score-row">
                  <span class="dim-score">{{ w.score }}分</span>
                  <span class="dim-rank">排名 {{ w.rank }}/{{ w.total }}</span>
                </div>
                <div class="dim-bar"><div class="bar-fill weakness" :style="{ width: Math.min(w.score, 100) + '%' }" /></div>
                <p class="dim-tip">{{ w.suggestion }}</p>
              </div>
            </div>

            <div v-if="suggestionData.actionItems?.length" class="action-list">
              <h4 class="group-title action">行动建议</h4>
              <ol>
                <li v-for="(item, i) in suggestionData.actionItems" :key="i">{{ item }}</li>
              </ol>
            </div>
          </div>
        </template>

        <el-empty v-else description="该学年暂无评价数据" />
      </template>

      <el-empty v-else description="该子女暂无评价数据" />
    </template>

    <el-empty v-else description="暂无关联子女信息" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { RadarChart, LineChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent, RadarComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getChildren, getStudentPage } from '@/api/student'
import { getComprehensiveGrowth } from '@/api/evaluation'
import type { Student } from '@/types/student'
import type { ComprehensiveEvaluation } from '@/types/evaluation'

use([RadarChart, LineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, RadarComponent, CanvasRenderer])

const chartColors = ['#4DB6AC', '#E57373', '#FFB74D', '#80CBC4', '#BCAAA4']

const loadingChildren = ref(true)
const loadingEval = ref(false)
const children = ref<Student[]>([])
const selectedChild = ref<Student | null>(null)
const evaluations = ref<ComprehensiveEvaluation[]>([])
const selectedYear = ref('')

const currentEval = computed(() => evaluations.value.find(e => e.academicYear === selectedYear.value))

const suggestionData = computed(() => {
  if (!currentEval.value?.suggestion) return null
  try { return JSON.parse(currentEval.value.suggestion) } catch { return null }
})

const radarOption = computed(() => {
  if (!currentEval.value) return {}
  const d = currentEval.value
  return {
    color: chartColors,
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
      axisName: { color: '#9CA8B7', fontSize: 12 },
      splitLine: { lineStyle: { color: '#2A3A4A' } },
      splitArea: { areaStyle: { color: ['rgba(77,182,172,0.02)', 'rgba(77,182,172,0.06)'] } },
      axisLine: { lineStyle: { color: '#2A3A4A' } },
    },
    series: [{
      type: 'radar',
      data: [{
        value: [d.moralScore, d.academicScore, d.physicalScore, d.artScore, d.practiceScore],
        areaStyle: { color: 'rgba(77,182,172,0.2)' },
        lineStyle: { color: '#4DB6AC', width: 2 },
        itemStyle: { color: '#4DB6AC' },
      }],
    }],
  }
})

const growthOption = computed(() => {
  if (!evaluations.value.length) return {}
  const sorted = [...evaluations.value].sort((a, b) => a.academicYear.localeCompare(b.academicYear))
  const years = sorted.map(e => e.academicYear)
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

async function selectChild(child: Student) {
  selectedChild.value = child
  loadingEval.value = true
  try {
    const res = await getComprehensiveGrowth(child.id) as unknown as { data: ComprehensiveEvaluation[] }
    evaluations.value = Array.isArray(res.data) ? res.data : [res.data].filter(Boolean)
    selectedYear.value = evaluations.value.length
      ? evaluations.value[evaluations.value.length - 1].academicYear
      : ''
  } catch {
    evaluations.value = []
  } finally {
    loadingEval.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getChildren() as unknown as { data: Student[] }
    const list = res.data || []
    // /student/children 不返回 realName，通过 /student/page 补全
    try {
      const pageRes = await getStudentPage({ current: 1, size: 999 }) as unknown as { data: { records: Student[] } }
      const nameMap = new Map<number, string>()
      for (const s of pageRes.data?.records || []) {
        nameMap.set(s.id, s.realName)
      }
      children.value = list.map(s => ({
        ...s,
        realName: nameMap.get(s.id) || s.studentNo,
      }))
    } catch {
      children.value = list.map(s => ({ ...s, realName: s.studentNo }))
    }
    if (children.value.length) {
      await selectChild(children.value[0])
    }
  } finally {
    loadingChildren.value = false
  }
})
</script>

<style scoped lang="scss">
.page-header {
  margin-bottom: 20px;
  .page-title { font-family: var(--font-display); font-size: 22px; color: var(--color-text-primary); }
}

.loading-box { height: 300px; }

.child-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  .child-tab {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 10px 24px;
    border-radius: 10px;
    background: var(--color-bg-secondary);
    border: 1px solid var(--color-border);
    cursor: pointer;
    transition: all 0.2s;

    .child-name { font-size: 15px; font-weight: 600; color: var(--color-text-primary); }
    .child-no { font-size: 12px; color: var(--color-text-secondary); margin-top: 2px; }

    &:hover { border-color: var(--color-accent-jade); }
    &.active {
      background: rgba(77, 182, 172, 0.15);
      border-color: var(--color-accent-jade);
      .child-name { color: var(--color-accent-jade); }
    }
  }
}

.year-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;

  .year-tab {
    padding: 6px 16px;
    border-radius: 8px;
    background: var(--color-bg-secondary);
    border: 1px solid var(--color-border);
    color: var(--color-text-secondary);
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover { border-color: var(--color-accent-jade); color: var(--color-text-primary); }
    &.active { background: rgba(77, 182, 172, 0.15); border-color: var(--color-accent-jade); color: var(--color-accent-jade); font-weight: 600; }
  }
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 20px;

  .overview-card {
    background: var(--color-bg-secondary);
    border: 1px solid var(--color-border);
    border-radius: 10px;
    padding: 14px;
    text-align: center;

    .card-label { display: block; font-size: 12px; color: var(--color-text-secondary); margin-bottom: 6px; }
    .card-value { display: block; font-family: var(--font-mono); font-size: 24px; font-weight: 700; color: var(--color-text-primary); &.jade { color: var(--color-accent-jade); } }
  }
}

.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.chart-card {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 16px 20px;

  .chart-title {
    font-family: var(--font-display); font-size: 15px; color: var(--color-text-primary);
    margin-bottom: 12px; padding-left: 10px; border-left: 3px solid var(--color-accent-jade);
  }
}

.cluster-info {
  display: flex; align-items: center; gap: 12px; margin-bottom: 20px;
  .cluster-label { font-size: 14px; color: var(--color-text-secondary); }
}

.suggestion-section {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 20px;

  .section-title {
    font-family: var(--font-display); font-size: 16px; color: var(--color-text-primary);
    margin-bottom: 16px; padding-left: 10px; border-left: 3px solid var(--color-accent-jade);
  }

  .suggestion-summary {
    background: rgba(77, 182, 172, 0.08);
    border: 1px solid rgba(77, 182, 172, 0.2);
    border-radius: 8px;
    padding: 14px 16px;
    margin-bottom: 16px;

    p { color: var(--color-text-primary); font-size: 14px; line-height: 1.6; margin: 0; }
  }

  .dim-group { margin-bottom: 16px; }

  .group-title {
    font-family: var(--font-display); font-size: 14px; margin-bottom: 10px; padding-left: 10px; border-left: 3px solid;
    &.strengths { color: #81C784; border-color: #81C784; }
    &.weakness { color: #FFB74D; border-color: #FFB74D; }
    &.action { color: var(--color-accent-jade); border-color: var(--color-accent-jade); }
  }

  .dim-item {
    background: rgba(27, 40, 56, 0.6);
    border: 1px solid var(--color-border);
    border-radius: 8px;
    padding: 12px 14px;
    margin-bottom: 8px;

    &.strength { border-left: 3px solid #81C784; }
    &.weakness { border-left: 3px solid #FFB74D; }

    .dim-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; .dim-name { font-weight: 600; font-size: 14px; color: var(--color-text-primary); } }
    .dim-score-row { display: flex; align-items: baseline; gap: 12px; margin-bottom: 6px; .dim-score { font-family: var(--font-mono); font-size: 18px; font-weight: 700; color: var(--color-accent-jade); } .dim-rank { font-size: 12px; font-family: var(--font-mono); color: var(--color-text-secondary); } }
    .dim-bar { height: 6px; background: rgba(42, 58, 74, 0.8); border-radius: 3px; overflow: hidden; margin-bottom: 8px; .bar-fill { height: 100%; border-radius: 3px; transition: width 0.6s ease; &.strength { background: linear-gradient(90deg, #4DB6AC, #81C784); } &.weakness { background: linear-gradient(90deg, #FFB74D, #E57373); } } }
    .dim-tip { font-size: 13px; color: var(--color-text-secondary); line-height: 1.5; margin: 0; padding-top: 6px; border-top: 1px solid rgba(42, 58, 74, 0.5); }
  }

  .action-list {
    ol { margin: 0; padding-left: 20px; li { color: var(--color-text-primary); font-size: 13px; line-height: 1.8; &::marker { color: var(--color-accent-jade); font-weight: 600; } } }
  }
}
</style>
