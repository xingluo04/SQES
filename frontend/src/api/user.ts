import request from './request'
import type { ApiResponse, PageResult, PageQuery } from '@/types/api'
import type { User } from '@/types/user'

export function getUserPage(params: PageQuery & { role?: string }) {
  return request.get<ApiResponse<PageResult<User>>>('/user/page', { params })
}

export function createUser(data: Partial<User> & { password?: string }) {
  return request.post<ApiResponse<string>>('/user', data)
}

export function updateUser(data: Partial<User>) {
  return request.put<ApiResponse<string>>('/user', data)
}

export function deleteUser(id: number) {
  return request.delete<ApiResponse<string>>(`/user/${id}`)
}

export function resetPassword(id: number) {
  return request.put<ApiResponse<string>>(`/user/resetPwd/${id}`)
}

export function changeStatus(id: number, status: number) {
  return request.put<ApiResponse<string>>(`/user/status/${id}/${status}`)
}
