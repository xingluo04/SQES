import request from './request'

export const executeCluster = (academicYear: string, clusterCount: number) =>
  request.post('/cluster/execute', null, { params: { academicYear, clusterCount } })

export const getClusterResults = (academicYear: string) =>
  request.get('/cluster/results', { params: { academicYear } })

export const getOptimalK = (academicYear: string, minK = 2, maxK = 10) =>
  request.get('/cluster/optimal-k', { params: { academicYear, minK, maxK } })
