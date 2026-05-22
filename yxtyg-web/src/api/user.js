import request from '@/utils/request'

export function getUserList(params) {
  return request({ url: '/users', method: 'get', params })
}

export function getProductManagers() {
  return request({ url: '/users/product-managers', method: 'get' })
}

export function createUser(data) {
  return request({ url: '/users', method: 'post', data })
}

export function updateUser(id, data) {
  return request({ url: `/users/${id}`, method: 'put', data })
}

export function disableUser(id) {
  return request({ url: `/users/${id}/disable`, method: 'post' })
}
