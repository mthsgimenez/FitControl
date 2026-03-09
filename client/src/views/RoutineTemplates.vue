<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '@/api/axios.js'

const templates = ref([])
const categories = ref([])
const allExercises = ref([])
const loading = ref(false)
const error = ref(null)

const showModal = ref(false)
const showPreviewModal = ref(false)
const editingTemplate = ref(null)
const previewTemplate = ref(null)
const deleteConfirmId = ref(null)

const form = ref({
  name: '',
  days: []
})

const exerciseOptions = computed(() =>
    allExercises.value.flatMap(cat =>
        cat.exercises.map(ex => ({ ...ex, categoryName: cat.name }))
    )
)

async function fetchTemplates() {
  loading.value = true
  error.value = null
  try {
    const { data } = await api.get('/routine-template')
    templates.value = data
  } catch (e) {
    error.value = 'Erro ao carregar templates'
  } finally {
    loading.value = false
  }
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

async function fetchFullTemplate(id) {
  const { data } = await api.get(`/routine-template/${id}`)
  return data
}

function openCreateModal() {
  editingTemplate.value = null
  form.value = { name: '', days: [{ exercises: [] }] }
  error.value = null
  showModal.value = true
}

async function openEditModal(templateSummary) {
  error.value = null
  try {
    const data = await fetchFullTemplate(templateSummary.id)
    editingTemplate.value = data
    form.value = {
      name: data.name,
      days: data.days.map(day => ({
        exercises: day.exercises.map(ex => ({ exerciseId: ex.exerciseId }))
      }))
    }
    showModal.value = true
  } catch (e) {
    error.value = 'Erro ao carregar template'
  }
}

async function openPreviewModal(templateSummary) {
  error.value = null
  previewTemplate.value = null
  showPreviewModal.value = true
  try {
    previewTemplate.value = await fetchFullTemplate(templateSummary.id)
  } catch (e) {
    error.value = 'Erro ao carregar template'
    showPreviewModal.value = false
  }
}

function addDay() {
  form.value.days.push({ exercises: [] })
}

function removeDay(dayIndex) {
  form.value.days.splice(dayIndex, 1)
}

function addExerciseToDay(dayIndex) {
  form.value.days[dayIndex].exercises.push({ exerciseId: null })
}

function removeExerciseFromDay(dayIndex, exIndex) {
  form.value.days[dayIndex].exercises.splice(exIndex, 1)
}

async function submitForm() {
  error.value = null
  try {
    const payload = {
      name: form.value.name,
      days: form.value.days.map(day => ({
        exercises: day.exercises
            .filter(ex => ex.exerciseId)
            .map(ex => ({ exerciseId: ex.exerciseId }))
      }))
    }
    if (editingTemplate.value) {
      await api.put(`/routine-template/${editingTemplate.value.id}`, payload)
    } else {
      await api.post('/routine-template', payload)
    }
    showModal.value = false
    await fetchTemplates()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao salvar template'
  }
}

async function deleteTemplate(id) {
  try {
    await api.delete(`/routine-template/${id}`)
    deleteConfirmId.value = null
    await fetchTemplates()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao remover template'
  }
}

onMounted(async () => {
  await Promise.all([fetchTemplates(), fetchExercises()])
})
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-black">Templates de Fichas</h1>
      <button
          @click="openCreateModal"
          class="px-4 py-2 bg-yellow-400 hover:bg-yellow-500 text-black font-bold text-sm"
      >
        + Novo Template
      </button>
    </div>

    <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

    <div v-if="loading" class="p-8 text-center text-gray-400">Carregando...</div>

    <div v-else-if="templates.length === 0" class="p-8 text-center text-gray-400 bg-white border border-gray-200">
      Nenhum template cadastrado
    </div>

    <div v-else class="grid grid-cols-1 gap-3">
      <div
          v-for="template in templates"
          :key="template.id"
          @click="openPreviewModal(template)"
          class="bg-white border border-gray-200 px-4 py-3 flex items-center justify-between cursor-pointer hover:bg-gray-50"
      >
        <span class="font-medium text-black">{{ template.name }}</span>
        <div class="flex gap-2" @click.stop>
          <button
              @click="openEditModal(template)"
              class="text-xs px-3 py-1 border border-gray-300 hover:bg-gray-100 font-medium"
          >
            Editar
          </button>
          <button
              v-if="deleteConfirmId !== template.id"
              @click="deleteConfirmId = template.id"
              class="text-xs px-3 py-1 border border-red-200 text-red-500 hover:bg-red-50 font-medium"
          >
            Remover
          </button>
          <template v-else>
            <button
                @click="deleteTemplate(template.id)"
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

    <!-- Preview Modal -->
    <div v-if="showPreviewModal" class="fixed inset-0 bg-gray-100 bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-xl p-6 shadow-lg max-h-[90vh] overflow-y-auto">

        <div v-if="!previewTemplate" class="py-8 text-center text-gray-400">Carregando...</div>

        <template v-else>
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-lg font-bold">{{ previewTemplate.name }}</h2>
            <span class="text-xs text-gray-400">{{ previewTemplate.days?.length }} dias</span>
          </div>

          <div class="space-y-4">
            <div
                v-for="(day, i) in previewTemplate.days"
                :key="i"
                class="border border-gray-200 p-4"
            >
              <p class="text-sm font-semibold text-gray-700 mb-3">Dia {{ i + 1 }}</p>
              <ul class="space-y-1">
                <li
                    v-for="ex in day.exercises"
                    :key="ex.exerciseId"
                    class="text-sm text-gray-600 flex items-center gap-2"
                >
                  <span class="w-1.5 h-1.5 rounded-full bg-yellow-400 shrink-0"></span>
                  {{ ex.exerciseName }}
                </li>
              </ul>
            </div>
          </div>
        </template>

        <div class="flex gap-3 mt-6">
          <button
              @click="showPreviewModal = false"
              class="w-full py-2 border border-gray-300 text-sm font-medium hover:bg-gray-50"
          >
            Fechar
          </button>
          <button
              v-if="previewTemplate"
              @click="showPreviewModal = false; openEditModal(previewTemplate)"
              class="w-full py-2 bg-yellow-400 hover:bg-yellow-500 text-black text-sm font-bold"
          >
            Editar
          </button>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-gray-100 bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-xl p-6 shadow-lg max-h-[90vh] overflow-y-auto">
        <h2 class="text-lg font-bold mb-4">
          {{ editingTemplate ? 'Editar Template' : 'Novo Template' }}
        </h2>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">Nome</label>
          <input
              v-model="form.name"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              placeholder="Ex: Push Pull Legs"
          />
        </div>

        <div class="space-y-4 mb-4">
          <div
              v-for="(day, dayIndex) in form.days"
              :key="dayIndex"
              class="border border-gray-200 p-3"
          >
            <div class="flex items-center justify-between mb-3">
              <p class="text-sm font-semibold">Dia {{ dayIndex + 1 }}</p>
              <button
                  @click="removeDay(dayIndex)"
                  class="text-xs text-red-400 hover:text-red-600"
              >
                Remover dia
              </button>
            </div>

            <div class="space-y-2">
              <div
                  v-for="(ex, exIndex) in day.exercises"
                  :key="exIndex"
                  class="flex gap-2 items-center"
              >
                <select
                    v-model="ex.exerciseId"
                    class="flex-1 px-3 py-1.5 border border-gray-300 focus:outline-none focus:border-black text-sm bg-white"
                >
                  <option :value="null">Selecione um exercício...</option>
                  <optgroup
                      v-for="cat in allExercises"
                      :key="cat.id"
                      :label="cat.name"
                  >
                    <option
                        v-for="exercise in cat.exercises"
                        :key="exercise.id"
                        :value="exercise.id"
                    >
                      {{ exercise.name }}
                    </option>
                  </optgroup>
                </select>
                <button
                    @click="removeExerciseFromDay(dayIndex, exIndex)"
                    class="text-xs text-red-400 hover:text-red-600 px-1"
                >
                  ✕
                </button>
              </div>
            </div>

            <button
                @click="addExerciseToDay(dayIndex)"
                class="mt-2 text-xs text-blue-500 hover:text-blue-700 font-medium"
            >
              + Exercício
            </button>
          </div>
        </div>

        <button
            @click="addDay"
            class="w-full py-2 border border-dashed border-gray-300 text-sm text-gray-500 hover:bg-gray-50 mb-4"
        >
          + Adicionar Dia
        </button>

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
            {{ editingTemplate ? 'Salvar' : 'Criar' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>