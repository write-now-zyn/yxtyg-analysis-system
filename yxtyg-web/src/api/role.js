import request from '@/utils/request'

export function getRoles() {
  return request({ url: '/roles', method: 'get' })
}

export function getPermissions() {
  return request({ url: '/roles/permissions', method: 'get' })
}

export function updateRolePermissions(code, data) {
  return request({ url: `/roles/${code}/permissions`, method: 'put', data })
}
