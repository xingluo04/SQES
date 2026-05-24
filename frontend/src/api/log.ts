import request from './request'
import type { ApiResponse, PageResult } from '@/types/api'
import type { LogPageQuery, OperationLog } from '@/types/log'

export function getLogPage(params: LogPageQuery) {
  return request.get<ApiResponse<PageResult<OperationLog>>>('/log/page', { params })
}

export function cleanLogs(beforeTime: string) {
  return request.delete<ApiResponse<string>>('/log/clean', { params: { beforeTime } })
}
