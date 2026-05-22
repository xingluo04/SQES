import request from './request'
import type { ApiResponse, PageResult, PageQuery } from '@/types/api'
import type { ClassInfo } from '@/types/class'

export function getClassPage(params: PageQuery) {
  return request.get<ApiResponse<PageResult<ClassInfo>>>('/class/page', { params })
}

export function getClassList() {
  return request.get<ApiResponse<ClassInfo[]>>('/class/list')
}

export function createClass(data: Partial<ClassInfo>) {
  return request.post<ApiResponse<string>>('/class', data)
}

export function updateClass(data: Partial<ClassInfo>) {
  return request.put<ApiResponse<string>>('/class', data)
}

export function deleteClass(id: number) {
  return request.delete<ApiResponse<string>>(`/class/${id}`)
}
