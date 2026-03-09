<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '@/api/axios.js'

const subscription = ref(null)
const plans = ref([])
const allMembers = ref([])
const loading = ref(false)
const error = ref(null)

const showPlansModal = ref(false)
const showAddBeneficiaryModal = ref(false)
const removeConfirmId = ref(null)
const checkoutLoading = ref(false)
const addBeneficiaryMemberId = ref(null)

const statusLabels = {
  ACTIVE: 'Ativo',
  PENDING: 'Pendente',
  CANCELLED: 'Cancelado',
  PAYMENT_FAILED: 'Pagamento falhou',
  EXPIRED: 'Expirado'
}

const statusColors = {
  ACTIVE: 'bg-green-100 text-green-700',
  PENDING: 'bg-yellow-100 text-yellow-700',
  CANCELLED: 'bg-gray-100 text-gray-500',
  PAYMENT_FAILED: 'bg-red-100 text-red-600',
  EXPIRED: 'bg-gray-100 text-gray-500'
}

const canAddBeneficiary = computed(() => {
  if (!subscription.value) return false
  const plan = subscription.value.membershipPlan
  const current = subscription.value.members?.length || 0
  return subscription.value.status === 'ACTIVE' && current < (plan.maxBeneficiaries || 1)
})

const availableMembers = computed(() => {
  if (!subscription.value) return []
  const memberIds = new Set(subscription.value.members?.map(m => m.id) || [])
  return allMembers.value.filter(m => !memberIds.has(m.id))
})

async function fetchSubscription() {
  loading.value = true
  error.value = null
  try {
    const { data } = await api.get('/subscriptions/my')
    subscription.value = data
  } catch (e) {
    if (e.response?.status === 404) {
      subscription.value = null
    } else {
      error.value = 'Erro ao carregar assinatura'
    }
  } finally {
    loading.value = false
  }
}

async function fetchPlans() {
  try {
    const { data } = await api.get('/membership-plan')
    plans.value = data
  } catch (e) {}
}

async function fetchMembers() {
  try {
    const { data } = await api.get('/member')
    allMembers.value = data
  } catch (e) {}
}

async function checkout(planId) {
  checkoutLoading.value = planId
  error.value = null
  try {
    const { data } = await api.post('/subscriptions/checkout', { planId })
    window.location.href = data.checkoutUrl
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao iniciar checkout'
    checkoutLoading.value = null
  }
}

async function addBeneficiary() {
  if (!addBeneficiaryMemberId.value) return
  error.value = null
  try {
    const { data } = await api.post(`/subscriptions/${subscription.value.id}/members`, {
      memberId: addBeneficiaryMemberId.value
    })
    subscription.value = data
    showAddBeneficiaryModal.value = false
    addBeneficiaryMemberId.value = null
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao adicionar beneficiário'
  }
}

async function removeBeneficiary(memberId) {
  try {
    await api.delete(`/subscriptions/${subscription.value.id}/members/${memberId}`)
    removeConfirmId.value = null
    await fetchSubscription()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao remover beneficiário'
  }
}

function formatPrice(price) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(price)
}

function formatDuration(months) {
  if (months === 1) return '1 mês'
  if (months === 12) return '1 ano'
  return `${months} meses`
}

onMounted(async () => {
  await Promise.all([fetchSubscription(), fetchPlans(), fetchMembers()])
})
</script>

