<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/axios.js'

const employees = ref([])
const loading = ref(false)
const error = ref(null)
const showModal = ref(false)
const showRolesModal = ref(false)
const deleteConfirmId = ref(null)
const editingEmployee = ref(null)

const availableRoles = ['ROLE_MANAGER', 'ROLE_FINANCE', 'ROLE_INSTRUCTOR']

const roleLabels = {
  ROLE_MANAGER: 'Gestor',
  ROLE_FINANCE: 'Financeiro',
  ROLE_INSTRUCTOR: 'Instrutor',
  MANAGER: 'Gestor',
  FINANCE: 'Financeiro',
  INSTRUCTOR: 'Instrutor'
}

const form = ref({
  email: '',
  admissionDate: '',
  personId: null,
  useExisting: false,
  person: {
    name: '',
    lastName: '',
    cpf: '',
    birthDate: ''
  }
})

const rolesForm = ref({
  employeeId: null,
  roles: []
})

async function fetchEmployees() {
  loading.value = true
  try {
    const { data } = await api.get('/employee')
    employees.value = data
  } catch (e) {
    error.value = 'Erro ao carregar funcionários'
  } finally {
    loading.value = false
  }
}

function openCreateModal() {
  form.value = {
    email: '',
    admissionDate: '',
    personId: null,
    useExisting: false,
    person: { name: '', lastName: '', cpf: '', birthDate: '' }
  }
  showModal.value = true
}

function openRolesModal(employee) {
  editingEmployee.value = employee
  rolesForm.value = {
    employeeId: employee.id,
    roles: employee.person.user.roles
        ?.filter(r => availableRoles.includes(`ROLE_${r}`))
        .map(r => `ROLE_${r}`) || []
  }
  showRolesModal.value = true
}

async function submitCreate() {
  error.value = null
  try {
    const payload = {
      email: form.value.email,
      admissionDate: form.value.admissionDate,
      roles: []
    }
    if (form.value.useExisting) {
      payload.personId = form.value.personId
    } else {
      payload.person = form.value.person
    }
    await api.post('/employee', payload)
    showModal.value = false
    await fetchEmployees()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao criar funcionário'
  }
}

async function submitRoles() {
  error.value = null
  try {
    await api.put(`/employee/${rolesForm.value.employeeId}`, {
      roles: rolesForm.value.roles.map(r => r.replace('ROLE_', ''))
    })
    showRolesModal.value = false
    await fetchEmployees()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao atualizar cargos'
  }
}

async function deleteEmployee(id) {
  try {
    await api.delete(`/employee/${id}`)
    deleteConfirmId.value = null
    await fetchEmployees()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao remover funcionário'
  }
}

function toggleRole(role) {
  const idx = rolesForm.value.roles.indexOf(role)
  if (idx === -1) {
    rolesForm.value.roles.push(role)
  } else {
    rolesForm.value.roles.splice(idx, 1)
  }
}

