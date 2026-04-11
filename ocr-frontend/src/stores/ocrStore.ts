import { create } from 'zustand'
import type { OcrRecord, OcrType } from '@/types'

interface OcrStore {
  // 当前选中的 OCR 类型
  currentOcrType: OcrType
  setCurrentOcrType: (type: OcrType) => void

  // 当前选中的记录
  selectedRecord: OcrRecord | null
  setSelectedRecord: (record: OcrRecord | null) => void

  // 搜索关键词
  searchKeyword: string
  setSearchKeyword: (keyword: string) => void

  // 刷新列表的触发器
  refreshTrigger: number
  triggerRefresh: () => void
}

export const useOcrStore = create<OcrStore>((set) => ({
  currentOcrType: 'TEXT',
  setCurrentOcrType: (type) => set({ currentOcrType: type }),

  selectedRecord: null,
  setSelectedRecord: (record) => set({ selectedRecord: record }),

  searchKeyword: '',
  setSearchKeyword: (keyword) => set({ searchKeyword: keyword }),

  refreshTrigger: 0,
  triggerRefresh: () => set((state) => ({ refreshTrigger: state.refreshTrigger + 1 })),
}))
