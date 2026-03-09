import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Register from "@/views/Register.vue";
import ResetPassword from "@/views/ResetPassword.vue";
import SetPassword from "@/views/SetPassword.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', name: 'Login', component: Login },
    { path: '/register', name: 'Register', component: Register },
    { path: '/reset-password', name: 'ResetPassword', component: ResetPassword },
    { path: '/set-password', name: 'SetPassword', component: SetPassword },
  ],
})

export default router
