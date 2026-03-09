<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/axios.js'

const categories = ref([])
const exercises = ref([])
const selectedCategory = ref(null)
const loading = ref(false)
const loadingExercises = ref(false)
const error = ref(null)

const showCategoryModal = ref(false)
const showExerciseModal = ref(false)
const editingExercise = ref(null)
const deleteCategoryConfirmId = ref(null)
const deleteExerciseConfirmId = ref(null)

const categoryForm = ref({ name: '' })
const exerciseForm = ref({ name: '' })

async function fetchCategories() {
  loading.value = true
  error.value = null
  try {
    const { data } = await api.get('/category')
    categories.value = data
  } catch (e) {
    error.value = 'Erro ao carregar categorias'
  } finally {
    loading.value = false
  }
}

async function selectCategory(category) {
  selectedCategory.value = category
  loadingExercises.value = true
  try {
    const { data } = await api.get(`/category/${category.id}/exercise`)
    exercises.value = data
  } catch (e) {
    error.value = 'Erro ao carregar exercícios'
  } finally {
    loadingExercises.value = false
  }
}

function openCategoryModal() {
  categoryForm.value = { name: '' }
  error.value = null
  showCategoryModal.value = true
}

function openCreateExerciseModal() {
  editingExercise.value = null
  exerciseForm.value = { name: '' }
  error.value = null
  showExerciseModal.value = true
}

function openEditExerciseModal(exercise) {
  editingExercise.value = exercise
  exerciseForm.value = { name: exercise.name }
  error.value = null
  showExerciseModal.value = true
}

async function submitCategory() {
  error.value = null
  try {
    await api.post('/category', categoryForm.value)
    showCategoryModal.value = false
    await fetchCategories()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao criar categoria'
  }
}

async function deleteCategory(id) {
  try {
    await api.delete(`/category/${id}`)
    deleteCategoryConfirmId.value = null
    if (selectedCategory.value?.id === id) {
      selectedCategory.value = null
      exercises.value = []
    }
    await fetchCategories()
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao remover categoria'
  }
}

async function submitExercise() {
  error.value = null
  try {
    if (editingExercise.value) {
      await api.put(`/category/${selectedCategory.value.id}/exercise/${editingExercise.value.id}`, exerciseForm.value)
    } else {
      await api.post(`/category/${selectedCategory.value.id}/exercise`, exerciseForm.value)
    }
    showExerciseModal.value = false
    await selectCategory(selectedCategory.value)
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao salvar exercício'
  }
}

async function deleteExercise(id) {
  try {
    await api.delete(`/category/${selectedCategory.value.id}/exercise/${id}`)
    deleteExerciseConfirmId.value = null
    await selectCategory(selectedCategory.value)
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao remover exercício'
  }
}

