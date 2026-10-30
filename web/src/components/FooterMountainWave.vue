<template>
  <div class="site-footer-wave-wrap" aria-hidden="true">
    <svg
      class="site-footer-wave-svg"
      xmlns="http://www.w3.org/2000/svg"
      :viewBox="`0 0 ${VIEW_W} ${VIEW_H}`"
      preserveAspectRatio="none"
    >
      <!-- 曲线上方白底，下方页脚色，边界即起伏线 -->
      <path :d="whiteCapPath" fill="#ffffff" />
      <path :d="footerFillPath" fill="rgb(248, 248, 251)" />
    </svg>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, shallowRef } from 'vue'

const VIEW_W = 1200
const VIEW_H = 80
const WAVE_Y = 36
const AMP = 22

function sampleWaveY(x: number, phase: number): number {
  const t = (x / VIEW_W) * Math.PI * 2
  return WAVE_Y + Math.sin(t + phase) * AMP + Math.sin(t * 2 + phase * 0.9) * (AMP * 0.18)
}

function buildCurvePoints(phase: number): Array<{ x: number; y: number }> {
  const steps = 12
  const pts: Array<{ x: number; y: number }> = []
  for (let i = 0; i <= steps; i++) {
    pts.push({ x: (i / steps) * VIEW_W, y: sampleWaveY((i / steps) * VIEW_W, phase) })
  }
  return pts
}

function curveToPath(pts: Array<{ x: number; y: number }>): string {
  let d = `M ${pts[0].x} ${pts[0].y}`
  for (let i = 0; i < pts.length - 1; i++) {
    const p0 = pts[Math.max(i - 1, 0)]
    const p1 = pts[i]
    const p2 = pts[i + 1]
    const p3 = pts[Math.min(i + 2, pts.length - 1)]
    const cp1x = p1.x + (p2.x - p0.x) / 6
    const cp1y = p1.y + (p2.y - p0.y) / 6
    const cp2x = p2.x - (p3.x - p1.x) / 6
    const cp2y = p2.y - (p3.y - p1.y) / 6
    d += ` C ${cp1x.toFixed(1)} ${cp1y.toFixed(1)} ${cp2x.toFixed(1)} ${cp2y.toFixed(1)} ${p2.x} ${p2.y}`
  }
  return d
}

function buildPaths(phase: number): { white: string; footer: string } {
  const pts = buildCurvePoints(phase)
  const curve = curveToPath(pts)
  const footer = `${curve} L ${VIEW_W} ${VIEW_H} L 0 ${VIEW_H} Z`

  let white = `M 0 0 L ${VIEW_W} 0`
  for (let i = pts.length - 1; i >= 0; i--) {
    white += ` L ${pts[i].x} ${pts[i].y}`
  }
  white += ' Z'

  return { white, footer }
}

const whiteCapPath = shallowRef(buildPaths(0).white)
const footerFillPath = shallowRef(buildPaths(0).footer)

let rafId = 0
let start = 0

function tick(now: number) {
  if (!start) start = now
  const { white, footer } = buildPaths(((now - start) / 1000) * 0.7)
  whiteCapPath.value = white
  footerFillPath.value = footer
  rafId = requestAnimationFrame(tick)
}

onMounted(() => {
  const initial = buildPaths(0)
  whiteCapPath.value = initial.white
  footerFillPath.value = initial.footer
  if (!window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    rafId = requestAnimationFrame(tick)
  }
})

onUnmounted(() => cancelAnimationFrame(rafId))
</script>
