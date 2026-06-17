# 货物管理与销售 ERP — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 交付一套内网多仓库 ERP（商品/批次库存/入出库/盘点/调拨/销售/报表/权限），分三阶段可验收。

**Architecture:** 单体 Spring Boot REST API + Vue3 SPA；SQLite 单文件库；所有库存变动经单据服务更新 `stock` 快照；JWT + 角色 + `user_warehouse` 做仓库隔离。

**Tech Stack:** Java 17, Spring Boot 3.3, Spring Data JPA, Spring Security, JWT, Flyway, SQLite; Vue 3, Vite, Element Plus, Pinia, Vue Router, Axios.

**Spec:** `docs/superpowers/specs/2026-06-17-inventory-sales-erp-design.md`

**UI Note:** 前端壳与主题任务必须调用 `frontend-design` 技能后再写代码。

---

## 文件结构（全项目）

```
systelm/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/systelm/
│       │   ├── SystelmApplication.java
│       │   ├── config/          # Security, CORS, JPA, SQLite
│       │   ├── entity/          # JPA 实体
│       │   ├── repository/      # Spring Data 接口
│       │   ├── dto/             # 请求/响应 DTO
│       │   ├── service/         # 业务逻辑
│       │   ├── controller/      # REST 控制器
│       │   ├── security/        # JWT filter, UserDetails
│       │   └── exception/       # 全局异常
│       ├── main/resources/
│       │   ├── application.yml
│       │   └── db/migration/    # Flyway SQL
│       └── test/java/com/systelm/
│           └── service/         # 业务单测
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── main.ts
│       ├── App.vue
│       ├── router/index.ts
│       ├── stores/auth.ts
│       ├── api/                 # axios 封装与各模块 API
│       ├── layouts/MainLayout.vue
│       ├── styles/theme.css     # frontend-design 产出
│       └── views/               # 各业务页面
├── data/                        # SQLite 文件（gitignore）
└── docs/superpowers/
```

---

# 阶段 0：项目脚手架

### Task 0.1: 初始化 Git 与目录

**Files:**
- Create: `.gitignore`
- Create: `data/.gitkeep`

- [ ] **Step 1: 创建 .gitignore**

```gitignore
# Java
backend/target/
backend/.idea/
*.iml

# Node
frontend/node_modules/
frontend/dist/

# DB
data/*.db
data/*.db-*

# OS
.DS_Store
Thumbs.db
```

- [ ] **Step 2: 初始化仓库**

Run:
```bash
cd C:\Users\dcy10\Desktop\trae\6.17\cursor\systelm
git init
git add .gitignore data/.gitkeep docs/
git commit -m "chore: init repo with docs"
```

Expected: 仓库创建成功

---

### Task 0.2: Spring Boot 后端脚手架

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/systelm/SystelmApplication.java`
- Create: `backend/src/main/resources/application.yml`

- [ ] **Step 1: 写 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.5</version>
    </parent>
    <groupId>com.systelm</groupId>
    <artifactId>backend</artifactId>
    <version>0.1.0</version>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.46.1.0</version>
        </dependency>
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-community-dialects</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.6</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 写启动类**

```java
package com.systelm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SystelmApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystelmApplication.class, args);
    }
}
```

- [ ] **Step 3: 写 application.yml**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:sqlite:../data/systelm.db
    driver-class-name: org.sqlite.JDBC
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.community.dialect.SQLiteDialect
  flyway:
    enabled: true
    locations: classpath:db/migration

systelm:
  jwt:
    secret: change-me-in-production-use-32-chars-min
    expiration-ms: 86400000
```

- [ ] **Step 4: 验证编译**

