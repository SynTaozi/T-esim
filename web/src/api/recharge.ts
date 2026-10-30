/** @deprecated 钱包充值已下线，保留空实现避免旧调用崩溃 */

export async function getBalance() {
  return { balance: 0, data: { balance: 0 } }
}

export async function getRecharges(_params?: any) {
  return { recharges: [], total: 0, data: { recharges: [], total: 0 } }
}

export async function createRecharge(_payload?: any) {
  throw new Error('Wallet recharge is not available')
}

export async function getRechargeByNo(_no: string) {
  return { recharge: null, data: null }
}
