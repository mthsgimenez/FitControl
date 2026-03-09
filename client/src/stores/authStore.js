import { defineStore } from 'pinia'
import axios from 'axios'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        accessToken: localStorage.getItem('accessToken') || null,
        refreshToken: localStorage.getItem('refreshToken') || null,
        expiresIn: localStorage.getItem('expiresIn') || null,
        _refreshTimer: null
    }),

    getters: {
        isAuthenticated: state => !!state.accessToken,
        isExpired: state => state.expiresIn
            ? Date.now() >= parseInt(state.expiresIn)
            : true
    },

    actions: {
        setTokens(accessToken, refreshToken, expiresIn) {
            this.accessToken = accessToken
            this.refreshToken = refreshToken
            this.expiresIn = Date.now() + (expiresIn - 30) * 1000
            localStorage.setItem('accessToken', accessToken)
            localStorage.setItem('refreshToken', refreshToken)
            localStorage.setItem('expiresIn', this.expiresIn)
            this.scheduleRefresh(expiresIn - 30)
        },

        scheduleRefresh(secondsUntilRefresh) {
            if (this._refreshTimer) clearTimeout(this._refreshTimer)
            this._refreshTimer = setTimeout(async () => {
                try {
                    await this.refresh()
                } catch {
                    this.logout()
                }
            }, secondsUntilRefresh * 1000)
        },

        async login(email, password) {
            const response = await axios.post(
                `${import.meta.env.VITE_API_URL}/auth/login`,
                { email, password }
            )

            const { accessToken, refreshToken, expiresIn } = response.data

            if (!accessToken) {
                throw new Error('Login falhou: resposta inválida do servidor')
            }

            this.setTokens(accessToken, refreshToken, expiresIn)
        },

        async refresh() {
            const response = await axios.post(
                `${import.meta.env.VITE_API_URL}/auth/refresh`,
                { refreshToken: this.refreshToken }
            )
            const { accessToken, refreshToken, expiresIn } = response.data
            this.setTokens(accessToken, refreshToken, expiresIn)
        },

        initializeFromStorage() {
            if (this.accessToken && this.expiresIn) {
                const secondsLeft = (parseInt(this.expiresIn) - Date.now()) / 1000
                if (secondsLeft > 0) {
                    this.scheduleRefresh(secondsLeft)
                } else {
                    this.refresh().catch(() => this.logout())
                }
            }
        },

        logout() {
            if (this._refreshTimer) clearTimeout(this._refreshTimer)
            this.accessToken = null
            this.refreshToken = null
            this.expiresIn = null
            this._refreshTimer = null
            localStorage.removeItem('accessToken')
            localStorage.removeItem('refreshToken')
            localStorage.removeItem('expiresIn')
        }
    }
})