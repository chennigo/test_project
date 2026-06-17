import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import MainLayout from '../layouts/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { public: true, title: '登录' },
    },
    {
      path: '/',
      component: MainLayout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('../views/DashboardView.vue'),
          meta: { title: '工作台', roles: ['admin', 'warehouse_sales', 'finance'] },
        },
        {
          path: 'admin/users',
          name: 'admin-users',
          component: () => import('../views/PlaceholderView.vue'),
          meta: { title: '用户管理', roles: ['admin'] },
        },
        {
          path: 'admin/warehouses',
          name: 'admin-warehouses',
          component: () => import('../views/PlaceholderView.vue'),
          meta: { title: '仓库管理', roles: ['admin'] },
        },
        {
          path: 'products',
          name: 'products',
          component: () => import('../views/PlaceholderView.vue'),
          meta: { title: '商品管理', roles: ['admin', 'warehouse_sales', 'finance'] },
        },
        {
          path: 'stock/in',
          name: 'stock-in',
          component: () => import('../views/PlaceholderView.vue'),
          meta: { title: '入库单', roles: ['admin', 'warehouse_sales'] },
        },
        {
          path: 'stock/out',
          name: 'stock-out',
          component: () => import('../views/PlaceholderView.vue'),
          meta: { title: '出库单', roles: ['admin', 'warehouse_sales'] },
        },
        {
          path: 'stock/check',
          name: 'stock-check',
          component: () => import('../views/PlaceholderView.vue'),
          meta: { title: '盘点单', roles: ['admin', 'warehouse_sales'] },
        },
        {
          path: 'stock/query',
          name: 'stock-query',
          component: () => import('../views/PlaceholderView.vue'),
          meta: { title: '库存查询', roles: ['admin', 'warehouse_sales', 'finance'] },
        },
        {
          path: 'transfer',
          name: 'transfer',
          component: () => import('../views/PlaceholderView.vue'),
          meta: { title: '仓库调拨', roles: ['admin', 'warehouse_sales'] },
        },
        {
          path: 'customers',
          name: 'customers',
          component: () => import('../views/PlaceholderView.vue'),
          meta: { title: '客户档案', roles: ['admin', 'warehouse_sales', 'finance'] },
        },
        {
          path: 'sales',
          name: 'sales',
          component: () => import('../views/PlaceholderView.vue'),
          meta: { title: '销售开单', roles: ['admin', 'warehouse_sales'] },
        },
        {
          path: 'reports',
          name: 'reports',
          component: () => import('../views/PlaceholderView.vue'),
          meta: { title: '报表中心', roles: ['admin', 'finance'] },
        },
        {
          path: 'alerts',
          name: 'alerts',
          component: () => import('../views/PlaceholderView.vue'),
          meta: { title: '库存预警', roles: ['admin', 'warehouse_sales', 'finance'] },
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.public) {
    if (auth.isAuthenticated && to.name === 'login') {
      return { name: 'dashboard' }
    }
    return true
  }

  if (!auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  const allowedRoles = to.meta.roles as string[] | undefined
  if (allowedRoles && auth.role && !allowedRoles.includes(auth.role)) {
    return { name: 'dashboard' }
  }

  return true
})

export default router
