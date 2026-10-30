import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNavigationStore = defineStore('navigation', () => {
  const isRouteLoading = ref(false)
  let routeLoadingTimer: ReturnType<typeof setTimeout> | null = null

  function startRouteLoading() {
    if (routeLoadingTimer) {
      clearTimeout(routeLoadingTimer)
      routeLoadingTimer = null
    }
    isRouteLoading.value = true
  }

  function finishRouteLoading() {
    routeLoadingTimer = setTimeout(() => {
      isRouteLoading.value = false
      routeLoadingTimer = null
    }, 80)
  }

  return {
    isRouteLoading,
    startRouteLoading,
    finishRouteLoading
  }
})
