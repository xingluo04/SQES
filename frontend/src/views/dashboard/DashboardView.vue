<template>
  <div class="page-view">
    <div class="page-header">
      <h2 class="page-title">数据看板</h2>
      <el-input
        v-model="academicYear"
        placeholder="输入学年 如 2023-2024"
        clearable
        class="year-input"
        @keyup.enter="handleYearChange"
        @clear="handleYearChange"
      >
        <template #append>
          <el-button @click="handleYearChange">查询</el-button>
        </template>
      </el-input>
    </div>

    <!-- 概览卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon student">🎓</div>
        <div class="stat-info">
          <span class="stat-value">{{ overview.studentCount }}</span>
          <span class="stat-label">学生总数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon class">🏫</div>
        <div class="stat-info">
          <span class="stat-value">{{ overview.classCount }}</span>
          <span class="stat-label">班级数量</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon eval">📊</div>
        <div class="stat-info">
          <span class="stat-value">{{ overview.evaluationCount }}</span>
          <span class="stat-label">评价记录</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon teacher">👨‍🏫</div>
        <div class="stat-info">
          <span class="stat-value">{{ overview.teacherCount }}</span>
          <span class="stat-label">教师数量</span>
        </div>
      </div>
    </div>

    <!-- 五维均分雷达图 + 成绩分布柱状图 -->
    <div class="chart-row">
      <div class="chart-card">
        <h3 class="chart-title">五维均分雷达图</h3>
        <v-chart :option="radarOption" autoresize style="height: 340px" />
      </div>
      <div class="chart-card">
        <h3 class="chart-title">综合成绩分布</h3>
        <v-chart :option="barOption" autoresize style="height: 340px" />
      </div>
    </div>

    <!-- 聚类分布饼图 + 班级对比堆叠柱状图 -->
    <div class="chart-row">
      <div class="chart-card">
        <h3 class="chart-title">聚类分布</h3>
        <v-chart :option="pieOption" autoresize style="height: 340px" />
      </div>
      <div class="chart-card">
        <h3 class="chart-title">班级聚类对比</h3>
        <v-chart :option="stackedBarOption" autoresize style="height: 340px" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { RadarChart, BarChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent, RadarComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getDashboardOverview, getDimensionAvg, getClusterDistribution, getScoreDistribution, getClassComparison } from '@/api/dashboard'

use([RadarChart, BarChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, RadarComponent, CanvasRenderer])

const chartColors = ['#4DB6AC', '#E57373', '#FFB74D', '#80CBC4', '#BCAAA4', '#90CAF9', '#CE93D8']

const academicYear = ref('2024-2025')
const overview = reactive({ studentCount: 0, classCount: 0, evaluationCount: 0, teacherCount: 0 })
const dimAvg = reactive({ dimensions: [] as string[], values: [] as number[] })
const clusterDist = ref<{ name: string; value: number }[]>([])
const scoreDist = reactive({ labels: [] as string[], values: [] as number[] })
const classComp = reactive({ classes: [] as string[], clusterNames: [] as string[], series: [] as { name: string; data: number[] }[] })

const radarOption = computed(() => {
  if (!dimAvg.dimensions.length) return {}
  return {
    color: ['#4DB6AC'],
    tooltip: {},
    radar: {
      indicator: dimAvg.dimensions.map(d => ({ name: d, max: 100 })),
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
        value: dimAvg.values,
        name: '全校均分',
        areaStyle: { color: 'rgba(77,182,172,0.2)' },
        lineStyle: { color: '#4DB6AC', width: 2 },
        itemStyle: { color: '#4DB6AC' },
      }],
    }],
  }
})

const barOption = computed(() => {
  if (!scoreDist.labels.length) return {}
  return {
    color: ['#4DB6AC'],
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 20, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: scoreDist.labels,
      axisLabel: { color: '#9CA8B7', fontSize: 11 },
      axisLine: { lineStyle: { color: '#2A3A4A' } },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#9CA8B7' },
      splitLine: { lineStyle: { color: '#233040' } },
    },
    series: [{
      type: 'bar',
      data: scoreDist.values,
      barWidth: '50%',
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: '#4DB6AC' },
            { offset: 1, color: '#2A6B66' },
          ],
        },
      },
      label: { show: true, position: 'top', color: '#9CA8B7', fontSize: 11 },
    }],
  }
})

