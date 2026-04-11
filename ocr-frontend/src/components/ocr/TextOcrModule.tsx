import { useState, useCallback } from 'react'
import { Card, Button, Space, Switch, message } from 'antd'
import { ScanOutlined, FilePdfOutlined } from '@ant-design/icons'
import { ImageUpload, UploadProgress, Result } from '@/components/common'
import { useOcrUpload } from '@/hooks'
import type { OcrResponse } from '@/types'
import { useNavigate } from 'react-router-dom'

export function TextOcrModule() {
  const navigate = useNavigate()
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [generatePdf, setGeneratePdf] = useState(false)

  const {
    isUploading,
    progress,
    error,
    uploadText,
    reset,
  } = useOcrUpload({
    onSuccess: (result) => {
      const ocrResult = result as OcrResponse
      if (ocrResult.data?.taskId) {
        message.success('识别成功！')
        navigate(`/result/${ocrResult.data.taskId}`)
      }
    },
    onError: (err) => {
      message.error(`识别失败：${err.message}`)
    },
  })

  const handleFileSelect = useCallback((file: File) => {
    setSelectedFile(file)
  }, [])

  const handleSubmit = async () => {
    if (!selectedFile) {
      message.warning('请选择要识别的图片')
      return
    }
    await uploadText(selectedFile, generatePdf)
  }

  const handleReset = () => {
    setSelectedFile(null)
    reset()
  }

  if (error) {
    return (
      <Result
        status="error"
        title="上传失败"
        subTitle={error.message}
        onRetry={handleReset}
        backTo="/text-ocr"
      />
    )
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6 animate-slide-up">
      <Card
        title="文字识别"
        extra={
          <Space>
            <span className="text-sm text-gray-500">生成 PDF 报告</span>
            <Switch
              checked={generatePdf}
              onChange={setGeneratePdf}
              checkedChildren={<FilePdfOutlined />}
            />
          </Space>
        }
      >
        <div className="space-y-4">
          <ImageUpload
            value={selectedFile}
            onChange={setSelectedFile}
            onFileSelect={handleFileSelect}
            disabled={isUploading}
          />

          {isUploading && (
            <UploadProgress
              percentage={progress.percentage}
              status="uploading"
              onCancel={reset}
            />
          )}

          {selectedFile && !isUploading && (
            <div className="bg-gray-50 rounded-lg p-4 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-500">已选择文件:</span>
                <span className="text-sm text-gray-700">{selectedFile.name}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-500">文件大小:</span>
                <span className="text-sm text-gray-700">
                  {(selectedFile.size / 1024 / 1024).toFixed(2)} MB
                </span>
              </div>
            </div>
          )}

          <Space className="w-full justify-center">
            <Button onClick={handleReset} disabled={isUploading}>
              重置
            </Button>
            <Button
              type="primary"
              icon={<ScanOutlined />}
              onClick={handleSubmit}
              disabled={!selectedFile || isUploading}
              loading={isUploading}
              size="large"
            >
              开始识别
            </Button>
          </Space>
        </div>
      </Card>

      <Card title="使用说明" size="small">
        <ul className="text-sm text-gray-600 space-y-2 list-disc list-inside">
          <li>支持 JPG、PNG、GIF、BMP、WEBP 格式图片</li>
          <li>支持中文和英文混合文字识别</li>
          <li>图片大小不超过 10MB</li>
          <li>可选生成 PDF 格式的识别报告</li>
          <li>识别结果将自动保存到历史记录</li>
        </ul>
      </Card>
    </div>
  )
}
