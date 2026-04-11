import { Link, useLocation } from 'react-router-dom'
import { Menu } from 'antd'
import type { MenuProps } from 'antd'
import {
  HomeOutlined,
  UploadOutlined,
  FileTextOutlined,
  CarOutlined,
  HistoryOutlined,
} from '@ant-design/icons'

const menuItems: MenuProps['items'] = [
  {
    key: '/',
    icon: <HomeOutlined />,
    label: <Link to="/">首页</Link>,
  },
  {
    key: '/upload',
    icon: <UploadOutlined />,
    label: <Link to="/upload">识别上传</Link>,
  },
  {
    key: '/text-ocr',
    icon: <FileTextOutlined />,
    label: <Link to="/text-ocr">文字识别</Link>,
  },
  {
    key: '/license-plate',
    icon: <CarOutlined />,
    label: <Link to="/license-plate">车牌识别</Link>,
  },
  {
    key: '/history',
    icon: <HistoryOutlined />,
    label: <Link to="/history">历史记录</Link>,
  },
]

export function Header() {
  const location = useLocation()
  const currentKey = location.pathname

  return (
    <header className="bg-white border-b border-gray-200 px-4 py-3 card-shadow">
      <div className="max-w-7xl mx-auto flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg flex items-center justify-center">
            <span className="text-white font-bold text-sm">OCR</span>
          </div>
          <h1 className="text-xl font-semibold text-gray-800">OCR Platform</h1>
        </div>
        <nav className="hidden md:block">
          <Menu
            mode="horizontal"
            selectedKeys={[currentKey]}
            items={menuItems}
            className="border-0 bg-transparent"
          />
        </nav>
      </div>
    </header>
  )
}
