<template>
  <div class="tutorials-page">
    <SiteHeader />

    <div class="page-hero">
      <div class="container">
        <h1>{{ $t('tutorials.title') }}</h1>
        <p>{{ $t('tutorials.subtitle') }}</p>
      </div>
    </div>

    <div class="container main-content">
      <div class="tutorials-layout">
        <aside class="tutorials-sidebar">
          <div class="sidebar-header">
            <span class="material-symbols-outlined">menu_book</span>
            <span>{{ $t('tutorials.categories') }}</span>
          </div>
          <nav class="category-nav">
            <button
              :class="['category-item', { active: !selectedCategoryId }]"
              @click="selectedCategoryId = null"
            >
              <span class="cat-name">{{ $t('tutorials.all') }}</span>
              <span class="cat-count">{{ tutorials.length }}</span>
            </button>
            <button
              v-for="category in categories"
              :key="category.id"
              :class="['category-item', { active: selectedCategoryId === category.id }]"
              @click="selectedCategoryId = category.id"
            >
              <span class="cat-name">{{ getCategoryName(category) }}</span>
              <span class="cat-count">{{ getCategoryCount(category.id) }}</span>
            </button>
          </nav>
        </aside>

        <main class="tutorials-main">
          <div class="toolbar">
            <div class="search-bar">
              <span class="material-symbols-outlined search-icon">search</span>
              <input
                v-model="searchQuery"
                type="text"
                :placeholder="$t('tutorials.searchPlaceholder')"
              />
            </div>
            <div class="type-filters">
              <button
                v-for="opt in typeOptions"
                :key="opt.value || 'all'"
                :class="['type-chip', { active: selectedType === opt.value }]"
                @click="selectedType = opt.value"
              >
                {{ opt.label }}
              </button>
            </div>
          </div>

          <div v-if="loading" class="loading-state">{{ $t('common.loading') }}</div>

          <div v-else-if="filteredTutorials.length === 0" class="empty-state">
            <p>{{ $t('tutorials.noResults') }}</p>
            <button class="reset-btn" @click="resetFilters">{{ $t('tutorials.resetSearch') }}</button>
          </div>

          <div v-else class="tutorial-grid">
            <template v-for="item in filteredTutorials" :key="item.id">
              <button
                v-if="item.contentType === 'VIDEO'"
                type="button"
                class="tutorial-card tutorial-card--video"
                @click="openVideoPlayer(item)"
              >
                <div class="card-cover">
                  <img
                    v-if="item.coverImageUrl"
                    :src="item.coverImageUrl"
                    :alt="getTitle(item)"
                    class="cover-img"
                  />
                  <div v-else class="cover-placeholder" data-type="VIDEO">
                    <span class="material-symbols-outlined">play_circle</span>
                  </div>
                  <span class="type-badge">{{ typeLabel('VIDEO') }}</span>
                  <span class="play-overlay" aria-hidden="true">
                    <span class="material-symbols-outlined">play_circle</span>
                  </span>
                </div>
                <div class="card-body">
                  <h3>{{ getTitle(item) }}</h3>
                  <p v-if="getSummary(item)">{{ getSummary(item) }}</p>
                  <div class="card-meta">
                    <span v-if="item.category">{{ getCategoryName(item.category) }}</span>
                    <span v-if="item.updatedAt">{{ formatDate(item.updatedAt) }}</span>
                  </div>
                </div>
              </button>

              <router-link
                v-else
                :to="`/tutorials/${item.slug}`"
                class="tutorial-card"
              >
                <div class="card-cover">
                  <img
                    v-if="item.coverImageUrl"
                    :src="item.coverImageUrl"
                    :alt="getTitle(item)"
                    class="cover-img"
                  />
                  <div v-else class="cover-placeholder" :data-type="item.contentType">
                    <span class="material-symbols-outlined">{{ typeIcon(item.contentType) }}</span>
                  </div>
                  <span class="type-badge">{{ typeLabel(item.contentType) }}</span>
                </div>
                <div class="card-body">
                  <h3>{{ getTitle(item) }}</h3>
                  <p v-if="getSummary(item)">{{ getSummary(item) }}</p>
                  <div class="card-meta">
                    <span v-if="item.category">{{ getCategoryName(item.category) }}</span>
                    <span v-if="item.updatedAt">{{ formatDate(item.updatedAt) }}</span>
                  </div>
                </div>
              </router-link>
            </template>
          </div>
        </main>
      </div>
    </div>

    <el-dialog
      v-model="videoDialogVisible"
      width="860px"
      top="8vh"
      align-center
      destroy-on-close
      append-to-body
      :show-close="true"
      :title="''"
      modal-class="tutorial-video-modal"
      class="tutorial-video-dialog"
      @closed="onVideoDialogClosed"
    >
      <div v-if="playingEmbed && playingEmbed.kind !== 'file'" class="dialog-embed-wrap">
        <iframe
          class="dialog-video-embed"
          :src="playingEmbed.src"
          title="Video"
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
          allowfullscreen
          referrerpolicy="no-referrer-when-downgrade"
          frameborder="0"
        />
      </div>
      <video
        v-else-if="playingEmbed"
        class="dialog-video-file"
        controls
        autoplay
        :src="playingEmbed.src"
        playsinline
      />
      <p v-else class="dialog-video-error">{{ $t('tutorials.videoUrlUnsupported') }}</p>
    </el-dialog>

    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import { tutorialApi, type Tutorial, type TutorialCategory, type TutorialContentType } from '@/api/tutorials'
