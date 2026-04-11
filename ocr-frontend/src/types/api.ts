/**
 * API 响应基础类型
 */
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data?: T
}

/**
 * 通用错误响应
 */
export interface ErrorResponse {
  code: string
  message: string
  details?: Record<string, string[]>
  timestamp: string
  path?: string
}
