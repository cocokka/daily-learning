import { Progress as AntdProgress } from 'antd'
import { CloseOutlined } from '@ant-design/icons'

interface UploadProgressProps {
  percentage: number
  status?: 'uploading' | 'success' | 'exception'
  showInfo?: boolean
  onCancel?: () => void
}

export function UploadProgress({
  percentage,
  status = 'uploading',
  showInfo = true,
  onCancel,
}: UploadProgressProps) {
  return (
    <div className="w-full space-y-2">
      <div className="flex items-center justify-between">
        {status === 'uploading' && (
          <span className="text-sm text-gray-600">上传中...</span>
        )}
        {status === 'success' && (
          <span className="text-sm text-green-600">上传完成</span>
        )}
        {status === 'exception' && (
          <span className="text-sm text-red-600">上传失败</span>
        )}
        {onCancel && status === 'uploading' && (
          <button
            onClick={onCancel}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <CloseOutlined className="text-lg" />
          </button>
        )}
      </div>
      <AntdProgress
        percent={percentage}
        status={status}
        showInfo={showInfo}
        strokeColor={{
          '0%': '#1677ff',
          '100%': '#52c41a',
        }}
      />
    </div>
  )
}
