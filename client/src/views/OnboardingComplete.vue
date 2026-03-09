<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
onMounted(() => router.push('/'))
</script>

<template>
  <div class="flex items-center justify-center min-h-screen">
    <p class="text-gray-400 text-sm">Redirecionando...</p>
  </div>
</template>