import { resolveVideoEmbed, type ResolvedVideoEmbed } from '@/utils/videoEmbed'

const { t, locale } = useI18n({ useScope: 'global' })

const loading = ref(true)
const categories = ref<TutorialCategory[]>([])
const tutorials = ref<Tutorial[]>([])
const selectedCategoryId = ref<number | null>(null)
const selectedType = ref<TutorialContentType | ''>('')
const searchQuery = ref('')

const videoDialogVisible = ref(false)
const playingTutorial = ref<Tutorial | null>(null)
const playingEmbed = ref<ResolvedVideoEmbed | null>(null)

const typeOptions = computed(() => [
  { value: '' as const, label: t('tutorials.typeAll') },
  { value: 'ARTICLE' as const, label: t('tutorials.typeArticle') },
  { value: 'VIDEO' as const, label: t('tutorials.typeVideo') },
  { value: 'PDF' as const, label: t('tutorials.typePdf') }
])

const filteredTutorials = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  return tutorials.value.filter((item) => {
    if (selectedCategoryId.value && item.categoryId !== selectedCategoryId.value) return false
    if (selectedType.value && item.contentType !== selectedType.value) return false
    if (!q) return true
    const title = getTitle(item).toLowerCase()
    const summary = (getSummary(item) || '').toLowerCase()
    return title.includes(q) || summary.includes(q)
  })
})

function getCategoryName(category: TutorialCategory) {
  return locale.value === 'en' && category.nameEn ? category.nameEn : category.name
}

function getTitle(item: Tutorial) {
  return locale.value === 'en' && item.titleEn ? item.titleEn : item.title
}

function getSummary(item: Tutorial) {
  return locale.value === 'en' && item.summaryEn ? item.summaryEn : item.summary
}

function getCategoryCount(categoryId: number) {
  return tutorials.value.filter((tItem) => tItem.categoryId === categoryId).length
}

function typeIcon(type: string) {
  if (type === 'VIDEO') return 'play_circle'
  if (type === 'PDF') return 'picture_as_pdf'
  return 'article'
}

function typeLabel(type: string) {
  if (type === 'VIDEO') return t('tutorials.typeVideo')
  if (type === 'PDF') return t('tutorials.typePdf')
  return t('tutorials.typeArticle')
}

function formatDate(dateStr: string) {
  try {
    return new Date(dateStr).toLocaleDateString(locale.value === 'en' ? 'en-US' : 'zh-CN')
  } catch {
    return dateStr
  }
}

function resetFilters() {
  selectedCategoryId.value = null
  selectedType.value = ''
  searchQuery.value = ''
}

function openVideoPlayer(item: Tutorial) {
  playingTutorial.value = item
  playingEmbed.value = item.mediaUrl ? resolveVideoEmbed(item.mediaUrl) : null
  videoDialogVisible.value = true
}

function onVideoDialogClosed() {
  playingTutorial.value = null
  playingEmbed.value = null
}

