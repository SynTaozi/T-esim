import api from './index'

export interface FAQCategory {
  id: number
  name: string
  nameEn?: string
  sort?: number
  status?: number
}

export interface FAQ {
  id: number
  categoryId: number
  question: string
  questionEn?: string
  answer: string
  answerEn?: string
  sort?: number
  status?: number
}

export const faqApi = {
  getCategories: () => api.get('/admin/faq/categories'),
  createCategory: (data: Partial<FAQCategory>) => api.post('/admin/faq/categories', data),
  updateCategory: (id: number, data: Partial<FAQCategory>) =>
    api.put(`/admin/faq/categories/${id}`, data),
  deleteCategory: (id: number) => api.delete(`/admin/faq/categories/${id}`),
  getFAQs: (categoryId?: number) =>
    api.get('/admin/faq', { params: categoryId ? { categoryId } : {} }),
  createFAQ: (data: Partial<FAQ>) => api.post('/admin/faq', data),
  updateFAQ: (id: number, data: Partial<FAQ>) => api.put(`/admin/faq/${id}`, data),
  deleteFAQ: (id: number) => api.delete(`/admin/faq/${id}`)
}
