<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

const token = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const error = ref(null)

onMounted(() => {
  token.value = route.query.token || ''
  if (!token.value) {
    error.value = 'Link inválido ou expirado'
  }
})

async function handleSubmit() {
  if (password.value !== confirmPassword.value) {
    error.value = 'As senhas não coincidem'
    return
  }
  error.value = null
  loading.value = true
  try {
    await axios.post(`${import.meta.env.VITE_API_URL}/password/set`, {
      token: token.value,
      password: password.value
    })
    router.push('/login')
  } catch (e) {
    error.value = e.response?.data?.detail || 'Link inválido ou expirado'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex items-center justify-center min-h-screen">
    <div class="w-full max-w-sm p-8 bg-white border border-gray-200 shadow-sm">
      <h1 class="text-2xl font-bold text-black mb-2">Nova Senha</h1>
      <p class="text-sm text-gray-500 mb-8">Digite e confirme sua nova senha</p>

      <form @submit.prevent="handleSubmit">
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">Nova Senha</label>
          <input
              v-model="password"
              type="password"
              required
              :disabled="!token"
              class="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black disabled:bg-gray-50 disabled:text-gray-400"
              placeholder="••••••••"
          />
        </div>

        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">Confirmar Nova Senha</label>
          <input
              v-model="confirmPassword"
              type="password"
              required
              :disabled="!token"
              class="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black disabled:bg-gray-50 disabled:text-gray-400"
              placeholder="••••••••"
          />
        </div>

        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

        <button
            type="submit"
            :disabled="loading || !token"
            class="w-full py-3 px-4 bg-yellow-400 hover:bg-yellow-500 text-black font-bold disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ loading ? 'Salvando...' : 'Redefinir Senha' }}
        </button>
      </form>

      <div class="text-center mt-6 pt-6 border-t border-gray-100">
        <router-link to="/login" class="text-sm text-blue-500 hover:text-blue-600">
          Voltar para o login
        </router-link>
      </div>
    </div>
  </div>
</template>