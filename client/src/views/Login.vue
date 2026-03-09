<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore.js';

const router = useRouter()
const auth = useAuthStore()

const email = ref('')
const password = ref('')
const error = ref(null)
const loading = ref(false)

async function handleLogin() {
  error.value = null
  loading.value = true
  try {
    await auth.login(email.value, password.value)
    router.push('/dashboard')
  } catch (e) {
    error.value = e.response?.data?.detail || 'E-mail ou senha inválidos'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex items-center justify-center min-h-screen">
    <div class="w-full max-w-sm p-8 bg-white border border-gray-200 shadow-sm">
      <h1 class="text-2xl font-bold text-black mb-8">Entrar</h1>
      <form @submit.prevent="handleLogin">
        <div class="mb-5">
          <label for="email" class="block text-sm font-medium text-gray-700 mb-2">E-mail</label>
          <input
              type="email"
              id="email"
              v-model="email"
              required
              class="w-full px-4 py-3 bg-white border border-gray-300 text-black placeholder-gray-400 focus:outline-none focus:border-black"
              placeholder="seu@email.com"
          />
        </div>
        <div class="mb-6">
          <div class="flex items-center justify-between mb-2">
            <label for="password" class="block text-sm font-medium text-gray-700">Senha</label>
            <router-link to="/reset-password" class="text-sm text-blue-500 hover:text-blue-600">Esqueceu sua senha?</router-link>
          </div>
          <input
              type="password"
              id="password"
              v-model="password"
              required
              class="w-full px-4 py-3 bg-white border border-gray-300 text-black placeholder-gray-400 focus:outline-none focus:border-black"
              placeholder="••••••••"
          />
        </div>

        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

        <button
            type="submit"
            :disabled="loading"
            class="w-full py-3 px-4 bg-yellow-400 hover:bg-yellow-500 text-black font-bold mb-8 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ loading ? 'Entrando...' : 'Acessar Plataforma' }}
        </button>
        <div class="text-center mt-4 pt-6 border-t border-gray-100">
          <router-link to="/register" class="text-sm text-blue-500 hover:text-blue-600">É dono(a) de uma academia? Clique aqui para se registrar</router-link>
        </div>
      </form>
    </div>
  </div>
</template>