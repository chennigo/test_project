<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const loading = ref(false)

async function handleSubmit() {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    await auth.login(username.value, password.value)
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } catch {
    ElMessage.error('登录失败，请检查用户名或密码')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-panel page-card">
      <div class="login-panel__header">
        <span class="layout-mark" aria-hidden="true" />
        <div>
          <h1 class="page-title">Systelm</h1>
          <p class="page-subtitle">货物管理与销售 — 内网登录</p>
        </div>
      </div>

      <p class="login-hint">
        示例单号样式：<span class="doc-no-inline">RK-20260617-001</span>
      </p>

      <el-form label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="用户名">
          <el-input v-model="username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="password"
            type="password"
            show-password
            autocomplete="current-password"
            @keyup.enter="handleSubmit"
          />
        </el-form-item>
        <el-button type="primary" :loading="loading" class="login-submit" @click="handleSubmit">
          登录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 1.5rem;
  background:
    linear-gradient(135deg, rgba(26, 58, 92, 0.04) 0%, transparent 50%),
    var(--color-bg);
}

.login-panel {
  width: min(100%, 400px);
}

.login-panel__header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.layout-mark {
  width: 4px;
  height: 36px;
  background: linear-gradient(180deg, var(--color-accent) 0%, var(--color-primary) 100%);
  border-radius: 2px;
}

.login-hint {
  margin: 0 0 1.25rem;
  font-size: 0.85rem;
  color: var(--color-text-muted);
}

.login-submit {
  width: 100%;
  margin-top: 0.5rem;
}
</style>
