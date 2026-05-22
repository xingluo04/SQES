import request from './request'

export const getDashboardOverview = () =>
  request.get('/dashboard/overview')

export const getDimensionAvg = (academicYear: string) =>
  request.get('/dashboard/dimension-avg', { params: { academicYear } })

export const getClusterDistribution = (academicYear: string) =>
  request.get('/dashboard/cluster-distribution', { params: { academicYear } })

export const getScoreDistribution = (academicYear: string) =>
  request.get('/dashboard/score-distribution', { params: { academicYear } })

export const getClassComparison = (academicYear: string) =>
  request.get('/dashboard/class-comparison', { params: { academicYear } })
