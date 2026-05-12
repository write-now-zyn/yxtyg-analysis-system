import request from '@/utils/request'

export function getEmbeddingConfigList() {
  return request({
    url: '/embedding-config/list',
    method: 'get'
  })
}

export function addEmbeddingConfig(data) {
  return request({
    url: '/embedding-config/add',
    method: 'post',
    data
  })
}

export function updateEmbeddingConfig(data) {
  return request({
    url: '/embedding-config/update',
    method: 'put',
    data
  })
}

export function deleteEmbeddingConfig(id) {
  return request({
    url: `/embedding-config/delete/${id}`,
    method: 'delete'
  })
}

export function testEmbeddingConnection(id) {
  return request({
    url: `/embedding-config/test/${id}`,
    method: 'post'
  })
}

export function testEmbeddingConnectionByConfig(data) {
  return request({
    url: '/embedding-config/test-config',
    method: 'post',
    data
  })
}

export function setDefaultEmbeddingConfig(id) {
  return request({
    url: `/embedding-config/set-default/${id}`,
    method: 'post'
  })
}
