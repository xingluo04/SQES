export interface OperationLog {
  id: number
  userId: number
  username: string
  module: string
  operation: string
  method: string
  params: string
  ip: string
  result: 'SUCCESS' | 'FAIL'
  errorMsg?: string
  costTime?: number
  createTime: string
}

export interface LogPageQuery {
  current: number
  size: number
  keyword?: string
  module?: string
  result?: string
  startTime?: string
  endTime?: string
}
