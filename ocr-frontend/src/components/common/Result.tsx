import { Result as AntdResult, Button } from 'antd'
import { ResultProps as AntdResultProps } from 'antd'
import { useNavigate } from 'react-router-dom'

interface ResultProps extends Omit<AntdResultProps, 'extra'> {
  onRetry?: () => void
  onBack?: () => void
  backTo?: string
}

export function Result({
  onRetry,
  onBack,
  backTo,
  extra,
  ...props
}: ResultProps) {
  const navigate = useNavigate()

  const handleBack = () => {
    if (onBack) {
      onBack()
    } else if (backTo) {
      navigate(backTo)
    } else {
      navigate(-1)
    }
  }

  const actions = (
    <>
      {onRetry && (
        <Button key="retry" type="primary" onClick={onRetry}>
          重试
        </Button>
      )}
      <Button key="back" onClick={handleBack}>
        返回
      </Button>
      {extra}
    </>
  )

  return <AntdResult {...props} extra={actions} />
}
