import { InboxOutlined } from '@ant-design/icons'
import { Upload } from 'antd'
import type { DraggerProps } from 'antd'
import { useRef, useCallback } from 'react'

const { Dragger } = Upload

interface ImageUploadProps {
  value?: File | null
  onChange?: (file: File | null) => void
  onFileSelect?: (file: File) => void
  disabled?: boolean
  accept?: string
  maxCount?: number
}

export function ImageUpload({
  value,
  onChange,
  onFileSelect,
  disabled = false,
  accept = 'image/*,.pdf',
  maxCount = 1,
}: ImageUploadProps) {
  const fileInputRef = useRef<HTMLInputElement>(null)

  const handleFileChange: DraggerProps['onChange'] = useCallback(
    (info) => {
      const file = info.file.originFileObj
      if (file) {
        onChange?.(file)
        onFileSelect?.(file)
      }
    },
    [onChange, onFileSelect],
  )

  const handleRemove = useCallback(() => {
    onChange?.(null)
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }, [onChange])

  const fileList = value
    ? [
        {
          uid: '-1',
          name: value.name,
          status: 'done',
          size: value.size,
          originFileObj: value,
        },
      ]
    : []

  return (
    <Dragger
      ref={fileInputRef as unknown as React.RefObject<typeof Dragger>}
      accept={accept}
      maxCount={maxCount}
      multiple={false}
      disabled={disabled}
      fileList={fileList}
      onRemove={handleRemove}
      onChange={handleFileChange}
      beforeUpload={() => false} // 阻止自动上传
      className="!rounded-xl"
    >
      <div className="py-8">
        <InboxOutlined className="text-5xl text-blue-500 mb-3" />
        <p className="text-base text-gray-600 mb-2">
          点击或拖拽文件到此区域上传
        </p>
        <p className="text-sm text-gray-400">
          支持 JPG、PNG、GIF、BMP、WEBP 格式图片，最大 10MB
        </p>
      </div>
    </Dragger>
  )
}
