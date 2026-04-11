import { useState, useCallback } from 'react'
import { config } from '@/config'

interface UseFileUploadOptions {
  maxSize?: number // MB
  allowedTypes?: string[]
  allowedExtensions?: string[]
}

/**
 * 文件上传验证 Hook
 */
export function useFileUpload(options: UseFileUploadOptions = {}) {
  const {
    maxSize = config.uploadMaxSize,
    allowedTypes = config.allowedImageTypes,
    allowedExtensions = config.allowedExtensions,
  } = options

  const [error, setError] = useState<string | null>(null)

  const validateFile = useCallback(
    (file: File): boolean => {
      // 检查文件大小
      if (file.size > maxSize * 1024 * 1024) {
        setError(`文件大小不能超过 ${maxSize}MB`)
        return false
      }

      // 检查文件类型
      if (!allowedTypes.includes(file.type)) {
        setError('不支持的文件类型')
        return false
      }

      // 检查文件扩展名
      const ext = '.' + file.name.split('.').pop()?.toLowerCase()
      if (!allowedExtensions.includes(ext)) {
        setError('不支持的文件格式')
        return false
      }

      setError(null)
      return true
    },
    [maxSize, allowedTypes, allowedExtensions],
  )

  const resetError = useCallback(() => {
    setError(null)
  }, [])

  return {
    error,
    validateFile,
    resetError,
  }
}
