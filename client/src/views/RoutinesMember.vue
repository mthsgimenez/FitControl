<script setup>
import {ref, onMounted, computed} from 'vue'
import api from '@/api/axios.js'
import { useAuthStore } from '@/stores/authStore.js'

const auth = useAuthStore()

const templates = ref([])
const allExercises = ref([])
const routines = ref([])
const loading = ref(false)
const error = ref(null)

const showModal = ref(false)
const showPreviewModal = ref(false)
const editingRoutine = ref(null)
const previewRoutine = ref(null)
const deleteConfirmId = ref(null)

const form = ref({ name: '', days: [] })

const currentUserUuid = computed(() => {
  if (!auth.accessToken) return null
  return JSON.parse(atob(auth.accessToken.split('.')[1])).sub
})

function canModify(routine) {
  return routine.createdByUserId === currentUserUuid.value
}

async function fetchTemplates() {
  try {
    const { data } = await api.get('/routine-template')
    templates.value = data
  } catch (e) {}
}

async function fetchExercises() {
  try {
    const { data: cats } = await api.get('/category')
    const results = await Promise.all(
        cats.map(async cat => {
          const { data: exs } = await api.get(`/category/${cat.id}/exercise`)
          return { ...cat, exercises: exs }
        })
    )
    allExercises.value = results.filter(c => c.exercises.length > 0)
  } catch (e) {}
}

async function fetchMyRoutines() {
  loading.value = true
  try {
    let memberId = auth.memberId
    if (!memberId) {
      const { data } = await api.get('/member/me')
      auth.memberId = data.id
      localStorage.setItem('memberId', data.id)
      memberId = data.id
    }
    const { data } = await api.get(`/routine/member/${memberId}`)
    routines.value = data
  } catch (e) {
    console.log('full error:', e)
    error.value = 'Erro ao carregar fichas'
  } finally {
    loading.value = false
  }
}

function openCreateModal() {
  editingRoutine.value = null
  form.value = { name: '', days: [{ exercises: [] }] }
  error.value = null
  showModal.value = true
}

async function openEditModal(routine) {
  error.value = null
  try {
    const { data } = await api.get(`/routine/${routine.id}`)
    editingRoutine.value = data
    form.value = {
      name: data.name,
      days: data.days.map(day => ({
        exercises: day.exercises.map(ex => ({
          exerciseId: ex.exerciseId,
          reps: ex.reps,
          series: ex.series,
          notes: ex.notes || ''
        }))
      }))
    }
    showModal.value = true
  } catch (e) {
    error.value = 'Erro ao carregar ficha'
  }
}

async function openPreviewModal(routine) {
  previewRoutine.value = null
  showPreviewModal.value = true
  try {
    const { data } = await api.get(`/routine/${routine.id}`)
    previewRoutine.value = data
  } catch (e) {
    showPreviewModal.value = false
  }
}

async function applyTemplate(templateId) {
  if (!templateId) return
  try {
    const { data } = await api.get(`/routine-template/${templateId}`)
    form.value.name = form.value.name || data.name
    form.value.days = data.days.map(day => ({
      exercises: day.exercises.map(ex => ({
        exerciseId: ex.exerciseId,
        reps: null,
        series: null,
        notes: ''
      }))
    }))
  } catch (e) {}
}

function addDay() { form.value.days.push({ exercises: [] }) }
function removeDay(i) { form.value.days.splice(i, 1) }
function addExercise(i) { form.value.days[i].exercises.push({ exerciseId: null, reps: null, series: null, notes: '' }) }
function removeExercise(di, ei) { form.value.days[di].exercises.splice(ei, 1) }

