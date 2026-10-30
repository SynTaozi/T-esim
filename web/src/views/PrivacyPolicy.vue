<template>
  <div class="privacy-page">
    <!-- 导航栏 -->
    <SiteHeader />

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="hero-bg"></div>
      <div class="container">
        <div class="breadcrumb">
          <router-link to="/">{{ $t('orderDetail.home') }}</router-link>
          <span class="sep">›</span>
          <span>{{ $t('footer.privacy') }}</span>
        </div>
        <h1>{{ $t('privacy.title') }}</h1>
        <p class="last-updated">{{ $t('privacy.lastUpdated') }}：2026年04月19日</p>
      </div>
    </div>

    <div class="content-container main-content">
      <div class="policy-card">
        <!-- 欢迎语 -->
        <section class="policy-section">
          <p class="intro">{{ $t('privacy.intro') }}</p>
          <p class="principles">{{ $t('privacy.principles') }}</p>
        </section>

        <!-- 一、我们如何收集和使用您的个人信息 -->
        <section class="policy-section">
          <h2>{{ $t('privacy.section1Title') }}</h2>
          <p class="note">{{ $t('privacy.section1Note') }}</p>
          
          <div class="subsection">
            <h3>{{ $t('privacy.subtitle1') }}</h3>
            <p>{{ $t('privacy.subtitle1Content') }}</p>
          </div>

          <div class="subsection">
            <h3>{{ $t('privacy.subtitle2') }}</h3>
            <p>{{ $t('privacy.subtitle2Content') }}</p>
          </div>

          <div class="subsection">
            <h3>{{ $t('privacy.subtitle3') }}</h3>
            <p>{{ $t('privacy.subtitle3Content') }}</p>
          </div>
        </section>

        <!-- 二、我们如何共享、转让、公开披露您的个人信息 -->
        <section class="policy-section">
          <h2>{{ $t('privacy.section2Title') }}</h2>
          <p class="intro">{{ $t('privacy.section2Intro') }}</p>
          <ul class="policy-list">
            <li>{{ $t('privacy.section2Item1') }}</li>
            <li>{{ $t('privacy.section2Item2') }}</li>
            <li>{{ $t('privacy.section2Item3') }}</li>
          </ul>
        </section>

        <!-- 三、我们如何保护您的个人信息 -->
        <section class="policy-section">
          <h2>{{ $t('privacy.section3Title') }}</h2>
          <p>{{ $t('privacy.section3Content') }}</p>
        </section>

        <!-- 四、您的权利 -->
        <section class="policy-section">
          <h2>{{ $t('privacy.section4Title') }}</h2>
          <p>{{ $t('privacy.section4Content') }}</p>
        </section>

        <!-- 五、联系我们 -->
        <section class="policy-section">
          <h2>{{ $t('privacy.section5Title') }}</h2>
          <p>{{ $t('privacy.section5Content') }}</p>
          <div class="contact-info">
            <div class="contact-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                <polyline points="22,6 12,13 2,6"/>
              </svg>
              <a href="mailto:support@tesim.com">support@tesim.com</a>
            </div>
          </div>
        </section>
      </div>
    </div>

    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'

const { t } = useI18n({ useScope: 'global' })

onMounted(() => {
  window.scrollTo(0, 0)
})
</script>

<style scoped>
.privacy-page {
  min-height: 100vh;
  background: #ffffff;
}

/* 标头：全站 container，标题左缘与顶栏 logo 对齐 */
.page-hero {
  position: relative;
  background: var(--bg-primary);
  padding: 40px 0 60px;
  overflow: hidden;
  border-bottom: 1px solid var(--border-subtle);
}

.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background:
    radial-gradient(ellipse at 30% 50%, var(--accent-muted) 0%, transparent 60%),
    radial-gradient(ellipse at 70% 50%, var(--accent-muted) 0%, transparent 60%);
  pointer-events: none;
}

.page-hero .container {
  position: relative;
  z-index: 1;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 16px;
}

.breadcrumb a {
  color: var(--accent-primary);
  text-decoration: none;
  transition: color 0.2s;
}

.breadcrumb a:hover {
  color: var(--text-primary);
}

.sep {
  color: var(--text-muted);
}

.page-hero h1 {
  font-size: clamp(28px, 5vw, 42px);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.last-updated {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

/* 正文：保持原 800px 居中窄栏，不随标头拉宽 */
.content-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px;
  box-sizing: border-box;
}

.main-content {
  padding: 40px 24px 80px;
  background: #ffffff;
}

.policy-card {
  background: var(--bg-card);
  border-radius: 16px;
  box-shadow: var(--shadow-md);
  overflow: hidden;
  border: 1px solid var(--border-subtle);
}

/* 章节样式 */
.policy-section {
  padding: 32px 40px;
  border-bottom: 1px solid var(--border-subtle);
}

.policy-section:last-child {
  border-bottom: none;
}

.policy-section h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.policy-section h2::before {
  content: '';
  width: 4px;
  height: 20px;
  background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));
  border-radius: 2px;
}

.policy-section p {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin-bottom: 12px;
}

.policy-section .intro {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 500;
}

.policy-section .principles {
  margin-top: 12px;
  font-size: 13px;
  color: var(--text-muted);
}

.policy-section .note {
  background: var(--bg-elevated);
  padding: 16px;
  border-radius: 10px;
  margin-bottom: 20px;
  border-left: 3px solid var(--accent-primary);
}

/* 子章节 */
.subsection {
  margin-top: 20px;
  padding: 16px 20px;
  background: var(--bg-elevated);
  border-radius: 12px;
}

.subsection h3 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.subsection h3::before {
  content: '•';
  color: var(--accent-primary);
  font-size: 18px;
}

.subsection p {
  margin-bottom: 0;
}

/* 列表样式 */
.policy-list {
  margin: 16px 0;
  padding-left: 20px;
}

.policy-list li {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin-bottom: 8px;
  position: relative;
}

.policy-list li::marker {
  color: var(--accent-primary);
}

/* 联系信息 */
.contact-info {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  background: var(--bg-elevated);
  border-radius: 10px;
  transition: all 0.2s;
}

.contact-item:hover {
  background: var(--bg-card);
  box-shadow: var(--shadow-md);
}

.contact-item svg {
  width: 20px;
  height: 20px;
  color: var(--accent-primary);
  flex-shrink: 0;
}

.contact-item a {
  color: var(--accent-primary);
  font-weight: 500;
  text-decoration: none;
  transition: color 0.2s;
}

.contact-item a:hover {
  color: var(--accent-hover);
  text-decoration: underline;
}

/* 响应式 */
@media (max-width: 600px) {
  .page-hero {
    padding: 32px 0;
  }

  .page-hero h1 {
    font-size: 28px;
  }

  .policy-section {
    padding: 24px 20px;
  }

  .policy-section h2 {
    font-size: 18px;
  }

  .subsection {
    padding: 14px 16px;
  }

  .nav {
    display: none;
  }
}
</style>
