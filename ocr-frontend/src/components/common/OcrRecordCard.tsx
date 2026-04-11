import { Card, Typography, Tag, Space, Button, Tooltip } from 'antd'
import {
  FileTextOutlined,
  CarOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
  DeleteOutlined,
  DownloadOutlined,
} from '@ant-design/icons'
import type { OcrRecord, OcrStatus } from '@/types'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

dayjs.locale('zh-cn')

interface OcrRecordCardProps {
  record: OcrRecord
  onClick?: (record: OcrRecord) => void
  onDelete?: (id: number) => void
  onDownload?: (taskId: string) => void
  showActions?: boolean
}

const statusConfig: Record<
  OcrStatus,
  { color: string; icon: React.ReactNode; text: string }
> = {
  PENDING: {
    color: 'default',
    icon: <SyncOutlined spin />,
    text: '等待处理',
  },
  PROCESSING: {
    color: 'processing',
    icon: <SyncOutlined spin />,
    text: '处理中',
  },
  SUCCESS: {
    color: 'success',
    icon: <CheckCircleOutlined />,
    text: '成功',
  },
  FAILED: {
    color: 'error',
    icon: <CloseCircleOutlined />,
    text: '失败',
  },
}

const typeConfig: Record<
  OcrRecord['ocrType'],
  { color: string; icon: React.ReactNode; text: string }
> = {
  TEXT: {
    color: 'blue',
    icon: <FileTextOutlined />,
    text: '文字识别',
  },
  LICENSE_PLATE: {
    color: 'green',
    icon: <CarOutlined />,
    text: '车牌识别',
  },
  ID_CARD: {
    color: 'purple',
    icon: <FileTextOutlined />,
    text: '身份证识别',
  },
}

export function OcrRecordCard({
  record,
  onClick,
  onDelete,
  onDownload,
  showActions = true,
}: OcrRecordCardProps) {
  const status = statusConfig[record.status]
  const type = typeConfig[record.ocrType]

  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation()
    onDelete?.(record.id)
  }

  const handleDownload = (e: React.MouseEvent) => {
    e.stopPropagation()
    onDownload?.(record.taskId)
  }

  return (
    <Card
      hoverable
      onClick={() => onClick?.(record)}
      className="card-shadow-hover cursor-pointer transition-all"
      size="small"
    >
      <div className="space-y-3">
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-2">
            <Tag color={type.color} icon={type.icon}>
              {type.text}
            </Tag>
            <Tag color={status.color} icon={status.icon}>
              {status.text}
            </Tag>
          </div>
          {showActions && (
            <Space size="small">
              <Tooltip title="下载图片">
                <Button
                  type="text"
                  icon={<DownloadOutlined />}
                  size="small"
                  onClick={handleDownload}
                />
              </Tooltip>
              <Tooltip title="删除记录">
                <Button
                  type="text"
                  danger
                  icon={<DeleteOutlined />}
                  size="small"
                  onClick={handleDelete}
                />
              </Tooltip>
            </Space>
          )}
        </div>

        <div className="space-y-2">
          <div className="flex items-center justify-between text-sm">
            <span className="text-gray-500">文件名:</span>
            <span className="text-gray-700 text-truncate max-w-[200px]">
              {record.fileName}
            </span>
          </div>

          {record.recognizedText && (
            <div className="bg-gray-50 rounded-lg p-3">
              <p className="text-sm text-gray-700 line-clamp-3">
                {record.recognizedText}
              </p>
            </div>
          )}

          {record.licensePlateNumber && (
            <div className="flex items-center gap-2">
              <span className="text-gray-500 text-sm">车牌号:</span>
              <Tag color="blue" className="text-base font-semibold">
                {record.licensePlateNumber}
              </Tag>
            </div>
          )}

          {record.confidence !== undefined && (
            <div className="flex items-center gap-2">
              <span className="text-gray-500 text-sm">置信度:</span>
              <div className="flex-1 bg-gray-200 rounded-full h-2 overflow-hidden">
                <div
                  className="bg-blue-500 h-full rounded-full transition-all duration-300"
                  style={{ width: `${record.confidence}%` }}
                />
              </div>
              <span className="text-sm text-gray-600">{record.confidence}%</span>
            </div>
          )}

          <div className="flex items-center justify-between text-xs text-gray-400">
            <span>{dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss')}</span>
            {record.updateTime !== record.createTime && (
              <span>更新于 {dayjs(record.updateTime).fromNow()}</span>
            )}
          </div>
        </div>
      </div>
    </Card>
  )
}
