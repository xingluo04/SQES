<template>
  <div class="page-view">
    <div class="page-header">
      <h2 class="page-title">聚类分析</h2>
    </div>

    <!-- 控制面板 -->
    <div class="control-card">
      <div class="control-row">
        <el-input v-model="academicYear" placeholder="学年 如 2023-2024" clearable class="year-input" />
        <el-input-number v-model="clusterCount" :min="2" :max="10" :step="1" />
        <el-button type="primary" :loading="executing" @click="handleExecute">执行聚类</el-button>
        <el-button @click="handleGetOptimalK" :loading="loadingK">推荐K值</el-button>
      </div>
      <div v-if="optimalKData" class="optimal-k-info">
        <el-tag type="success" size="large">推荐K值：{{ optimalKData.optimalK }}</el-tag>
        <span class="vote-info">
          Gap投票={{ optimalKData.voteDetails.gapVote }},
          轮廓投票={{ optimalKData.voteDetails.silhouetteVote }},
          DBI投票={{ optimalKData.voteDetails.dbiVote }},
          CHI投票={{ optimalKData.voteDetails.chiVote }}
        </span>
      </div>
    </div>

    <!-- 无数据提示 -->
    <el-empty v-if="!resultData && !loading" description="暂无聚类结果，请点击「执行聚类」开始分析" />

    <template v-if="resultData">
      <!-- 评估指标卡片 -->
      <div class="metric-cards">
        <div class="metric-card">
          <span class="metric-label">轮廓系数</span>
          <span class="metric-value jade">{{ resultData.silhouetteScore?.toFixed(4) }}</span>
          <span class="metric-desc">越接近1越好</span>
        </div>
        <div class="metric-card">
          <span class="metric-label">DBI 指数</span>
          <span class="metric-value amber">{{ resultData.daviesBouldinIndex?.toFixed(4) }}</span>
          <span class="metric-desc">越小越好</span>
        </div>
        <div class="metric-card">
          <span class="metric-label">CHI 指数</span>
          <span class="metric-value vermilion">{{ resultData.calinskiHarabaszIndex?.toFixed(2) }}</span>
          <span class="metric-desc">越大越好</span>
        </div>
        <div class="metric-card">
          <span class="metric-label">聚类数量</span>
          <span class="metric-value primary">{{ resultData.clusters?.length || 0 }}</span>
          <span class="metric-desc">分组数</span>
        </div>
      </div>

      <!-- 聚类分布饼图 + 聚类均分雷达图 -->
      <div class="chart-row">
        <div class="chart-card" v-if="resultData.clusters?.length">
          <h3 class="chart-title">聚类分布</h3>
          <v-chart :option="pieOption" autoresize style="height: 360px" />
        </div>
        <div class="chart-card">
          <h3 class="chart-title">聚类均分雷达图</h3>
          <v-chart :option="radarOption" autoresize style="height: 360px" />
        </div>
      </div>

      <!-- PCA 散点图 + 相关性矩阵热力图 -->
      <div class="chart-row">
        <div class="chart-card">
          <h3 class="chart-title">PCA 降维散点图</h3>
          <v-chart :option="scatterOption" autoresize style="height: 360px" />
        </div>
        <div class="chart-card" v-if="resultData.correlationMatrix">
          <h3 class="chart-title">维度相关性矩阵</h3>
          <v-chart :option="heatmapOption" autoresize style="height: 360px" />
        </div>
      </div>

      <!-- 聚类分组表格 -->
      <div class="table-card">
        <h3 class="chart-title">聚类分组概览</h3>
        <el-table :data="resultData.clusters" stripe>
          <el-table-column prop="clusterLabel" label="编号" width="70" align="center" />
          <el-table-column prop="clusterName" label="分组名称" width="140" />
          <el-table-column prop="studentCount" label="学生数" width="80" align="center" />
          <el-table-column prop="avgMoral" label="德育均分" width="90" align="center">
            <template #default="{ row }"><span class="score-dim">{{ row.avgMoral }}</span></template>
          </el-table-column>
          <el-table-column prop="avgAcademic" label="智育均分" width="90" align="center">
            <template #default="{ row }"><span class="score-dim">{{ row.avgAcademic }}</span></template>
          </el-table-column>
          <el-table-column prop="avgPhysical" label="体育均分" width="90" align="center">
            <template #default="{ row }"><span class="score-dim">{{ row.avgPhysical }}</span></template>
          </el-table-column>
          <el-table-column prop="avgArt" label="美育均分" width="90" align="center">
            <template #default="{ row }"><span class="score-dim">{{ row.avgArt }}</span></template>
          </el-table-column>
          <el-table-column prop="avgPractice" label="劳动均分" width="90" align="center">
            <template #default="{ row }"><span class="score-dim">{{ row.avgPractice }}</span></template>
          </el-table-column>
          <el-table-column prop="description" label="特征描述" min-width="200" show-overflow-tooltip />
        </el-table>
      </div>

      <!-- 描述性统计 + ANOVA -->
      <div class="chart-row">
        <div class="table-card">
          <h3 class="chart-title">描述性统计</h3>
          <el-table :data="resultData.descriptiveStats?.dimensions" stripe size="small" style="width: 100%">
            <el-table-column prop="label" label="维度" min-width="80" />
            <el-table-column prop="mean" label="均值" min-width="70" align="center" />
            <el-table-column prop="std" label="标准差" min-width="70" align="center" />
            <el-table-column prop="cv" label="变异系数" min-width="80" align="center">
              <template #default="{ row }">{{ row.cv?.toFixed(4) }}</template>
            </el-table-column>
            <el-table-column prop="min" label="最小值" min-width="70" align="center" />
            <el-table-column prop="max" label="最大值" min-width="70" align="center" />
            <el-table-column prop="lowDiscrimination" label="低区分度" min-width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.lowDiscrimination ? 'warning' : 'success'" size="small">
                  {{ row.lowDiscrimination ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="table-card" v-if="resultData.anovaResult">
          <h3 class="chart-title">ANOVA 显著性检验</h3>
          <el-table :data="resultData.anovaResult?.dimensions" stripe size="small" style="width: 100%">
            <el-table-column prop="dimension" label="维度" min-width="60" />
            <el-table-column prop="fValue" label="F值" min-width="80" align="center">
              <template #default="{ row }">{{ row.fValue?.toFixed(3) }}</template>
            </el-table-column>
            <el-table-column prop="pValue" label="P值" min-width="80" align="center">
              <template #default="{ row }">{{ row.pValue?.toFixed(4) }}</template>
            </el-table-column>
            <el-table-column prop="etaSquared" label="η²" min-width="70" align="center">
              <template #default="{ row }">{{ row.etaSquared?.toFixed(4) }}</template>
            </el-table-column>
            <el-table-column prop="highlySignificant" label="极显著" min-width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.highlySignificant ? 'success' : 'info'" size="small">
                  {{ row.highlySignificant ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 最优K分析图 -->
      <div class="chart-card" v-if="optimalKData">
        <h3 class="chart-title">最优K值分析</h3>
        <v-chart :option="optimalKOption" autoresize style="height: 340px" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { ScatterChart, RadarChart, LineChart, BarChart, HeatmapChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent, VisualMapComponent, RadarComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { executeCluster, getClusterResults, getOptimalK } from '@/api/cluster'
import type { ClusterResult, OptimalKResult } from '@/types/cluster'

use([ScatterChart, RadarChart, LineChart, BarChart, HeatmapChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, VisualMapComponent, RadarComponent, CanvasRenderer])

const chartColors = ['#4DB6AC', '#E57373', '#FFB74D', '#80CBC4', '#BCAAA4', '#90CAF9', '#CE93D8', '#A5D6A7', '#FFCC80', '#EF9A9A']
const dimLabels = ['德育', '智育', '体育', '美育', '劳动']

const academicYear = ref('2024-2025')
const clusterCount = ref(5)
const executing = ref(false)
const loadingK = ref(false)
const loading = ref(false)
const resultData = ref<ClusterResult | null>(null)
const optimalKData = ref<OptimalKResult | null>(null)

async function handleExecute() {
  if (!academicYear.value) return ElMessage.warning('请输入学年')
  executing.value = true
  loading.value = true
  try {
    await executeCluster(academicYear.value, clusterCount.value)
    const res = await getClusterResults(academicYear.value) as unknown as { data: ClusterResult }
    resultData.value = res.data
    ElMessage.success('聚类分析完成')
  } catch {
    ElMessage.error('聚类执行失败')
  } finally {
    executing.value = false
    loading.value = false
  }
}

async function handleGetOptimalK() {
  if (!academicYear.value) return ElMessage.warning('请输入学年')
  loadingK.value = true
  try {
    const res = await getOptimalK(academicYear.value) as unknown as { data: OptimalKResult }
    optimalKData.value = res.data
    clusterCount.value = res.data.optimalK
    ElMessage.success(`推荐K值：${res.data.optimalK}`)
  } catch {
    ElMessage.error('获取推荐K值失败')
  } finally {
    loadingK.value = false
  }
}

async function loadResults() {
  if (!academicYear.value) return
  loading.value = true
  try {
    const res = await getClusterResults(academicYear.value) as unknown as { data: ClusterResult }
    if (res.data?.clusters?.length) {
      resultData.value = res.data
    }
  } catch {
    // 无历史结果，忽略
  } finally {
    loading.value = false
  }
}

onMounted(loadResults)

/* PCA 散点图 */
const scatterOption = computed(() => {
  if (!resultData.value?.pcaPoints?.length) return {}
  const points = resultData.value.pcaPoints
  const clusters = resultData.value.clusters || []
  const series = clusters.map((c, i) => ({
    name: c.clusterName || `簇${c.clusterLabel}`,
    type: 'scatter' as const,
    data: points.filter(p => p.cluster === c.clusterLabel).map(p => [p.x, p.y]),
    symbolSize: 8,
    itemStyle: { color: chartColors[i % chartColors.length] },
  }))
  return {
    color: chartColors,
    tooltip: { trigger: 'item' },
    legend: { data: clusters.map(c => c.clusterName || `簇${c.clusterLabel}`), textStyle: { color: '#9CA8B7' }, top: 0 },
    grid: { left: 50, right: 20, top: 40, bottom: 40 },
    xAxis: { name: 'PC1', nameTextStyle: { color: '#9CA8B7' }, axisLabel: { color: '#9CA8B7' }, splitLine: { lineStyle: { color: '#233040' } } },
    yAxis: { name: 'PC2', nameTextStyle: { color: '#9CA8B7' }, axisLabel: { color: '#9CA8B7' }, splitLine: { lineStyle: { color: '#233040' } } },
    series,
  }
})

/* 雷达图 */
const radarOption = computed(() => {
  if (!resultData.value?.clusters?.length) return {}
  const clusters = resultData.value.clusters
  return {
    color: chartColors,
    tooltip: {},
    legend: { data: clusters.map(c => c.clusterName), textStyle: { color: '#9CA8B7' }, top: 0, type: 'scroll' },
    radar: {
      indicator: dimLabels.map(l => ({ name: l, max: 100 })),
      shape: 'polygon',
      splitNumber: 4,
      axisName: { color: '#9CA8B7', fontSize: 12 },
      splitLine: { lineStyle: { color: '#2A3A4A' } },
      splitArea: { areaStyle: { color: ['rgba(77,182,172,0.02)', 'rgba(77,182,172,0.06)'] } },
      axisLine: { lineStyle: { color: '#2A3A4A' } },
    },
    series: [{
      type: 'radar',
      data: clusters.map((c, i) => ({
        name: c.clusterName,
        value: [c.avgMoral, c.avgAcademic, c.avgPhysical, c.avgArt, c.avgPractice],
        areaStyle: { color: chartColors[i] + '20' },
        lineStyle: { color: chartColors[i], width: 2 },
        itemStyle: { color: chartColors[i] },
      })),
    }],
  }
})

/* 聚类分布饼图 */
const pieOption = computed(() => {
  if (!resultData.value?.clusters?.length) return {}
  const clusters = resultData.value.clusters
  const total = clusters.reduce((s, c) => s + c.studentCount, 0)
  return {
    color: chartColors,
    tooltip: {
      formatter: (p: any) => `${p.name}：${p.value}人 (${p.percent}%)`
    },
    legend: {
      orient: 'vertical',
      right: 20,
      top: 'center',
      formatter: (name: string) => {
        const c = clusters.find(cl => cl.clusterName === name)
        return c ? `${name}  {dim|${c.studentCount}人}` : name
      },
      textStyle: {
        color: '#C0B8B0',
        rich: {
          dim: { color: '#9CA8B7', fontSize: 11, padding: [0, 0, 0, 6] }
        }
      }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#1B2838', borderWidth: 2 },
      label: {
        show: true,
        formatter: '{b}\n{d}%',
        color: '#C0B8B0',
        fontSize: 11,
      },
      labelLine: { lineStyle: { color: '#2A3A4A' } },
      emphasis: {
        label: { fontSize: 14, fontWeight: 'bold' },
        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' }
      },
      data: clusters.map(c => ({
        name: c.clusterName,
        value: c.studentCount,
      })),
    }],
  }
})

/* 相关性热力图 */
const heatmapOption = computed(() => {
  const matrix = resultData.value?.correlationMatrix
  if (!matrix?.length) return {}
  const data: [number, number, number][] = []
  for (let i = 0; i < matrix.length; i++) {
    for (let j = 0; j < matrix[i].length; j++) {
      data.push([j, i, matrix[i][j]])
    }
  }
  return {
    tooltip: { formatter: (p: any) => `${dimLabels[p.data[1]]} × ${dimLabels[p.data[0]]}：${p.data[2].toFixed(4)}` },
    grid: { left: 80, right: 40, top: 20, bottom: 50 },
    xAxis: { type: 'category', data: dimLabels, axisLabel: { color: '#9CA8B7' }, axisLine: { lineStyle: { color: '#2A3A4A' } }, splitArea: { show: true, areaStyle: { color: ['rgba(27,40,56,0.3)', 'rgba(27,40,56,0.6)'] } } },
    yAxis: { type: 'category', data: dimLabels, axisLabel: { color: '#9CA8B7' }, axisLine: { lineStyle: { color: '#2A3A4A' } } },
    visualMap: { min: 0, max: 1, calculable: true, orient: 'horizontal', left: 'center', bottom: 0, inRange: { color: ['#1B2838', '#2A3F55', '#4DB6AC', '#81C784'] }, textStyle: { color: '#9CA8B7' } },
    series: [{
      type: 'heatmap',
      data,
      label: { show: true, formatter: (p: any) => p.data[2].toFixed(2), color: '#E8E0D8', fontSize: 11 },
      itemStyle: { borderColor: '#1B2838', borderWidth: 2 },
    }],
  }
})

/* 最优K分析图 */
const optimalKOption = computed(() => {
  if (!optimalKData.value) return {}
  const d = optimalKData.value
  return {
    color: ['#4DB6AC', '#E57373', '#FFB74D', '#80CBC4'],
    tooltip: { trigger: 'axis' },
    legend: { data: ['Gap统计量', '轮廓系数', 'DBI指数', 'CHI指数'], textStyle: { color: '#9CA8B7' }, top: 0 },
    grid: { left: 60, right: 60, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: d.gapByK.map(g => `K=${g.k}`), axisLabel: { color: '#9CA8B7' }, axisLine: { lineStyle: { color: '#2A3A4A' } } },
    yAxis: [
      { type: 'value', name: 'Gap / 轮廓', nameTextStyle: { color: '#9CA8B7' }, axisLabel: { color: '#9CA8B7' }, splitLine: { lineStyle: { color: '#233040' } } },
      { type: 'value', name: 'DBI / CHI', nameTextStyle: { color: '#9CA8B7' }, axisLabel: { color: '#9CA8B7' }, splitLine: { show: false } },
    ],
    series: [
      { name: 'Gap统计量', type: 'line', data: d.gapByK.map(g => +g.value.toFixed(4)), smooth: true, symbol: 'circle', symbolSize: 6 },
      { name: '轮廓系数', type: 'line', data: d.silhouetteByK.map(g => +g.value.toFixed(4)), smooth: true, symbol: 'circle', symbolSize: 6 },
      { name: 'DBI指数', type: 'line', yAxisIndex: 1, data: d.dbiByK.map(g => +g.value.toFixed(4)), smooth: true, symbol: 'diamond', symbolSize: 6 },
      { name: 'CHI指数', type: 'line', yAxisIndex: 1, data: d.chiByK.map(g => +g.value.toFixed(2)), smooth: true, symbol: 'triangle', symbolSize: 6 },
    ],
  }
})
</script>

<style scoped lang="scss">
.page-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;
  .page-title { font-family: var(--font-display); font-size: 22px; color: var(--color-text-primary); }
}

.control-card {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;

  .control-row {
    display: flex;
    align-items: center;
    gap: 12px;

    .year-input { width: 200px; }
  }

  .optimal-k-info {
    margin-top: 12px;
    display: flex;
    align-items: center;
    gap: 16px;

    .vote-info {
      font-size: 13px;
      color: var(--color-text-secondary);
    }
  }
}

.metric-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;

  .metric-card {
    background: var(--color-bg-secondary);
    border: 1px solid var(--color-border);
    border-radius: 12px;
    padding: 16px;
    text-align: center;

    .metric-label {
      display: block;
      font-size: 13px;
      color: var(--color-text-secondary);
      margin-bottom: 8px;
    }

    .metric-value {
      display: block;
      font-family: var(--font-mono);
      font-size: 28px;
      font-weight: 700;
      margin-bottom: 4px;

      &.jade { color: var(--color-accent-jade); }
      &.amber { color: var(--color-accent-amber); }
      &.vermilion { color: var(--color-accent-vermilion); }
      &.primary { color: var(--color-text-primary); }
    }

    .metric-desc {
      font-size: 11px;
      color: var(--color-text-secondary);
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
  margin-bottom: 20px;

  .chart-title {
    font-family: var(--font-display);
    font-size: 15px;
    color: var(--color-text-primary);
    margin-bottom: 12px;
    padding-left: 10px;
    border-left: 3px solid var(--color-accent-jade);
  }
}

.table-card {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;

  .chart-title {
    font-family: var(--font-display);
    font-size: 15px;
    color: var(--color-text-primary);
    margin-bottom: 12px;
    padding-left: 10px;
    border-left: 3px solid var(--color-accent-jade);
  }
}

.score-dim { font-family: var(--font-mono); font-size: 13px; }
</style>
