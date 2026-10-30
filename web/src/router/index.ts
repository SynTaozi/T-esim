import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNavigationStore } from '@/stores/navigation'
import { applyPageSeo, resolveRouteSeo, type RouteSeoMeta } from '@/utils/seo'
import { i18n } from '@/i18n'
import Home from '@/views/Home.vue'
import Products from '@/views/Products.vue'
import FAQ from '@/views/FAQ.vue'
import Tutorials from '@/views/Tutorials.vue'
import About from '@/views/About.vue'
/** 同步引入，避免懒加载旧 chunk 闪出历史 mock 支付页 */
import StripeCheckoutRedirect from '@/components/payment/StripeCheckoutRedirect.vue'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    guest?: boolean
    paymentMethod?: string
    seo?: RouteSeoMeta
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: {
      seo: {
        title: 'Tesim - 全球 eSIM 与实体卡',
        titleEn: 'Tesim - Global eSIM & Physical SIM',
        description: 'Tesim 提供全球 eSIM 与实体 SIM 卡，覆盖多国家地区，即买即用。',
        descriptionEn: 'Tesim offers global eSIM and physical SIM cards for travel and overseas stay.'
      }
    }
  },
  {
    path: '/products',
    name: 'Products',
    component: Products,
    meta: {
      guest: true,
      seo: {
        title: '选购全球 eSIM 与实体卡 | Tesim',
        titleEn: 'Shop Global eSIM & SIM Cards | Tesim',
        description: '浏览 Tesim 全球流量与电话套餐，支持 eSIM 即时开通与实体卡邮寄。',
        descriptionEn: 'Browse Tesim global data and phone plans. Instant eSIM and physical SIM shipping.'
      }
    }
  },
  {
    path: '/faq',
    name: 'FAQ',
    component: FAQ,
    meta: {
      seo: {
        title: '常见问题 | Tesim eSIM 与实体卡',
        titleEn: 'FAQ | Tesim eSIM & SIM',
        description: '了解 Tesim eSIM 激活、实体卡邮寄、套餐有效期、支付与售后等常见问题。',
        descriptionEn: 'Answers about Tesim eSIM activation, shipping, validity, payments, and support.'
      }
    }
  },
  {
    path: '/tutorials',
    name: 'Tutorials',
    component: Tutorials,
    meta: {
      seo: {
        title: '安装/使用教程 | Tesim',
        titleEn: 'Install & Usage Guides | Tesim',
        description: 'Tesim 图文、视频与 PDF 教程：安装激活、账号订单等。',
        descriptionEn: 'Tesim guides for install, account, and orders.'
      }
    }
  },
  {
    path: '/tutorials/:slug',
    name: 'TutorialDetail',
    component: () => import('@/views/TutorialDetail.vue'),
    meta: {
      seo: {
        title: '教程详情 | Tesim',
        titleEn: 'Tutorial | Tesim',
        description: '查看 Tesim 教程详情。',
        descriptionEn: 'View Tesim tutorial details.'
      }
    }
  },
  {
    path: '/privacy',
    name: 'PrivacyPolicy',
    component: () => import('@/views/PrivacyPolicy.vue'),
    meta: {
      seo: {
        title: '隐私政策 | Tesim',
        titleEn: 'Privacy Policy | Tesim',
        description: 'Tesim 隐私政策：我们如何收集、使用与保护您的个人信息。',
        descriptionEn: 'Tesim Privacy Policy: how we collect, use, and protect your personal information.'
      }
    }
  },
  {
    path: '/terms',
    name: 'TermsOfService',
    component: () => import('@/views/TermsOfService.vue'),
    meta: {
      seo: {
        title: '服务条款 | Tesim',
        titleEn: 'Terms of Service | Tesim',
        description: 'Tesim 服务条款：使用本网站与购买产品前请仔细阅读。',
        descriptionEn: 'Tesim Terms of Service for using the website and purchasing products.'
      }
    }
  },
  {
    path: '/disclaimer',
    name: 'Disclaimer',
    component: () => import('@/views/Disclaimer.vue'),
    meta: {
      seo: {
        title: '免责声明 | Tesim',
        titleEn: 'Disclaimer | Tesim',
        description: 'Tesim 免责声明与使用须知。',
        descriptionEn: 'Tesim disclaimer and usage notices.'
      }
    }
  },
  {
    path: '/refund-policy',
    name: 'RefundPolicy',
    component: () => import('@/views/RefundPolicy.vue'),
    meta: {
      seo: {
        title: '退款政策 | Tesim',
        titleEn: 'Refund Policy | Tesim',
        description: 'Tesim eSIM 与实体卡退款政策说明。',
        descriptionEn: 'Tesim refund policy for eSIM and physical SIM products.'
      }
    }
  },
  {
    path: '/about',
    name: 'About',
    component: About,
    meta: {
      seo: {
        title: '关于我们 | Tesim',
        titleEn: 'About Us | Tesim',
        description: 'Tesim 提供全球 eSIM 与连接服务，为出海与旅行用户提供即买即用的通信方案。',
        descriptionEn: 'Tesim is a global eSIM and connectivity platform for travelers and overseas users.'
      }
    }
  },
  {
    path: '/contact',
    name: 'Contact',
    component: () => import('@/views/ContactUs.vue'),
    meta: {
      seo: {
        title: '联系我们 | Tesim',
        titleEn: 'Contact Us | Tesim',
        description: '联系 Tesim 客服与商务合作渠道。',
        descriptionEn: 'Contact Tesim support and business partnership channels.'
      }
    }
  },
  {
    path: '/products/:id',
    name: 'ProductDetail',
    component: () => import('@/views/ProductDetail.vue'),
    meta: {
      guest: true,
      seo: {
        title: '产品详情 | Tesim',
        titleEn: 'Product Detail | Tesim',
        description: '查看 Tesim 套餐详情、覆盖国家、流量与价格，立即购买全球 eSIM 或实体卡。',
        descriptionEn: 'View Tesim plan details, coverage, data and pricing.'
      }
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { seo: { noindex: true, title: '登录 | Tesim', titleEn: 'Login | Tesim' } }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { seo: { noindex: true, title: '注册 | Tesim', titleEn: 'Register | Tesim' } }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/ForgotPassword.vue'),
    meta: { seo: { noindex: true, title: '忘记密码 | Tesim', titleEn: 'Forgot Password | Tesim' } }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('@/views/Checkout.vue'),
    meta: { requiresAuth: true, seo: { noindex: true } }
  },
  {
    path: '/order-confirm',
    name: 'OrderConfirm',
    component: () => import('@/views/OrderConfirm.vue'),
    meta: { requiresAuth: true, seo: { noindex: true } }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('@/views/Orders.vue'),
    meta: { requiresAuth: true, seo: { noindex: true } }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/views/OrderDetail.vue'),
    meta: { requiresAuth: true, seo: { noindex: true } }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true, seo: { noindex: true } }
  },
  {
    path: '/payment-confirm',
    name: 'PaymentConfirm',
    component: () => import('@/views/PaymentConfirm.vue'),
    meta: { requiresAuth: true, seo: { noindex: true } }
  },
  {
    path: '/tickets',
    name: 'Tickets',
    component: () => import('@/views/Tickets.vue'),
    meta: { requiresAuth: true, seo: { noindex: true } }
  },
  {
    path: '/ticket/new',
    name: 'NewTicket',
    component: () => import('@/views/NewTicket.vue'),
    meta: { requiresAuth: true, seo: { noindex: true } }
  },
  {
    path: '/ticket/:id',
    name: 'TicketDetail',
    component: () => import('@/views/TicketDetail.vue'),
    meta: { requiresAuth: true, seo: { noindex: true } }
  },
  {
    path: '/pay/:id',
    name: 'Pay',
    component: () => import('@/views/UsdtPay.vue'),
    meta: { requiresAuth: true, seo: { noindex: true } }
  },
  {
    path: '/checkout/usdt/:id',
    name: 'UsdtPay',
    component: () => import('@/views/UsdtPay.vue'),
    meta: { requiresAuth: true, seo: { noindex: true } }
  },
  {
    path: '/checkout/wechat/:id',
    name: 'WechatPay',
    component: StripeCheckoutRedirect,
    props: { paymentMethod: 'WECHAT' },
    meta: { requiresAuth: true, paymentMethod: 'WECHAT', seo: { noindex: true } }
  },
  {
    path: '/checkout/alipay/:id',
    name: 'Alipay',
    component: StripeCheckoutRedirect,
    props: { paymentMethod: 'ALIPAY' },
    meta: { requiresAuth: true, paymentMethod: 'ALIPAY', seo: { noindex: true } }
  },
  {
    path: '/checkout/bankcard/:id',
    name: 'BankCardPay',
    component: StripeCheckoutRedirect,
    props: { paymentMethod: 'BANK_CARD' },
    meta: { requiresAuth: true, paymentMethod: 'BANK_CARD', seo: { noindex: true } }
  },
  {
    path: '/checkout/stripe/success',
    name: 'StripePaySuccess',
    component: () => import('@/views/StripePaySuccess.vue'),
    meta: { requiresAuth: true, seo: { noindex: true } }
  },
  {
    path: '/checkout/stripe/cancel',
    name: 'StripePayCancel',
    component: () => import('@/views/StripePayCancel.vue'),
    meta: { requiresAuth: true, seo: { noindex: true } }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const navigationStore = useNavigationStore()

  if (to.path !== from.path) {
    navigationStore.startRouteLoading()
  }

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  } else {
    next()
  }
})

router.afterEach((to) => {
  useNavigationStore().finishRouteLoading()

  const locale = String(i18n.global.locale.value || 'zh')
  document.documentElement.lang = locale === 'en' ? 'en' : 'zh-CN'

  if (to.name === 'ProductDetail') return

  const seo = resolveRouteSeo(to.meta.seo, locale)
  applyPageSeo({
    ...seo,
    path: to.path
  })
})

export default router
