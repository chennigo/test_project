<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()

const warehouseHint = computed(() => {
  if (auth.role === 'admin') return '可访问全部仓库'
  if (auth.warehouseIds.length === 0) return '尚未分配仓库'
  return `已分配 ${auth.warehouseIds.length} 个仓库`
})

const placeholderStats = [
  { label: '商品总数', value: '—' },
  { label: '今日入库', value: '—' },
  { label: '今日出库', value: '—' },
  { label: '库存预警', value: '—' },
]
</script>

<template>
  <div class="dashboard">
    <div class="page-card welcome-card">
      <h2 class="page-title">工作台</h2>
      <p class="page-subtitle">{{ auth.displayName || auth.username }}，欢迎回来</p>

      <div class="stats">
        <div class="stat">
          <span class="stat__label">当前角色</span>
          <span class="stat__value">{{ auth.roleLabel }}</span>
        </div>
        <div class="stat">
          <span class="stat__label">仓库权限</span>
          <span class="stat__value">{{ warehouseHint }}</span>
        </div>
      </div>

      <p class="doc-demo">
        单据编号预览：<span class="doc-no">CK-20260617-008</span>
      </p>
    </div>

    <div class="page-card">
      <h3 class="section-title">数据概览</h3>
      <p class="hint">以下统计为占位，报表模块上线后接入真实数据。</p>
      <div class="stats stats--grid">
        <div v-for="item in placeholderStats" :key="item.label" class="stat stat--placeholder">
          <span class="stat__label">{{ item.label }}</span>
          <span class="stat__value stat__value--large">{{ item.value }}</span>
        </div>
      </div>
    </div>

    <div class="page-card">
      <h3 class="section-title">快捷入口</h3>
      <div class="quick-links">
        <router-link to="/products" class="quick-link">商品管理</router-link>
        <router-link to="/stock/in" class="quick-link">入库单</router-link>
        <router-link to="/stock/out" class="quick-link">出库单</router-link>
        <router-link to="/stock/check" class="quick-link">盘点单</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 960px;
}

.welcome-card {
  border-left: 4px solid var(--color-primary);
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.stats--grid {
  margin-top: 1rem;
  margin-bottom: 0;
}

.stat {
  padding: 0.75rem 1rem;
  background: var(--color-bg);
  border-radius: var(--radius-sm);
}

.stat--placeholder {
  border: 1px dashed var(--color-border);
}

.stat__label {
  display: block;
  font-size: 0.78rem;
  color: var(--color-text-muted);
  margin-bottom: 0.25rem;
}

.stat__value {
  font-weight: 500;
}

.stat__value--large {
  font-family: var(--font-display);
  font-size: 1.5rem;
}

.doc-demo {
  margin: 0;
  font-size: 0.9rem;
  color: var(--color-text-muted);
}

.section-title {
  font-family: var(--font-display);
  font-size: 1rem;
  margin-bottom: 0.5rem;
}

.hint {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 0.92rem;
}

.quick-links {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.quick-link {
  padding: 0.4rem 0.85rem;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 0.9rem;
  transition: border-color 0.15s;
}

.quick-link:hover {
  border-color: var(--color-primary);
}
</style>
