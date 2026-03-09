<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/axios.js'

const members = ref([])
const loading = ref(false)
const error = ref(null)
const deleteConfirmId = ref(null)
const editingMember = ref(null)
const showCreateModal = ref(false)
const showEditModal = ref(false)

const trainingLevels = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED']
const trainingLevelLabels = {
  BEGINNER: 'Iniciante',
  INTERMEDIATE: 'Intermediário',
  ADVANCED: 'Avançado'
}

const createForm = ref({
  email: '',
  goal: '',
  trainingLevel: '',
  restrictions: '',
  personId: null,
  useExisting: false,
  person: { name: '', lastName: '', cpf: '', birthDate: '' }
})

const editForm = ref({
  goal: '',
  trainingLevel: '',
  restrictions: ''
})

async function fetchMembers() {
  loading.value = true
  error.value = null
  try {
    const { data } = await api.get('/member')
    members.value = data
  } catch (e) {
    error.value = 'Erro ao carregar membros'
  } finally {
    loading.value = false
  }
}

function openCreateModal() {
  createForm.value = {
    email: '',
    goal: '',
    trainingLevel: '',
    restrictions: '',
    personId: null,
    useExisting: false,
    person: { name: '', lastName: '', cpf: '', birthDate: '' }
  }
  error.value = null
  showCreateModal.value = true
}

function openEditModal(member) {
  editingMember.value = member
  editForm.value = {
    goal: member.goal || '',
    trainingLevel: member.trainingLevel || '',
    restrictions: member.restrictions || ''
  }
  error.value = null
  showEditModal.value = true
}

async function submitCreate() {
  error.value = null
  try {
    const payload = {
      goal: createForm.value.goal,
      trainingLevel: createForm.value.trainingLevel,
      restrictions: createForm.value.restrictions
    }
    if (createForm.value.useExisting) {
      payload.personId = createForm.value.personId
    } else {
      payload.email = createForm.value.email
      payload.person = createForm.value.person
    }
    await api.post('/member', payload)
    showCreateModal.value = false
    await fetchMembers()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao criar membro'
  }
}

async function submitEdit() {
  error.value = null
  try {
    await api.put(`/member/${editingMember.value.id}`, editForm.value)
    showEditModal.value = false
    await fetchMembers()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao atualizar membro'
  }
}

async function deleteMember(id) {
  try {
    await api.delete(`/member/${id}`)
    deleteConfirmId.value = null
    await fetchMembers()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao remover membro'
  }
}

