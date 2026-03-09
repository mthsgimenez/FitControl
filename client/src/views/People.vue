<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/axios.js'

const people = ref([])
const loading = ref(false)
const error = ref(null)
const searchCpf = ref('')
const deleteConfirmId = ref(null)
const editingPerson = ref(null)
const showEditModal = ref(false)

const editForm = ref({
  name: '',
  lastName: ''
})

async function fetchPeople(cpf = null) {
  loading.value = true
  error.value = null
  try {
    const params = cpf ? { cpf } : {}
    const { data } = await api.get('/person', { params })
    people.value = Array.isArray(data) ? data : [data]
  } catch (e) {
    error.value = 'Erro ao carregar pessoas'
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  if (searchCpf.value.trim()) {
    fetchPeople(searchCpf.value.trim())
  } else {
    fetchPeople()
  }
}

function clearSearch() {
  searchCpf.value = ''
  fetchPeople()
}

function openEditModal(person) {
  editingPerson.value = person
  editForm.value = {
    name: person.name,
    lastName: person.lastName
  }
  showEditModal.value = true
}

async function submitEdit() {
  error.value = null
  try {
    await api.put(`/person/${editingPerson.value.id}`, editForm.value)
    showEditModal.value = false
    await fetchPeople()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao atualizar pessoa'
  }
}

async function deletePerson(id) {
  try {
    await api.delete(`/person/${id}`)
    deleteConfirmId.value = null
    await fetchPeople()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao remover pessoa'
  }
}

onMounted(fetchPeople)
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-black">Pessoas</h1>
    </div>

    <!-- Search -->
    <div class="flex gap-2 mb-4">
      <input
          v-model="searchCpf"
          type="text"
          placeholder="Buscar por CPF..."
          maxlength="11"
          @keyup.enter="handleSearch"
          class="px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm w-64"
      />
      <button
          @click="handleSearch"
          class="px-4 py-2 bg-yellow-400 hover:bg-yellow-500 text-black text-sm font-bold"
      >
        Buscar
      </button>
      <button
          v-if="searchCpf"
          @click="clearSearch"
          class="px-4 py-2 border border-gray-300 hover:bg-gray-50 text-sm font-medium"
      >
        Limpar
      </button>
    </div>

    <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

    <!-- Table -->
    <div class="bg-white border border-gray-200">
      <div v-if="loading" class="p-8 text-center text-gray-400">Carregando...</div>
      <div v-else-if="people.length === 0" class="p-8 text-center text-gray-400">
        Nenhuma pessoa encontrada
      </div>
      <table v-else class="w-full text-sm">
        <thead class="border-b border-gray-200">
        <tr class="text-left">
          <th class="px-4 py-3 font-semibold text-gray-600">Id</th>
          <th class="px-4 py-3 font-semibold text-gray-600">Nome</th>
          <th class="px-4 py-3 font-semibold text-gray-600">CPF</th>
          <th class="px-4 py-3 font-semibold text-gray-600">Nascimento</th>
          <th class="px-4 py-3 font-semibold text-gray-600">Ações</th>
        </tr>
        </thead>
        <tbody>
        <tr
            v-for="person in people"
            :key="person.id"
            class="border-b border-gray-100 hover:bg-gray-50"
        >
          <td class="px-4 py-3 font-medium">{{ person.id }}</td>
          <td class="px-4 py-3 font-medium">{{ person.name }} {{ person.lastName }}</td>
          <td class="px-4 py-3 text-gray-500">{{ person.cpf || '—' }}</td>
          <td class="px-4 py-3 text-gray-500">{{ person.birthDate || '—' }}</td>
          <td class="px-4 py-3">
            <div class="flex gap-2">
              <button
                  @click="openEditModal(person)"
                  class="text-xs px-3 py-1 border border-gray-300 hover:bg-gray-50 font-medium"
              >
                Editar
              </button>
              <button
                  v-if="deleteConfirmId !== person.id"
                  @click="deleteConfirmId = person.id"
                  class="text-xs px-3 py-1 border border-red-200 text-red-500 hover:bg-red-50 font-medium"
              >
                Remover
              </button>
              <template v-else>
                <button
                    @click="deletePerson(person.id)"
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

    <!-- Edit Modal -->
    <div v-if="showEditModal" class="fixed inset-0 bg-gray-100 bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-sm p-6 shadow-lg">
        <h2 class="text-lg font-bold mb-4">Editar Pessoa</h2>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">Nome</label>
          <input
              v-model="editForm.name"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
          />
        </div>

        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">Sobrenome</label>
          <input
              v-model="editForm.lastName"
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