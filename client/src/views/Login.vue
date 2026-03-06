<script setup>
</script>
<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="w-full max-w-md bg-white shadow-lg rounded-2xl p-8">

      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-gray-800">Login</h1>
        <p class="text-gray-500 mt-2">Entre na sua conta</p>
      </div>

      <form class="space-y-6">

        <div>
          <label
              for="email"
              class="block text-sm font-medium text-gray-700 mb-1"
          >
            Email
          </label>
          <input
              id="email"
              type="email"
              placeholder="seu@email.com"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-yellow-500 focus:border-transparent transition"
          />
        </div>

        <div>
          <label
              for="password"
              class="block text-sm font-medium text-gray-700 mb-1"
          >
            Senha
          </label>
          <input
              id="password"
              type="password"
              placeholder="••••••••"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-yellow-500 focus:border-transparent transition"
          />
        </div>

        <div class="text-sm">
          <a
              href="#"
              class="text-sm text-blue-400 hover:text-blue-600 font-medium"
          >
            Esqueceu a senha?
          </a>
        </div>

        <button
            type="submit"
            class="w-full bg-yellow-400 text-gray-900 py-2.5 rounded-lg font-semibold hover:bg-yellow-500 transition"
        >
          Entrar
        </button>

      </form>

      <div class="mt-6 text-center text-sm text-gray-500">
        É dono de academia?
        <a href="#" class="text-yellow-500 hover:text-yellow-700 font-medium">
          Cadastre sua academia
        </a>
      </div>

    </div>
  </div>
</template>
<style scoped>
</style>