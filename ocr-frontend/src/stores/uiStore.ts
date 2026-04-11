import { create } from 'zustand'

interface UiStore {
  // 侧边栏是否折叠
  sidebarCollapsed: boolean
  setSidebarCollapsed: (collapsed: boolean) => void

  // 当前主题模式
  theme: 'light' | 'dark'
  setTheme: (theme: 'light' | 'dark') => void

  // 全局 loading 状态
  globalLoading: boolean
  setGlobalLoading: (loading: boolean) => void
}

export const useUiStore = create<UiStore>((set) => ({
  sidebarCollapsed: false,
  setSidebarCollapsed: (collapsed) => set({ sidebarCollapsed: collapsed }),

  theme: 'light',
  setTheme: (theme) => set({ theme }),

  globalLoading: false,
  setGlobalLoading: (loading) => set({ globalLoading: loading }),
}))
