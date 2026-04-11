import { Spin } from 'antd'
import type { SpinProps } from 'antd'

interface LoadingProps extends SpinProps {
  fullScreen?: boolean
  tip?: string
}

export function Loading({
  fullScreen = false,
  tip = '加载中...',
  ...props
}: LoadingProps) {
  if (fullScreen) {
    return (
      <div className="fixed inset-0 bg-white/80 backdrop-blur-sm flex items-center justify-center z-50">
        <Spin tip={tip} size="large" {...props} />
      </div>
    )
  }

  return <Spin tip={tip} {...props} />
}
