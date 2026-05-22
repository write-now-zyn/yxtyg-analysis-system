import request from '@/utils/request'

export function getDemandWorkloadList(params) {
  return request({ url: '/demand-workloads', method: 'get', params })
}

export function getDemandWorkloadDetail(id) {
  return request({ url: `/demand-workloads/${id}`, method: 'get' })
}

export function createDemandWorkload(data) {
  return request({ url: '/demand-workloads', method: 'post', data })
}

export function updateDemandWorkload(id, data) {
  return request({ url: `/demand-workloads/${id}`, method: 'put', data })
}

export function deleteDemandWorkload(id) {
  return request({ url: `/demand-workloads/${id}`, method: 'delete' })
}

export function importDemandWorkload(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/demand-workloads/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function downloadDemandTemplate() {
  return request({ url: '/demand-workloads/template', method: 'get', responseType: 'blob' })
}

export function exportDemandWorkload(params) {
  return request({ url: '/demand-workloads/export', method: 'get', params, responseType: 'blob' })
}

export function submitFinalWorkload(id, data) {
  return request({ url: `/demand-workloads/${id}/submit-final`, method: 'post', data })
}

export function confirmDemandWorkload(id) {
  return request({ url: `/demand-workloads/${id}/confirm`, method: 'post' })
}

export function remindDemandWorkload(id, data) {
  return request({ url: `/demand-workloads/${id}/reminders`, method: 'post', data })
}

export function getDemandStatistics(params) {
  return request({ url: '/demand-workloads/statistics', method: 'get', params })
}
