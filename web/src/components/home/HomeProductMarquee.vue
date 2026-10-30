<template>
  <section class="home-product-marquee" aria-label="Popular products">
    <div
      ref="viewportRef"
      class="marquee-viewport"
      @mouseenter="paused = true"
      @mouseleave="paused = false"
    >
      <div ref="trackRef" class="marquee-track">
        <div
          v-for="seg in 2"
          :key="`seg-${seg}`"
          class="marquee-group"
          :ref="seg === 1 ? setGroupRef : undefined"
          :aria-hidden="seg === 2 ? 'true' : undefined"
        >
          <router-link
            v-for="(product, idx) in segmentItems"
            :key="`${seg}-${idx}-${product.id}`"
            :to="`/products/${product.id}`"
            class="marquee-card"
            :tabindex="seg === 2 ? -1 : undefined"
          >
            <div class="marquee-card-img-wrap">
              <img
                v-if="productImage(product)"
                :src="productImage(product)"
                :alt="productName(product)"
                loading="eager"
                draggable="false"
                @load="onImageLoad"
              />
              <span v-else class="marquee-card-fallback">{{ productName(product).charAt(0) }}</span>
            </div>
            <div class="marquee-card-body">
              <p class="marquee-card-name">{{ productName(product) }}</p>
              <p class="marquee-card-price">
                <span class="price-from">{{ $t('home.productMarquee.from') }}</span>
                <span class="price-value">{{ formatPrice(product) }}</span>
              </p>
            </div>
          </router-link>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { Product } from '@/api/product'

const props = defineProps<{
  products: Product[]
}>()

const { locale } = useI18n({ useScope: 'global' })

const viewportRef = ref<HTMLElement | null>(null)
const trackRef = ref<HTMLElement | null>(null)
const groupRef = ref<HTMLElement | null>(null)

/** 单段内产品重复次数，保证段宽 >= 视口，避免露白 */
const repeatTimes = ref(1)
const paused = ref(false)

let rafId = 0
let offset = 0
let setWidth = 0
let running = false
const speed = 0.55

function setGroupRef(el: Element | null) {
  // Vue: v-for="seg in 2" 时 seg 为 1、2
  groupRef.value = (el as HTMLElement) || null
}

const segmentItems = computed(() => {
  const list = props.products
  if (!list.length) return []
  const out: Product[] = []
  for (let r = 0; r < repeatTimes.value; r++) {
    out.push(...list)
  }
  return out
})

function productName(p: Product) {
  return locale.value === 'zh' ? p.name : (p.nameEn || p.name)
}

function productImage(p: Product) {
  return p.displayImageUrl || p.imageUrl || ''
}

function formatPrice(p: Product) {
  return p.price != null ? `$${p.price}` : '—'
}

function getFirstGroup(): HTMLElement | null {
  return groupRef.value || trackRef.value?.querySelector('.marquee-group') || null
}

function measure() {
  const group = getFirstGroup()
  if (!group) return 0
  // padding-right 已计入 offsetWidth，段宽即循环步长
  return group.offsetWidth
}

async function ensureFilled() {
  const viewport = viewportRef.value
  if (!viewport || !props.products.length) return

  // 先重置，再按视口补足：单段宽度至少盖住视口，循环时不会露白
  repeatTimes.value = 1
  await nextTick()

  let guard = 0
  while (guard < 12) {
    const w = measure()
    const need = Math.max(viewport.clientWidth * 1.25, 800)
    if (w > 0 && w >= need) break
    repeatTimes.value += 1
    await nextTick()
    guard++
  }

  setWidth = measure()
  if (setWidth > 0) {
    // 规范化到 (-setWidth, 0]
    offset = offset % setWidth
    if (offset > 0) offset -= setWidth
  }
  applyTransform()
}

function applyTransform() {
  const track = trackRef.value
  if (track) {
    track.style.transform = `translate3d(${offset}px, 0, 0)`
  }
}

function tick() {
  if (running) {
    if (!paused.value && setWidth > 0 && trackRef.value) {
      offset -= speed
      if (offset <= -setWidth) {
        offset += setWidth
      }
      applyTransform()
    }
    rafId = requestAnimationFrame(tick)
  }
}

function start() {
  if (running) return
  running = true
  rafId = requestAnimationFrame(tick)
}

function stop() {
  running = false
  cancelAnimationFrame(rafId)
}

let loadTimer = 0
function onImageLoad() {
  window.clearTimeout(loadTimer)
  loadTimer = window.setTimeout(() => {
    setWidth = measure()
  }, 50)
}

let resizeObserver: ResizeObserver | null = null

onMounted(async () => {
  await ensureFilled()
  start()

  if (typeof ResizeObserver !== 'undefined' && viewportRef.value) {
    resizeObserver = new ResizeObserver(() => {
      void ensureFilled()
    })
    resizeObserver.observe(viewportRef.value)
  }
})

watch(
  () => props.products,
  async () => {
    offset = 0
    await ensureFilled()
  },
  { deep: true }
)

onBeforeUnmount(() => {
  stop()
  resizeObserver?.disconnect()
  window.clearTimeout(loadTimer)
})
</script>

<style scoped>
.home-product-marquee {
  padding: 0 0 48px;
  overflow: hidden;
}

.marquee-viewport {
  width: 100%;
  overflow: hidden;
  mask-image: linear-gradient(90deg, transparent, #000 8%, #000 92%, transparent);
}

.marquee-track {
  display: flex;
  width: max-content;
  will-change: transform;
}

.marquee-group {
  display: flex;
  gap: 20px;
  flex: 0 0 auto;
  /* 段尾间距计入宽度，两段拼接无空缝 */
  padding-right: 20px;
  box-sizing: content-box;
}

.marquee-card {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 20px 12px 12px;
  background: var(--surface-container-lowest, #fff);
  border: 1px solid color-mix(in srgb, #1d1d1f 8%, transparent);
  border-radius: 999px;
  text-decoration: none;
  color: inherit;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
}

.marquee-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 89, 181, 0.12);
}

.marquee-card-img-wrap {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, #e8f0ff, #d6e4ff);
}

.marquee-card-img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.marquee-card-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-weight: 700;
  color: #0059b5;
  font-size: 18px;
}

.marquee-card-body {
  min-width: 0;
}

.marquee-card-name {
  margin: 0 0 2px;
  font-size: 14px;
  font-weight: 600;
  color: var(--on-background, #1d1d1f);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 160px;
}

.marquee-card-price {
  margin: 0;
  font-size: 13px;
  color: var(--on-surface-variant, #6e6e73);
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-value {
  font-weight: 700;
  color: #0059b5;
}
</style>
