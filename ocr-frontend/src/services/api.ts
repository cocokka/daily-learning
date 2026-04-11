import axios, { AxiosError, AxiosRequestConfig, AxiosInstance } from 'axios'
import { config } from '@/config'
import { ErrorResponse } from '@/types'

/**
 * 创建 axios 实例
 */
const apiClient: AxiosInstance = axios.create({
  baseURL: config.apiBasePath,
  timeout: config.timeout,
  headers: {
    'Content-Type': 'application/json',
  },
})

/**
 * 请求拦截器
 */
apiClient.interceptors.request.use(
  (config) => {
    // 添加 traceId
    const traceId = `trace-${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
    if (config.headers) {
      config.headers['X-Trace-Id'] = traceId
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

/**
 * 响应拦截器
 */
apiClient.interceptors.response.use(
  (response) => {
    return response
  },
  (error: AxiosError<ErrorResponse>) => {
    // 统一错误处理
    if (error.response) {
      const { status, data } = error.response
      console.error(`API Error [${status}]:`, data?.message || error.message)
    } else if (error.request) {
      console.error('Network Error: Unable to reach the server')
    } else {
      console.error('Request Error:', error.message)
    }
    return Promise.reject(error)
  },
)

/**
 * 通用的 API 请求方法
 */
export const request = {
  get<T>(url: string, config?: AxiosRequestConfig) {
    return apiClient.get<T>(url, config).then((res) => res.data)
  },

  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return apiClient.post<T>(url, data, config).then((res) => res.data)
  },

  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return apiClient.put<T>(url, data, config).then((res) => res.data)
  },

  patch<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return apiClient.patch<T>(url, data, config).then((res) => res.data)
  },

  delete<T>(url: string, config?: AxiosRequestConfig) {
    return apiClient.delete<T>(url, config).then((res) => res.data)
  },

  upload<T>(
    url: string,
    formData: FormData,
    onProgress?: (percentage: number) => void,
    config?: AxiosRequestConfig,
  ) {
    return apiClient
      .post<T>(url, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        onUploadProgress: (event) => {
          if (onProgress && event.total) {
            const percentage = Math.round((event.loaded * 100) / event.total)
            onProgress(percentage)
          }
        },
        ...config,
      })
      .then((res) => res.data)
  },
}

export default apiClient
