<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => (route.meta.title as string) || '页面')
</script>

<template>
  <div class="placeholder page-card">
    <h2 class="page-title">{{ title }}</h2>
    <p class="page-subtitle">此模块将在后续任务中实现</p>
    <p class="doc-sample">
      单据号示例：<span class="doc-no-inline">DB-20260617-003</span>
    </p>
  </div>
</template>

<style scoped>
.placeholder {
  max-width: 720px;
}

.doc-sample {
  margin: 0;
  font-size: 0.9rem;
  color: var(--color-text-muted);
}
</style>
