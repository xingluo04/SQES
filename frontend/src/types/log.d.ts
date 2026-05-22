export interface OperationLog {
  id: number
  userId: number
  username: string
  operation: string
  method: string
  params: string
  ip: string
  createTime: string
}
