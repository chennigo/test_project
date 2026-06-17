<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import {
  Box,
  DataAnalysis,
  Document,
  Goods,
  HomeFilled,
  OfficeBuilding,
  Sell,
  Switch,
  User,
  Warning,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

interface MenuItem {
  index: string
  title: string
  icon: typeof HomeFilled
  roles: string[]
}

interface MenuGroup {
  label: string
  items: MenuItem[]
}

const menuGroups = computed<MenuGroup[]>(() => {
  const role = auth.role
  const groups: MenuGroup[] = [
    {
      label: '概览',
      items: [
        { index: '/dashboard', title: '工作台', icon: HomeFilled, roles: ['admin', 'warehouse_sales', 'finance'] },
      ],
    },
    {
      label: '系统',
      items: [
        { index: '/system/users', title: '用户管理', icon: User, roles: ['admin'] },
        { index: '/system/warehouses', title: '仓库管理', icon: OfficeBuilding, roles: ['admin'] },
      ],
    },
    {
      label: '基础资料',
      items: [
        { index: '/products', title: '商品管理', icon: Goods, roles: ['admin', 'warehouse_sales', 'finance'] },
        { index: '/customers', title: '客户档案', icon: User, roles: ['admin', 'warehouse_sales', 'finance'] },
      ],
    },
    {
      label: '库存作业',
      items: [
        { index: '/stock/in', title: '入库单', icon: Document, roles: ['admin', 'warehouse_sales'] },
        { index: '/stock/out', title: '出库单', icon: Document, roles: ['admin', 'warehouse_sales'] },
        { index: '/stock/check', title: '盘点单', icon: Document, roles: ['admin', 'warehouse_sales'] },
        { index: '/stock/query', title: '库存查询', icon: Box, roles: ['admin', 'warehouse_sales', 'finance'] },
        { index: '/stock/transfer', title: '仓库调拨', icon: Switch, roles: ['admin', 'warehouse_sales'] },
      ],
    },
    {
      label: '销售',
      items: [
        { index: '/sales/orders', title: '销售开单', icon: Sell, roles: ['admin', 'warehouse_sales'] },
      ],
    },
    {
      label: '分析',
      items: [
        { index: '/reports', title: '报表中心', icon: DataAnalysis, roles: ['admin', 'finance'] },
        { index: '/alerts', title: '库存预警', icon: Warning, roles: ['admin', 'warehouse_sales', 'finance'] },
      ],
    },
  ]

  return groups
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => !role || item.roles.includes(role)),
    }))
    .filter((group) => group.items.length > 0)
})

const activeMenu = computed(() => route.path)

function handleLogout() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="layout">
    <header class="layout-header">
      <div class="layout-header__brand">
        <span class="layout-header__mark" aria-hidden="true" />
        <div>
          <h1 class="layout-header__title">Systelm</h1>
          <p class="layout-header__tagline">货物管理与销售</p>
        </div>
      </div>

      <div class="layout-header__user">
        <span class="layout-header__name">{{ auth.displayName || auth.username }}</span>
        <span class="layout-header__role">{{ auth.roleLabel }}</span>
        <el-button size="small" text @click="handleLogout">退出</el-button>
      </div>
    </header>

    <div class="layout-body">
      <aside class="layout-sidebar">
        <nav v-for="group in menuGroups" :key="group.label" class="sidebar-group">
          <p class="sidebar-group__label">{{ group.label }}</p>
          <el-menu
            :default-active="activeMenu"
            router
            class="sidebar-menu"
            background-color="transparent"
            text-color="var(--color-text-inverse)"
            active-text-color="#fbbf24"
          >
            <el-menu-item
              v-for="item in group.items"
              :key="item.index"
              :index="item.index"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.title }}</span>
            </el-menu-item>
          </el-menu>
        </nav>
      </aside>

      <main class="layout-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
}

.layout-header {
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.25rem;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.layout-header__brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.layout-header__mark {
  width: 4px;
  height: 28px;
  background: linear-gradient(180deg, var(--color-accent) 0%, var(--color-primary) 100%);
  border-radius: 2px;
}

.layout-header__title {
  font-family: var(--font-display);
  font-size: 1.25rem;
  line-height: 1.1;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.layout-header__tagline {
  margin: 0;
  font-size: 0.75rem;
  color: var(--color-text-muted);
  letter-spacing: 0.08em;
}

.layout-header__user {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.layout-header__name {
  font-weight: 500;
}

.layout-header__role {
  font-size: 0.78rem;
  color: var(--color-text-muted);
  padding: 0.15rem 0.5rem;
  background: var(--color-bg);
  border-radius: var(--radius-sm);
}

.layout-body {
  flex: 1;
  display: flex;
  min-height: 0;
}

.layout-sidebar {
  width: var(--sidebar-width);
  flex-shrink: 0;
  background: var(--color-sidebar);
  overflow-y: auto;
  padding: 0.75rem 0 1rem;
}

.sidebar-group {
  margin-bottom: 0.5rem;
}

.sidebar-group__label {
  margin: 0;
  padding: 0.5rem 1rem 0.35rem;
  font-size: 0.68rem;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(240, 244, 248, 0.45);
}

.sidebar-menu {
  border-right: none;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 40px;
  line-height: 40px;
  margin: 0 0.5rem 2px;
  border-radius: var(--radius-sm);
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: var(--color-sidebar-hover);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: var(--color-sidebar-active);
  font-weight: 500;
}

.layout-main {
  flex: 1;
  overflow: auto;
  padding: 1.25rem 1.5rem;
}
</style>
