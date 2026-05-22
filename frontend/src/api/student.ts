import request from './request'
import type { ApiResponse, PageResult, PageQuery } from '@/types/api'
import type { Student } from '@/types/student'

export function getStudentPage(params: PageQuery & { classId?: number }) {
  return request.get<ApiResponse<PageResult<Student>>>('/student/page', { params })
}

export function getMyStudent() {
  return request.get<ApiResponse<Student>>('/student/my')
}

export function getChildren() {
  return request.get<ApiResponse<Student[]>>('/student/children')
}

export function getStudentsByClass(classId: number) {
  return request.get<ApiResponse<Student[]>>(`/student/byClass/${classId}`)
}

export function createStudent(data: Partial<Student>) {
  return request.post<ApiResponse<string>>('/student', data)
}

export function updateStudent(data: Partial<Student>) {
  return request.put<ApiResponse<string>>('/student', data)
}

export function deleteStudent(id: number) {
  return request.delete<ApiResponse<string>>(`/student/${id}`)
}
