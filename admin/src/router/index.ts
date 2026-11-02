import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/components/layout/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'products', name: 'Products', component: () => import('@/views/Products.vue'), meta: { title: '产品管理' } },
      { path: 'products/create', name: 'ProductCreate', component: () => import('@/views/ProductForm.vue'), meta: { title: '新建产品' } },
      { path: 'products/edit/:id', name: 'ProductEdit', component: () => import('@/views/ProductForm.vue'), meta: { title: '编辑产品' } },
      { path: 'products/plan-sync', name: 'ProductPlanSync', component: () => import('@/views/ProductPlanSync.vue'), meta: { title: '套餐同步' } },
      { path: 'product-categories', name: 'ProductCategories', component: () => import('@/views/ProductCategories.vue'), meta: { title: '产品大分类' } },
      { path: 'product-subcategories', name: 'ProductSubcategories', component: () => import('@/views/ProductSubcategories.vue'), meta: { title: '产品二级分类' } },
      { path: 'orders', name: 'Orders', component: () => import('@/views/Orders.vue'), meta: { title: '订单管理' } },
      { path: 'tickets', name: 'Tickets', component: () => import('@/views/Tickets.vue'), meta: { title: '工单管理' } },
      { path: 'users', name: 'Users', component: () => import('@/views/Users.vue'), meta: { title: '用户管理' } },
      { path: 'admin-account', name: 'AdminAccount', component: () => import('@/views/AdminAccount.vue'), meta: { title: '管理员账号' } },
      { path: 'payment-methods', name: 'PaymentMethods', component: () => import('@/views/PaymentMethods.vue'), meta: { title: '支付方式' } },
      { path: 'faq', name: 'FAQ', component: () => import('@/views/FAQ.vue'), meta: { title: '常见问题' } },
      { path: 'tutorials', name: 'Tutorials', component: () => import('@/views/Tutorials.vue'), meta: { title: '教程管理' } },
      { path: 'app-download-qrcodes', name: 'AppDownloadQrcodes', component: () => import('@/views/AppDownloadQrcodes.vue'), meta: { title: 'App下载二维码' } },
      { path: 'coupons', name: 'Coupons', component: () => import('@/views/Coupons.vue'), meta: { title: '优惠券' } },
      { path: 'email-accounts', name: 'EmailAccounts', component: () => import('@/views/EmailAccounts.vue'), meta: { title: '邮件账户' } },
      { path: 'email-templates', name: 'EmailTemplates', component: () => import('@/views/EmailTemplates.vue'), meta: { title: '邮件模板' } },
      { path: 'emails', name: 'Emails', component: () => import('@/views/Emails.vue'), meta: { title: '收发件' } },
      { path: 'card-platform', name: 'CardPlatform', component: () => import('@/views/CardPlatform.vue'), meta: { title: '发卡平台' } },
      { path: 'card-platform/bulk-mapping', name: 'CardPlatformBulkMapping', component: () => import('@/views/CardPlatformBulkMapping.vue'), meta: { title: '批量映射' } },
      { path: 'dingtalk', name: 'DingTalk', component: () => import('@/views/DingTalk.vue'), meta: { title: '钉钉通知' } },
      { path: 'settings', name: 'Settings', component: () => import('@/views/Settings.vue'), meta: { title: '系统设置' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  const requiresAuth = to.matched.some((r) => r.meta.requiresAuth)
  if (requiresAuth && !auth.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && auth.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router
