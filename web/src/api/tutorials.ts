import api from './index'

export type TutorialContentType = 'ARTICLE' | 'VIDEO' | 'PDF'

export interface TutorialCategory {
  id: number
  name: string
  nameEn?: string
  icon?: string
  sort: number
  status: number
}

export interface Tutorial {
  id: number
  categoryId: number
  slug: string
  contentType: TutorialContentType | string
  title: string
  titleEn?: string
  summary?: string
  summaryEn?: string
  content?: string
  contentEn?: string
  coverImageUrl?: string
  mediaUrl?: string
  sort: number
  status: number
  createdAt?: string
  updatedAt?: string
  category?: TutorialCategory
}

export const tutorialApi = {
  getOverview: () => api.get('/tutorials/overview'),
  getCategories: () => api.get('/tutorials/categories'),
  getTutorials: (params?: { categoryId?: number; contentType?: string }) =>
    api.get('/tutorials', { params }),
  getBySlug: (slug: string) => api.get(`/tutorials/${encodeURIComponent(slug)}`)
}
