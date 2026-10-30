import axios from 'axios';
import router from '@/router';
import { resolvePostLoginRedirect } from '@/utils/authNavigation';

// 为产品补充三个新字段（用于测试，后端完整编译后移除）
function addProductFields(product: any) {
  // plan_type, sms_support, topup_type 临时补充
  if (product.plan_type === undefined || product.plan_type === null) {
    product.plan_type = product.planType ?? 0
  }
  if (product.sms_support === undefined || product.sms_support === null) {
    product.sms_support = product.smsSupport ?? 0
  }
  if (product.data_support === undefined || product.data_support === null) {
    product.data_support = product.dataSupport ?? (product.dataLimit ? 1 : 0)
  }
  if (product.voice_support === undefined || product.voice_support === null) {
    product.voice_support = product.voiceSupport ?? 0
  }
  if (product.topup_type === undefined || product.topup_type === null) {
    product.topup_type = product.topupType ?? 0
  }
  // features 字段补充（后端已返回，只是前端接口未声明，这里确保拿到）
  if (!product.features && product.features !== '') {
    product.features = product.features || null
  }
  if (!product.featuresEn && product.featuresEn !== '') {
    product.featuresEn = product.featuresEn || null
  }
}

const api = axios.create({
  // 开发环境使用直接连接后端，生产环境使用相对路径
  baseURL: import.meta.env.DEV ? 'http://localhost:8080/api' : '/api',
  timeout: 60000,  // 增加至60秒，避免长时间查询超时
  headers: {
    'Cache-Control': 'no-cache',
    Pragma: 'no-cache'
  }
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    console.log('API Request:', config.url, 'Token:', token ? 'present' : 'missing')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    if ((config.method || 'get').toLowerCase() === 'get') {
      config.params = { ...(config.params || {}), _t: Date.now() }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    // 确保返回的是对象格式
    const data = response.data
    if (typeof data === 'object' && data !== null) {
      // 为产品数据补充三个新字段用于测试（后端完全编译后可移除）
      if (response.config.url?.includes('/products')) {
        try {
          if (data.data?.products && Array.isArray(data.data.products)) {
            // 处理分页产品列表
            data.data.products.forEach((product: any) => {
              addProductFields(product)
            })
          } else if (Array.isArray(data.data)) {
            // 处理非分页产品列表
            data.data.forEach((product: any) => {
              addProductFields(product)
            })
          } else if (data.data?.id) {
            // 处理单个产品
            addProductFields(data.data)
          }
        } catch (e) {
          console.warn('Failed to add product fields:', e)
        }
      }
      return data
    }
    // 如果返回的不是对象（比如HTML错误页面），返回统一的错误响应
    console.warn('Invalid response format:', typeof data, response.status)
    return {
      code: response.status || 500,
      message: '服务器返回了无效的数据格式',
      data: null
    }
  },
  (error) => {
    // 401：清除失效 token，但仅在「需要登录的页面」才跳转登录页
    if (error.response?.status === 401) {
      const hadToken = !!localStorage.getItem('token')
      if (hadToken) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        import('@/stores/auth').then(({ useAuthStore }) => {
          useAuthStore().logout()
        })
      }

      const requiresAuth = router.currentRoute.value.meta.requiresAuth === true
      if (requiresAuth && router.currentRoute.value.path !== '/login') {
        const redirect = resolvePostLoginRedirect(router.currentRoute.value.fullPath, '')
        router.push({
          path: '/login',
          query: redirect ? { redirect } : undefined
        })
      }
    }
    
    // 保留原始错误信息，包括后端返回的message
    const errorMessage = error.response?.data?.message || error.message || '请求失败'
    const customError = new Error(errorMessage)
    customError.response = error.response
    customError.code = error.response?.data?.code
    return Promise.reject(customError)
  }
)

export default api;
