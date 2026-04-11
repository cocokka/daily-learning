import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Card, Button, Space, Tag, Tabs, Spin, message } from 'antd'
import {
  ArrowLeftOutlined,
  DownloadOutlined,
  CopyOutlined,
  DeleteOutlined,
} from '@ant-design/icons'
import { useOcrResult, useDeleteRecord, useDownloadImage } from '@/hooks'
import { OcrRecordCard } from '@/components/common'
import type { OcrRecord } from '@/types'

export function ResultPage() {
  const { taskId } = useParams<{ taskId: string }>()
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState('text')

  const { data: resultData, isLoading } = useOcrResult(taskId || null)
  const deleteMutation = useDeleteRecord()
  const downloadMutation = useDownloadImage()

  const handleCopy = async () => {
    if (resultData?.data?.recognizedText) {
      await navigator.clipboard.writeText(resultData.data.recognizedText)
      message.success('已复制到剪贴板')
    }
  }

  const handleDownload = async () => {
    if (taskId) {
      try {
        await downloadMutation.mutateAsync(taskId)
        message.success('下载成功')
      } catch {
        message.error('下载失败')
      }
    }
  }

  const handleDelete = async () => {
    if (resultData?.data) {
      try {
        await deleteMutation.mutateAsync((resultData.data as unknown as OcrRecord).id)
        message.success('删除成功')
        navigate('/history')
      } catch {
        message.error('删除失败')
      }
    }
  }

  const handleBack = () => {
    navigate(-1)
  }

  if (isLoading) {
    return (
      <div className="flex justify-center py-20">
        <Spin size="large" tip="正在获取识别结果..." />
      </div>
    )
  }

  if (!resultData?.data) {
    return (
      <div className="text-center py-20">
        <p className="text-gray-500 mb-4">未找到识别结果</p>
        <Button type="primary" onClick={handleBack} icon={<ArrowLeftOutlined />}>
          返回上一页
        </Button>
      </div>
    )
  }

  const ocrData = resultData.data

  return (
    <div className="max-w-4xl mx-auto space-y-4 animate-slide-up">
      <div className="flex items-center gap-3 mb-4">
        <Button
          icon={<ArrowLeftOutlined />}
          onClick={handleBack}
          className="text-gray-600"
        />
        <h2 className="text-xl font-semibold text-gray-800">识别结果</h2>
      </div>

      <Card>
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: 'text',
              label: '识别文本',
              children: (
                <div className="space-y-4">
                  <div className="bg-gray-50 rounded-lg p-4 min-h-[200px] max-h-[500px] overflow-auto">
                    <pre className="whitespace-pre-wrap text-sm text-gray-700 font-mono">
                      {ocrData.recognizedText || '暂无识别结果'}
                    </pre>
                  </div>
                  <Space>
                    <Button
                      icon={<CopyOutlined />}
                      onClick={handleCopy}
                      disabled={!ocrData.recognizedText}
                    >
                      复制文本
                    </Button>
                  </Space>
                </div>
              ),
            },
            {
              key: 'details',
              label: '详细信息',
              children: (
                <div className="space-y-4">
                  {ocrData.licensePlateNumber && (
                    <div className="flex items-center gap-3">
                      <span className="text-gray-500">车牌号:</span>
                      <Tag color="blue" className="text-lg font-semibold px-3 py-1">
                        {ocrData.licensePlateNumber}
                      </Tag>
                    </div>
                  )}
                  {ocrData.confidence !== undefined && (
                    <div className="space-y-2">
                      <span className="text-gray-500">置信度:</span>
                      <div className="flex items-center gap-3">
                        <div className="flex-1 bg-gray-200 rounded-full h-3 overflow-hidden">
                          <div
                            className="bg-blue-500 h-full rounded-full transition-all duration-300"
                            style={{ width: `${ocrData.confidence}%` }}
                          />
                        </div>
                        <span className="text-sm text-gray-600 font-medium">
                          {ocrData.confidence}%
                        </span>
                      </div>
                    </div>
                  )}
                  {ocrData.keywords && ocrData.keywords.length > 0 && (
                    <div className="space-y-2">
                      <span className="text-gray-500">关键词:</span>
                      <div className="flex flex-wrap gap-2">
                        {ocrData.keywords.map((keyword, index) => (
                          <Tag key={index} color="geekblue">
                            {keyword}
                          </Tag>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              ),
            },
          ]}
        />
      </Card>

      <div className="flex justify-center gap-3">
        <Button icon={<DownloadOutlined />} onClick={handleDownload}>
          下载原图
        </Button>
        <Button danger icon={<DeleteOutlined />} onClick={handleDelete}>
          删除记录
        </Button>
      </div>
    </div>
  )
}
