import request from './request'
import type { ApiResponse } from '@/types/api'

export function importExcel(file: File, academicYear: string) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('academicYear', academicYear)
  return request.post('/import/excel', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000,
  })
}

export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResponse<string>>('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000,
  })
}
