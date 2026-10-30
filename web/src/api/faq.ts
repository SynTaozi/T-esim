import api from './index'

export interface FAQCategory {
  id: number
  name: string
  nameEn?: string
  sort: number
  status: number
  createdAt?: string
  updatedAt?: string
}

export interface FAQ {
  id: number
  categoryId: number
  question: string
  questionEn?: string
  answer: string
  answerEn?: string
  sort: number
  status: number
  category?: FAQCategory
  createdAt?: string
  updatedAt?: string
}

export const faqApi = {
  // FAQ分类
  getCategories: () => api.get('/faq/categories'),
  getCategoryById: (id: number) => api.get(`/faq/categories/${id}`),
  createCategory: (data: Omit<FAQCategory, 'id'>) => api.post('/faq/categories', data),
  updateCategory: (id: number, data: Partial<FAQCategory>) => api.put(`/faq/categories/${id}`, data),
  deleteCategory: (id: number) => api.delete(`/faq/categories/${id}`),
  
  // FAQ
  getFAQs: (categoryId?: number) => api.get('/faq', { params: { categoryId } }),
  getFAQById: (id: number) => api.get(`/faq/${id}`),
  createFAQ: (data: Omit<FAQ, 'id'>) => api.post('/faq', data),
  updateFAQ: (id: number, data: Partial<FAQ>) => api.put(`/faq/${id}`, data),
  deleteFAQ: (id: number) => api.delete(`/faq/${id}`)
}
