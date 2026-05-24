export interface User {
  id: number
  username: string
  realName: string
  role: 'admin' | 'teacher' | 'student' | 'parent'
  avatar: string | null
  phone: string | null
  email: string | null
  gender: number | null
  status: number
  createTime: string
  updateTime: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginData {
  token: string
  userId: number
  username: string
  realName: string
  role: string
  avatar: string | null
}

export interface RegisterRequest {
  username: string
  password: string
  realName: string
  role: string
  phone?: string
  email?: string
  gender?: number
}

export interface ProfileUpdateRequest {
  realName: string
  gender: number
  phone: string
  email: string
  avatar: string
}

export interface PasswordUpdateRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}
