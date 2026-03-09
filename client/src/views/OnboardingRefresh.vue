<script setup>
import { onMounted, ref } from 'vue'
import api from '@/api/axios.js'

const error = ref(false)

onMounted(async () => {
  try {
    const { data } = await api.post('/api/stripe/onboarding-link')
    window.location.href = data.onboardingUrl
  } catch (e) {
    error.value = true
  }
})
</script>

<template>
  <div class="flex items-center justify-center min-h-screen">
    <div class="text-center">
      <p v-if="!error" class="text-gray-400 text-sm">Redirecionando para o Stripe...</p>
      <div v-else>
        <p class="text-red-500 text-sm mb-4">Erro ao gerar link de onboarding.</p>
        <a href="/" class="text-sm text-blue-500 hover:text-blue-600">Voltar ao início</a>
      </div>
    </div>
  </div>
</template>