const pieOption = computed(() => {
  if (!clusterDist.value.length) return {}
  return {
    color: chartColors,
    tooltip: { formatter: (p: any) => `${p.name}：${p.value}人 (${p.percent}%)` },
    legend: {
      orient: 'vertical',
      right: 20,
      top: 'center',
      formatter: (name: string) => {
        const item = clusterDist.value.find(i => i.name === name)
        return item ? `${name}  {dim|${item.value}人}` : name
      },
      textStyle: {
        color: '#C0B8B0',
        rich: { dim: { color: '#9CA8B7', fontSize: 11, padding: [0, 0, 0, 6] } },
      },
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#1B2838', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%', color: '#C0B8B0', fontSize: 11 },
      labelLine: { lineStyle: { color: '#2A3A4A' } },
      emphasis: {
        label: { fontSize: 14, fontWeight: 'bold' },
        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' },
      },
      data: clusterDist.value,
    }],
  }
})

const stackedBarOption = computed(() => {
  if (!classComp.classes.length) return {}
  return {
    color: chartColors,
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: classComp.clusterNames, textStyle: { color: '#9CA8B7' }, top: 0, type: 'scroll' },
    grid: { left: 60, right: 20, top: 40, bottom: 40 },
    xAxis: {
      type: 'category',
      data: classComp.classes,
      axisLabel: { color: '#9CA8B7', fontSize: 11 },
      axisLine: { lineStyle: { color: '#2A3A4A' } },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#9CA8B7' },
      splitLine: { lineStyle: { color: '#233040' } },
    },
    series: classComp.series.map((s, i) => ({
      name: s.name,
      type: 'bar' as const,
      stack: 'total',
      data: s.data,
      itemStyle: { color: chartColors[i % chartColors.length] },
      emphasis: { focus: 'series' as const },
    })),
  }
})

async function loadOverview() {
  const res = await getDashboardOverview() as unknown as { data: typeof overview }
  Object.assign(overview, res.data)
}

async function loadDimAvg() {
  const res = await getDimensionAvg(academicYear.value) as unknown as { data: { dimensions: string[]; values: number[] } }
  dimAvg.dimensions = res.data.dimensions
  dimAvg.values = res.data.values
}

async function loadClusterDist() {
  const res = await getClusterDistribution(academicYear.value) as unknown as { data: { name: string; value: number }[] }
  clusterDist.value = res.data
}

async function loadScoreDist() {
  const res = await getScoreDistribution(academicYear.value) as unknown as { data: { labels: string[]; values: number[] } }
  scoreDist.labels = res.data.labels
  scoreDist.values = res.data.values
}

async function loadClassComp() {
  const res = await getClassComparison(academicYear.value) as unknown as { data: { classes: string[]; clusterNames: string[]; series: { name: string; data: number[] }[] } }
  classComp.classes = res.data.classes
  classComp.clusterNames = res.data.clusterNames
  classComp.series = res.data.series
}

function isValidYear(val: string) {
  return /^\d{4}-\d{4}$/.test(val)
}

function handleYearChange() {
  const val = academicYear.value?.trim()
  if (!val) return ElMessage.warning('请输入学年')
  if (!isValidYear(val)) return ElMessage.warning('学年格式应为 YYYY-YYYY，如 2023-2024')
  loadAll()
}

async function loadAll() {
  await Promise.all([loadDimAvg(), loadClusterDist(), loadScoreDist(), loadClassComp()])
}

onMounted(async () => {
  await loadOverview()
  await loadAll()
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

  .year-input {
    width: 240px;
  }
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;

  .stat-card {
    display: flex;
    align-items: center;
    gap: 16px;
    background: var(--color-bg-secondary);
    border: 1px solid var(--color-border);
    border-radius: 12px;
    padding: 20px;

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;

      &.student { background: rgba(77, 182, 172, 0.15); }
      &.class { background: rgba(255, 183, 77, 0.15); }
      &.eval { background: rgba(128, 203, 196, 0.15); }
      &.teacher { background: rgba(229, 115, 115, 0.15); }
    }

    .stat-info {
      display: flex;
      flex-direction: column;

      .stat-value {
        font-family: var(--font-mono);
        font-size: 28px;
        font-weight: 700;
        color: var(--color-text-primary);
        line-height: 1.2;
      }

      .stat-label {
        font-size: 13px;
        color: var(--color-text-secondary);
        margin-top: 4px;
      }
    }
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
    font-family: var(--font-display);
    font-size: 15px;
    color: var(--color-text-primary);
    margin-bottom: 12px;
    padding-left: 10px;
    border-left: 3px solid var(--color-accent-jade);
  }
}
</style>
