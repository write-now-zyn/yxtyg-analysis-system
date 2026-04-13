import request from '@/utils/request'

export function getModelConfigList() {
  return request({
    url: '/model-config/list',
    method: 'get'
  })
}

export function addModelConfig(data) {
  return request({
    url: '/model-config/add',
    method: 'post',
    data
  })
}

export function updateModelConfig(data) {
  return request({
    url: '/model-config/update',
    method: 'put',
    data
  })
}

export function deleteModelConfig(id) {
  return request({
    url: `/model-config/delete/${id}`,
    method: 'delete'
  })
}

export function testConnection(id) {
  return request({
    url: `/model-config/test/${id}`,
    method: 'post'
  })
}

export function testConnectionByConfig(data) {
  return request({
    url: '/model-config/test-config',
    method: 'post',
    data
  })
}

export function fetchModels(id) {
  return request({
    url: `/model-config/fetch-models/${id}`,
    method: 'get'
  })
}

export function setDefaultConfig(id) {
  return request({
    url: `/model-config/set-default/${id}`,
    method: 'post'
  })
}
