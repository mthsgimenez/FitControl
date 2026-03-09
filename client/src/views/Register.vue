<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

// steps: 1 = tenant data, 2 = email, 3 = verification code
const step = ref(1)
const loading = ref(false)
const error = ref(null)

const form = ref({
  legalName: '',
  tradeName: '',
  cnpj: '',
  postalCode: '',
  email: '',
  password: '',
  confirmPassword: '',
  verificationCode: ''
})

async function submitTenantData() {
  if (form.value.password !== form.value.confirmPassword) {
    error.value = 'As senhas não coincidem'
    return
  }
  error.value = null
  step.value = 2
}

async function sendVerificationEmail() {
  error.value = null
  loading.value = true
  try {
    await axios.post(`${import.meta.env.VITE_API_URL}/auth/verify-email`, {
      email: form.value.email
    })
    step.value = 3
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao enviar e-mail de verificação'
  } finally {
    loading.value = false
  }
}

async function submitRegistration() {
  error.value = null
  loading.value = true
  try {
    await axios.post(`${import.meta.env.VITE_API_URL}/auth/register`, {
      email: form.value.email,
      password: form.value.password,
      cnpj: form.value.cnpj,
      postalCode: form.value.postalCode,
      legalName: form.value.legalName,
      tradeName: form.value.tradeName,
      verificationCode: form.value.verificationCode
    })
    router.push('/login')
  } catch (e) {
    error.value = e.response?.data?.detail || 'Erro ao realizar cadastro'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex items-center justify-center min-h-screen">
    <div class="w-full max-w-sm p-8 bg-white border border-gray-200 shadow-sm">

      <!-- Step indicator -->
      <div class="flex items-center mb-8">
        <div v-for="s in 3" :key="s" class="flex items-center">
          <div
              :class="[
                'w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold',
                step >= s ? 'bg-yellow-400 text-black' : 'bg-gray-200 text-gray-400'
              ]"
          >{{ s }}</div>
          <div v-if="s < 3" :class="['h-px w-8', step > s ? 'bg-yellow-400' : 'bg-gray-200']" />
        </div>
      </div>

      <!-- Step 1: Tenant data -->
      <form v-if="step === 1" @submit.prevent="submitTenantData">
        <h1 class="text-2xl font-bold text-black mb-6">Dados da Academia</h1>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">Razão Social</label>
          <input
              v-model="form.legalName"
              type="text"
              required
              class="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black"
              placeholder="Nome Legal LTDA"
          />
        </div>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">Nome Fantasia</label>
          <input
              v-model="form.tradeName"
              type="text"
              class="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black"
              placeholder="Academia XYZ"
          />
        </div>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">CNPJ</label>
          <input
              v-model="form.cnpj"
              type="text"
              required
              maxlength="14"
              class="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black"
              placeholder="00000000000000"
          />
        </div>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">CEP</label>
          <input
              v-model="form.postalCode"
              type="text"
              required
              maxlength="8"
              class="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black"
              placeholder="00000000"
          />
        </div>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">Senha</label>
          <input
              v-model="form.password"
              type="password"
              required
              class="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black"
              placeholder="••••••••"
          />
        </div>

        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">Confirmar Senha</label>
          <input
              v-model="form.confirmPassword"
              type="password"
              required
              class="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black"
              placeholder="••••••••"
          />
        </div>

        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

        <button
            type="submit"
            class="w-full py-3 px-4 bg-yellow-400 hover:bg-yellow-500 text-black font-bold"
        >
          Continuar
        </button>

        <div class="text-center mt-6 pt-6 border-t border-gray-100">
          <router-link to="/login" class="text-sm text-blue-500 hover:text-blue-600">
            Já tem uma conta? Entrar
          </router-link>
        </div>
      </form>

      <!-- Step 2: Email -->
      <form v-else-if="step === 2" @submit.prevent="sendVerificationEmail">
        <h1 class="text-2xl font-bold text-black mb-2">Seu E-mail</h1>
        <p class="text-sm text-gray-500 mb-6">Enviaremos um código de verificação</p>

        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">E-mail</label>
          <input
              v-model="form.email"
              type="email"
              required
              class="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black"
              placeholder="seu@email.com"
          />
        </div>

        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

        <div class="flex gap-3">
          <button
              type="button"
              @click="step = 1"
              class="w-full py-3 px-4 border border-gray-300 text-gray-700 font-bold hover:bg-gray-50"
          >
            Voltar
          </button>
          <button
              type="submit"
              :disabled="loading"
              class="w-full py-3 px-4 bg-yellow-400 hover:bg-yellow-500 text-black font-bold disabled:opacity-50"
          >
            {{ loading ? 'Enviando...' : 'Enviar Código' }}
          </button>
        </div>
      </form>

      <!-- Step 3: Verification code -->
      <form v-else-if="step === 3" @submit.prevent="submitRegistration">
        <h1 class="text-2xl font-bold text-black mb-2">Verificar E-mail</h1>
        <p class="text-sm text-gray-500 mb-6">
          Digite o código enviado para <span class="font-medium text-black">{{ form.email }}</span>
        </p>

        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">Código de Verificação</label>
          <input
              v-model="form.verificationCode"
              type="text"
              required
              maxlength="6"
              class="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black text-center text-2xl tracking-widest"
              placeholder="000000"
          />
        </div>

        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

        <div class="flex gap-3">
          <button
              type="button"
              @click="step = 2"
              class="w-full py-3 px-4 border border-gray-300 text-gray-700 font-bold hover:bg-gray-50"
          >
            Voltar
          </button>
          <button
              type="submit"
              :disabled="loading"
              class="w-full py-3 px-4 bg-yellow-400 hover:bg-yellow-500 text-black font-bold disabled:opacity-50"
          >
            {{ loading ? 'Cadastrando...' : 'Finalizar Cadastro' }}
          </button>
        </div>

        <button
            type="button"
            @click="sendVerificationEmail"
            :disabled="loading"
            class="w-full mt-3 text-sm text-blue-500 hover:text-blue-600 disabled:opacity-50"
        >
          Reenviar código
        </button>
      </form>

    </div>
  </div>
</template>