Run:
```bash
cd backend && mvn -q compile
```
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add backend/
git commit -m "feat: scaffold Spring Boot backend"
```

---

### Task 0.3: Vue3 前端脚手架

**Files:**
- Create: `frontend/` via Vite

- [ ] **Step 1: 创建 Vue 项目**

Run:
```bash
cd C:\Users\dcy10\Desktop\trae\6.17\cursor\systelm
npm create vite@latest frontend -- --template vue-ts
cd frontend
npm install
npm install element-plus @element-plus/icons-vue vue-router pinia axios
```

- [ ] **Step 2: 配置 vite 代理**

Modify `frontend/vite.config.ts`:

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

- [ ] **Step 3: 验证启动**

Run:
```bash
cd frontend && npm run build
```
Expected: build 成功

- [ ] **Step 4: Commit**

```bash
git add frontend/
git commit -m "feat: scaffold Vue3 frontend"
```

---

# 阶段 1：登录权限 + 仓库 + 商品 + 入出库盘点

### Task 1.1: Flyway 数据库迁移 V1

**Files:**
- Create: `backend/src/main/resources/db/migration/V1__init_schema.sql`

- [ ] **Step 1: 写迁移脚本**

```sql
CREATE TABLE role (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE warehouse (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    address TEXT,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE user_account (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    display_name TEXT NOT NULL,
    role_id INTEGER NOT NULL REFERENCES role(id),
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE user_warehouse (
    user_id INTEGER NOT NULL REFERENCES user_account(id),
    warehouse_id INTEGER NOT NULL REFERENCES warehouse(id),
    PRIMARY KEY (user_id, warehouse_id)
);

CREATE TABLE product_category (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    parent_id INTEGER REFERENCES product_category(id)
);

CREATE TABLE product (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    sku TEXT NOT NULL UNIQUE,
    category_id INTEGER REFERENCES product_category(id),
    cost_price REAL NOT NULL DEFAULT 0,
    sale_price REAL NOT NULL DEFAULT 0,
    unit TEXT NOT NULL DEFAULT '件',
    min_stock REAL NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE product_batch (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL REFERENCES product(id),
    batch_no TEXT NOT NULL,
    production_date TEXT,
    UNIQUE(product_id, batch_no)
);

CREATE TABLE stock (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    warehouse_id INTEGER NOT NULL REFERENCES warehouse(id),
    product_id INTEGER NOT NULL REFERENCES product(id),
    batch_id INTEGER NOT NULL REFERENCES product_batch(id),
    quantity REAL NOT NULL DEFAULT 0,
    UNIQUE(warehouse_id, product_id, batch_id)
);

CREATE TABLE stock_in (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    doc_no TEXT NOT NULL UNIQUE,
    warehouse_id INTEGER NOT NULL REFERENCES warehouse(id),
    operator_id INTEGER NOT NULL REFERENCES user_account(id),
    remark TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE stock_in_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    stock_in_id INTEGER NOT NULL REFERENCES stock_in(id),
    product_id INTEGER NOT NULL REFERENCES product(id),
    batch_id INTEGER NOT NULL REFERENCES product_batch(id),
    quantity REAL NOT NULL
);

CREATE TABLE stock_out (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    doc_no TEXT NOT NULL UNIQUE,
    warehouse_id INTEGER NOT NULL REFERENCES warehouse(id),
    operator_id INTEGER NOT NULL REFERENCES user_account(id),
    out_type TEXT NOT NULL,
    remark TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE stock_out_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    stock_out_id INTEGER NOT NULL REFERENCES stock_out(id),
    product_id INTEGER NOT NULL REFERENCES product(id),
    batch_id INTEGER NOT NULL REFERENCES product_batch(id),
    quantity REAL NOT NULL
);

CREATE TABLE stock_check (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    doc_no TEXT NOT NULL UNIQUE,
    warehouse_id INTEGER NOT NULL REFERENCES warehouse(id),
    operator_id INTEGER NOT NULL REFERENCES user_account(id),
    remark TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE stock_check_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    stock_check_id INTEGER NOT NULL REFERENCES stock_check(id),
    product_id INTEGER NOT NULL REFERENCES product(id),
    batch_id INTEGER NOT NULL REFERENCES product_batch(id),
    book_qty REAL NOT NULL,
    actual_qty REAL NOT NULL,
    diff_qty REAL NOT NULL
);

INSERT INTO role (name) VALUES ('admin'), ('warehouse_sales'), ('finance');
```

- [ ] **Step 2: 验证迁移**

Run:
```bash
cd backend && mvn -q spring-boot:run
```
Expected: 应用启动，Flyway 执行 V1 成功（Ctrl+C 停止）

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/resources/db/migration/
git commit -m "feat: add initial database schema"
```

---

### Task 1.2: JPA 实体 — Role / User / Warehouse

**Files:**
- Create: `backend/src/main/java/com/systelm/entity/Role.java`
- Create: `backend/src/main/java/com/systelm/entity/UserAccount.java`
- Create: `backend/src/main/java/com/systelm/entity/Warehouse.java`
- Create: `backend/src/main/java/com/systelm/repository/UserAccountRepository.java`
- Create: `backend/src/main/java/com/systelm/repository/WarehouseRepository.java`

- [ ] **Step 1: 写 Role 实体**

```java
package com.systelm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

- [ ] **Step 2: 写 UserAccount 实体**

```java
package com.systelm.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_account")
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_warehouse",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "warehouse_id")
    )
    private Set<Warehouse> warehouses = new HashSet<>();

    // getters/setters for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Set<Warehouse> getWarehouses() { return warehouses; }
    public void setWarehouses(Set<Warehouse> warehouses) { this.warehouses = warehouses; }
}
```

- [ ] **Step 3: 写 Warehouse 实体与 Repository**

`Warehouse.java`:
```java
package com.systelm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "warehouse")
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String address;
    @Column(nullable = false)
    private String status = "ACTIVE";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

`UserAccountRepository.java`:
```java
package com.systelm.repository;

import com.systelm.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
}
```

`WarehouseRepository.java`:
```java
package com.systelm.repository;

import com.systelm.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {}
```

- [ ] **Step 4: 编译验证**

Run: `cd backend && mvn -q compile`
Expected: SUCCESS

- [ ] **Step 5: Commit**

```bash
git commit -am "feat: add user and warehouse entities"
```

---

### Task 1.3: 种子管理员账号

**Files:**
- Create: `backend/src/main/resources/db/migration/V2__seed_admin.sql`
- Create: `backend/src/main/java/com/systelm/config/DataInitializer.java`（仅开发环境可选；生产用迁移种子）

- [ ] **Step 1: 写 V2 迁移（BCrypt hash of "admin123"）**

```sql
INSERT INTO user_account (username, password_hash, display_name, role_id)
SELECT 'admin',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.R9p5K8xqJ7zqJm5K0u',
       '系统管理员',
       id
FROM role WHERE name = 'admin';
```

> 密码明文：`admin123`（首次登录后提示修改）

- [ ] **Step 2: 重启验证 admin 存在**

Run: `cd backend && mvn -q spring-boot:run`，查日志无 Flyway 错误

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/resources/db/migration/V2__seed_admin.sql
git commit -m "feat: seed default admin user"
```

---

### Task 1.4: JWT 登录 API（TDD）

**Files:**
- Create: `backend/src/main/java/com/systelm/security/JwtService.java`
- Create: `backend/src/main/java/com/systelm/security/JwtAuthFilter.java`
- Create: `backend/src/main/java/com/systelm/config/SecurityConfig.java`
- Create: `backend/src/main/java/com/systelm/dto/LoginRequest.java`
- Create: `backend/src/main/java/com/systelm/dto/LoginResponse.java`
- Create: `backend/src/main/java/com/systelm/controller/AuthController.java`
- Test: `backend/src/test/java/com/systelm/controller/AuthControllerTest.java`

- [ ] **Step 1: 写失败测试**

```java
package com.systelm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.systelm.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void login_withValidCredentials_returnsToken() throws Exception {
        LoginRequest req = new LoginRequest("admin", "admin123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.username").value("admin"));
    }
}
```

`LoginRequest.java`:
```java
package com.systelm.dto;

