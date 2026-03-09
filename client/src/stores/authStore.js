import { defineStore } from 'pinia'
import axios from 'axios'
import router from '@/router'
import api from "@/api/axios.js";

export const useAuthStore = defineStore('auth', {
    state: () => ({
        accessToken: localStorage.getItem('accessToken') || null,
        refreshToken: localStorage.getItem('refreshToken') || null,
        expiresAt: localStorage.getItem('expiresAt') ? parseInt(localStorage.getItem('expiresAt')) : null,
        roles: JSON.parse(localStorage.getItem('roles') || '[]'),
        email: localStorage.getItem('email') || null,
        memberId: localStorage.getItem('memberId') ? parseInt(localStorage.getItem('memberId')) : null,
        _refreshTimer: null
    }),

    getters: {
        isAuthenticated: state => !!state.accessToken,
        isExpired: state => state.expiresAt
            ? Date.now() >= state.expiresAt
            : true,
        isOwner: state => state.roles.includes('ROLE_OWNER')
    },

    actions: {
        setTokens(accessToken, refreshToken, expiresIn) {
            const payload = JSON.parse(atob(accessToken.split('.')[1]))
            this.roles = payload.roles || []
            this.email = payload.email || null
            this.expiresAt = Date.now() + (expiresIn - 30) * 1000

            this.accessToken = accessToken
            this.refreshToken = refreshToken

            localStorage.setItem('accessToken', accessToken)
            localStorage.setItem('refreshToken', refreshToken)
            localStorage.setItem('expiresAt', this.expiresAt)
            localStorage.setItem('roles', JSON.stringify(this.roles))
            localStorage.setItem('email', this.email)

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
            this.setTokens(accessToken, refreshToken, expiresIn)

            if (this.roles.includes('ROLE_MEMBER')) {
                try {
                    const { data } = await api.get('/member/me')
                    this.memberId = data.id
                    localStorage.setItem('memberId', data.id)
                } catch (e) {}
            }
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
            if (this.accessToken && this.expiresAt) {
                const secondsLeft = (this.expiresAt - Date.now()) / 1000
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
            this.expiresAt = null
            this.roles = []
            this.email = null
            this._refreshTimer = null
            localStorage.removeItem('accessToken')
            localStorage.removeItem('refreshToken')
            localStorage.removeItem('expiresAt')
            localStorage.removeItem('roles')
            localStorage.removeItem('email')
            localStorage.removeItem('memberId')
            router.push('/login')
        }
    }
})