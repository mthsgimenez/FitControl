<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/axios.js'

const members = ref([])
const workouts = ref([])
const currentWorkout = ref(null)
const loading = ref(false)
const loadingWorkouts = ref(false)
const error = ref(null)
const selectedMemberId = ref(null)
const page = ref(0)
const totalPages = ref(0)

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
  currentWorkout.value = null
  page.value = 0
  await fetchWorkouts()
}

async function fetchWorkouts() {
  loadingWorkouts.value = true
  try {
    const { data } = await api.get(`/workout/member/${selectedMemberId.value}?page=${page.value}&size=10&sort=workoutDate,desc`)
    workouts.value = data.content
    totalPages.value = data.totalPages
  } catch (e) {
    error.value = 'Erro ao carregar treinos'
  } finally {
    loadingWorkouts.value = false
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

function getMemberName(id) {
  const m = members.value.find(m => m.id === id)
  return m ? `${m.person.name} ${m.person.lastName}` : `Membro ${id}`
}

async function prevPage() {
  if (page.value > 0) {
    page.value--
    await fetchWorkouts()
  }
}

async function nextPage() {
  if (page.value < totalPages.value - 1) {
    page.value++
    await fetchWorkouts()
  }
}

onMounted(fetchMembers)
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold text-black mb-6">Treinos dos Alunos</h1>

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

      <!-- Workouts panel -->
      <div class="flex-1">
        <h2 class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">
          {{ selectedMemberId ? `Treinos — ${getMemberName(selectedMemberId)}` : 'Treinos' }}
        </h2>

        <div v-if="!selectedMemberId" class="p-8 text-center text-gray-400 bg-white border border-gray-200 text-sm">
          Selecione um membro
        </div>

        <template v-else-if="!currentWorkout">
          <div class="bg-white border border-gray-200">
            <div v-if="loadingWorkouts" class="p-8 text-center text-gray-400 text-sm">Carregando...</div>
            <div v-else-if="workouts.length === 0" class="p-8 text-center text-gray-400 text-sm">
              Nenhum treino registrado
            </div>
            <div v-else>
              <div
                  v-for="workout in workouts"
                  :key="workout.id"
                  class="flex items-center justify-between px-4 py-3 border-b border-gray-100 hover:bg-gray-50"
              >
                <span class="text-sm font-medium">{{ workout.workoutDate }}</span>
                <button
                    @click="viewWorkout(workout.id)"
                    class="text-xs px-3 py-1 border border-gray-300 hover:bg-gray-50 font-medium"
                >
                  Ver
                </button>
              </div>
            </div>
          </div>

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="flex items-center justify-between mt-4">
            <button
                @click="prevPage"
                :disabled="page === 0"
                class="text-xs px-3 py-1 border border-gray-300 hover:bg-gray-50 font-medium disabled:opacity-40"
            >
              ← Anterior
            </button>
            <span class="text-xs text-gray-400">Página {{ page + 1 }} de {{ totalPages }}</span>
            <button
                @click="nextPage"
                :disabled="page >= totalPages - 1"
                class="text-xs px-3 py-1 border border-gray-300 hover:bg-gray-50 font-medium disabled:opacity-40"
            >
              Próxima →
            </button>
          </div>
        </template>

        <!-- Workout detail view -->
        <template v-else>
          <div class="flex items-center gap-3 mb-4">
            <button
                @click="currentWorkout = null"
                class="text-xs px-3 py-1 border border-gray-300 hover:bg-gray-50 font-medium"
            >
              ← Voltar
            </button>
            <span class="text-sm text-gray-500">{{ currentWorkout.workoutDate }}</span>
          </div>

          <div class="space-y-4">
            <div
                v-for="ex in currentWorkout.exercises"
                :key="ex.id"
                class="bg-white border border-gray-200 p-4"
            >
              <h3 class="font-bold text-sm mb-3">{{ ex.exerciseName }}</h3>

              <div v-if="ex.sets.length === 0" class="text-xs text-gray-400">
                Nenhuma série registrada
              </div>
              <table v-else class="w-full text-sm">
                <thead>
                <tr class="text-left text-xs text-gray-400 border-b border-gray-100">
                  <th class="pb-2 font-medium">Série</th>
                  <th class="pb-2 font-medium">Peso (kg)</th>
                  <th class="pb-2 font-medium">Reps</th>
                  <th class="pb-2 font-medium">Obs</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="(set, i) in ex.sets" :key="set.id" class="border-b border-gray-50">
                  <td class="py-1.5 pr-4 text-gray-500">{{ i + 1 }}</td>
                  <td class="py-1.5 pr-4">{{ set.weight ?? '—' }}</td>
                  <td class="py-1.5 pr-4">{{ set.repetitions ?? '—' }}</td>
                  <td class="py-1.5 text-gray-400 text-xs">{{ set.notes || '—' }}</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>