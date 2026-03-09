<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '@/api/axios.js'
import { useAuthStore } from '@/stores/authStore.js'

const auth = useAuthStore()

const routines = ref([])
const allExercises = ref([])
const workouts = ref([])
const currentWorkout = ref(null)
const loading = ref(false)
const error = ref(null)

const selectedRoutineId = ref(null)
const selectedDayIndex = ref(null)
const showRoutinePicker = ref(false)
const showAddSetModal = ref(false)
const editingSet = ref(null)
const targetPerformedExerciseId = ref(null)

const setForm = ref({ repetitions: null, weight: null, notes: '' })

const selectedRoutine = computed(() =>
    routines.value.find(r => r.id === selectedRoutineId.value) || null
)

const selectedDay = computed(() => {
  if (!selectedRoutine.value || selectedDayIndex.value === null) return null
  return selectedRoutine.value.days?.[selectedDayIndex.value] || null
})

async function fetchRoutines() {
  try {
    const { data } = await api.get(`/routine/member/${auth.memberId}`)
    // fetch full routine for each to get days/exercises
    const full = await Promise.all(data.map(r => api.get(`/routine/${r.id}`).then(res => res.data)))
    routines.value = full
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

async function fetchWorkouts() {
  loading.value = true
  try {
    const { data } = await api.get(`/workout/member/${auth.memberId}?page=0&size=10&sort=workoutDate,desc`)
    workouts.value = data.content
  } catch (e) {
    error.value = 'Erro ao carregar treinos'
  } finally {
    loading.value = false
  }
}

async function startWorkout() {
  error.value = null
  try {
    const { data } = await api.post('/workout')
    currentWorkout.value = data

    // if a routine day is selected, pre-add all exercises
    if (selectedDay.value) {
      for (const ex of selectedDay.value.exercises) {
        const { data: updated } = await api.post(`/workout/${data.id}/exercise`, {
          exerciseId: ex.exerciseId
        })
        currentWorkout.value = updated
      }
    }

    showRoutinePicker.value = false
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao iniciar treino'
  }
}

async function addExercise(exerciseId) {
  try {
    const { data } = await api.post(`/workout/${currentWorkout.value.id}/exercise`, { exerciseId })
    currentWorkout.value = data
  } catch (e) {
    error.value = 'Erro ao adicionar exercício'
  }
}

async function deleteExercise(performedExerciseId) {
  try {
    await api.delete(`/workout/${currentWorkout.value.id}/exercise/${performedExerciseId}`)
    const { data } = await api.get(`/workout/${currentWorkout.value.id}`)
    currentWorkout.value = data
  } catch (e) {
    error.value = 'Erro ao remover exercício'
  }
}

function openAddSetModal(performedExerciseId, existingSet = null) {
  targetPerformedExerciseId.value = performedExerciseId
  editingSet.value = existingSet
  setForm.value = existingSet
      ? { repetitions: existingSet.repetitions, weight: existingSet.weight, notes: existingSet.notes || '' }
      : { repetitions: null, weight: null, notes: '' }
  showAddSetModal.value = true
}

async function submitSet() {
  error.value = null
  try {
    const payload = {
      repetitions: parseInt(setForm.value.repetitions),
      weight: parseFloat(setForm.value.weight),
      notes: setForm.value.notes || null
    }
    if (editingSet.value) {
      const { data } = await api.put(
          `/workout/${currentWorkout.value.id}/exercise/${targetPerformedExerciseId.value}/set/${editingSet.value.id}`,
          payload
      )
      currentWorkout.value = data
    } else {
      const { data } = await api.post(
          `/workout/${currentWorkout.value.id}/exercise/${targetPerformedExerciseId.value}/set`,
          payload
      )
      currentWorkout.value = data
    }
    showAddSetModal.value = false
  } catch (e) {
    error.value = 'Erro ao salvar série'
  }
}

async function deleteSet(performedExerciseId, setId) {
  try {
    await api.delete(`/workout/${currentWorkout.value.id}/exercise/${performedExerciseId}/set/${setId}`)
    const { data } = await api.get(`/workout/${currentWorkout.value.id}`)
    currentWorkout.value = data
  } catch (e) {
    error.value = 'Erro ao remover série'
  }
}

async function finishWorkout() {
  await fetchWorkouts()
  currentWorkout.value = null
  selectedRoutineId.value = null
  selectedDayIndex.value = null
}

async function deleteWorkout(id) {
  try {
    await api.delete(`/workout/${id}`)
    await fetchWorkouts()
  } catch (e) {
    error.value = 'Erro ao remover treino'
  }
}

async function viewWorkout(id) {
  try {
    const { data } = await api.get(`/workout/${id}`)
    currentWorkout.value = data
  } catch (e) {
    error.value = 'Erro ao carregar treino'
  }
}

onMounted(async () => {
  let memberId = auth.memberId
  if (!memberId) {
    const { data } = await api.get('/member/me')
    auth.memberId = data.id
    localStorage.setItem('memberId', data.id)
  }
  await Promise.all([fetchRoutines(), fetchExercises(), fetchWorkouts()])
})
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-black">Registrar Treino</h1>
      <button
          v-if="!currentWorkout"
          @click="showRoutinePicker = true"
          class="px-4 py-2 bg-yellow-400 hover:bg-yellow-500 text-black font-bold text-sm"
      >
        + Iniciar Treino
      </button>
      <button
          v-else
          @click="finishWorkout"
          class="px-4 py-2 bg-black hover:bg-gray-800 text-white font-bold text-sm"
      >
        Finalizar Treino
      </button>
    </div>

    <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

    <!-- Active workout -->
    <div v-if="currentWorkout">
      <div class="flex items-center gap-3 mb-4">
        <p class="text-sm text-gray-500">{{ currentWorkout.workoutDate }}</p>
        <span class="text-xs px-2 py-0.5 bg-yellow-400 font-bold">Em andamento</span>
      </div>

      <!-- Exercises -->
      <div class="space-y-4 mb-4">
        <div
            v-for="ex in currentWorkout.exercises"
            :key="ex.id"
            class="bg-white border border-gray-200 p-4"
        >
          <div class="flex items-center justify-between mb-3">
            <h3 class="font-bold text-sm">{{ ex.exerciseName }}</h3>
            <button
                @click="deleteExercise(ex.id)"
                class="text-xs text-red-400 hover:text-red-600"
            >
              Remover
            </button>
          </div>

          <!-- Sets table -->
          <table v-if="ex.sets.length > 0" class="w-full text-sm mb-3">
            <thead>
            <tr class="text-left text-xs text-gray-400 border-b border-gray-100">
              <th class="pb-2 font-medium">Série</th>
              <th class="pb-2 font-medium">Peso (kg)</th>
              <th class="pb-2 font-medium">Reps</th>
              <th class="pb-2 font-medium">Obs</th>
              <th class="pb-2"></th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(set, i) in ex.sets" :key="set.id" class="border-b border-gray-50">
              <td class="py-1.5 pr-4 text-gray-500">{{ i + 1 }}</td>
              <td class="py-1.5 pr-4">{{ set.weight ?? '—' }}</td>
              <td class="py-1.5 pr-4">{{ set.repetitions ?? '—' }}</td>
              <td class="py-1.5 pr-4 text-gray-400 text-xs">{{ set.notes || '—' }}</td>
              <td class="py-1.5">
                <div class="flex gap-2">
                  <button
                      @click="openAddSetModal(ex.id, set)"
                      class="text-xs text-blue-500 hover:text-blue-700"
                  >
                    Editar
                  </button>
                  <button
                      @click="deleteSet(ex.id, set.id)"
                      class="text-xs text-red-400 hover:text-red-600"
                  >
                    ✕
                  </button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>

          <button
              @click="openAddSetModal(ex.id)"
              class="text-xs text-blue-500 hover:text-blue-700 font-medium"
          >
            + Adicionar Série
          </button>
        </div>
      </div>

      <!-- Add exercise -->
      <div class="bg-white border border-dashed border-gray-300 p-4">
        <p class="text-sm font-medium text-gray-600 mb-2">Adicionar exercício</p>
        <select
            @change="e => { if (e.target.value) { addExercise(parseInt(e.target.value)); e.target.value = '' } }"
            class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm bg-white"
        >
          <option value="">Selecione um exercício...</option>
          <optgroup v-for="cat in allExercises" :key="cat.id" :label="cat.name">
            <option v-for="exercise in cat.exercises" :key="exercise.id" :value="exercise.id">
              {{ exercise.name }}
            </option>
          </optgroup>
        </select>
      </div>
    </div>

    <!-- Workout history -->
    <div v-else>
      <h2 class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">Histórico</h2>
      <div v-if="loading" class="p-8 text-center text-gray-400">Carregando...</div>
      <div v-else-if="workouts.length === 0" class="p-8 text-center text-gray-400 bg-white border border-gray-200 text-sm">
        Nenhum treino registrado
      </div>
      <div v-else class="bg-white border border-gray-200">
        <div
            v-for="workout in workouts"
            :key="workout.id"
            class="flex items-center justify-between px-4 py-3 border-b border-gray-100 hover:bg-gray-50"
        >
          <div class="flex items-center gap-3">
            <span class="text-sm font-medium">{{ workout.workoutDate }}</span>
          </div>
          <div class="flex gap-2">
            <button
                @click="viewWorkout(workout.id)"
                class="text-xs px-3 py-1 border border-gray-300 hover:bg-gray-50 font-medium"
            >
              Ver
            </button>
            <button
                @click="deleteWorkout(workout.id)"
                class="text-xs px-3 py-1 border border-red-200 text-red-500 hover:bg-red-50 font-medium"
            >
              Remover
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Routine picker modal -->
    <div v-if="showRoutinePicker" class="fixed inset-0 bg-gray-100 bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-md p-6 shadow-lg">
        <h2 class="text-lg font-bold mb-4">Iniciar Treino</h2>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">
            Ficha <span class="text-gray-400 font-normal">(opcional)</span>
          </label>
          <select
              v-model="selectedRoutineId"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm bg-white"
          >
            <option :value="null">Sem ficha</option>
            <option v-for="r in routines" :key="r.id" :value="r.id">{{ r.name }}</option>
          </select>
        </div>

        <div v-if="selectedRoutine" class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">Dia</label>
          <select
              v-model="selectedDayIndex"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm bg-white"
          >
            <option :value="null">Selecione...</option>
            <option v-for="(day, i) in selectedRoutine.days" :key="i" :value="i">
              Dia {{ i + 1 }} ({{ day.exercises.length }} exercícios)
            </option>
          </select>
        </div>

        <div class="flex gap-3">
          <button
              @click="showRoutinePicker = false"
              class="w-full py-2 border border-gray-300 text-sm font-medium hover:bg-gray-50"
          >
            Cancelar
          </button>
          <button
              @click="startWorkout"
              class="w-full py-2 bg-yellow-400 hover:bg-yellow-500 text-black text-sm font-bold"
          >
            Iniciar
          </button>
        </div>
      </div>
    </div>

    <!-- Add/Edit set modal -->
    <div v-if="showAddSetModal" class="fixed inset-0 bg-gray-100 bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-sm p-6 shadow-lg">
        <h2 class="text-lg font-bold mb-4">{{ editingSet ? 'Editar Série' : 'Nova Série' }}</h2>

        <div class="grid grid-cols-2 gap-4 mb-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Peso (kg)</label>
            <input
                v-model="setForm.weight"
                type="number"
                step="0.5"
                min="0"
                class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
                placeholder="0.0"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Repetições</label>
            <input
                v-model="setForm.repetitions"
                type="number"
                min="1"
                class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
                placeholder="0"
            />
          </div>
        </div>

        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">Observações</label>
          <input
              v-model="setForm.notes"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
              placeholder="Opcional..."
          />
        </div>

        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

        <div class="flex gap-3">
          <button
              @click="showAddSetModal = false"
              class="w-full py-2 border border-gray-300 text-sm font-medium hover:bg-gray-50"
          >
            Cancelar
          </button>
          <button
              @click="submitSet"
              class="w-full py-2 bg-yellow-400 hover:bg-yellow-500 text-black text-sm font-bold"
          >
            {{ editingSet ? 'Salvar' : 'Adicionar' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>