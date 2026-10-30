<template>
  <div class="faq-page">
    <!-- 导航栏 -->
    <SiteHeader />

    <!-- 页面标题 -->
    <div class="page-hero">
      <div class="container">
        <h1>{{ $t('faq.title') }}</h1>
        <p>{{ $t('faq.subtitle') }}</p>
      </div>
    </div>

    <div class="container main-content">
      <div class="faq-layout">
        <!-- 左侧分类导航 -->
        <aside class="faq-sidebar">
          <div class="sidebar-header">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 6h16M4 12h16M4 18h16"/>
            </svg>
            <span>{{ $t('faq.categories') }}</span>
          </div>
          <nav class="category-nav">
            <button
              :class="['category-item', { active: !selectedCategoryId }]"
              @click="selectedCategoryId = null">
              <span class="cat-icon">📋</span>
              <span class="cat-name">{{ $t('faq.allQuestions') }}</span>
              <span class="cat-count">{{ faqs.length }}</span>
            </button>
            <button
              v-for="category in categories"
              :key="category.id"
              :class="['category-item', { active: selectedCategoryId === category.id }]"
              @click="selectedCategoryId = category.id">
              <span class="cat-icon">{{ getCategoryIcon(category) }}</span>
              <span class="cat-name">{{ getCategoryName(category) }}</span>
              <span class="cat-count">{{ getCategoryFAQCount(category.id) }}</span>
            </button>
          </nav>
          
          <!-- 联系卡片 -->
          <div class="contact-card">
            <div class="contact-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
              </svg>
            </div>
            <h4>{{ $t('faq.needHelp') }}</h4>
            <p>{{ $t('faq.replyWithin') }}</p>
            <a href="mailto:service@tesim.com" class="contact-btn">
              {{ $t('faq.contactUs') }}
            </a>
          </div>
        </aside>

        <!-- 右侧问题列表 -->
        <main class="faq-main">
          <!-- 搜索 -->
          <div class="search-bar">
            <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="m21 21-4.3-4.3"/>
            </svg>
            <input
              type="text"
              v-model="searchQuery"
              :placeholder="$t('faq.searchPlaceholder')"
            />
            <span v-if="searchQuery" class="search-hint">
              {{ filteredFAQs.length }} {{ $t('faq.results') }}
            </span>
          </div>

          <!-- 当前分类标题 -->
          <div class="section-header" v-if="selectedCategory">
            <h2>{{ getCategoryName(selectedCategory) }}</h2>
            <span class="faq-count">{{ filteredFAQs.length }} {{ $t('faq.questions') }}</span>
          </div>

          <!-- FAQ 手风琴列表 -->
          <div v-loading="loading" class="faq-accordion">
            <div
              v-for="(faq, index) in filteredFAQs"
              :key="faq.id"
              :class="['accordion-item', { open: expandedFAQIds.includes(faq.id) }]"
              :style="{ '--delay': `${index * 60}ms` }">
              <button class="accordion-header" @click="toggleFAQ(faq.id)">
                <span class="accordion-number">{{ String(index + 1).padStart(2, '0') }}</span>
                <span class="accordion-title">{{ getFAQQuestion(faq) }}</span>
                <span class="accordion-arrow">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="m6 9 6 6 6-6"/>
                  </svg>
                </span>
              </button>
              <div class="accordion-content">
                <div class="accordion-body">
                  <div class="answer-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="20 6 9 17 4 12"/>
                    </svg>
                  </div>
                  <div class="answer-content" v-html="getFAQAnswerHTML(faq)"></div>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="filteredFAQs.length === 0 && !loading" class="empty-state">
              <div class="empty-illustration">
                <svg viewBox="0 0 120 120" fill="none">
                  <circle cx="60" cy="60" r="50" fill="#f1f5f9"/>
                  <circle cx="60" cy="50" r="20" fill="#e2e8f0"/>
                  <path d="M55 45h10M55 55h10" stroke="#94a3b8" stroke-width="2" stroke-linecap="round"/>
                  <circle cx="60" cy="85" r="3" fill="#cbd5e1"/>
                  <path d="M50 85h20" stroke="#cbd5e1" stroke-width="3" stroke-linecap="round"/>
                </svg>
              </div>
              <h3>{{ $t('faq.noResults') }}</h3>
              <p>{{ $t('faq.tryOther') }}</p>
              <button class="reset-btn" @click="searchQuery = ''; selectedCategoryId = null">
                {{ $t('faq.resetSearch') }}
              </button>
            </div>
          </div>
        </main>
      </div>

      <!-- 底部联系方式 -->
      <div class="contact-footer">
        <h3>{{ $t('faq.contactTitle') }}</h3>
        <div class="contact-methods">
          <a href="https://wa.me/1234567890" target="_blank" class="contact-method-item whatsapp">
            <div class="method-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 01-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 01-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 012.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0012.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 005.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 00-3.48-8.413z"/>
              </svg>
            </div>
            <div class="method-text">
              <span class="method-label">{{ $t('faq.whatsapp') }}</span>
              <span class="method-value">+1 213 343 8884</span>
            </div>
          </a>
          <a href="mailto:support@tesim.com" class="contact-method-item email">
            <div class="method-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                <polyline points="22,6 12,13 2,6"/>
              </svg>
            </div>
            <div class="method-text">
              <span class="method-label">{{ $t('faq.email') }}</span>
              <span class="method-value">support@tesim.com</span>
            </div>
          </a>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
        <SiteFooter />

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { faqApi, FAQ, FAQCategory } from '@/api/faq'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
const { locale, t } = useI18n({ useScope: 'global' })
const authStore = useAuthStore()

