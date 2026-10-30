# T-esim 数据库设计说明

> 原则：**功能对齐 mocbase-sim，结构按成熟电商方案重做**；多语言**仅中英**。

## 1. 多语言方案（已定稿）

**只支持 `zh` / `en`**，采用同表双语列：

| 约定 | 示例 |
|------|------|
| 字段后缀 | `name_zh` / `name_en`、`title_zh` / `title_en` |
| 前端切换 | `locale=zh` → 取 `*_zh`；`locale=en` → 取 `*_en` |
| UI 文案 | 按钮/菜单等仍用前端 `vue-i18n`，不进库 |
| 不加语言 | 不做 `*_i18n` 翻译表（两语言场景下国内电商常用、查询更简单） |

订单行 `title_zh` / `title_en` 为**下单快照**，与商品现名无关。

## 2. 设计目标

| 目标 | 做法 |
|------|------|
| 查询效率 | 覆盖国家关系表、SKU 可售单元、订单组合索引 |
| 规范分层 | 会员 / 商品 / 订单 / 支付 / 履约 / 营销 / 客服 / 内容 / 系统 |
| 商品订单 | SPU + SKU；订单头与支付/物流拆表 |

## 3. 商品与订单

```
product_category (name_zh/name_en)
       │
    product (文案双列 + 业务属性)
       │
  product_sku (月数档 / 售价)
       │
  product_coverage (country_code 可索引)
       │
  cart_item / sales_order_item
```

订单：

```
sales_order
  ├─ sales_order_item
  ├─ sales_order_address
  ├─ sales_order_payment
  └─ sales_order_shipment
```

## 4. 金额

- 货币：`USD`（`currency` 预留）
- 金额：`DECIMAL(12,2)`

## 5. 功能覆盖

| 能力 | 表 |
|------|-----|
| 用户/余额/邀请 | `member` |
| 地址 | `member_address` |
| 分类/商品/SKU/覆盖国 | `product_*` |
| 购物车/订单 | `cart_item` / `sales_order*` |
| 支付/充值 | `payment_channel` / `wallet_recharge` |
| 已购资产/上游 | `member_subscription` / `supplier_*` / `fulfillment_task` |
| 券/佣金 | `coupon*` / `commission_*` |
| 工单 | `ticket` / `ticket_message` |
| FAQ/教程等 | `cms_*` |
| 邮件/钉钉 | `sys_*` |

## 6. 文件

- `server/sql/01_schema.sql`
- 本文档
