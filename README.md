# Systelm — 货物管理与销售一体化系统

面向公司内部使用的 ERP 系统，集成库存管理与销售功能，支持多仓库运营。员工通过浏览器访问，适用于 6～20 人内网场景。

## 功能模块

| 模块 | 说明 |
| --- | --- |
| 商品管理 | 商品档案、分类、批次（批次号、生产日期） |
| 库存操作 | 入库、出库、盘点、库存查询 |
| 仓库调拨 | 跨仓库调拨，按仓库权限隔离 |
| 销售 | 客户档案、销售开单、历史订单 |
| 报表 | 销售汇总、库存汇总、低库存预警 |
| 系统管理 | 用户、角色、仓库配置（超级管理员） |

## 技术栈

- **后端：** Spring Boot 3.3、Java 17、Spring Security + JWT、JPA、Flyway、SQLite
- **前端：** Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router

## 项目结构

```
systelm/
├── backend/          # Spring Boot 后端（REST API + 静态资源）
├── frontend/         # Vue 3 前端
├── data/             # SQLite 数据库（运行时生成）
└── docs/             # 设计文档与实施计划
```

## 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+（前端开发/构建）

## 快速开始

### 开发模式

前后端分离运行，前端通过 Vite 代理访问后端 API。

```bash
# 1. 启动后端（在项目根目录）
cd backend
mvn spring-boot:run

# 2. 启动前端（新终端）
cd frontend
npm install
npm run dev
```

- 前端：http://localhost:5173
- 后端 API：http://localhost:8080

首次启动时 Flyway 会自动创建数据库并初始化 schema；默认管理员账号见下方。

### 生产部署

前端打包后复制到后端静态资源目录，再构建 JAR 单包部署。

```bash
cd frontend
npm install
npm run build

# Windows PowerShell
Copy-Item -Recurse -Force dist/* ../backend/src/main/resources/static/

cd ../backend
mvn package -DskipTests
java -jar target/backend-0.1.0.jar
```

访问 http://localhost:8080 即可使用完整应用。

## 默认账号

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `admin123` | 超级管理员 |

> 生产环境请立即修改默认密码，并在 `application.yml` 中更换 JWT secret（`systelm.jwt.secret`，至少 32 字符）。

## 角色与权限

| 角色 | 权限 |
| --- | --- |
| **admin** | 全部功能，含用户/仓库配置 |
| **warehouse_sales** | 入库、出库、盘点、调拨、销售开单；仅限被分配的仓库 |
| **finance** | 只读报表与库存查询，不可修改业务数据 |

## 配置

主要配置位于 `backend/src/main/resources/application.yml`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `server.port` | `8080` | 服务端口 |
| `spring.datasource.url` | `jdbc:sqlite:../data/systelm.db` | SQLite 数据库路径 |
| `systelm.jwt.expiration-ms` | `86400000` | JWT 有效期（毫秒，默认 24 小时） |

## 运行测试

```bash
cd backend
mvn test
```

## 文档

- [设计规格](docs/superpowers/specs/2026-06-17-inventory-sales-erp-design.md)
- [实施计划](docs/superpowers/plans/2026-06-17-inventory-sales-erp.md)

## License

Internal use.