const categories = ref<FAQCategory[]>([])
const faqs = ref<FAQ[]>([])
const loading = ref(false)
const selectedCategoryId = ref<number | null>(null)
const expandedFAQIds = ref<number[]>([])
const searchQuery = ref('')

const selectedCategory = computed(() => {
  if (!selectedCategoryId.value) return null
  return categories.value.find(c => c.id === selectedCategoryId.value)
})

const filteredFAQs = computed(() => {
  let result = faqs.value.filter(faq => faq.status === 1)
  
  if (selectedCategoryId.value) {
    result = result.filter(faq => Number(faq.categoryId) === Number(selectedCategoryId.value))
  }
  
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(faq => 
      getFAQQuestion(faq).toLowerCase().includes(query) ||
      getFAQAnswer(faq).toLowerCase().includes(query)
    )
  }
  
  return result
})

onMounted(() => {
  loadData()
})

async function loadData() {
  try {
    loading.value = true
    const catRes = await faqApi.getCategories()
    if (catRes?.code === 200 && catRes?.data) {
      const uniqueMap = new Map()
      catRes.data
        .filter((cat: any) => cat.status === 1)
        .forEach((cat: any) => {
          const key = cat.name || cat.nameEn
          if (key && !uniqueMap.has(key)) {
            uniqueMap.set(key, cat)
          }
        })
      categories.value = Array.from(uniqueMap.values())
    }
    
    const faqRes = await faqApi.getFAQs()
    if (faqRes?.code === 200 && faqRes?.data) {
      const uniqueFAQMap = new Map()
      faqRes.data.forEach((faq: any) => {
        if (!uniqueFAQMap.has(faq.id)) {
          uniqueFAQMap.set(faq.id, faq)
        }
      })
      faqs.value = Array.from(uniqueFAQMap.values())
    }
  } catch (error) {
    console.error('加载 FAQ 数据失败:', error)
  } finally {
    loading.value = false
  }
}

function toggleLocale() {
  _toggleLocale()
}

function getCategoryName(category: FAQCategory): string {
  if (locale.value === 'en' && category.nameEn) {
    return category.nameEn
  }
  return category.name
}

function getCategoryIcon(category: FAQCategory): string {
  const name = locale.value === 'en' && category.nameEn ? category.nameEn : category.name
  const iconMap: Record<string, string> = {
    '购买': '🛒',
    'Purchase': '🛒',
    '激活': '📱',
    'Activation': '📱',
    '激活与使用': '📱',
    'Activation & Usage': '📱',
    '支持': '🎧',
    'Support': '🎧',
    '技术支持': '🎧',
    'Technical Support': '🎧',
    '充值': '💳',
    'Recharge': '💳',
    '账户与充值': '💳',
    'Account & Recharge': '💳'
  }
  return iconMap[name] || '📖'
}

