# T-esim

全球 eSIM / SIM 服务平台（基于 mocbase-sim 能力重构）。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3.2 · MyBatis-Plus · Redis · MySQL |
| 用户端 | Vue 3 · Vite · TypeScript · Element Plus · Pinia · vue-i18n |
| 管理端 | Vue 3 · Vite · TypeScript · Element Plus · Pinia |

## 目录结构

```
T-esim/
├── server/                  # 后端 API 服务
│   ├── sql/                 # 建表 / 迁移脚本（待补充）
│   └── src/main/
│       ├── java/com/tesim/
│       │   ├── common/      # 统一响应、异常、常量
│       │   ├── config/      # MyBatis-Plus / Redis / 跨域等
│       │   ├── controller/  # 用户端接口
│       │   │   └── admin/   # 管理端接口
│       │   ├── dto/         # 入参 / 出参对象
│       │   ├── entity/      # 数据库实体
│       │   ├── mapper/      # MyBatis-Plus Mapper
│       │   ├── service/     # 业务接口
│       │   │   └── impl/    # 业务实现
│       │   └── util/        # 工具类
│       └── resources/
│           ├── mapper/      # Mapper XML
│           └── db/
├── web/                     # 用户端（C 端）
├── admin/                   # 管理端（B 端）
└── docs/                    # 设计 / 接口文档
```

### 命名说明

| 目录 | 含义 |
|---|---|
| `server` | 后端服务，对应常见开源项目中的 `*-server` / `api` |
| `web` | 面向用户的站点，对应 storefront / portal |
| `admin` | 运营后台控制台 |
| `com.tesim.*` | 后端包名；`util` 用单数（Java 惯例），`mapper`/`entity`/`service` 对齐 MyBatis-Plus 常见分层 |

## 当前进度

- [x] 仓库与目录骨架
- [x] 后端 Maven 工程（Spring Boot + MyBatis-Plus + Redis）
- [x] 前端双端 Vue3 工程脚手架
- [x] 数据库设计与建表脚本（`server/sql/01_schema.sql`，说明见 `docs/database-design.md`）
- [ ] 业务模块迁移 / 精简实现

## 本地启动（脚手架阶段）

```bash
# 后端
cd server
mvn spring-boot:run

# 用户端
cd web
npm install
npm run dev

# 管理端
cd admin
npm install
npm run dev
```

> 说明：当前仅为空项目初始化，无业务接口与页面；数据库稍后单独设计创建。
