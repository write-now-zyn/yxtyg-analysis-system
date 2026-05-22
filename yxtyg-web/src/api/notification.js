import request from '@/utils/request'

export function getNotifications() {
  return request({ url: '/notifications', method: 'get' })
}

export function markNotificationRead(id) {
  return request({ url: `/notifications/${id}/read`, method: 'post' })
}
