<template>
  <div class="site-header-shell">
  <header class="site-header">
    <div class="container header-content">
      <div class="logo">
        <router-link to="/">
          <span class="logo-icon">
            <img src="/logo.png" alt="Tesim logo" />
          </span>
          <span class="logo-text">Tesim</span>
        </router-link>
      </div>
      <button class="menu-toggle" :aria-expanded="mobileMenuOpen" aria-label="Toggle menu" @click="toggleMobileMenu">
        <span class="menu-bar"></span>
        <span class="menu-bar"></span>
        <span class="menu-bar"></span>
      </button>
      <nav class="nav" :class="{ 'mobile-open': mobileMenuOpen }">
        <router-link to="/" :class="{ active: isActiveRoute('/') }" @click="onNavClick">{{ $t('nav.home') }}</router-link>
        <router-link to="/products" :class="{ active: isActiveRoute('/products') || isActiveRoute('/product') }" @click="onNavClick">{{ $t('nav.products') }}</router-link>
        <router-link to="/faq" :class="{ active: isActiveRoute('/faq') }" @click="onNavClick">{{ $t('nav.faq') }}</router-link>
        <router-link to="/tutorials" :class="{ active: isActiveRoute('/tutorials') }" @click="onNavClick">{{ $t('nav.tutorials') }}</router-link>
        <router-link to="/about" :class="{ active: isActiveRoute('/about') }" @click="onNavClick">{{ $t('nav.about') }}</router-link>
        <router-link to="/tickets" :class="{ active: isTicketsActive() }" @click="onNavClick">{{ $t('nav.tickets') }}</router-link>
        <router-link to="/contact" :class="{ active: isActiveRoute('/contact') }" @click="onNavClick">{{ $t('nav.contactUs') }}</router-link>
        <div class="mobile-actions">
          <button
            type="button"
            class="lang-toggle"
            @click="toggleLocale"
            :title="locale === 'zh' ? '切换至英文' : 'Switch to Chinese'"
            :aria-label="locale === 'zh' ? '切换至英文' : 'Switch to Chinese'"
          >
            <NavLocaleFlag :target-locale="locale === 'zh' ? 'en' : 'zh'" />
          </button>
          <span class="header-actions-divider" aria-hidden="true" />
          <router-link v-if="!authStore.isLoggedIn" :to="loginTo" class="btn-login">{{ $t('nav.login') }}</router-link>
          <router-link v-if="!authStore.isLoggedIn" to="/register" class="btn-primary">{{ $t('nav.register') }}</router-link>
          <router-link v-if="authStore.isLoggedIn" to="/profile" class="user-avatar">
            <el-icon><User /></el-icon>
          </router-link>
        </div>
      </nav>
      <div class="header-actions desktop-actions">
        <button
          type="button"
          class="lang-toggle"
          @click="toggleLocale"
          :title="locale === 'zh' ? '切换至英文' : 'Switch to Chinese'"
          :aria-label="locale === 'zh' ? '切换至英文' : 'Switch to Chinese'"
        >
          <NavLocaleFlag :target-locale="locale === 'zh' ? 'en' : 'zh'" />
        </button>
        <span class="header-actions-divider" aria-hidden="true" />
        <router-link v-if="!authStore.isLoggedIn" :to="loginTo" class="btn-login">{{ $t('nav.login') }}</router-link>
        <router-link v-if="!authStore.isLoggedIn" to="/register" class="btn-primary">{{ $t('nav.register') }}</router-link>
        <router-link v-if="authStore.isLoggedIn" to="/profile" class="user-avatar">
          <el-icon><User /></el-icon>
        </router-link>
      </div>
    </div>
  </header>
  <div class="site-header-spacer" aria-hidden="true" />
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { toggleLocale as _toggleLocale } from '@/i18n'
import { User } from '@element-plus/icons-vue'
import { getLoginRedirectTarget } from '@/utils/authNavigation'
import NavLocaleFlag from '@/components/NavLocaleFlag.vue'

const { locale, t } = useI18n({ useScope: 'global' })
const route = useRoute()
const authStore = useAuthStore()
const mobileMenuOpen = ref(false)

