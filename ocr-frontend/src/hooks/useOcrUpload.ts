import { useCallback, useState } from 'react'
import { ocrService } from '@/services'
import type { UploadProgress } from '@/types'

interface UseOcrUploadOptions {
  onSuccess?: (result: unknown) => void
  onError?: (error: Error) => void
}

/**
 * OCR 上传 Hook
 */
export function useOcrUpload(options: UseOcrUploadOptions = {}) {
  const [isUploading, setIsUploading] = useState(false)
  const [progress, setProgress] = useState<UploadProgress>({
    loaded: 0,
    total: 0,
    percentage: 0,
  })
  const [error, setError] = useState<Error | null>(null)

  const handleProgress = useCallback((percentage: number) => {
    setProgress((prev) => ({
      ...prev,
      loaded: (percentage * prev.total) / 100,
      percentage,
    }))
  }, [])

  const uploadText = useCallback(
    async (file: File, generatePdf = false) => {
      setIsUploading(true)
      setError(null)
      setProgress({ loaded: 0, total: file.size, percentage: 0 })

      try {
        const formData = new FormData()
        formData.append('file', file)

        const result = await ocrService.recognizeText(
          formData,
          generatePdf,
          handleProgress,
        )
        options.onSuccess?.(result)
        return result
      } catch (err) {
        const error = err as Error
        setError(error)
        options.onError?.(error)
        throw error
      } finally {
        setIsUploading(false)
      }
    },
    [handleProgress, options],
  )

  const uploadLicensePlate = useCallback(
    async (file: File) => {
      setIsUploading(true)
      setError(null)
      setProgress({ loaded: 0, total: file.size, percentage: 0 })

      try {
        const formData = new FormData()
        formData.append('file', file)

        const result = await ocrService.recognizeLicensePlate(
          formData,
          handleProgress,
        )
        options.onSuccess?.(result)
        return result
      } catch (err) {
        const error = err as Error
        setError(error)
        options.onError?.(error)
        throw error
      } finally {
        setIsUploading(false)
      }
    },
    [handleProgress, options],
  )

  const reset = useCallback(() => {
    setIsUploading(false)
    setProgress({ loaded: 0, total: 0, percentage: 0 })
    setError(null)
  }, [])

  return {
    isUploading,
    progress,
    error,
    uploadText,
    uploadLicensePlate,
    reset,
  }
}