public record LoginRequest(String username, String password) {}
```

`LoginResponse.java`:
```java
package com.systelm.dto;

import java.util.List;

public record LoginResponse(
    String token,
    String username,
    String displayName,
    String role,
    List<Long> warehouseIds
) {}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd backend && mvn -q test -Dtest=AuthControllerTest`
Expected: FAIL（404 或 bean 缺失）

- [ ] **Step 3: 实现 JwtService + SecurityConfig + AuthController**

`JwtService.java` 核心方法:
```java
public String generateToken(String username) { /* HS256, subject=username */ }
public String extractUsername(String token) { /* parse claims */ }
public boolean isTokenValid(String token, UserDetails user) { /* exp + subject */ }
```

`SecurityConfig.java` 要点:
```java
http.csrf(csrf -> csrf.disable())
    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/login").permitAll()
        .anyRequest().authenticated())
    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
```

`AuthController.java`:
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest req) {
        // authenticate via AuthenticationManager
        // return JWT + user info + warehouseIds
    }
}
```

- [ ] **Step 4: 运行测试通过**

Run: `cd backend && mvn -q test -Dtest=AuthControllerTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git commit -am "feat: add JWT login API"
```

---

### Task 1.5: WarehouseAccessService（仓库权限校验）

**Files:**
- Create: `backend/src/main/java/com/systelm/service/WarehouseAccessService.java`
- Test: `backend/src/test/java/com/systelm/service/WarehouseAccessServiceTest.java`

