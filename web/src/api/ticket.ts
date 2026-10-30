import api from './index'

export function createTicket(data: any) {
  return api.post('/tickets', data)
}

export function getTickets(params?: any) {
  return api.get('/tickets', { params })
}

export function getTicketByNo(ticketNo: string) {
  return api.get(`/tickets/no/${ticketNo}`)
}

export function getTicketById(id: number) {
  return api.get(`/tickets/${id}`)
}

/** 用户追问 */
export function followUpTicket(id: number, content: string) {
  return api.post(`/tickets/${id}/replies`, { content })
}

// 管理员接口
export function getAdminTickets(params?: any) {
  return api.get('/admin/tickets', { params })
}

export function getAdminTicket(id: number) {
  return api.get(`/admin/tickets/${id}`)
}

export function replyTicket(id: number, data: any) {
  return api.post(`/admin/tickets/${id}/reply`, data)
}

export function closeTicket(id: number) {
  return api.post(`/admin/tickets/${id}/close`)
}