const loginTo = computed(() => {
  const redirect = getLoginRedirectTarget(route)
  return redirect ? { path: '/login', query: { redirect } } : { path: '/login' }
})

watch(
  () => route.path,
  () => {
    mobileMenuOpen.value = false
  }
)

function handleResize() {
  if (window.innerWidth > 900) {
    mobileMenuOpen.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})

function toggleLocale() {
  _toggleLocale()
}

function toggleMobileMenu() {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

function onNavClick() {
  mobileMenuOpen.value = false
}

function isActiveRoute(path: string): boolean {
  return route.path === path || route.path.startsWith(path + '/')
}

function isTicketsActive(): boolean {
  return route.path.startsWith('/ticket') || route.path.startsWith('/tickets')
}
</script>

<style scoped>
/* Header layout — divider/shadow 见 main.css .site-header */
.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  position: relative;
}

.logo {
  flex-shrink: 0;
}

.logo a {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.logo-icon {
  width: 32px;
  height: 32px;
  aspect-ratio: 1 / 1;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
  display: block;
}

.nav {
  display: flex;
  gap: 28px;
  align-items: center;
}

.nav-referral-mobile {
  display: none;
  align-items: center;
  gap: 10px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.header-referral-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  color: var(--sh-text-secondary, #6e6e73);
  transition: color 0.2s;
}

.header-referral-link :deep(.nav-referral-money-icon) {
  font-size: 26px;
  color: currentColor;
}

.header-referral-link:hover,
.header-referral-link.active {
  color: var(--sh-accent, #0071e3);
}

.header-actions-divider {
  width: 1px;
  height: 18px;
  background: rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
}

.mobile-actions,
.menu-toggle {
  display: none;
}

.menu-toggle {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: 1px solid var(--sh-border, #d2d2d7);
  background: var(--sh-bg-card, #fff);
  cursor: pointer;
  padding: 8px;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 4px;
}

.menu-bar {
  width: 18px;
  height: 2px;
  background: var(--sh-text-primary, #1d1d1f);
  border-radius: 2px;
}

.lang-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  padding: 0;
  border-radius: 50%;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.lang-toggle:hover {
  transform: scale(1.04);
}

.btn-login {
  font-weight: 700;
  font-size: 14px;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 9999px;
  border: 1px solid var(--sh-accent, #0071e3);
  transition: background 0.2s, color 0.2s;
}

.btn-primary {
  color: #ffffff;
  font-weight: 700;
  font-size: 14px;
  padding: 8px 24px;
  border-radius: 9999px;
  text-decoration: none;
  transition: transform 0.2s, box-shadow 0.2s;
}

.btn-primary:hover {
  transform: scale(1.02);
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  text-decoration: none;
  transition: all 0.2s;
}

.user-avatar .el-icon {
  font-size: 18px;
}

@media (max-width: 900px) {
  .header-content {
    height: 60px;
  }

  .menu-toggle {
    display: inline-flex;
    order: 3;
  }

  .desktop-actions {
    display: none;
  }

  .nav {
    position: absolute;
    top: calc(100% + 8px);
    left: 16px;
    right: 16px;
    z-index: 120;
    background: var(--sh-bg-card, #fff);
    border: 1px solid var(--sh-border, #d2d2d7);
    border-radius: 14px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    padding: 12px;
    gap: 6px;
    flex-direction: column;
    align-items: stretch;
    opacity: 0;
    pointer-events: none;
    transform: translateY(-8px);
    transition: all 0.2s ease;
  }

  .nav.mobile-open {
    opacity: 1;
    pointer-events: auto;
    transform: translateY(0);
  }

  .nav a {
    padding: 10px 12px;
    border-radius: 10px;
  }

  .header-referral-link {
    display: none;
  }

  .mobile-actions {
    margin-top: 8px;
    padding-top: 10px;
    border-top: 1px solid var(--site-chrome-divider, rgba(0, 0, 0, 0.05));
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
  }

  .btn-primary,
  .btn-login,
  .lang-toggle {
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .logo a {
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .logo-icon {
    width: 28px;
    height: 28px;
  }

  .logo a {
    gap: 6px;
  }

  .nav {
    left: 12px;
    right: 12px;
  }
}
</style>