function getCategoryFAQCount(categoryId: number): number {
  return faqs.value.filter(f => Number(f.categoryId) === categoryId && f.status === 1).length
}

function getFAQQuestion(faq: FAQ): string {
  if (locale.value === 'en' && faq.questionEn) {
    return faq.questionEn
  }
  return faq.question
}

function getFAQAnswer(faq: FAQ): string {
  if (locale.value === 'en' && faq.answerEn) {
    return faq.answerEn
  }
  return faq.answer
}

// 支持HTML格式的答案渲染（可插入图片）
function getFAQAnswerHTML(faq: FAQ): string {
  const answer = getFAQAnswer(faq)
  // 换行转<br>，支持 ![alt](url) 图片格式
  return answer
    .replace(/\n/g, '<br>')
    .replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" style="max-width: 100%; border-radius: 8px; margin: 12px 0;" />')
}

function toggleFAQ(id: number) {
  const index = expandedFAQIds.value.indexOf(id)
  if (index > -1) {
    expandedFAQIds.value.splice(index, 1)
  } else {
    expandedFAQIds.value.push(id)
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.faq-page {
  min-height: 100vh;
}

.container {
  max-width: var(--site-content-max-width, 1300px);
  margin: 0 auto;
  padding: 0 var(--site-content-padding-x, 24px);
}

.logo a {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.logo-icon {
  width: 28px;
  height: 28px;
}



.nav {
  display: flex;
  gap: 32px;
}

.nav a {
  text-decoration: none;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s;
}

.nav a:hover, .nav a.active {
  color: var(--primary);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.lang-toggle {
  padding: 6px 14px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
}

.btn-login {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
}

.btn-primary {
  padding: 8px 18px;
  border-radius: 6px;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  color: white;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  color: white;
  text-decoration: none;
}

/* Hero */
.page-hero {
  padding: 48px 0;
  text-align: center;
}

.page-hero h1 {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.page-hero p {
  font-size: 16px;
  color: var(--text-secondary);
}

/* 主内容布局 */
.main-content {
  padding: 40px 24px 80px;
}

.faq-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 32px;
}

/* 侧边栏 */
.faq-sidebar {
  position: sticky;
  top: 88px;
  height: fit-content;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 0 16px;
  border-bottom: 1px solid color-mix(in srgb, var(--border-subtle) 55%, transparent);
  box-shadow: 0 1px 0 color-mix(in srgb, var(--border-subtle) 35%, transparent);
  margin-bottom: 16px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.sidebar-header svg {
  width: 16px;
  height: 16px;
}

.category-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: none;
  background: transparent;
  border-radius: 10px;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s;
  width: 100%;
}

.category-item:hover {
  background: var(--bg-light);
}

.category-item.active {
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  color: white;
}

.category-item.active .cat-count {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.cat-icon {
  font-size: 18px;
}

.cat-name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
}

.cat-count {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  background: var(--bg-light);
  border-radius: 10px;
  color: var(--text-muted);
}

/* 联系卡片 */
.contact-card {
  margin-top: 24px;
  padding: 20px;
  background: linear-gradient(135deg, var(--bg-elevated) 0%, var(--bg-card) 100%);
  border-radius: 14px;
  text-align: center;
}

.contact-icon {
  width: 44px;
  height: 44px;
  margin: 0 auto 12px;
  background: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
}

.contact-icon svg {
  width: 22px;
  height: 22px;
  color: var(--accent-primary);
}

.contact-card h4 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.contact-card p {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 16px;
}

.contact-btn {
  display: inline-block;
  padding: 10px 20px;
  background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));
  color: white;
  text-decoration: none;
  font-size: 13px;
  font-weight: 600;
  border-radius: 8px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.contact-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow);
}

/* 主内容区 */
.faq-main {
  min-width: 0;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  background: white;
  border: 2px solid var(--border);
  border-radius: 12px;
  margin-bottom: 24px;
  transition: border-color 0.2s;
}

.search-bar:focus-within {
  border-color: var(--primary);
}

.search-icon {
  width: 20px;
  height: 20px;
  color: var(--text-muted);
  flex-shrink: 0;
}

.search-bar input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  color: var(--text-primary);
  background: transparent;
}

