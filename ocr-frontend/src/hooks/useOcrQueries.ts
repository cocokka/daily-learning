import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ocrService } from '@/services'
import type { SearchParams, OcrRecord } from '@/types'

const QUERY_KEYS = {
  RECORDS: 'ocrRecords' as const,
  RECORD: (taskId: string) => ['ocrRecord', taskId] as const,
  SEARCH: (params: SearchParams) => ['ocrSearch', params] as const,
}

/**
 * 获取 OCR 记录列表
 */
export function useOcrRecords(page = 0, size = 10) {
  return useQuery({
    queryKey: [QUERY_KEYS.RECORDS, page, size],
    queryFn: () => ocrService.getRecords(page, size),
  })
}

/**
 * 根据 taskId 获取 OCR 结果
 */
export function useOcrResult(taskId: string | null) {
  return useQuery({
    queryKey: QUERY_KEYS.RECORD(taskId || ''),
    queryFn: () => ocrService.getResultByTaskId(taskId!),
    enabled: !!taskId,
    refetchInterval: (query) => {
      const data = query.state.data?.data
      if (data?.status === 'PROCESSING') {
        return 2000 // 轮询间隔 2 秒
      }
      return false
    },
  })
}

/**
 * 搜索 OCR 结果
 */
export function useOcrSearch(params: SearchParams) {
  return useQuery({
    queryKey: QUERY_KEYS.SEARCH(params),
    queryFn: () => ocrService.search(params),
  })
}

/**
 * 删除 OCR 记录
 */
export function useDeleteRecord() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => ocrService.deleteRecord(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.RECORDS] })
    },
  })
}

/**
 * 批量删除 OCR 记录
 */
export function useDeleteRecords() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (ids: number[]) => {
      await Promise.all(ids.map((id) => ocrService.deleteRecord(id)))
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.RECORDS] })
    },
  })
}

/**
 * 下载图片
 */
export function useDownloadImage() {
  return useMutation({
    mutationFn: async (taskId: string) => {
      const blob = await ocrService.downloadImage(taskId)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `ocr-image-${taskId}.png`
      a.click()
      URL.revokeObjectURL(url)
    },
  })
}

export { QUERY_KEYS }