onMounted(fetchEmployees)
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-black">Funcionários</h1>
      <button
          @click="openCreateModal"
          class="px-4 py-2 bg-yellow-400 hover:bg-yellow-500 text-black font-bold text-sm"
      >
        + Novo Funcionário
      </button>
    </div>

    <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

    <!-- Table -->
    <div class="bg-white border border-gray-200">
      <div v-if="loading" class="p-8 text-center text-gray-400">Carregando...</div>
      <div v-else-if="employees.length === 0" class="p-8 text-center text-gray-400">
        Nenhum funcionário cadastrado
      </div>
      <table v-else class="w-full text-sm">
        <thead class="border-b border-gray-200">
        <tr class="text-left">
          <th class="px-4 py-3 font-semibold text-gray-600">Nome</th>
          <th class="px-4 py-3 font-semibold text-gray-600">E-mail</th>
          <th class="px-4 py-3 font-semibold text-gray-600">Admissão</th>
          <th class="px-4 py-3 font-semibold text-gray-600">Cargos</th>
          <th class="px-4 py-3 font-semibold text-gray-600">Ações</th>
        </tr>
        </thead>
        <tbody>
        <tr
            v-for="emp in employees"
            :key="emp.id"
            class="border-b border-gray-100 hover:bg-gray-50"
        >
          <td class="px-4 py-3 font-medium">{{ emp.person.name }} {{ emp.person.lastName }}</td>
          <td class="px-4 py-3 text-gray-500">{{ emp.person.user.email }}</td>
          <td class="px-4 py-3 text-gray-500">{{ emp.admissionDate }}</td>
          <td class="px-4 py-3">
            <div class="flex flex-wrap gap-1">
                <span
                    v-for="role in emp.person.user.roles"
                    :key="role.name"
                    class="px-2 py-0.5 bg-yellow-100 text-yellow-800 text-xs font-medium"
                >
                  {{ roleLabels[role.name] || role.name }}
                </span>
              <span v-if="!emp.person.user.roles?.length" class="text-gray-400 text-xs">Sem cargos</span>
            </div>
          </td>
          <td class="px-4 py-3">
            <div class="flex gap-2">
              <button
                  @click="openRolesModal(emp)"
                  class="text-xs px-3 py-1 border border-gray-300 hover:bg-gray-50 font-medium"
              >
                Cargos
              </button>
              <button
                  v-if="deleteConfirmId !== emp.id"
                  @click="deleteConfirmId = emp.id"
                  class="text-xs px-3 py-1 border border-red-200 text-red-500 hover:bg-red-50 font-medium"
              >
                Remover
              </button>
              <template v-else>
                <button
                    @click="deleteEmployee(emp.id)"
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
    <div v-if="showModal" class="fixed inset-0 bg-gray-100 bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-md p-6 shadow-lg">
        <h2 class="text-lg font-bold mb-4">Novo Funcionário</h2>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">Data de Admissão</label>
          <input
              v-model="form.admissionDate"
              type="date"
              required
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
          />
        </div>

        <div class="mb-4">
          <label class="flex items-center gap-2 text-sm font-medium text-gray-700 cursor-pointer">
            <input type="checkbox" v-model="form.useExisting" class="rounded" />
            Usar pessoa existente
          </label>
        </div>

        <template v-if="form.useExisting">
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-2">ID da Pessoa</label>
            <input
                v-model="form.personId"
                type="number"
                class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
            />
          </div>
        </template>

        <template v-else>
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-2">E-mail</label>
            <input
                v-model="form.email"
                type="email"
                required
                class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
            />
          </div>

          <div class="grid grid-cols-2 gap-3 mb-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Nome</label>
              <input
                  v-model="form.person.name"
                  type="text"
                  class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Sobrenome</label>
              <input
                  v-model="form.person.lastName"
                  type="text"
                  class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-3 mb-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">CPF</label>
              <input
                  v-model="form.person.cpf"
                  type="text"
                  maxlength="11"
                  class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Nascimento</label>
              <input
                  v-model="form.person.birthDate"
                  type="date"
                  class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              />
            </div>
          </div>
        </template>

        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

        <div class="flex gap-3">
          <button
              @click="showModal = false"
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

    <!-- Roles Modal -->
    <div v-if="showRolesModal" class="fixed inset-0 bg-gray-100 bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-sm p-6 shadow-lg">
        <h2 class="text-lg font-bold mb-2">Cargos</h2>
        <p class="text-sm text-gray-500 mb-4">
          {{ editingEmployee?.person.name }} {{ editingEmployee?.person.lastName }}
        </p>

        <div class="space-y-2 mb-6">
          <label
              v-for="role in availableRoles"
              :key="role"
              class="flex items-center gap-3 p-3 border border-gray-200 cursor-pointer hover:bg-gray-50"
          >
            <input
                type="checkbox"
                :checked="rolesForm.roles.includes(role)"
                @change="toggleRole(role)"
            />
            <span class="text-sm font-medium">{{ roleLabels[role] }}</span>
          </label>
        </div>

        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

        <div class="flex gap-3">
          <button
              @click="showRolesModal = false"
              class="w-full py-2 border border-gray-300 text-sm font-medium hover:bg-gray-50"
          >
            Cancelar
          </button>
          <button
              @click="submitRoles"
              class="w-full py-2 bg-yellow-400 hover:bg-yellow-500 text-black text-sm font-bold"
          >
            Salvar
          </button>
        </div>
      </div>
    </div>

  </div>
</template>