import api from './index'

export const themeApi = {
  /**
   * 获取系统当前主题配置
   */
  getCurrentTheme: () => api.get('/system/theme'),
  
  /**
   * 设置系统主题（仅管理员）
   */
  setTheme: (themeId: string, themeName: string) => 
    api.post('/admin/system/theme', { themeId, themeName })
}