onMounted(fetchCategories)
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-black">Exercícios</h1>
    </div>

    <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

    <div class="flex gap-6">

      <!-- Categories panel -->
      <div class="w-64 shrink-0">
        <div class="flex items-center justify-between mb-3">
          <h2 class="text-sm font-semibold text-gray-600 uppercase tracking-wider">Categorias</h2>
          <button
              @click="openCategoryModal"
              class="text-xs px-2 py-1 bg-yellow-400 hover:bg-yellow-500 text-black font-bold"
          >
            + Nova
          </button>
        </div>

        <div class="bg-white border border-gray-200">
          <div v-if="loading" class="p-4 text-center text-gray-400 text-sm">Carregando...</div>
          <div v-else-if="categories.length === 0" class="p-4 text-center text-gray-400 text-sm">
            Nenhuma categoria
          </div>
          <div v-else>
            <div
                v-for="category in categories"
                :key="category.id"
                :class="[
                  'flex items-center justify-between px-3 py-2 border-b border-gray-100 cursor-pointer text-sm',
                  selectedCategory?.id === category.id ? 'bg-yellow-400 font-bold' : 'hover:bg-gray-50'
                ]"
                @click="selectCategory(category)"
            >
              <span class="truncate">{{ category.name }}</span>
              <div class="flex items-center gap-1 ml-2 shrink-0">
                <template v-if="deleteCategoryConfirmId === category.id">
                  <button
                      @click.stop="deleteCategory(category.id)"
                      class="text-xs px-2 py-0.5 bg-red-500 text-white hover:bg-red-600"
                  >
                    Ok
                  </button>
                  <button
                      @click.stop="deleteCategoryConfirmId = null"
                      class="text-xs px-2 py-0.5 border border-gray-300 hover:bg-gray-100"
                  >
                    Não
                  </button>
                </template>
                <button
                    v-else
                    @click.stop="deleteCategoryConfirmId = category.id"
                    class="text-xs text-red-400 hover:text-red-600 px-1"
                >
                  ✕
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Exercises panel -->
      <div class="flex-1">
        <div class="flex items-center justify-between mb-3">
          <h2 class="text-sm font-semibold text-gray-600 uppercase tracking-wider">
            {{ selectedCategory ? `Exercícios — ${selectedCategory.name}` : 'Exercícios' }}
          </h2>
          <button
              v-if="selectedCategory"
              @click="openCreateExerciseModal"
              class="text-xs px-2 py-1 bg-yellow-400 hover:bg-yellow-500 text-black font-bold"
          >
            + Novo
          </button>
        </div>

        <div class="bg-white border border-gray-200">
          <div v-if="!selectedCategory" class="p-8 text-center text-gray-400 text-sm">
            Selecione uma categoria
          </div>
          <div v-else-if="loadingExercises" class="p-8 text-center text-gray-400 text-sm">
            Carregando...
          </div>
          <div v-else-if="exercises.length === 0" class="p-8 text-center text-gray-400 text-sm">
            Nenhum exercício nesta categoria
          </div>
          <table v-else class="w-full text-sm">
            <thead class="border-b border-gray-200">
            <tr class="text-left">
              <th class="px-4 py-3 font-semibold text-gray-600">Nome</th>
              <th class="px-4 py-3 font-semibold text-gray-600">Ações</th>
            </tr>
            </thead>
            <tbody>
            <tr
                v-for="exercise in exercises"
                :key="exercise.id"
                class="border-b border-gray-100 hover:bg-gray-50"
            >
              <td class="px-4 py-3 font-medium">{{ exercise.name }}</td>
              <td class="px-4 py-3">
                <div class="flex gap-2">
                  <button
                      @click="openEditExerciseModal(exercise)"
                      class="text-xs px-3 py-1 border border-gray-300 hover:bg-gray-50 font-medium"
                  >
                    Editar
                  </button>
                  <button
                      v-if="deleteExerciseConfirmId !== exercise.id"
                      @click="deleteExerciseConfirmId = exercise.id"
                      class="text-xs px-3 py-1 border border-red-200 text-red-500 hover:bg-red-50 font-medium"
                  >
                    Remover
                  </button>
                  <template v-else>
                    <button
                        @click="deleteExercise(exercise.id)"
                        class="text-xs px-3 py-1 bg-red-500 text-white hover:bg-red-600 font-medium"
                    >
                      Confirmar
                    </button>
                    <button
                        @click="deleteExerciseConfirmId = null"
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
      </div>
    </div>

    <!-- Category Modal -->
    <div v-if="showCategoryModal" class="fixed inset-0 bg-gray-100 bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-sm p-6 shadow-lg">
        <h2 class="text-lg font-bold mb-4">Nova Categoria</h2>

        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">Nome</label>
          <input
              v-model="categoryForm.name"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              placeholder="Ex: Peito, Costas, Pernas..."
          />
        </div>

        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

        <div class="flex gap-3">
          <button
              @click="showCategoryModal = false"
              class="w-full py-2 border border-gray-300 text-sm font-medium hover:bg-gray-50"
          >
            Cancelar
          </button>
          <button
              @click="submitCategory"
              class="w-full py-2 bg-yellow-400 hover:bg-yellow-500 text-black text-sm font-bold"
          >
            Criar
          </button>
        </div>
      </div>
    </div>

    <!-- Exercise Modal -->
    <div v-if="showExerciseModal" class="fixed inset-0 bg-gray-100 bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-sm p-6 shadow-lg">
        <h2 class="text-lg font-bold mb-4">{{ editingExercise ? 'Editar Exercício' : 'Novo Exercício' }}</h2>

        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">Nome</label>
          <input
              v-model="exerciseForm.name"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              placeholder="Ex: Supino reto, Agachamento..."
          />
        </div>

        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

        <div class="flex gap-3">
          <button
              @click="showExerciseModal = false"
              class="w-full py-2 border border-gray-300 text-sm font-medium hover:bg-gray-50"
          >
            Cancelar
          </button>
          <button
              @click="submitExercise"
              class="w-full py-2 bg-yellow-400 hover:bg-yellow-500 text-black text-sm font-bold"
          >
            {{ editingExercise ? 'Salvar' : 'Criar' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>