async function loadData() {
  loading.value = true
  try {
    const res: any = await tutorialApi.getOverview()
    if (res.code === 200 && res.data) {
      categories.value = res.data.categories || []
      tutorials.value = res.data.tutorials || []
    }
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.tutorials-page {
  min-height: 100vh;
  background: #f6f7f9;
  color: #1a1d26;
}

.container {
  max-width: 1160px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-hero {
  padding: 48px 0 28px;
  background: linear-gradient(180deg, #eef4fb 0%, #f6f7f9 100%);
}

.page-hero h1 {
  margin: 0 0 8px;
  font-size: 32px;
  font-weight: 700;
}

.page-hero p {
  margin: 0;
  color: #717785;
  font-size: 15px;
}

.main-content {
  padding: 24px 20px 64px;
}

.tutorials-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 28px;
  align-items: start;
}

.tutorials-sidebar {
  background: #fff;
  border: 1px solid rgba(193, 198, 214, 0.45);
  border-radius: 12px;
  padding: 16px 12px;
  position: sticky;
  top: 88px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 14px;
  font-weight: 600;
  color: #414753;
  border-bottom: 1px solid rgba(193, 198, 214, 0.35);
  margin-bottom: 8px;
}

.category-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  border: none;
  background: transparent;
  border-radius: 8px;
  padding: 10px 12px;
  cursor: pointer;
  color: #414753;
  text-align: left;
}

.category-item:hover {
  background: rgba(0, 89, 181, 0.06);
}

.category-item.active {
  background: rgba(0, 89, 181, 0.1);
  color: #0059b5;
  font-weight: 600;
}

.cat-count {
  font-size: 12px;
  color: #717785;
  background: #f0f2f5;
  border-radius: 999px;
  padding: 2px 8px;
}

.toolbar {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border: 1px solid rgba(193, 198, 214, 0.55);
  border-radius: 10px;
  padding: 10px 14px;
}

.search-icon {
  color: #717785;
  font-size: 20px;
}

.search-bar input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
}

.type-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.type-chip {
  border: 1px solid rgba(193, 198, 214, 0.7);
  background: #fff;
  color: #414753;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 13px;
  cursor: pointer;
}

.type-chip.active {
  border-color: #0059b5;
  background: rgba(0, 89, 181, 0.08);
  color: #0059b5;
  font-weight: 600;
}

.tutorial-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.tutorial-card {
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid rgba(193, 198, 214, 0.45);
  border-radius: 12px;
  overflow: hidden;
  text-decoration: none;
  color: inherit;
  transition: box-shadow 0.2s, transform 0.2s;
  text-align: left;
  font: inherit;
  padding: 0;
  cursor: pointer;
}

.tutorial-card--video {
  appearance: none;
  width: 100%;
}

.tutorial-card:hover {
  box-shadow: 0 8px 24px rgba(26, 29, 38, 0.08);
  transform: translateY(-2px);
}

.card-cover {
  position: relative;
  aspect-ratio: 16 / 9;
  background: #eef1f6;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0059b5;
  background: linear-gradient(135deg, #e8f1fb, #f4f6f9);
}

.cover-placeholder .material-symbols-outlined {
  font-size: 48px;
}

.type-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(26, 29, 38, 0.78);
  color: #fff;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 6px;
  z-index: 1;
}

.play-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.22);
  color: #fff;
  z-index: 1;
}

.play-overlay .material-symbols-outlined {
  font-size: 56px;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.35));
}

.tutorial-card--video:hover .play-overlay {
  background: rgba(0, 0, 0, 0.32);
}

.card-body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card-body h3 {
  margin: 0;
  font-size: 16px;
  line-height: 1.4;
}

.card-body p {
  margin: 0;
  font-size: 13px;
  color: #717785;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  color: #9aa1ad;
  margin-top: 4px;
}

.loading-state,
.empty-state {
  background: #fff;
  border-radius: 12px;
  padding: 48px 24px;
  text-align: center;
  color: #717785;
  border: 1px solid rgba(193, 198, 214, 0.4);
}

.reset-btn {
  margin-top: 12px;
  border: 1px solid #0059b5;
  background: transparent;
  color: #0059b5;
  border-radius: 8px;
  padding: 8px 16px;
  cursor: pointer;
}

@media (max-width: 900px) {
  .tutorials-layout {
    grid-template-columns: 1fr;
  }

  .tutorials-sidebar {
    position: static;
  }

  .tutorial-grid {
    grid-template-columns: 1fr;
  }
}

.dialog-embed-wrap {
  position: relative;
  width: 100%;
  padding-top: 56.25%;
  background: #000;
  overflow: hidden;
}

.dialog-video-embed {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  border: 0;
}

.dialog-video-file {
  display: block;
  width: 100%;
  max-height: 75vh;
  background: #000;
}

.dialog-video-error {
  margin: 0;
  padding: 24px 16px;
  color: #fff;
  font-size: 14px;
  text-align: center;
  background: #111;
}
</style>

<style>
/* append-to-body 后需全局样式去掉边框/标题区 */
.tutorial-video-dialog.el-dialog {
  background: transparent !important;
  box-shadow: none !important;
  border: none !important;
  border-radius: 0 !important;
  overflow: visible;
}

.tutorial-video-dialog .el-dialog__header {
  display: none !important;
  padding: 0 !important;
  margin: 0 !important;
  border: none !important;
}

.tutorial-video-dialog .el-dialog__body {
  padding: 0 !important;
  background: transparent !important;
  color: inherit;
}

.tutorial-video-dialog .el-dialog__headerbtn {
  display: none !important;
}

.tutorial-video-modal {
  background: rgba(0, 0, 0, 0.72) !important;
}
</style>
