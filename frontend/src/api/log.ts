import request from './request'
import type { ApiResponse, PageResult, PageQuery } from '@/types/api'
import type { OperationLog } from '@/types/log'

export function getLogPage(params: PageQuery) {
  return request.get<ApiResponse<PageResult<OperationLog>>>('/log/page', { params })
}

export function clearLogs() {
  return request.delete<ApiResponse<string>>('/log/clear')
}
