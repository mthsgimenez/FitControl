import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import './style.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

import { useAuthStore } from "@/stores/authStore.js";
const auth = useAuthStore();
auth.initializeFromStorage();

app.mount('#app')