async function submitForm() {
  error.value = null
  try {
    const payload = {
      name: form.value.name,
      memberId: auth.memberId,
      days: form.value.days.map(day => ({
        exercises: day.exercises
            .filter(ex => ex.exerciseId)
            .map(ex => ({
              exerciseId: ex.exerciseId,
              reps: ex.reps ? parseInt(ex.reps) : null,
              series: ex.series ? parseInt(ex.series) : null,
              notes: ex.notes || null
            }))
      }))
    }
    if (editingRoutine.value) {
      await api.put(`/routine/${editingRoutine.value.id}`, payload)
    } else {
      await api.post('/routine', payload)
    }
    showModal.value = false
    await fetchMyRoutines()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao salvar ficha'
  }
}

async function deleteRoutine(id) {
  try {
    await api.delete(`/routine/${id}`)
    deleteConfirmId.value = null
    await fetchMyRoutines()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao remover ficha'
  }
}

onMounted(async () => {
  await Promise.all([fetchTemplates(), fetchExercises(), fetchMyRoutines()])
})
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-black">Minhas Fichas</h1>
      <button
          @click="openCreateModal"
          class="px-4 py-2 bg-yellow-400 hover:bg-yellow-500 text-black font-bold text-sm"
      >
        + Nova Ficha
      </button>
    </div>

    <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

    <div class="bg-white border border-gray-200">
      <div v-if="loading" class="p-8 text-center text-gray-400">Carregando...</div>
      <div v-else-if="routines.length === 0" class="p-8 text-center text-gray-400 text-sm">
        Nenhuma ficha cadastrada
      </div>
      <div v-else>
        <div
            v-for="routine in routines"
            :key="routine.id"
            @click="openPreviewModal(routine)"
            class="flex items-center justify-between px-4 py-3 border-b border-gray-100 hover:bg-gray-50 cursor-pointer"
        >
          <span class="text-sm font-medium">{{ routine.name }}</span>
          <div class="flex gap-2" @click.stop>
            <button v-if="canModify(routine)"
                @click="openEditModal(routine)"
                class="text-xs px-3 py-1 border border-gray-300 hover:bg-gray-100 font-medium"
            >
              Editar
            </button>
            <button
                v-if="canModify(routine) && deleteConfirmId !== routine.id"
                @click="deleteConfirmId = routine.id"
                class="text-xs px-3 py-1 border border-red-200 text-red-500 hover:bg-red-50 font-medium"
            >
              Remover
            </button>
            <template v-if="canModify(routine) && deleteConfirmId === routine.id">
              <button
                  @click="deleteRoutine(routine.id)"
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
        </div>
      </div>
    </div>

    <!-- Preview Modal -->
    <div v-if="showPreviewModal" class="fixed inset-0 bg-gray-100 bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-xl p-6 shadow-lg max-h-[90vh] overflow-y-auto">
        <div v-if="!previewRoutine" class="py-8 text-center text-gray-400">Carregando...</div>
        <template v-else>
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-lg font-bold">{{ previewRoutine.name }}</h2>
            <span class="text-xs text-gray-400">{{ previewRoutine.days?.length }} dias</span>
          </div>
          <div class="space-y-4">
            <div v-for="(day, i) in previewRoutine.days" :key="i" class="border border-gray-200 p-4">
              <p class="text-sm font-semibold text-gray-700 mb-3">Dia {{ i + 1 }}</p>
              <table class="w-full text-sm">
                <thead>
                <tr class="text-left text-xs text-gray-400 border-b border-gray-100">
                  <th class="pb-2 font-medium">Exercício</th>
                  <th class="pb-2 font-medium">Séries</th>
                  <th class="pb-2 font-medium">Reps</th>
                  <th class="pb-2 font-medium">Obs</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="ex in day.exercises" :key="ex.exerciseId" class="border-b border-gray-50">
                  <td class="py-1.5 pr-4 font-medium">{{ ex.exerciseName }}</td>
                  <td class="py-1.5 pr-4 text-gray-500">{{ ex.series ?? '—' }}</td>
                  <td class="py-1.5 pr-4 text-gray-500">{{ ex.reps ?? '—' }}</td>
                  <td class="py-1.5 text-gray-400 text-xs">{{ ex.notes || '—' }}</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </template>
        <div class="flex gap-3 mt-6">
          <button @click="showPreviewModal = false" class="w-full py-2 border border-gray-300 text-sm font-medium hover:bg-gray-50">
            Fechar
          </button>
          <button
              v-if="previewRoutine && canModify(previewRoutine)"
              @click="showPreviewModal = false; openEditModal(previewRoutine)"
              class="w-full py-2 bg-yellow-400 hover:bg-yellow-500 text-black text-sm font-bold"
          >
            Editar
          </button>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-gray-100 bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-2xl p-6 shadow-lg max-h-[90vh] overflow-y-auto">
        <h2 class="text-lg font-bold mb-4">{{ editingRoutine ? 'Editar Ficha' : 'Nova Ficha' }}</h2>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">Nome</label>
          <input
              v-model="form.name"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              placeholder="Ex: Push Pull Legs"
          />
        </div>

        <div class="mb-6 p-3 bg-gray-50 border border-gray-200">
          <label class="block text-sm font-medium text-gray-700 mb-2">
            Usar template como base
            <span class="text-gray-400 font-normal">(opcional — substitui os dias)</span>
          </label>
          <select
              @change="e => applyTemplate(e.target.value)"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm bg-white"
          >
            <option value="">Selecione um template...</option>
            <option v-for="t in templates" :key="t.id" :value="t.id">{{ t.name }}</option>
          </select>
        </div>

        <div class="space-y-4 mb-4">
          <div v-for="(day, dayIndex) in form.days" :key="dayIndex" class="border border-gray-200 p-3">
            <div class="flex items-center justify-between mb-3">
              <p class="text-sm font-semibold">Dia {{ dayIndex + 1 }}</p>
              <button @click="removeDay(dayIndex)" class="text-xs text-red-400 hover:text-red-600">Remover dia</button>
            </div>
            <div class="space-y-2">
              <div v-for="(ex, exIndex) in day.exercises" :key="exIndex" class="grid grid-cols-12 gap-2 items-center">
                <select v-model="ex.exerciseId" class="col-span-5 px-3 py-1.5 border border-gray-300 focus:outline-none focus:border-black text-sm bg-white">
                  <option :value="null">Exercício...</option>
                  <optgroup v-for="cat in allExercises" :key="cat.id" :label="cat.name">
                    <option v-for="exercise in cat.exercises" :key="exercise.id" :value="exercise.id">{{ exercise.name }}</option>
                  </optgroup>
                </select>
                <input v-model="ex.series" type="number" min="1" placeholder="Séries" class="col-span-2 px-2 py-1.5 border border-gray-300 focus:outline-none focus:border-black text-sm" />
                <input v-model="ex.reps" type="number" min="1" placeholder="Reps" class="col-span-2 px-2 py-1.5 border border-gray-300 focus:outline-none focus:border-black text-sm" />
                <input v-model="ex.notes" type="text" placeholder="Obs" class="col-span-2 px-2 py-1.5 border border-gray-300 focus:outline-none focus:border-black text-sm" />
                <button @click="removeExercise(dayIndex, exIndex)" class="col-span-1 text-xs text-red-400 hover:text-red-600 text-center">✕</button>
              </div>
            </div>
            <button @click="addExercise(dayIndex)" class="mt-2 text-xs text-blue-500 hover:text-blue-700 font-medium">+ Exercício</button>
          </div>
        </div>

        <button @click="addDay" class="w-full py-2 border border-dashed border-gray-300 text-sm text-gray-500 hover:bg-gray-50 mb-4">
          + Adicionar Dia
        </button>

        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

        <div class="flex gap-3">
          <button @click="showModal = false" class="w-full py-2 border border-gray-300 text-sm font-medium hover:bg-gray-50">Cancelar</button>
          <button @click="submitForm" class="w-full py-2 bg-yellow-400 hover:bg-yellow-500 text-black text-sm font-bold">
            {{ editingRoutine ? 'Salvar' : 'Criar' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>