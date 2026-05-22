import request from './request'

export function importExcel(file: File, academicYear: string) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('academicYear', academicYear)
  return request.post('/import/excel', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000,
  })
}
