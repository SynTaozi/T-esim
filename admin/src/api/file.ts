import api, { type ApiResult } from './index'

export interface UploadFileResult {
  url: string
  name?: string
  size?: number
}

/** 上传图片，返回可访问 URL（如 /api/uploads/shipping/xxx.png） */
export function uploadImage(file: File, scene = 'shipping') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('scene', scene)
  return api.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }) as Promise<ApiResult<UploadFileResult>>
}
