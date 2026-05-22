import request from './request'
import type { ApiResponse } from '@/types/api'
import type { LoginRequest, LoginData, RegisterRequest, User } from '@/types/user'

export function login(data: LoginRequest) {
  return request.post<ApiResponse<LoginData>>('/auth/login', data)
}

export function register(data: RegisterRequest) {
  return request.post<ApiResponse<string>>('/auth/register', data)
}

export function getUserInfo() {
  return request.get<ApiResponse<User>>('/user/info')
}
