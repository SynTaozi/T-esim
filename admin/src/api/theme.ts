import api from './index'

export const themeApi = {
  getCurrentTheme: () => api.get('/system/theme'),
  setTheme: (themeId: string, themeName: string) =>
    api.post('/admin/system/theme', { themeId, themeName })
}