- [ ] **Step 1: 写失败测试**

```java
package com.systelm.service;

import com.systelm.entity.*;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class WarehouseAccessServiceTest {
  private final WarehouseAccessService service = new WarehouseAccessService();

  @Test
  void admin_canAccessAnyWarehouse() {
    UserAccount admin = userWithRole("admin", Set.of());
    assertTrue(service.canAccessWarehouse(admin, 99L));
  }

  @Test
  void warehouseSales_canOnlyAccessAssignedWarehouse() {
    Warehouse w1 = warehouse(1L);
    UserAccount user = userWithRole("warehouse_sales", Set.of(w1));
    assertTrue(service.canAccessWarehouse(user, 1L));
    assertFalse(service.canAccessWarehouse(user, 2L));
  }

  // helper methods: userWithRole, warehouse
}
```

- [ ] **Step 2: 运行确认 FAIL**

Run: `mvn -q test -Dtest=WarehouseAccessServiceTest`

- [ ] **Step 3: 实现**

```java
package com.systelm.service;

import com.systelm.entity.UserAccount;
import org.springframework.stereotype.Service;

@Service
public class WarehouseAccessService {
    public boolean canAccessWarehouse(UserAccount user, Long warehouseId) {
        if ("admin".equals(user.getRole().getName())) {
            return true;
        }
        return user.getWarehouses().stream()
            .anyMatch(w -> w.getId().equals(warehouseId));
    }

    public void requireWarehouseAccess(UserAccount user, Long warehouseId) {
        if (!canAccessWarehouse(user, warehouseId)) {
            throw new IllegalArgumentException("无权操作该仓库");
        }
    }
}
```

- [ ] **Step 4: 测试 PASS + Commit**

```bash
mvn -q test -Dtest=WarehouseAccessServiceTest
git commit -am "feat: add warehouse access guard"
```

---

### Task 1.6: 商品 / 分类 / 批次实体与 API

**Files:**
- Create: `backend/src/main/java/com/systelm/entity/Product.java`
- Create: `backend/src/main/java/com/systelm/entity/ProductCategory.java`
- Create: `backend/src/main/java/com/systelm/entity/ProductBatch.java`
- Create: repositories + `ProductService.java` + `ProductController.java`
- Test: `backend/src/test/java/com/systelm/service/ProductServiceTest.java`

- [ ] **Step 1: 写 ProductServiceTest — 创建商品与批次**

```java
@Test
void createProduct_withBatch_persistsBoth() {
    Product product = productService.createProduct(
        new CreateProductCommand("矿泉水", "SKU-001", 2.0, 3.0, "瓶", 10.0));
    ProductBatch batch = productService.createBatch(
        new CreateBatchCommand(product.getId(), "B20260101", "2026-01-01"));
    assertNotNull(batch.getId());
    assertEquals("B20260101", batch.getBatchNo());
}
```

- [ ] **Step 2: FAIL → 实现实体/服务/控制器**

