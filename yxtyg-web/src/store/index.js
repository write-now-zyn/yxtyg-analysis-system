import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    cities: ['南京', '苏州', '无锡', '常州', '镇江', '扬州', '泰州', '南通', '盐城', '淮安', '连云港', '徐州', '宿迁'],
    token: localStorage.getItem('yxtyg_token') || '',
    user: JSON.parse(localStorage.getItem('yxtyg_user') || 'null'),
    permissions: JSON.parse(localStorage.getItem('yxtyg_permissions') || '[]')
  },
  getters: {
    cities: state => state.cities,
    isLoggedIn: state => Boolean(state.token),
    roleCode: state => state.user ? state.user.roleCode : '',
    hasPermission: state => code => state.permissions.includes(code),
    hasRole: state => roles => {
      if (!state.user) return false
      return roles.includes(state.user.roleCode)
    }
  },
  mutations: {
    setAuth(state, payload) {
      state.token = payload.token
      state.user = payload.user
      state.permissions = payload.permissions || []
      localStorage.setItem('yxtyg_token', state.token)
      localStorage.setItem('yxtyg_user', JSON.stringify(state.user))
      localStorage.setItem('yxtyg_permissions', JSON.stringify(state.permissions))
    },
    clearAuth(state) {
      state.token = ''
      state.user = null
      state.permissions = []
      localStorage.removeItem('yxtyg_token')
      localStorage.removeItem('yxtyg_user')
      localStorage.removeItem('yxtyg_permissions')
    }
  },
  actions: {},
  modules: {}
})
