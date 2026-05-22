import request from './request'
import type { ApiResponse, PageResult, PageQuery } from '@/types/api'
import type { Notice } from '@/types/notice'

export function getNoticePage(params: PageQuery) {
  return request.get<ApiResponse<PageResult<Notice>>>('/notice/page', { params })
}

export function getNoticeDetail(id: number) {
  return request.get<ApiResponse<Notice>>(`/notice/${id}`)
}

export function createNotice(data: Partial<Notice>) {
  return request.post<ApiResponse<string>>('/notice', data)
}

export function updateNotice(data: Partial<Notice>) {
  return request.put<ApiResponse<string>>('/notice', data)
}

export function deleteNotice(id: number) {
  return request.delete<ApiResponse<string>>(`/notice/${id}`)
}
