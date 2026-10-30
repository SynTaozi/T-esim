import { defineStore } from 'pinia'
import { ref } from 'vue'

/** @deprecated 购物车已下线；保留空 store 兼容过渡代码 */
export const useCartStore = defineStore('cart', () => {
  const items = ref<any[]>([])
  async function fetchCart() {
    items.value = []
  }
  async function updateItem(_id: any, _quantity: number) {}
  async function removeItem(_id: any) {}
  async function clear() {
    items.value = []
  }
  return { items, fetchCart, updateItem, removeItem, clear }
})