API 端点:
- `GET/POST/PUT /api/products`
- `GET/POST /api/product-categories`
- `GET/POST /api/products/{id}/batches`

- [ ] **Step 3: PASS + Commit**

```bash
git commit -am "feat: add product category batch APIs"
```

---

### Task 1.7: StockService 核心库存逻辑（TDD）

**Files:**
- Create: `backend/src/main/java/com/systelm/entity/Stock.java`
- Create: `backend/src/main/java/com/systelm/repository/StockRepository.java`
- Create: `backend/src/main/java/com/systelm/service/StockService.java`
- Test: `backend/src/test/java/com/systelm/service/StockServiceTest.java`

- [ ] **Step 1: 写失败测试 — 入库增加库存**

```java
@Test
void increaseStock_createsOrUpdatesQuantity() {
    stockService.increase(warehouseId, productId, batchId, 100);
  assertEquals(100, stockService.getQuantity(warehouseId, productId, batchId));
    stockService.increase(warehouseId, productId, batchId, 50);
  assertEquals(150, stockService.getQuantity(warehouseId, productId, batchId));
}
```

- [ ] **Step 2: 写失败测试 — 出库不足抛错**

```java
@Test
void decreaseStock_whenInsufficient_throws() {
    stockService.increase(warehouseId, productId, batchId, 10);
    assertThrows(InsufficientStockException.class,
        () -> stockService.decrease(warehouseId, productId, batchId, 20));
}
```

- [ ] **Step 3: 实现 StockService**

```java
@Transactional
public void increase(Long warehouseId, Long productId, Long batchId, double qty) {
    Stock stock = stockRepository
        .findByWarehouseIdAndProductIdAndBatchId(warehouseId, productId, batchId)
        .orElseGet(() -> newStock(warehouseId, productId, batchId));
    stock.setQuantity(stock.getQuantity() + qty);
    stockRepository.save(stock);
}

@Transactional
public void decrease(Long warehouseId, Long productId, Long batchId, double qty) {
    Stock stock = stockRepository
        .findByWarehouseIdAndProductIdAndBatchId(warehouseId, productId, batchId)
        .orElseThrow(() -> new InsufficientStockException("库存不存在"));
    if (stock.getQuantity() < qty) {
        throw new InsufficientStockException("库存不足");
    }
    stock.setQuantity(stock.getQuantity() - qty);
    stockRepository.save(stock);
}
```

- [ ] **Step 4: 测试 PASS + Commit**

```bash
git commit -am "feat: add stock increase/decrease service"
```

---

### Task 1.8: 入库 / 出库 / 盘点单据服务

**Files:**
- Create: `StockInService.java`, `StockOutService.java`, `StockCheckService.java`
- Create: 对应 Controller
- Test: `StockInServiceTest.java`, `StockCheckServiceTest.java`

- [ ] **Step 1: StockInServiceTest**

```java
@Test
void confirmStockIn_updatesStockSnapshot() {
    var cmd = new StockInCommand(warehouseId, List.of(
        new StockInItemCommand(productId, batchId, 30)));
    stockInService.confirm(operator, cmd);
    assertEquals(30, stockService.getQuantity(warehouseId, productId, batchId));
}
```

- [ ] **Step 2: StockCheckServiceTest — 盘盈盘亏**

```java
@Test
void confirmStockCheck_adjustsStockByDiff() {
    stockService.increase(warehouseId, productId, batchId, 10);
    var cmd = new StockCheckCommand(warehouseId, List.of(
        new StockCheckItemCommand(productId, batchId, 8))); // 盘亏 2
    stockCheckService.confirm(operator, cmd);
    assertEquals(8, stockService.getQuantity(warehouseId, productId, batchId));
}
```

- [ ] **Step 3: 实现三服务 + REST API**

- `POST /api/stock-ins` 创建并确认入库
- `POST /api/stock-outs` 创建并确认出库（`out_type`: LOSS/SAMPLE/OTHER）
- `POST /api/stock-checks` 创建并确认盘点

每个服务:
1. 校验 `warehouseAccessService.requireWarehouseAccess`
2. 生成 `doc_no`（如 `IN20260617001`）
3. 写单据 + 明细
4. 调用 `StockService`

