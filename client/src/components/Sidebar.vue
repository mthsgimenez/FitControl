<script setup>
import { computed, ref } from 'vue'
import { useAuthStore } from '@/stores/authStore.js'

const auth = useAuthStore()
const collapsed = ref(false)

const hasRole = (role) => auth.roles.includes(role)
const isOwner = computed(() => hasRole('ROLE_OWNER'))
const isManager = computed(() => isOwner.value || hasRole('ROLE_MANAGER'))
const isFinance = computed(() => isOwner.value || hasRole('ROLE_FINANCE'))
const isInstructor = computed(() => isOwner.value || hasRole('ROLE_INSTRUCTOR'))
const isMember = computed(() => hasRole('ROLE_MEMBER'))

const managerLinks = [
  { label: 'Funcionários', to: '/employees' },
  { label: 'Membros', to: '/members' },
  { label: 'Pessoas', to: '/people' }
]
const financeLinks = [
  { label: 'Planos', to: '/plans' },
  { label: 'Assinaturas e Pagamentos', to: '/subscriptions' }
]
const instructorLinks = [
  { label: 'Exercícios', to: '/exercises' },
  { label: 'Templates de Fichas', to: '/routine-templates' },
  { label: 'Fichas de Treino', to: '/routines' },
  { label: 'Alunos', to: '/students' }
]
const memberLinks = [
  { label: 'Meu Plano', to: '/my-plan' },
  { label: 'Fichas de Treino', to: '/routines' },
  { label: 'Registrar Treino', to: '/workout' }
]
</script>

<template>
  <aside
      :class="[
        'min-h-screen bg-white border-r border-gray-200 flex flex-col transition-all duration-300',
        collapsed ? 'w-16' : 'w-64'
      ]"
  >
    <!-- Header -->
    <div class="p-4 border-b border-gray-200 flex items-center justify-between">
      <span v-if="!collapsed" class="text-xl font-bold text-black">FitControl</span>
      <button
          @click="collapsed = !collapsed"
          class="p-1 hover:bg-gray-100 rounded ml-auto"
      >
        <svg
            :class="['w-5 h-5 text-gray-500 transition-transform duration-300', collapsed ? 'rotate-180' : '']"
            fill="none" stroke="currentColor" viewBox="0 0 24 24"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
      </button>
    </div>

    <!-- Nav -->
    <nav class="flex-1 p-2 space-y-6 overflow-y-auto overflow-x-hidden">
      <template v-if="!collapsed">

        <div v-if="isManager">
          <p class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2 px-2">Gestão</p>
          <ul class="space-y-1">
            <li v-for="link in managerLinks" :key="link.to">
              <router-link
                  :to="link.to"
                  class="flex items-center px-3 py-2 text-sm text-gray-700 hover:bg-yellow-50 hover:text-black font-medium rounded"
                  active-class="bg-yellow-400 text-black"
              >
                {{ link.label }}
              </router-link>
            </li>
          </ul>
        </div>

        <div v-if="isFinance">
          <p class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2 px-2">Financeiro</p>
          <ul class="space-y-1">
            <li v-for="link in financeLinks" :key="link.to">
              <router-link
                  :to="link.to"
                  class="flex items-center px-3 py-2 text-sm text-gray-700 hover:bg-yellow-50 hover:text-black font-medium rounded"
                  active-class="bg-yellow-400 text-black"
              >
                {{ link.label }}
              </router-link>
            </li>
          </ul>
        </div>

        <div v-if="isInstructor">
          <p class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2 px-2">Treinos</p>
          <ul class="space-y-1">
            <li v-for="link in instructorLinks" :key="link.to">
              <router-link
                  :to="link.to"
                  class="flex items-center px-3 py-2 text-sm text-gray-700 hover:bg-yellow-50 hover:text-black font-medium rounded"
                  active-class="bg-yellow-400 text-black"
              >
                {{ link.label }}
              </router-link>
            </li>
          </ul>
        </div>

        <div v-if="isMember">
          <p class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2 px-2">Minha Área</p>
          <ul class="space-y-1">
            <li v-for="link in memberLinks" :key="link.to">
              <router-link
                  :to="link.to"
                  class="flex items-center px-3 py-2 text-sm text-gray-700 hover:bg-yellow-50 hover:text-black font-medium rounded"
                  active-class="bg-yellow-400 text-black"
              >
                {{ link.label }}
              </router-link>
            </li>
          </ul>
        </div>

      </template>
    </nav>

    <!-- Footer -->
    <div class="p-3 border-t border-gray-200">
      <div v-if="!collapsed" class="flex items-center justify-between gap-2">
        <span class="text-xs text-gray-500 truncate">{{ auth.email != null ? auth.email : "" }}</span>
        <button
            @click="auth.logout"
            class="shrink-0 px-2 py-2 text-xs text-gray-700 hover:bg-red-50 hover:text-red-600 font-medium whitespace-nowrap rounded"
        >
          Sair
        </button>
      </div>
    </div>

  </aside>
</template>