import { Routes, Route } from 'react-router-dom'
import { Layout } from '@/components/layout'
import { HomePage } from '@/pages/Home'
import { UploadPage } from '@/pages/Upload'
import { TextOcrPage } from '@/pages/TextOcrPage'
import { LicensePlatePage } from '@/pages/LicensePlatePage'
import { HistoryPage } from '@/pages/History'
import { ResultPage } from '@/pages/Result'
import { NotFoundPage } from '@/pages/NotFound'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="upload" element={<UploadPage />} />
        <Route path="text-ocr" element={<TextOcrPage />} />
        <Route path="license-plate" element={<LicensePlatePage />} />
        <Route path="history" element={<HistoryPage />} />
        <Route path="result/:taskId" element={<ResultPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
