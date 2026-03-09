import axios from 'axios'
import router from '@/router'

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL
})

api.interceptors.request.use(async config => {
    const { useAuthStore } = await import('@/stores/authStore.js')
    const auth = useAuthStore()

    if (auth.isExpired && auth.refreshToken) {
        try {
            await auth.refresh()
        } catch {
            auth.logout()
            router.push('/login')
            return Promise.reject(new Error('Session expired'))
        }
    }

    if (auth.accessToken) {
        config.headers.Authorization = `Bearer ${auth.accessToken}`
    }

    return config
})

api.interceptors.response.use(
    response => response,
    async error => {
        const { useAuthStore } = await import('@/stores/authStore.js')
        const auth = useAuthStore()
        const originalRequest = error.config

        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true

            try {
                await auth.refresh()
                originalRequest.headers.Authorization = `Bearer ${auth.accessToken}`
                return api(originalRequest)
            } catch {
                auth.logout()
                router.push('/login')
                return Promise.reject(error)
            }
        }

        return Promise.reject(error)
    }
)

export default api