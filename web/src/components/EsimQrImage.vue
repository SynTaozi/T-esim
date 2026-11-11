<template>
  <div class="esim-qr-wrap">
    <img v-if="displaySrc" :src="displaySrc" :alt="alt" :class="imgClass" :style="imgStyle" />
    <p v-else-if="value && !loading" class="esim-qr-fallback">{{ value }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import QRCode from 'qrcode'

const props = withDefaults(defineProps<{
  value?: string
  alt?: string
  size?: number
  imgClass?: string
}>(), {
  alt: 'ESIM QR Code',
  size: 240
})

const displaySrc = ref('')
const loading = ref(false)

/** 用 max 边限制尺寸，保留原始宽高比，避免远端非正方形二维码被拉扁 */
const imgStyle = {
  maxWidth: `${props.size}px`,
  maxHeight: `${props.size}px`,
  width: 'auto',
  height: 'auto',
  objectFit: 'contain' as const,
  display: 'block'
}

function isDirectImageSource(v: string) {
  if (/^https?:\/\//i.test(v) || v.startsWith('data:image/')) return true
  // 站内上传路径，如 /api/uploads/shipping/xxx.png
  if (v.startsWith('/api/uploads/') || v.startsWith('/uploads/')) return true
  return /\.(png|jpe?g|gif|webp|svg|bmp)(\?.*)?$/i.test(v)
}

async function render(value?: string) {
  displaySrc.value = ''
  if (!value?.trim()) return
  const raw = value.trim()
  if (isDirectImageSource(raw)) {
    displaySrc.value = raw
    return
  }
  loading.value = true
  try {
    displaySrc.value = await QRCode.toDataURL(raw, { width: props.size, margin: 2 })
  } catch {
    displaySrc.value = ''
  } finally {
    loading.value = false
  }
}

watch(() => props.value, (v) => { render(v) }, { immediate: true })
</script>

<style scoped>
.esim-qr-wrap {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
}

.esim-qr-fallback {
  margin: 0;
  font-size: 12px;
  color: #666;
  word-break: break-all;
  max-width: 280px;
}
</style>
