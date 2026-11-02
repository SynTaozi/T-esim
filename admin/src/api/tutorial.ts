import api from './index'

export interface TutorialCategory {
  id: number
  name: string
  nameEn?: string
  icon?: string
  sort?: number
  status?: number
}

export interface Tutorial {
  id: number
  categoryId: number
  slug?: string
  contentType?: string
  title: string
  titleEn?: string
  summary?: string
  content?: string
  coverImageUrl?: string
  sort?: number
  status?: number
}

export const tutorialApi = {
  getCategories: () => api.get('/admin/tutorials/categories'),
  createCategory: (data: Partial<TutorialCategory>) =>
    api.post('/admin/tutorials/categories', data),
  updateCategory: (id: number, data: Partial<TutorialCategory>) =>
    api.put(`/admin/tutorials/categories/${id}`, data),
  deleteCategory: (id: number) => api.delete(`/admin/tutorials/categories/${id}`),
  getTutorials: (params?: { categoryId?: number; contentType?: string }) =>
    api.get('/admin/tutorials', { params }),
  createTutorial: (data: Partial<Tutorial>) => api.post('/admin/tutorials', data),
  updateTutorial: (id: number, data: Partial<Tutorial>) =>
    api.put(`/admin/tutorials/${id}`, data),
  deleteTutorial: (id: number) => api.delete(`/admin/tutorials/${id}`)
}
