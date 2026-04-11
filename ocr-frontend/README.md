# OCR Platform Frontend

基于 React 19 + TypeScript + Vite 的 OCR 识别前端应用。

## 技术栈

- **框架**: React 19
- **构建工具**: Vite 6
- **语言**: TypeScript 5.7
- **UI 组件**: Ant Design 5
- **路由**: React Router 7
- **状态管理**: Zustand
- **数据请求**: Axios + TanStack Query
- **样式**: TailwindCSS 4

## 项目结构

```
ocr-frontend/
├── src/
│   ├── components/        # 组件
│   │   ├── common/        # 通用组件 (上传、进度条、卡片等)
│   │   ├── layout/        # 布局组件 (Header, Footer, Layout)
│   │   └── ocr/           # OCR 功能组件
│   ├── config/            # 应用配置
│   ├── hooks/             # 自定义 Hooks
│   ├── pages/             # 页面组件
│   ├── services/          # API 服务
│   ├── stores/            # 状态管理 (Zustand)
│   ├── styles/            # 全局样式
│   ├── types/             # TypeScript 类型定义
│   ├── utils/             # 工具函数
│   ├── App.tsx            # 应用入口
│   └── main.tsx           # React 入口
├── public/                # 静态资源
├── index.html             # HTML 模板
├── vite.config.ts         # Vite 配置
├── tsconfig.json          # TypeScript 配置
├── package.json           # 依赖配置
└── README.md              # 项目说明

```

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

应用将在 `http://localhost:3000` 启动，API 请求会自动代理到后端 `http://localhost:8080`。

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 功能特性

- **文字识别**: 支持中英文混合 OCR 识别
- **车牌识别**: 支持各类车牌（蓝牌、绿牌、黄牌）识别
- **历史记录**: 查看、搜索、筛选和管理所有识别记录
- **PDF 生成**: 可选生成 PDF 格式的识别报告
- **响应式设计**: 完美适配桌面和移动设备
- **模块化架构**: 易于扩展新的识别类型

## API 代理

开发模式下，Vite 会自动将 `/ocr` 路径的请求代理到后端服务：

```typescript
// vite.config.ts
server: {
  port: 3000,
  proxy: {
    '/ocr': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

## 扩展指南

### 添加新的 OCR 类型

1. 在 `src/types/ocr.ts` 中添加新的 `OcrType`
2. 在 `src/services/ocrService.ts` 中添加对应的 API 方法
3. 在 `src/components/ocr/` 中创建新的功能组件
4. 在 `src/pages/` 中创建对应的页面
5. 在 `src/App.tsx` 中添加路由

## 后端依赖

本前端项目需要配合 `ocr-platform` 后端服务使用。后端基于 Spring Boot 4.0.5 WebFlux 构建，提供 RESTful API。

## License

MIT
