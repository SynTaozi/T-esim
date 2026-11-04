# 邮件模板变量说明

渲染引擎支持：

- `{{varName}}`（推荐）
- `${VAR_NAME}`（注册验证码模板用的写法）
- `{{#if varName}}...{{/if}}`（变量非空时显示）

同义名会自动兼容（如 `trackingNumber` ↔ `trackingNo`，`validityMinutes` ↔ `expireMinutes`）。

## AUTH_REGISTER_CODE / AUTH_RESET_CODE

| 变量 | 说明 |
|------|------|
| `code` / `${CODE}` | 6 位验证码 |
| `email` / `${EMAIL}` | 收件邮箱 |
| `validityMinutes` / `${VALIDITY_MINUTES}` | 有效分钟数（当前 10） |
| `expireMinutes` | 同上（别名） |

## ORDER_SHIPPING

| 变量 | 说明 |
|------|------|
| `orderNo` | 订单号 |
| `packageName` | 套餐/商品名 |
| `shippingCarrier` | 物流公司（可用 `#if`） |
| `trackingNumber` | 物流单号（可用 `#if`） |
| `phone` | 收件电话（可用 `#if`） |
| `qrCodeUrl` | 仅当激活信息是 http(s) URL 时填充（可用 `#if`） |
| `remark` | 备注；非 URL 激活码会自动追加到此 |
| `shippingTime` | 发货时间 `yyyy-MM-dd HH:mm` |

## ORDER_CUSTOMER_NOTICE

| 变量 | 说明 |
|------|------|
| `subject` | 管理员填写的主题 |
| `orderNo` | 订单号 |
| `noticeContent` | 通知正文（换行已转 `<br/>`） |

## TICKET_REPLY

| 变量 | 说明 |
|------|------|
| `ticketNo` | 工单号 |
| `subject` | 工单标题 |
| `replyContent` | 客服回复正文 |
| `repliedBy` | 回复人 |
| `ticketUrl` | 前台工单链接 |
