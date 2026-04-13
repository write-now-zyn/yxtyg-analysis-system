import request from '@/utils/request'

export function getAgentConfigList() {
  return request({
    url: '/agent-config/list',
    method: 'get'
  })
}

export function updateAgentConfig(data) {
  return request({
    url: '/agent-config/update',
    method: 'put',
    data
  })
}

export function getAgentConfigDetail(code) {
  return request({
    url: `/agent-config/detail/${code}`,
    method: 'get'
  })
}
