import { useState } from 'react'
import { Card, Select, Input, Space, Pagination, Empty, message, Popconfirm } from 'antd'
import { SearchOutlined, ClearOutlined, DeleteOutlined } from '@ant-design/icons'
import { OcrRecordCard } from '@/components/common'
import { useOcrRecords, useOcrSearch, useDeleteRecord, useDownloadImage } from '@/hooks'
import type { OcrRecord, OcrStatus, OcrType } from '@/types'
import { useOcrStore } from '@/stores'

const { Search } = Input

export function HistoryPage() {
  const [page, setPage] = useState(0)
  const [pageSize] = useState(10)
  const [keyword, setKeyword] = useState('')
  const [filterType, setFilterType] = useState<OcrType | undefined>()
  const [filterStatus, setFilterStatus] = useState<OcrStatus | undefined>()

  const deleteRecordMutation = useDeleteRecord()
  const downloadImageMutation = useDownloadImage()
  const searchTrigger = useOcrStore((state) => state.refreshTrigger)

  const isSearching = !!(keyword || filterType || filterStatus)

  const { data: recordsData, isLoading: recordsLoading } = useOcrRecords(
    page,
    pageSize,
  )
  const { data: searchData, isLoading: searchLoading } = useOcrSearch({
    keyword: keyword || undefined,
    ocrType: filterType,
    status: filterStatus,
    page,
    size: pageSize,
  })

  const { data, isLoading } = isSearching
    ? { data: searchData, isLoading: searchLoading }
    : { data: recordsData, isLoading: recordsLoading }

  const handleDelete = async (id: number) => {
    try {
      await deleteRecordMutation.mutateAsync(id)
      message.success('删除成功')
    } catch {
      message.error('删除失败')
    }
  }

  const handleDownload = async (taskId: string) => {
    try {
      await downloadImageMutation.mutateAsync(taskId)
      message.success('下载成功')
    } catch {
      message.error('下载失败')
    }
  }

  const handleSearch = (value: string) => {
    setKeyword(value)
    setPage(0)
  }

  const handleClear = () => {
    setKeyword('')
    setFilterType(undefined)
    setFilterStatus(undefined)
    setPage(0)
  }

  const handleRecordClick = (record: OcrRecord) => {
    window.location.href = `/result/${record.taskId}`
  }

  const isLoadingData = isLoading || recordsLoading || searchLoading

  return (
    <div className="space-y-4 animate-fade-in">
      <Card>
        <div className="space-y-4">
          {/* 搜索和筛选 */}
          <div className="flex flex-wrap gap-3">
            <Search
              placeholder="搜索识别内容..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onSearch={handleSearch}
              prefix={<SearchOutlined />}
              className="flex-1 min-w-[200px]"
              allowClear
            />
            <Select
              placeholder="识别类型"
              value={filterType}
              onChange={(value) => {
                setFilterType(value)
                setPage(0)
              }}
              allowClear
              className="w-[120px]"
              options={[
                { label: '文字识别', value: 'TEXT' },
                { label: '车牌识别', value: 'LICENSE_PLATE' },
                { label: '身份证识别', value: 'ID_CARD' },
              ]}
            />
            <Select
              placeholder="状态"
              value={filterStatus}
              onChange={(value) => {
                setFilterStatus(value)
                setPage(0)
              }}
              allowClear
              className="w-[120px]"
              options={[
                { label: '等待处理', value: 'PENDING' },
                { label: '处理中', value: 'PROCESSING' },
                { label: '成功', value: 'SUCCESS' },
                { label: '失败', value: 'FAILED' },
              ]}
            />
            {(keyword || filterType || filterStatus) && (
              <button
                onClick={handleClear}
                className="px-4 py-2 border border-gray-300 rounded-lg text-gray-500 hover:text-gray-700 hover:border-gray-400 transition-all"
              >
                <ClearOutlined /> 清除筛选
              </button>
            )}
          </div>

          {/* 记录列表 */}
          {isLoadingData ? (
            <div className="flex justify-center py-12">
              <div className="text-gray-500">加载中...</div>
            </div>
          ) : data?.content && data.content.length > 0 ? (
            <div className="space-y-3">
              {data.content.map((record) => (
                <div key={record.id} className="relative">
                  <OcrRecordCard
                    record={record}
                    onClick={handleRecordClick}
                    onDelete={handleDelete}
                    onDownload={handleDownload}
                  />
                </div>
              ))}

              {/* 分页 */}
              <div className="flex justify-center pt-4">
                <Pagination
                  current={page + 1}
                  total={data.totalElements}
                  pageSize={pageSize}
                  onChange={(current) => setPage(current - 1)}
                  showSizeChanger={false}
                  showTotal={(total) => `共 ${total} 条记录`}
                />
              </div>
            </div>
          ) : (
            <Empty
              description={isSearching ? '未找到相关记录' : '暂无识别记录'}
              className="py-12"
            />
          )}
        </div>
      </Card>
    </div>
  )
}
