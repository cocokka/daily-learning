import { request } from './api'
import type {
  OcrResponse,
  OcrRecord,
  PageResponse,
  LicensePlateResponse,
  SearchParams,
} from '@/types'

/**
 * OCR 识别服务
 */
export const ocrService = {
  /**
   * 文字识别
   * @param formData - 包含图片文件的 FormData
   * @param generatePdf - 是否生成 PDF
   * @param onProgress - 上传进度回调
   */
  recognizeText: async (
    formData: FormData,
    generatePdf = false,
    onProgress?: (percentage: number) => void,
  ): Promise<OcrResponse> => {
    if (generatePdf) {
      formData.append('generatePdf', 'true')
    }
    return request.upload<OcrResponse>(
      '/ocr/recognize/text',
      formData,
      onProgress,
    )
  },

  /**
   * 车牌识别
   * @param formData - 包含图片文件的 FormData
   * @param onProgress - 上传进度回调
   */
  recognizeLicensePlate: async (
    formData: FormData,
    onProgress?: (percentage: number) => void,
  ): Promise<LicensePlateResponse> => {
    return request.upload<LicensePlateResponse>(
      '/ocr/recognize/license-plate',
      formData,
      onProgress,
    )
  },

  /**
   * 根据 taskId 获取识别结果
   */
  getResultByTaskId: async (taskId: string): Promise<OcrResponse> => {
    return request.get<OcrResponse>(`/ocr/result/${taskId}`)
  },

  /**
   * 获取所有 OCR 记录（分页）
   */
  getRecords: async (page = 0, size = 10): Promise<PageResponse<OcrRecord>> => {
    return request.get<PageResponse<OcrRecord>>('/ocr/records', {
      params: { page, size },
    })
  },

  /**
   * 搜索 OCR 结果
   */
  search: async (params: SearchParams): Promise<PageResponse<OcrRecord>> => {
    return request.get<PageResponse<OcrRecord>>('/ocr/search', { params })
  },

  /**
   * 下载原图
   */
  downloadImage: async (taskId: string): Promise<Blob> => {
    return request.get(`/ocr/records/image/${taskId}`, {
      responseType: 'blob',
    })
  },

  /**
   * 删除记录
   */
  deleteRecord: async (id: number): Promise<void> => {
    return request.delete(`/ocr/record/${id}`)
  },
}