- [ ] **Step 4: 测试 PASS + Commit**

```bash
git commit -am "feat: add stock in out check document flows"
```

---

### Task 1.9: 用户与仓库管理 API（超管）

**Files:**
- Create: `UserService.java`, `UserController.java`
- Create: `WarehouseController.java`

- [ ] **Step 1: 实现仓库 CRUD**

- `GET/POST/PUT /api/warehouses`（admin only）

- [ ] **Step 2: 实现用户 CRUD + 分配仓库**

- `GET/POST/PUT /api/users`（admin only）
- 请求体含 `roleName`, `warehouseIds`

- [ ] **Step 3: 手工验证**

```bash
# 登录拿 token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
# 用 token 创建仓库
```

- [ ] **Step 4: Commit**

```bash
git commit -am "feat: add admin user and warehouse management APIs"
```

---

### Task 1.10: 前端 — frontend-design 主题与布局壳

**Files:**
- Create: `frontend/src/styles/theme.css`
- Create: `frontend/src/layouts/MainLayout.vue`
- Modify: `frontend/src/main.ts`

- [ ] **Step 1: 调用 `frontend-design` 技能**

根据 brief「内部多仓 ERP、一线仓管/销售、高效录单」产出设计计划:
- 配色 token（4–6 色）
- 字体 pairing
- 侧栏+顶栏布局
- 签名元素（如：仓库状态色条、单据号 monospace 样式）

- [ ] **Step 2: 写 theme.css（按设计计划）**

示例结构:
```css
:root {
  --color-primary: #1e4d6b;
  --color-accent: #e8a838;
  --color-bg: #f0f2f5;
  --color-surface: #ffffff;
  --font-display: 'DM Sans', system-ui, sans-serif;
  --font-body: 'IBM Plex Sans', system-ui, sans-serif;
  --font-mono: 'IBM Plex Mono', monospace;
}
```

- [ ] **Step 3: 实现 MainLayout.vue**

- 顶栏：系统名、当前用户、退出
- 侧栏：按 `authStore.role` 动态菜单
- 主区 `<router-view />`

- [ ] **Step 4: Commit**

```bash
git commit -am "feat: add frontend shell with custom theme"
```

---

### Task 1.11: 前端 — 登录 + 阶段1业务页面

**Files:**
- Create: `frontend/src/views/LoginView.vue`
- Create: `frontend/src/views/warehouse/`, `product/`, `stock/` 下各页面
- Create: `frontend/src/api/*.ts`, `frontend/src/stores/auth.ts`

- [ ] **Step 1: auth store + 路由守卫**

```typescript
// stores/auth.ts
export const useAuthStore = defineStore('auth', {
  state: () => ({ token: '', role: '', warehouseIds: [] as number[] }),
  actions: {
    async login(username: string, password: string) { /* POST /api/auth/login */ },
    logout() { this.token = ''; /* clear localStorage */ },
  },
})
```

- [ ] **Step 2: 登录页**

- 表单：用户名、密码
- 成功后跳转 `/dashboard`

- [ ] **Step 3: 仓库/商品/入库/出库/盘点页面**

每页模式：表格 + 搜索 + 新建对话框
- 仓库管理（admin）
- 商品管理（admin 可编辑，其他只读）
- 入库单：选仓 → 添加行（商品、批次、数量）
- 出库单：选仓 → 添加行，显示可用库存
- 盘点单：选仓 → 加载账面库存 → 填实盘数

- [ ] **Step 4: 阶段1手工验收清单**

- [ ] admin 登录成功
- [ ] 创建 2 个仓库，创建仓管用户并分配仓库
- [ ] 录入商品与批次，入库 100 件
- [ ] 出库 30 件，库存变 70
- [ ] 盘点改为 65，库存变 65
- [ ] 仓管用户无法看到未分配仓库

- [ ] **Step 5: Commit**

```bash
git commit -am "feat: complete phase 1 frontend pages"
```

---

# 阶段 2：调拨 + 客户 + 销售

### Task 2.1: Flyway V3 — 调拨/客户/销售表

