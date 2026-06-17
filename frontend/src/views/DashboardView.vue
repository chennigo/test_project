<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()

const warehouseHint = computed(() => {
  if (auth.role === 'admin') return '可访问全部仓库'
  if (auth.warehouseIds.length === 0) return '尚未分配仓库'
  return `已分配 ${auth.warehouseIds.length} 个仓库`
})
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
      <h3 class="section-title">快捷入口</h3>
      <p class="hint">请从左侧菜单进入各业务模块，页面内容将在后续任务中实现。</p>
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

.stat {
  padding: 0.75rem 1rem;
  background: var(--color-bg);
  border-radius: var(--radius-sm);
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
</style>
