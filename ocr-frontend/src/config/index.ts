/**
 * 应用配置
 */
export const config = {
  // API 基础路径
  apiBasePath: '/api',

  // API 超时时间
  timeout: 30000,

  // 上传文件大小限制 (MB)
  uploadMaxSize: 10,

  // 允许的文件类型
  allowedImageTypes: ['image/jpeg', 'image/png', 'image/gif', 'image/bmp', 'image/webp'],

  // 允许的文件扩展名
  allowedExtensions: ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.pdf'],
} as const
