import request from '@/utils/request'

// 分页查询体验官
export function getExperimenterList(params) {
  return request({
    url: '/experimenter/list',
    method: 'get',
    params
  })
}

// 获取所有体验官
export function getAllExperimenters() {
  return request({
    url: '/experimenter/all',
    method: 'get'
  })
}

// 获取体验官详情
export function getExperimenter(id) {
  return request({
    url: `/experimenter/${id}`,
    method: 'get'
  })
}

// 新增体验官
export function addExperimenter(data) {
  return request({
    url: '/experimenter/add',
    method: 'post',
    data
  })
}

// 更新体验官
export function updateExperimenter(data) {
  return request({
    url: '/experimenter/update',
    method: 'put',
    data
  })
}

// 删除体验官
export function deleteExperimenter(id) {
  return request({
    url: `/experimenter/delete/${id}`,
    method: 'delete'
  })
}

// 批量删除体验官
export function batchDeleteExperimenters(ids) {
  return request({
    url: '/experimenter/batchDelete',
    method: 'delete',
    data: ids
  })
}

// 设置/取消接口人
export function setContact(id, isContact) {
  return request({
    url: `/experimenter/setContact/${id}`,
    method: 'put',
    params: { isContact }
  })
}

// 导出Excel
export function exportExperimenter(params) {
  window.open(`/api/experimenter/export?${new URLSearchParams(params).toString()}`)
}
