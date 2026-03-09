import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore.js'
import MainLayout from '@/layouts/MainLayout.vue'

const routes = [
  { path: '/login', component: () => import('@/views/Login.vue') },
  { path: '/register', component: () => import('@/views/Register.vue') },
  { path: '/reset-password', component: () => import('@/views/ResetPassword.vue') },
  { path: '/set-password', component: () => import('@/views/SetPassword.vue') },
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      // { path: 'employees', component: () => import('@/views/Employees.vue') },
      // { path: 'members', component: () => import('@/views/Members.vue') },
      // { path: 'plans', component: () => import('@/views/Plans.vue') },
      // { path: 'subscriptions', component: () => import('@/views/Subscriptions.vue') },
      // { path: 'exercises', component: () => import('@/views/Exercises.vue') },
      // { path: 'routine-templates', component: () => import('@/views/RoutineTemplates.vue') },
      // { path: 'routines', component: () => import('@/views/Routines.vue') },
      // { path: 'my-plan', component: () => import('@/views/MyPlan.vue') },
      // { path: 'workout', component: () => import('@/views/Workout.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    next('/login')
    return
  }
  next()
})

export default router