.search-bar input::placeholder {
  color: var(--text-muted);
}

.search-hint {
  font-size: 13px;
  color: var(--text-muted);
  padding: 4px 10px;
  background: var(--bg-light);
  border-radius: 6px;
}

/* 分类标题 */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.faq-count {
  font-size: 13px;
  color: var(--text-muted);
}

/* 手风琴列表 */
.faq-accordion {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.accordion-item {
  background: white;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  animation: slideIn 0.3s ease forwards;
  animation-delay: var(--delay, 0ms);
  opacity: 0;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.accordion-header {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
  padding: 18px 20px;
  border: none;
  background: transparent;
  cursor: pointer;
  text-align: left;
  transition: background 0.2s;
}

.accordion-header:hover {
  background: var(--bg-light);
}

.accordion-item.open .accordion-header {
  background: var(--bg-light);
}

.accordion-number {
  font-size: 12px;
  font-weight: 700;
  color: var(--accent-primary);
  background: var(--accent-muted);
  padding: 4px 10px;
  border-radius: 6px;
}

.accordion-title {
  flex: 1;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.accordion-arrow {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  transition: transform 0.3s;
}

.accordion-arrow svg {
  width: 18px;
  height: 18px;
}

.accordion-item.open .accordion-arrow {
  transform: rotate(180deg);
  color: var(--primary);
}

.accordion-content {
  display: none;
}

.accordion-item.open .accordion-content {
  display: block;
}

.accordion-body {
  display: flex;
  gap: 14px;
  padding: 0 20px 20px 56px;
}

.answer-icon {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--accent-muted);
  border-radius: 50%;
}

.answer-icon svg {
  width: 14px;
  height: 14px;
  color: var(--accent-success);
}

.answer-content {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.answer-content img {
  max-width: 100%;
  border-radius: 8px;
  margin: 12px 0;
  display: block;
}

/* 空状态 */
.empty-state {
  padding: 60px 20px;
  text-align: center;
  background: white;
  border-radius: 14px;
}

.empty-illustration {
  width: 120px;
  height: 120px;
  margin: 0 auto 20px;
}

.empty-illustration svg {
  width: 100%;
  height: 100%;
}

.empty-state h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.empty-state p {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 20px;
}

.reset-btn {
  padding: 10px 24px;
  border: none;
  background: var(--bg-light);
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.reset-btn:hover {
  background: var(--primary);
  color: white;
}

/* 底部联系方式 */
.contact-footer {
  margin-top: 60px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: var(--shadow-sm);
}

.contact-footer h3 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 20px;
  text-align: center;
}

.contact-methods {
  display: flex;
  justify-content: center;
  gap: 24px;
  flex-wrap: wrap;
}

.contact-method-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  background: var(--bg-light);
  border-radius: 12px;
  text-decoration: none;
  transition: all 0.2s;
}

.contact-method-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.contact-method-item .method-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 10px;
}

.contact-method-item .method-icon svg {
  width: 22px;
  height: 22px;
}

.contact-method-item.whatsapp .method-icon svg {
  color: #25D366;
}

.contact-method-item.email .method-icon svg {
  color: #3B82F6;
}

.contact-method-item .method-text {
  display: flex;
  flex-direction: column;
}

.contact-method-item .method-label {
  font-size: 12px;
  color: var(--text-muted);
}

.contact-method-item .method-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

/* 响应式 */
@media (max-width: 900px) {
  .faq-layout {
    grid-template-columns: 1fr;
  }

  .faq-sidebar {
    position: static;
  }

  .category-nav {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 8px;
  }

  .category-item {
    flex: 0 0 auto;
    padding: 10px 14px;
    background: white;
    border: 1px solid var(--border);
  }

  .category-item.active {
    border-color: transparent;
  }

  .cat-count {
    display: none;
  }

  .nav {
    display: none;
  }
}

@media (max-width: 600px) {
  .contact-footer {
    padding: 24px;
  }

  .contact-footer h3 {
    font-size: 18px;
  }

  .contact-methods {
    flex-direction: column;
  }

  .contact-method-item {
    width: 100%;
    justify-content: center;
  }
}
</style>