<template>
  <div class="max-w-2xl">
    <h1 class="text-2xl font-bold text-black mb-6">Meu Plano</h1>

    <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

    <div v-if="loading" class="p-8 text-center text-gray-400">Carregando...</div>

    <!-- No subscription -->
    <div v-else-if="!subscription" class="bg-white border border-gray-200 p-8 text-center">
      <p class="text-gray-500 mb-4">Você ainda não possui uma assinatura ativa.</p>
      <button
          @click="showPlansModal = true"
          class="px-6 py-3 bg-yellow-400 hover:bg-yellow-500 text-black font-bold text-sm"
      >
        Ver Planos
      </button>
    </div>

    <!-- Active subscription -->
    <div v-else class="space-y-4">

      <!-- Plan card -->
      <div class="bg-white border border-gray-200 p-6">
        <div class="flex items-start justify-between mb-4">
          <div>
            <h2 class="text-lg font-bold text-black">{{ subscription.membershipPlan.name }}</h2>
            <p class="text-sm text-gray-500 mt-0.5">
              {{ formatPrice(subscription.membershipPlan.price) }} / {{ formatDuration(subscription.membershipPlan.durationValue) }}
            </p>
          </div>
          <span :class="['text-xs px-2 py-1 font-medium', statusColors[subscription.status] || 'bg-gray-100 text-gray-500']">
            {{ statusLabels[subscription.status] || subscription.status }}
          </span>
        </div>

        <div class="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p class="text-gray-400 text-xs mb-0.5">Início</p>
            <p class="font-medium">{{ subscription.startDate || '—' }}</p>
          </div>
          <div>
            <p class="text-gray-400 text-xs mb-0.5">Vencimento</p>
            <p class="font-medium">{{ subscription.endDate || '—' }}</p>
          </div>
        </div>
      </div>

      <!-- Beneficiaries -->
      <div class="bg-white border border-gray-200 p-6">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h3 class="font-bold text-sm">Beneficiários</h3>
            <p class="text-xs text-gray-400 mt-0.5">
              {{ subscription.members?.length || 0 }} / {{ subscription.membershipPlan.maxBeneficiaries || 1 }}
            </p>
          </div>
          <button
              v-if="canAddBeneficiary"
              @click="showAddBeneficiaryModal = true"
              class="text-xs px-3 py-1 bg-yellow-400 hover:bg-yellow-500 text-black font-bold"
          >
            + Adicionar
          </button>
        </div>

        <div v-if="!subscription.members?.length" class="text-sm text-gray-400">
          Nenhum beneficiário adicionado
        </div>
        <ul v-else class="space-y-2">
          <li
              v-for="member in subscription.members"
              :key="member.id"
              class="flex items-center justify-between py-2 border-b border-gray-100 last:border-0"
          >
            <span class="text-sm font-medium">{{ member.name }} {{ member.lastName }}</span>
            <div class="flex gap-2">
              <button
                  v-if="removeConfirmId !== member.id"
                  @click="removeConfirmId = member.id"
                  class="text-xs text-red-400 hover:text-red-600"
              >
                Remover
              </button>
              <template v-else>
                <button
                    @click="removeBeneficiary(member.id)"
                    class="text-xs px-2 py-0.5 bg-red-500 text-white hover:bg-red-600"
                >
                  Confirmar
                </button>
                <button
                    @click="removeConfirmId = null"
                    class="text-xs px-2 py-0.5 border border-gray-300 hover:bg-gray-50"
                >
                  Cancelar
                </button>
              </template>
            </div>
          </li>
        </ul>
      </div>

      <!-- Subscribe to new plan if not active -->
      <div v-if="subscription.status !== 'ACTIVE'" class="text-center pt-2">
        <button
            @click="showPlansModal = true"
            class="px-6 py-3 bg-yellow-400 hover:bg-yellow-500 text-black font-bold text-sm"
        >
          Ver Planos
        </button>
      </div>
    </div>

    <!-- Plans Modal -->
    <div v-if="showPlansModal" class="fixed inset-0 bg-gray-100 bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-md p-6 shadow-lg">
        <h2 class="text-lg font-bold mb-4">Escolha um Plano</h2>

        <div class="space-y-3 mb-6">
          <div
              v-for="plan in plans"
              :key="plan.id"
              class="border border-gray-200 p-4 hover:border-yellow-400 transition-colors"
          >
            <div class="flex items-start justify-between mb-2">
              <div>
                <p class="font-bold text-sm">{{ plan.name }}</p>
                <p class="text-xs text-gray-400 mt-0.5">{{ formatDuration(plan.durationValue) }}</p>
              </div>
              <p class="font-bold text-black">{{ formatPrice(plan.price) }}</p>
            </div>
            <p class="text-xs text-gray-400 mb-3">
              Até {{ plan.maxBeneficiaries || 1 }} beneficiário(s)
            </p>
            <button
                @click="checkout(plan.id)"
                :disabled="checkoutLoading === plan.id"
                class="w-full py-2 bg-yellow-400 hover:bg-yellow-500 text-black text-sm font-bold disabled:opacity-50"
            >
              {{ checkoutLoading === plan.id ? 'Redirecionando...' : 'Assinar' }}
            </button>
          </div>
        </div>

        <button
            @click="showPlansModal = false"
            class="w-full py-2 border border-gray-300 text-sm font-medium hover:bg-gray-50"
        >
          Fechar
        </button>
      </div>
    </div>

    <!-- Add Beneficiary Modal -->
    <div v-if="showAddBeneficiaryModal" class="fixed inset-0 bg-gray-100 bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-sm p-6 shadow-lg">
        <h2 class="text-lg font-bold mb-4">Adicionar Beneficiário</h2>

        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">Membro</label>
          <select
              v-model="addBeneficiaryMemberId"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm bg-white"
          >
            <option :value="null">Selecione...</option>
            <option v-for="m in availableMembers" :key="m.id" :value="m.id">
              {{ m.person.name }} {{ m.person.lastName }}
            </option>
          </select>
        </div>

        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

        <div class="flex gap-3">
          <button
              @click="showAddBeneficiaryModal = false"
              class="w-full py-2 border border-gray-300 text-sm font-medium hover:bg-gray-50"
          >
            Cancelar
          </button>
          <button
              @click="addBeneficiary"
              :disabled="!addBeneficiaryMemberId"
              class="w-full py-2 bg-yellow-400 hover:bg-yellow-500 text-black text-sm font-bold disabled:opacity-50"
          >
            Adicionar
          </button>
        </div>
      </div>
    </div>

  </div>
</template>