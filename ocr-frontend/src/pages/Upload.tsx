import { useState, useCallback } from 'react'
import { Card, Segmented, Space, message } from 'antd'
import type { SegmentedValue } from 'antd'
import { FileTextOutlined, CarOutlined } from '@ant-design/icons'
import { ImageUpload, UploadProgress } from '@/components/common'
import { useOcrUpload } from '@/hooks'
import type { OcrResponse, OcrType, LicensePlateResponse } from '@/types'
import { useNavigate } from 'react-router-dom'

type OcrTab = 'TEXT' | 'LICENSE_PLATE'

export function UploadPage() {
  const navigate = useNavigate()
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [activeTab, setActiveTab] = useState<OcrTab>('TEXT')

  const {
    isUploading,
    progress,
    error,
    uploadText,
    uploadLicensePlate,
    reset,
  } = useOcrUpload({
    onSuccess: (result) => {
      message.success('识别成功！')
      if (activeTab === 'TEXT') {
        const ocrResult = result as OcrResponse
        if (ocrResult.data?.taskId) {
          navigate(`/result/${ocrResult.data.taskId}`)
        }
      } else {
        const plateResult = result as LicensePlateResponse
        if (plateResult.data?.plateNumber) {
          navigate(`/result/${plateResult.data.plateNumber}`)
        }
      }
    },
    onError: (err) => {
      message.error(`识别失败：${err.message}`)
    },
  })

  const handleFileSelect = useCallback((file: File) => {
    setSelectedFile(file)
  }, [])

  const handleTabChange = (value: SegmentedValue) => {
    setActiveTab(value as OcrTab)
    handleReset()
  }

  const handleSubmit = async () => {
    if (!selectedFile) {
      message.warning('请选择要识别的图片')
      return
    }
    if (activeTab === 'TEXT') {
      await uploadText(selectedFile)
    } else {
      await uploadLicensePlate(selectedFile)
    }
  }

  const handleReset = () => {
    setSelectedFile(null)
    reset()
  }

  const tabOptions = [
    {
      label: (
        <span>
          <FileTextOutlined /> 文字识别
        </span>
      ),
      value: 'TEXT',
    },
    {
      label: (
        <span>
          <CarOutlined /> 车牌识别
        </span>
      ),
      value: 'LICENSE_PLATE',
    },
  ]

  return (
    <div className="max-w-2xl mx-auto space-y-6 animate-slide-up">
      <Card
        title={
          <div className="flex items-center justify-between">
            <span>OCR 识别上传</span>
            <Segmented
              options={tabOptions}
              value={activeTab}
              onChange={handleTabChange}
            />
          </div>
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
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-500">识别类型:</span>
                <span className="text-sm text-gray-700">
                  {activeTab === 'TEXT' ? '文字识别' : '车牌识别'}
                </span>
              </div>
            </div>
          )}

          <Space className="w-full justify-center">
            <button
              onClick={handleReset}
              disabled={isUploading}
              className="px-6 py-2 border border-gray-300 rounded-lg text-gray-600 hover:text-gray-800 hover:border-gray-400 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            >
              重置
            </button>
            <button
              onClick={handleSubmit}
              disabled={!selectedFile || isUploading}
              className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed font-medium"
            >
              开始识别
            </button>
          </Space>
        </div>
      </Card>

      {activeTab === 'TEXT' && (
        <Card title="使用说明" size="small">
          <ul className="text-sm text-gray-600 space-y-2 list-disc list-inside">
            <li>支持 JPG、PNG、GIF、BMP、WEBP 格式图片</li>
            <li>支持中文和英文混合文字识别</li>
            <li>图片大小不超过 10MB</li>
            <li>识别结果将自动保存到历史记录</li>
          </ul>
        </Card>
      )}

      {activeTab === 'LICENSE_PLATE' && (
        <Card title="使用说明" size="small">
          <ul className="text-sm text-gray-600 space-y-2 list-disc list-inside">
            <li>支持常见车牌类型识别（蓝牌、绿牌、黄牌等）</li>
            <li>支持中文车牌识别</li>
            <li>请确保车牌在图片中清晰可见</li>
            <li>识别结果将自动保存到历史记录</li>
          </ul>
        </Card>
      )}
    </div>
  )
}
