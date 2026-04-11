import { Card, Row, Col, Statistic, Button } from 'antd'
import {
  FileTextOutlined,
  CarOutlined,
  UploadOutlined,
  HistoryOutlined,
  ArrowRightOutlined,
  ThunderboltOutlined,
  SafetyOutlined,
  CloudOutlined,
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useOcrRecords } from '@/hooks'

export function HomePage() {
  const navigate = useNavigate()
  const { data: recordsData } = useOcrRecords(0, 100)

  const features = [
    {
      icon: <FileTextOutlined className="text-3xl text-blue-500" />,
      title: '文字识别',
      description: '支持中英文混合识别，高精度 OCR 引擎',
      path: '/text-ocr',
      color: 'blue',
    },
    {
      icon: <CarOutlined className="text-3xl text-green-500" />,
      title: '车牌识别',
      description: '支持各类车牌识别，包括新能源车牌',
      path: '/license-plate',
      color: 'green',
    },
    {
      icon: <UploadOutlined className="text-3xl text-purple-500" />,
      title: '批量上传',
      description: '支持批量图片上传，自动排队处理',
      path: '/upload',
      color: 'purple',
    },
    {
      icon: <HistoryOutlined className="text-3xl text-orange-500" />,
      title: '历史记录',
      description: '查看和管理所有识别历史记录',
      path: '/history',
      color: 'orange',
    },
  ]

  const advantages = [
    {
      icon: <ThunderboltOutlined className="text-yellow-500" />,
      title: '快速识别',
      description: '基于 WebFlux 响应式架构，毫秒级响应',
    },
    {
      icon: <SafetyOutlined className="text-green-500" />,
      title: '安全可靠',
      description: '数据加密存储，权限管控',
    },
    {
      icon: <CloudOutlined className="text-blue-500" />,
      title: '高可扩展',
      description: '模块化设计，支持功能扩展',
    },
  ]

  const pageData = recordsData?.data
  const content = pageData?.content || []
  const totalRecords = pageData?.totalElements || 0
  const successRecords = content.filter((r) => r.status === 'SUCCESS').length || 0

  return (
    <div className="space-y-6 animate-fade-in">
      {/* 统计卡片 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="总记录数"
              value={totalRecords}
              prefix={<HistoryOutlined />}
              valueStyle={{ color: '#1677ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="成功识别"
              value={successRecords}
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="文字识别"
              value={content.filter((r: any) => r.ocrType === 'TEXT').length || 0}              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="车牌识别"
              value={content.filter((r: any) => r.ocrType === 'LICENSE_PLATE').length || 0}              prefix={<CarOutlined />}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 功能入口 */}
      <div className="py-4">
        <h2 className="text-xl font-semibold text-gray-800 mb-4">功能入口</h2>
        <Row gutter={[16, 16]}>
          {features.map((feature, index) => (
            <Col xs={24} sm={12} lg={6} key={index}>
              <Card
                hoverable
                className="card-shadow-hover h-full cursor-pointer transition-all"
                onClick={() => navigate(feature.path)}
              >
                <div className="text-center space-y-3">
                  <div className="flex justify-center">{feature.icon}</div>
                  <h3 className="font-medium text-gray-800">{feature.title}</h3>
                  <p className="text-sm text-gray-500">{feature.description}</p>
                  <Button type="link" icon={<ArrowRightOutlined />} className="p-0">
                    立即使用
                  </Button>
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      </div>

      {/* 平台优势 */}
      <div className="py-4">
        <h2 className="text-xl font-semibold text-gray-800 mb-4">平台优势</h2>
        <Row gutter={[16, 16]}>
          {advantages.map((advantage, index) => (
            <Col xs={24} sm={12} lg={8} key={index}>
              <Card className="h-full">
                <div className="flex items-start gap-3">
                  <div className="flex-shrink-0">{advantage.icon}</div>
                  <div>
                    <h3 className="font-medium text-gray-800 mb-1">{advantage.title}</h3>
                    <p className="text-sm text-gray-500">{advantage.description}</p>
                  </div>
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      </div>
    </div>
  )
}