**Files:**
- Create: `backend/src/main/resources/db/migration/V3__transfer_customer_sales.sql`

- [ ] **Step 1: 写迁移**

```sql
CREATE TABLE customer (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    contact TEXT,
    phone TEXT,
    address TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE transfer (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    doc_no TEXT NOT NULL UNIQUE,
    from_warehouse_id INTEGER NOT NULL REFERENCES warehouse(id),
    to_warehouse_id INTEGER NOT NULL REFERENCES warehouse(id),
    operator_id INTEGER NOT NULL REFERENCES user_account(id),
    status TEXT NOT NULL DEFAULT 'COMPLETED',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE transfer_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transfer_id INTEGER NOT NULL REFERENCES transfer(id),
    product_id INTEGER NOT NULL REFERENCES product(id),
    batch_id INTEGER NOT NULL REFERENCES product_batch(id),
    quantity REAL NOT NULL
);

CREATE TABLE sales_order (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    doc_no TEXT NOT NULL UNIQUE,
    customer_id INTEGER NOT NULL REFERENCES customer(id),
    warehouse_id INTEGER NOT NULL REFERENCES warehouse(id),
    operator_id INTEGER NOT NULL REFERENCES user_account(id),
    total_amount REAL NOT NULL,
    status TEXT NOT NULL DEFAULT 'COMPLETED',
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE sales_order_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sales_order_id INTEGER NOT NULL REFERENCES sales_order(id),
    product_id INTEGER NOT NULL REFERENCES product(id),
    batch_id INTEGER NOT NULL REFERENCES product_batch(id),
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    amount REAL NOT NULL
);
```

- [ ] **Step 2: Commit**

```bash
git commit -am "feat: add transfer customer sales schema"
```

---

### Task 2.2: TransferService（TDD）

**Files:**
- Create: `TransferService.java`, `TransferController.java`
- Test: `TransferServiceTest.java`

- [ ] **Step 1: 写失败测试**

```java
@Test
void confirmTransfer_movesStockBetweenWarehouses() {
    stockService.increase(fromWarehouseId, productId, batchId, 50);
    transferService.confirm(operator, new TransferCommand(
        fromWarehouseId, toWarehouseId,
        List.of(new TransferItemCommand(productId, batchId, 20))));
    assertEquals(30, stockService.getQuantity(fromWarehouseId, productId, batchId));
    assertEquals(20, stockService.getQuantity(toWarehouseId, productId, batchId));
}
```

- [ ] **Step 2: 实现 — 校验双方仓库权限、from 扣减、to 增加**

- [ ] **Step 3: API `POST /api/transfers` + Commit**

---

### Task 2.3: CustomerService + SalesOrderService（TDD）

**Files:**
- Create: `CustomerService.java`, `SalesOrderService.java`
- Test: `SalesOrderServiceTest.java`

- [ ] **Step 1: 写失败测试**

```java
@Test
void confirmSalesOrder_decreasesStockAndCalculatesTotal() {
    stockService.increase(warehouseId, productId, batchId, 100);
    var order = salesOrderService.confirm(operator, new SalesOrderCommand(
        customerId, warehouseId,
        List.of(new SalesOrderItemCommand(productId, batchId, 5, 10.0))));
    assertEquals(50.0, order.getTotalAmount());
    assertEquals(95, stockService.getQuantity(warehouseId, productId, batchId));
}
```

- [ ] **Step 2: 实现客户 CRUD + 销售确认（同时写 stock_out 记录，out_type=SALES）**

API:
- `GET/POST/PUT /api/customers`
- `GET /api/customers/{id}/orders`
- `POST /api/sales-orders`
- `GET /api/stocks?warehouseId=&productId=`

- [ ] **Step 3: 测试 PASS + Commit**

---

### Task 2.4: 前端 — 调拨/客户/销售/库存查询页

**Files:**
- Create: `frontend/src/views/transfer/`, `customer/`, `sales/`, `stock/StockListView.vue`

- [ ] **Step 1: 调拨页** — 双仓选择，校验调出仓库存

- [ ] **Step 2: 客户页** — CRUD + 历史订单抽屉

