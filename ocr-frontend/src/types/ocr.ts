/**
 * OCR 类型定义
 */

// OCR 识别类型
export type OcrType = 'TEXT' | 'LICENSE_PLATE' | 'ID_CARD'

// OCR 状态
export type OcrStatus = 'PENDING' | 'PROCESSING' | 'SUCCESS' | 'FAILED'

// OCR 请求参数
export interface OcrRecognizeRequest {
  file: File
  generatePdf?: boolean
}

// OCR 识别结果
export interface OcrResult {
  taskId: string
  recognizedText: string
  confidence?: number
  keywords?: string[]
  ocrType: OcrType
  licensePlateNumber?: string
  plateConfidence?: number
  plateColor?: string
}

// OCR 响应
export interface OcrResponse {
  code: number
  message: string
  data?: OcrResult
}

// OCR 记录
export interface OcrRecord {
  id: number
  taskId: string
  fileName: string
  filePath: string
  recognizedText: string
  ocrType: OcrType
  licensePlateNumber?: string
  pdfUrl?: string
  status: OcrStatus
  confidence?: number
  createTime: string
  updateTime: string
}

// 分页响应
export interface PageResponse<T> {
  content: T[]
  pageable: {
    pageNumber: number
    pageSize: number
    sort: {
      empty: boolean
      sorted: boolean
      unsorted: boolean
    }
  }
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
  empty: boolean
}

// 车牌识别结果
export interface LicensePlateResult {
  plateNumber: string
  confidence: number
  plateColor?: string
  plateType?: string
  region?: string
}

// 车牌识别响应
export interface LicensePlateResponse {
  code: number
  message: string
  data?: LicensePlateResult
}

// 搜索参数
export interface SearchParams {
  keyword?: string
  ocrType?: OcrType
  status?: OcrStatus
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

// 上传进度
export interface UploadProgress {
  loaded: number
  total: number
  percentage: number
}
