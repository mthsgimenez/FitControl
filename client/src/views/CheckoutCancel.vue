<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-50">
    <div class="bg-white border border-gray-200 shadow-sm p-10 text-center max-w-sm w-full">
      <div class="w-12 h-12 bg-gray-200 rounded-full flex items-center justify-center mx-auto mb-4">
        <svg class="w-6 h-6 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </div>
      <h1 class="text-xl font-bold text-black mb-2">Pagamento cancelado</h1>
      <p class="text-sm text-gray-500 mb-6">Sua assinatura não foi processada.</p>
      <button
          @click="router.push('/my-plan')"
          class="w-full py-3 bg-yellow-400 hover:bg-yellow-500 text-black font-bold text-sm"
      >
        Ver planos
      </button>
    </div>
  </div>
</template>