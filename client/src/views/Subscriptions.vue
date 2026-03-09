<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/axios.js'

const members = ref([])
const subscriptions = ref([])
const loading = ref(false)
const loadingSubscriptions = ref(false)
const error = ref(null)
const selectedMemberId = ref(null)
const expandedSubscriptionId = ref(null)

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

const paymentStatusColors = {
  paid: 'bg-green-100 text-green-700',
  failed: 'bg-red-100 text-red-600',
  open: 'bg-yellow-100 text-yellow-700'
}

async function fetchMembers() {
  loading.value = true
  try {
    const { data } = await api.get('/member')
    members.value = data
  } catch (e) {
    error.value = 'Erro ao carregar membros'
  } finally {
    loading.value = false
  }
}

async function selectMember(memberId) {
  selectedMemberId.value = memberId
  expandedSubscriptionId.value = null
  subscriptions.value = []
  loadingSubscriptions.value = true
  try {
    const { data } = await api.get(`/subscriptions/member/${memberId}`)
    subscriptions.value = data
  } catch (e) {
    error.value = 'Erro ao carregar assinaturas'
  } finally {
    loadingSubscriptions.value = false
  }
}

function toggleExpand(subscriptionId) {
  expandedSubscriptionId.value =
      expandedSubscriptionId.value === subscriptionId ? null : subscriptionId
}

function getMemberName(id) {
  const m = members.value.find(m => m.id === id)
  return m ? `${m.person.name} ${m.person.lastName}` : `Membro ${id}`
}

function formatPrice(amount) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(amount)
}

function formatDuration(months) {
  if (months === 1) return '1 mês'
  if (months === 12) return '1 ano'
  return `${months} meses`
}

function formatDateTime(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('pt-BR')
}

onMounted(fetchMembers)
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold text-black mb-6">Assinaturas e Pagamentos</h1>

    <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

    <div class="flex gap-6">

      <!-- Members panel -->
      <div class="w-56 shrink-0">
        <h2 class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">Membros</h2>
        <div class="bg-white border border-gray-200">
          <div v-if="loading" class="p-4 text-center text-gray-400 text-sm">Carregando...</div>
          <div v-else-if="members.length === 0" class="p-4 text-center text-gray-400 text-sm">
            Nenhum membro
          </div>
          <div
              v-for="member in members"
              :key="member.id"
              @click="selectMember(member.id)"
              :class="[
                'px-3 py-2 text-sm cursor-pointer border-b border-gray-100',
                selectedMemberId === member.id ? 'bg-yellow-400 font-bold' : 'hover:bg-gray-50'
              ]"
          >
            {{ member.person.name }} {{ member.person.lastName }}
          </div>
        </div>
      </div>

      <!-- Subscriptions panel -->
      <div class="flex-1">
        <h2 class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">
          {{ selectedMemberId ? `Assinaturas — ${getMemberName(selectedMemberId)}` : 'Assinaturas' }}
        </h2>

        <div v-if="!selectedMemberId" class="p-8 text-center text-gray-400 bg-white border border-gray-200 text-sm">
          Selecione um membro
        </div>

        <div v-else-if="loadingSubscriptions" class="p-8 text-center text-gray-400 bg-white border border-gray-200 text-sm">
          Carregando...
        </div>

        <div v-else-if="subscriptions.length === 0" class="p-8 text-center text-gray-400 bg-white border border-gray-200 text-sm">
          Nenhuma assinatura encontrada
        </div>

        <div v-else class="space-y-3">
          <div
              v-for="sub in subscriptions"
              :key="sub.id"
              class="bg-white border border-gray-200"
          >
            <!-- Subscription header -->
            <div
                class="flex items-center justify-between px-4 py-3 cursor-pointer hover:bg-gray-50"
                @click="toggleExpand(sub.id)"
            >
              <div class="flex items-center gap-4">
                <div>
                  <p class="text-sm font-bold">{{ sub.membershipPlan.name }}</p>
                  <p class="text-xs text-gray-400 mt-0.5">
                    {{ formatPrice(sub.membershipPlan.price) }} / {{ formatDuration(sub.membershipPlan.durationValue) }}
                  </p>
                </div>
                <span :class="['text-xs px-2 py-0.5 font-medium', statusColors[sub.status] || 'bg-gray-100 text-gray-500']">
                  {{ statusLabels[sub.status] || sub.status }}
                </span>
              </div>
              <div class="flex items-center gap-4">
                <div class="text-right text-xs text-gray-400">
                  <p>{{ sub.startDate }} → {{ sub.endDate }}</p>
                  <p v-if="sub.gatewayCurrentPeriodEnd" class="mt-0.5">
                    Período atual até {{ sub.gatewayCurrentPeriodEnd }}
                  </p>
                </div>
                <span class="text-gray-400 text-sm">
                  {{ expandedSubscriptionId === sub.id ? '▲' : '▼' }}
                </span>
              </div>
            </div>

            <!-- Expanded content -->
            <div v-if="expandedSubscriptionId === sub.id" class="border-t border-gray-100">

              <!-- Beneficiaries -->
              <div class="px-4 py-3 border-b border-gray-100">
                <p class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Beneficiários</p>
                <div v-if="!sub.members?.length" class="text-xs text-gray-400">Nenhum beneficiário</div>
                <div v-else class="flex flex-wrap gap-2">
                  <span
                      v-for="member in sub.members"
                      :key="member.id"
                      class="text-xs px-2 py-1 bg-gray-100 text-gray-600 font-medium"
                  >
                    {{ member.name }} {{ member.lastName }}
                  </span>
                </div>
              </div>

              <!-- Payments -->
              <div class="px-4 py-3">
                <p class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">Pagamentos</p>
                <div v-if="!sub.payments?.length" class="text-xs text-gray-400">
                  Nenhum pagamento registrado
                </div>
                <table v-else class="w-full text-sm">
                  <thead>
                  <tr class="text-left text-xs text-gray-400 border-b border-gray-100">
                    <th class="pb-2 font-medium">Valor</th>
                    <th class="pb-2 font-medium">Status</th>
                    <th class="pb-2 font-medium">Gateway</th>
                    <th class="pb-2 font-medium">Pago em</th>
                    <th class="pb-2 font-medium">Criado em</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr
                      v-for="payment in sub.payments"
                      :key="payment.id"
                      class="border-b border-gray-50"
                  >
                    <td class="py-1.5 pr-4 font-medium">
                      {{ formatPrice(payment.amount) }}
                      <span class="text-xs text-gray-400 font-normal">{{ payment.currency?.toUpperCase() }}</span>
                    </td>
                    <td class="py-1.5 pr-4">
                        <span :class="['text-xs px-2 py-0.5 font-medium', paymentStatusColors[payment.status] || 'bg-gray-100 text-gray-500']">
                          {{ payment.status }}
                        </span>
                    </td>
                    <td class="py-1.5 pr-4 text-gray-500 text-xs">{{ payment.gateway }}</td>
                    <td class="py-1.5 pr-4 text-gray-500 text-xs">{{ formatDateTime(payment.paidAt) }}</td>
                    <td class="py-1.5 text-gray-500 text-xs">{{ formatDateTime(payment.createdAt) }}</td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>