- [ ] **Step 3: 销售开单页** — 选客户、发货仓、商品行（批次/数量/单价），显示合计

- [ ] **Step 4: 库存查询页** — 按仓/商品/批次筛选

- [ ] **Step 5: 阶段2验收 + Commit**

---

# 阶段 3：报表与库存预警

### Task 3.1: ReportService

**Files:**
- Create: `ReportService.java`, `ReportController.java`
- Test: `ReportServiceTest.java`

- [ ] **Step 1: 库存报表测试**

```java
@Test
void stockReport_returnsQuantityAndValue() {
    // 入库后查询
    var rows = reportService.stockSummary(admin, null);
    assertFalse(rows.isEmpty());
    assertTrue(rows.get(0).totalValue() > 0);
}
```

- [ ] **Step 2: 实现报表 API**

- `GET /api/reports/stock` — 各仓库存数量、金额（qty × cost_price）
- `GET /api/reports/sales?from=&to=` — 按日/商品/客户汇总
- `GET /api/reports/profit?from=&to=` — 销售额 − 成本
- `GET /api/reports/low-stock` — `sum(stock.qty) < product.min_stock`

财务角色：全部只读；仓管仅看自己仓的 low-stock

- [ ] **Step 3: 测试 PASS + Commit**

---

### Task 3.2: 前端报表页

**Files:**
- Create: `frontend/src/views/report/StockReportView.vue`
- Create: `frontend/src/views/report/SalesReportView.vue`
- Create: `frontend/src/views/report/ProfitReportView.vue`
- Create: `frontend/src/views/report/LowStockView.vue`

- [ ] **Step 1: 各报表页 — 日期筛选 + Element Plus 表格/简单图表**

- [ ] **Step 2: 阶段3验收清单**

- [ ] 库存报表金额正确
- [ ] 销售报表按日期筛选
- [ ] 低库存商品标红
- [ ] 财务账号只能看报表，不能改数据

- [ ] **Step 3: Commit**

---

### Task 3.3: 生产打包与内网部署

**Files:**
- Modify: `backend/pom.xml`（静态资源打包）
- Create: `scripts/start.bat`, `scripts/start.sh`

- [ ] **Step 1: 配置 Spring Boot 托管 frontend/dist**

```java
// WebMvcConfig.java
registry.addResourceHandler("/**")
    .addResourceLocations("classpath:/static/");
```

构建流程:
```bash
cd frontend && npm run build
cp -r dist/* ../backend/src/main/resources/static/
cd ../backend && mvn -q package -DskipTests
```

- [ ] **Step 2: 写启动脚本**

`scripts/start.bat`:
```bat
java -jar backend\target\backend-0.1.0.jar
```

- [ ] **Step 3: 验证单 JAR 访问 `http://localhost:8080`**

- [ ] **Step 4: Commit + tag**

```bash
git commit -am "chore: add production build and start scripts"
git tag v0.1.0-mvp
```

---

## 规格覆盖自检

| 规格要求 | 对应任务 |
|----------|----------|
| 多仓库 | Task 1.2, 1.5, 1.9 |
| 批次库存 | Task 1.6, 1.7 |
| 入库/出库/盘点 | Task 1.8 |
| 调拨 | Task 2.1, 2.2 |
| 客户+销售 | Task 2.1, 2.3, 2.4 |
| 报表+预警 | Task 3.1, 3.2 |
| 角色权限+仓库隔离 | Task 1.4, 1.5, 1.9 |
| JWT 登录 | Task 1.4 |
| frontend-design | Task 1.10 |
| SQLite 内网部署 | Task 0.2, 3.3 |
| 不含采购 | 无相关任务 ✓ |

---

## 执行建议顺序

1. 阶段 0（Task 0.1–0.3）— 约 30 分钟
2. 阶段 1 后端（Task 1.1–1.9）— 核心，先通 API
3. 阶段 1 前端（Task 1.10–1.11）— 可调 frontend-design
4. **暂停验收阶段 1**
5. 阶段 2 → 验收
6. 阶段 3 → 验收
7. Task 3.3 打包部署
