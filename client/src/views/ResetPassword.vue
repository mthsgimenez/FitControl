<script setup>
import { ref } from 'vue'
import axios from 'axios'

const email = ref('')
const loading = ref(false)
const error = ref(null)
const submitted = ref(false)

async function handleSubmit() {
  error.value = null
  loading.value = true
  try {
    await axios.post(`${import.meta.env.VITE_API_URL}/password/request-token`, {
      email: email.value
    })
    submitted.value = true
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao enviar e-mail de recuperação'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex items-center justify-center min-h-screen">
    <div class="w-full max-w-sm p-8 bg-white border border-gray-200 shadow-sm">

      <template v-if="!submitted">
        <h1 class="text-2xl font-bold text-black mb-2">Recuperar Senha</h1>
        <p class="text-sm text-gray-500 mb-8">Enviaremos um link para redefinir sua senha</p>

        <form @submit.prevent="handleSubmit">
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">E-mail</label>
            <input
                v-model="email"
                type="email"
                required
                class="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black"
                placeholder="seu@email.com"
            />
          </div>

          <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

          <button
              type="submit"
              :disabled="loading"
              class="w-full py-3 px-4 bg-yellow-400 hover:bg-yellow-500 text-black font-bold disabled:opacity-50"
          >
            {{ loading ? 'Enviando...' : 'Enviar Link' }}
          </button>
        </form>
      </template>

      <template v-else>
        <div class="text-center">
          <div class="w-12 h-12 bg-yellow-400 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h1 class="text-2xl font-bold text-black mb-2">E-mail Enviado</h1>
          <p class="text-sm text-gray-500 mb-8">
            Verifique sua caixa de entrada em
            <span class="font-medium text-black">{{ email }}</span>
          </p>
        </div>
      </template>

      <div class="text-center mt-6 pt-6 border-t border-gray-100">
        <router-link to="/login" class="text-sm text-blue-500 hover:text-blue-600">
          Voltar para o login
        </router-link>
      </div>
    </div>
  </div>
</template>