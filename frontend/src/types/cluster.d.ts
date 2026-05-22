/** 聚类分析类型定义 */

/** 最优K查询结果 */
export interface OptimalKResult {
  optimalK: number
  gapOptimalK: number
  kRange: { min: number; max: number }
  gapByK: MetricPoint[]
  gapSEByK: MetricPoint[]
  dbiByK: MetricPoint[]
  silhouetteByK: MetricPoint[]
  chiByK: MetricPoint[]
  voteDetails: {
    silhouetteVote: number
    dbiVote: number
    chiVote: number
    gapVote: number
  }
}

export interface MetricPoint {
  k: number
  value: number
}

/** 聚类执行结果 */
export interface ClusterResult {
  silhouetteScore: number
  calinskiHarabaszIndex: number
  daviesBouldinIndex: number
  descriptiveStats: DescriptiveStats
  pcaPoints: PcaPoint[]
  anovaResult: AnovaResult | null
  correlationMatrix: number[][]
  clusters: ClusterInfo[]
}

export interface DescriptiveStats {
  dimensions: DimensionStat[]
}

export interface DimensionStat {
  dimension: string
  label: string
  mean: number
  std: number
  cv: number
  min: number
  max: number
  lowDiscrimination: boolean
}

export interface PcaPoint {
  cluster: number
  x: number
  y: number
}

export interface AnovaResult {
  dimensions: AnovaDimension[]
}

export interface AnovaDimension {
  dimension: string
  fValue: number
  dfBetween: number
  dfWithin: number
  pValue: number
  significant: boolean
  highlySignificant: boolean
  etaSquared: number
}

export interface ClusterInfo {
  id: number
  academicYear: string
  clusterCount: number
  silhouetteScore: number
  daviesBouldinIndex: number
  calinskiHarabaszIndex: number
  clusterLabel: number
  clusterName: string
  studentCount: number
  avgMoral: number
  avgAcademic: number
  avgPhysical: number
  avgArt: number
  avgPractice: number
  description: string
  createTime: string
}
