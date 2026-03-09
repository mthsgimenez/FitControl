<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '@/api/axios.js'
import { useAuthStore } from '@/stores/authStore.js'

const auth = useAuthStore()
const isFinance = computed(() =>
    auth.roles.includes('ROLE_FINANCE') || auth.roles.includes('ROLE_OWNER')
)

const plans = ref([])
const loading = ref(false)
const error = ref(null)
const showModal = ref(false)
const editingPlan = ref(null)
const deleteConfirmId = ref(null)

const form = ref({
  name: '',
  price: '',
  durationValue: '',
  maxBeneficiaries: 1
})

async function fetchPlans() {
  loading.value = true
  error.value = null
  try {
    const { data } = await api.get('/membership-plan')
    plans.value = data
  } catch (e) {
    error.value = 'Erro ao carregar planos'
  } finally {
    loading.value = false
  }
}

function openCreateModal() {
  editingPlan.value = null
  form.value = { name: '', price: '', durationValue: '', maxBeneficiaries: 1 }
  error.value = null
  showModal.value = true
}

function openEditModal(plan) {
  editingPlan.value = plan
  form.value = {
    name: plan.name,
    price: plan.price,
    durationValue: plan.durationValue,
    maxBeneficiaries: plan.maxBeneficiaries
  }
  error.value = null
  showModal.value = true
}

async function submitForm() {
  error.value = null
  try {
    const payload = {
      name: form.value.name,
      price: parseFloat(form.value.price),
      durationValue: parseInt(form.value.durationValue),
      maxBeneficiaries: parseInt(form.value.maxBeneficiaries)
    }
    if (editingPlan.value) {
      await api.put(`/membership-plan/${editingPlan.value.id}`, payload)
    } else {
      await api.post('/membership-plan', payload)
    }
    showModal.value = false
    await fetchPlans()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao salvar plano'
  }
}

async function deactivatePlan(id) {
  try {
    await api.delete(`/membership-plan/${id}`)
    deleteConfirmId.value = null
    await fetchPlans()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao desativar plano'
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

onMounted(fetchPlans)
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-black">Planos</h1>
      <button
          v-if="isFinance"
          @click="openCreateModal"
          class="px-4 py-2 bg-yellow-400 hover:bg-yellow-500 text-black font-bold text-sm"
      >
        + Novo Plano
      </button>
    </div>

    <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

    <div v-if="loading" class="p-8 text-center text-gray-400">Carregando...</div>

    <div v-else-if="plans.length === 0" class="p-8 text-center text-gray-400 bg-white border border-gray-200 text-sm">
      Nenhum plano cadastrado
    </div>

    <div v-else class="bg-white border border-gray-200">
      <table class="w-full text-sm">
        <thead class="border-b border-gray-200">
        <tr class="text-left">
          <th class="px-4 py-3 font-semibold text-gray-600">Nome</th>
          <th class="px-4 py-3 font-semibold text-gray-600">Preço</th>
          <th class="px-4 py-3 font-semibold text-gray-600">Duração</th>
          <th class="px-4 py-3 font-semibold text-gray-600">Beneficiários</th>
          <th class="px-4 py-3 font-semibold text-gray-600">Status</th>
          <th v-if="isFinance" class="px-4 py-3 font-semibold text-gray-600">Ações</th>
        </tr>
        </thead>
        <tbody>
        <tr
            v-for="plan in plans"
            :key="plan.id"
            class="border-b border-gray-100 hover:bg-gray-50"
        >
          <td class="px-4 py-3 font-medium">{{ plan.name }}</td>
          <td class="px-4 py-3 text-gray-700">{{ formatPrice(plan.price) }}</td>
          <td class="px-4 py-3 text-gray-500">{{ formatDuration(plan.durationValue) }}</td>
          <td class="px-4 py-3 text-gray-500">{{ plan.maxBeneficiaries ?? '—' }}</td>
          <td class="px-4 py-3">
              <span :class="[
                'text-xs px-2 py-0.5 font-medium',
                plan.isActive ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'
              ]">
                {{ plan.isActive ? 'Ativo' : 'Inativo' }}
              </span>
          </td>
          <td v-if="isFinance" class="px-4 py-3">
            <div class="flex gap-2">
              <button
                  @click="openEditModal(plan)"
                  class="text-xs px-3 py-1 border border-gray-300 hover:bg-gray-50 font-medium"
              >
                Editar
              </button>
              <button
                  v-if="deleteConfirmId !== plan.id && plan.isActive"
                  @click="deleteConfirmId = plan.id"
                  class="text-xs px-3 py-1 border border-red-200 text-red-500 hover:bg-red-50 font-medium"
              >
                Desativar
              </button>
              <template v-if="deleteConfirmId === plan.id">
                <button
                    @click="deactivatePlan(plan.id)"
                    class="text-xs px-3 py-1 bg-red-500 text-white hover:bg-red-600 font-medium"
                >
                  Confirmar
                </button>
                <button
                    @click="deleteConfirmId = null"
                    class="text-xs px-3 py-1 border border-gray-300 hover:bg-gray-50 font-medium"
                >
                  Cancelar
                </button>
              </template>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-gray-100 bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-sm p-6 shadow-lg">
        <h2 class="text-lg font-bold mb-4">{{ editingPlan ? 'Editar Plano' : 'Novo Plano' }}</h2>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">Nome</label>
          <input
              v-model="form.name"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              placeholder="Ex: Mensal, Trimestral..."
          />
        </div>

        <div class="grid grid-cols-2 gap-3 mb-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Preço (R$)</label>
            <input
                v-model="form.price"
                type="number"
                step="0.01"
                min="0.01"
                class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
                placeholder="0,00"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Duração (meses)</label>
            <input
                v-model="form.durationValue"
                type="number"
                min="1"
                class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
                placeholder="1"
            />
          </div>
        </div>

        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">Máx. Beneficiários</label>
          <input
              v-model="form.maxBeneficiaries"
              type="number"
              min="1"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              placeholder="1"
          />
        </div>

        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

        <div class="flex gap-3">
          <button
              @click="showModal = false"
              class="w-full py-2 border border-gray-300 text-sm font-medium hover:bg-gray-50"
          >
            Cancelar
          </button>
          <button
              @click="submitForm"
              class="w-full py-2 bg-yellow-400 hover:bg-yellow-500 text-black text-sm font-bold"
          >
            {{ editingPlan ? 'Salvar' : 'Criar' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>