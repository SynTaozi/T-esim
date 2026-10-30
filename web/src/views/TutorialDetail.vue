<template>
  <div class="tutorial-detail-page">
    <SiteHeader />

    <div class="container" v-loading="loading">
      <nav class="breadcrumb" v-if="tutorial">
        <router-link to="/tutorials">{{ $t('tutorials.title') }}</router-link>
        <span class="sep">/</span>
        <span v-if="tutorial.category">{{ getCategoryName(tutorial.category) }}</span>
        <span class="sep" v-if="tutorial.category">/</span>
        <span class="current">{{ getTitle(tutorial) }}</span>
      </nav>

      <template v-if="tutorial">
        <header class="detail-header">
          <span class="type-badge">{{ typeLabel(tutorial.contentType) }}</span>
          <h1>{{ getTitle(tutorial) }}</h1>
          <p v-if="getSummary(tutorial)" class="summary">{{ getSummary(tutorial) }}</p>
          <div class="meta">
            <span v-if="tutorial.category">{{ getCategoryName(tutorial.category) }}</span>
            <span v-if="tutorial.updatedAt">{{ formatDate(tutorial.updatedAt) }}</span>
          </div>
        </header>

        <section v-if="tutorial.contentType === 'VIDEO' && tutorial.mediaUrl" class="media-block">
          <div v-if="videoEmbed && videoEmbed.kind !== 'file'" class="embed-wrap">
            <iframe
              class="video-embed"
              :src="videoEmbed.src"
              title="Video"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
              allowfullscreen
              referrerpolicy="no-referrer-when-downgrade"
              frameborder="0"
            />
          </div>
          <video
            v-else-if="videoEmbed"
            class="video-player"
            controls
            :src="videoEmbed.src"
            playsinline
          />
          <p v-else class="media-error">{{ $t('tutorials.videoUrlUnsupported') }}</p>
        </section>

        <section v-else-if="tutorial.contentType === 'PDF' && tutorial.mediaUrl" class="media-block">
          <div class="pdf-actions">
            <a class="open-btn" :href="tutorial.mediaUrl" target="_blank" rel="noopener noreferrer">
              {{ $t('tutorials.openPdf') }}
            </a>
          </div>
          <iframe class="pdf-frame" :src="tutorial.mediaUrl" title="PDF" />
        </section>

        <article
          v-if="getContent(tutorial)"
          class="content-body"
          v-html="getContent(tutorial)"
        />

        <div class="back-row">
          <router-link to="/tutorials" class="back-link">{{ $t('tutorials.backToList') }}</router-link>
        </div>
      </template>

      <div v-else-if="!loading" class="empty-state">
        <p>{{ $t('tutorials.notFound') }}</p>
        <router-link to="/tutorials" class="back-link">{{ $t('tutorials.backToList') }}</router-link>
      </div>
    </div>

    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import { tutorialApi, type Tutorial, type TutorialCategory } from '@/api/tutorials'
import { resolveVideoEmbed } from '@/utils/videoEmbed'

const route = useRoute()
const { t, locale } = useI18n({ useScope: 'global' })

const loading = ref(true)
const tutorial = ref<Tutorial | null>(null)

const videoEmbed = computed(() => {
  if (!tutorial.value?.mediaUrl) return null
  return resolveVideoEmbed(tutorial.value.mediaUrl)
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

function getContent(item: Tutorial) {
  return locale.value === 'en' && item.contentEn ? item.contentEn : item.content
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

async function loadTutorial(slug: string) {
  loading.value = true
  tutorial.value = null
  try {
    const res: any = await tutorialApi.getBySlug(slug)
    if (res.code === 200) {
      tutorial.value = res.data
    }
  } catch {
    tutorial.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTutorial(String(route.params.slug || ''))
})

watch(
  () => route.params.slug,
  (slug) => {
    if (slug) loadTutorial(String(slug))
  }
)
</script>

<style scoped>
.tutorial-detail-page {
  min-height: 100vh;
  background: #f6f7f9;
  color: #1a1d26;
}

.container {
  max-width: 880px;
  margin: 0 auto;
  padding: 28px 20px 64px;
}

.breadcrumb {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #717785;
  margin-bottom: 20px;
}

.breadcrumb a {
  color: #0059b5;
  text-decoration: none;
}

.sep {
  color: #c1c6d6;
}

.current {
  color: #414753;
}

.detail-header {
  margin-bottom: 24px;
}

.type-badge {
  display: inline-block;
  background: rgba(0, 89, 181, 0.1);
  color: #0059b5;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 999px;
  margin-bottom: 10px;
}

.detail-header h1 {
  margin: 0 0 10px;
  font-size: 30px;
  line-height: 1.3;
}

.summary {
  margin: 0 0 12px;
  color: #717785;
  font-size: 15px;
  line-height: 1.6;
}

.meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #9aa1ad;
}

.media-block {
  margin-bottom: 24px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid rgba(193, 198, 214, 0.45);
  overflow: hidden;
}

.video-player {
  display: block;
  width: 100%;
  max-height: 70vh;
  background: #000;
}

.embed-wrap {
  position: relative;
  width: 100%;
  padding-top: 56.25%;
  background: #000;
}

.video-embed {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  border: 0;
}

.media-error {
  margin: 0;
  padding: 20px 16px;
  color: #b42318;
  font-size: 14px;
}

.pdf-actions {
  display: none;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(193, 198, 214, 0.35);
}

.open-btn {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 8px;
  background: #0059b5;
  color: #fff;
  text-decoration: none;
  font-size: 14px;
}

.pdf-frame {
  width: 100%;
  min-height: 72vh;
  border: none;
  background: #fff;
}

.content-body {
  background: #fff;
  border: 1px solid rgba(193, 198, 214, 0.45);
  border-radius: 12px;
  padding: 24px 28px;
  line-height: 1.75;
  font-size: 15px;
}

.content-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
}

.content-body :deep(p) {
  margin: 0 0 1em;
}

.content-body :deep(h2),
.content-body :deep(h3) {
  margin: 1.4em 0 0.6em;
}

.back-row {
  margin-top: 28px;
}

.back-link {
  color: #0059b5;
  text-decoration: none;
  font-weight: 600;
}

.empty-state {
  background: #fff;
  border-radius: 12px;
  padding: 48px 24px;
  text-align: center;
  color: #717785;
}

@media (max-width: 768px) {
  .detail-header h1 {
    font-size: 24px;
  }

  .pdf-actions {
    display: block;
  }

  .pdf-frame {
    display: none;
  }
}
</style>