onMounted(fetchMembers)
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-black">Membros</h1>
      <button
          @click="openCreateModal"
          class="px-4 py-2 bg-yellow-400 hover:bg-yellow-500 text-black font-bold text-sm"
      >
        + Novo Membro
      </button>
    </div>

    <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

    <!-- Table -->
    <div class="bg-white border border-gray-200">
      <div v-if="loading" class="p-8 text-center text-gray-400">Carregando...</div>
      <div v-else-if="members.length === 0" class="p-8 text-center text-gray-400">
        Nenhum membro cadastrado
      </div>
      <table v-else class="w-full text-sm">
        <thead class="border-b border-gray-200">
        <tr class="text-left">
          <th class="px-4 py-3 font-semibold text-gray-600">Nome</th>
          <th class="px-4 py-3 font-semibold text-gray-600">E-mail</th>
          <th class="px-4 py-3 font-semibold text-gray-600">Nível</th>
          <th class="px-4 py-3 font-semibold text-gray-600">Objetivo</th>
          <th class="px-4 py-3 font-semibold text-gray-600">Restrições</th>
          <th class="px-4 py-3 font-semibold text-gray-600">Ações</th>
        </tr>
        </thead>
        <tbody>
        <tr
            v-for="member in members"
            :key="member.id"
            class="border-b border-gray-100 hover:bg-gray-50"
        >
          <td class="px-4 py-3 font-medium">{{ member.person.name }} {{ member.person.lastName }}</td>
          <td class="px-4 py-3 text-gray-500">{{ member.person.user.email }}</td>
          <td class="px-4 py-3">
              <span v-if="member.trainingLevel" class="px-2 py-0.5 bg-yellow-100 text-yellow-800 text-xs font-medium">
                {{ trainingLevelLabels[member.trainingLevel] || member.trainingLevel }}
              </span>
            <span v-else class="text-gray-400 text-xs">—</span>
          </td>
          <td class="px-4 py-3 text-gray-500 max-w-32 truncate">{{ member.goal || '—' }}</td>
          <td class="px-4 py-3 text-gray-500 max-w-32 truncate">{{ member.restrictions || '—' }}</td>
          <td class="px-4 py-3">
            <div class="flex gap-2">
              <button
                  @click="openEditModal(member)"
                  class="text-xs px-3 py-1 border border-gray-300 hover:bg-gray-50 font-medium"
              >
                Editar
              </button>
              <button
                  v-if="deleteConfirmId !== member.id"
                  @click="deleteConfirmId = member.id"
                  class="text-xs px-3 py-1 border border-red-200 text-red-500 hover:bg-red-50 font-medium"
              >
                Remover
              </button>
              <template v-else>
                <button
                    @click="deleteMember(member.id)"
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

    <!-- Create Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-gray-100 bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-md p-6 shadow-lg max-h-screen overflow-y-auto">
        <h2 class="text-lg font-bold mb-4">Novo Membro</h2>

        <div class="mb-4">
          <label class="flex items-center gap-2 text-sm font-medium text-gray-700 cursor-pointer">
            <input type="checkbox" v-model="createForm.useExisting" />
            Usar pessoa existente
          </label>
        </div>

        <template v-if="createForm.useExisting">
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-2">ID da Pessoa</label>
            <input
                v-model="createForm.personId"
                type="number"
                class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
            />
          </div>
        </template>

        <template v-else>
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-2">E-mail</label>
            <input
                v-model="createForm.email"
                type="email"
                class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
            />
          </div>
          <div class="grid grid-cols-2 gap-3 mb-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Nome</label>
              <input
                  v-model="createForm.person.name"
                  type="text"
                  class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Sobrenome</label>
              <input
                  v-model="createForm.person.lastName"
                  type="text"
                  class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-3 mb-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">CPF</label>
              <input
                  v-model="createForm.person.cpf"
                  type="text"
                  maxlength="11"
                  class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Nascimento</label>
              <input
                  v-model="createForm.person.birthDate"
                  type="date"
                  class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              />
            </div>
          </div>
        </template>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">Nível de Treino</label>
          <select
              v-model="createForm.trainingLevel"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm bg-white"
          >
            <option value="">Selecione...</option>
            <option v-for="level in trainingLevels" :key="level" :value="level">
              {{ trainingLevelLabels[level] }}
            </option>
          </select>
        </div>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">Objetivo</label>
          <input
              v-model="createForm.goal"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              placeholder="Ex: Hipertrofia, emagrecimento..."
          />
        </div>

        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">Restrições</label>
          <input
              v-model="createForm.restrictions"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              placeholder="Ex: Problema no joelho..."
          />
        </div>

        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

        <div class="flex gap-3">
          <button
              @click="showCreateModal = false"
              class="w-full py-2 border border-gray-300 text-sm font-medium hover:bg-gray-50"
          >
            Cancelar
          </button>
          <button
              @click="submitCreate"
              class="w-full py-2 bg-yellow-400 hover:bg-yellow-500 text-black text-sm font-bold"
          >
            Criar
          </button>
        </div>
      </div>
    </div>

    <!-- Edit Modal -->
    <div v-if="showEditModal" class="fixed inset-0 bg-gray-100 bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-sm p-6 shadow-lg">
        <h2 class="text-lg font-bold mb-2">Editar Membro</h2>
        <p class="text-sm text-gray-500 mb-4">
          {{ editingMember?.person.name }} {{ editingMember?.person.lastName }}
        </p>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">Nível de Treino</label>
          <select
              v-model="editForm.trainingLevel"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm bg-white"
          >
            <option value="">Selecione...</option>
            <option v-for="level in trainingLevels" :key="level" :value="level">
              {{ trainingLevelLabels[level] }}
            </option>
          </select>
        </div>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">Objetivo</label>
          <input
              v-model="editForm.goal"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
          />
        </div>

        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">Restrições</label>
          <input
              v-model="editForm.restrictions"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
          />
        </div>

        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

        <div class="flex gap-3">
          <button
              @click="showEditModal = false"
              class="w-full py-2 border border-gray-300 text-sm font-medium hover:bg-gray-50"
          >
            Cancelar
          </button>
          <button
              @click="submitEdit"
              class="w-full py-2 bg-yellow-400 hover:bg-yellow-500 text-black text-sm font-bold"
          >
            Salvar
          </button>
        </div>
      </div>
    </div>

  </div>
</template>