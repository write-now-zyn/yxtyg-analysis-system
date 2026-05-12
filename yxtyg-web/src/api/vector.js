import request from '@/utils/request'

/**
 * 智能分单相关API
 */

// 智能推荐
export function recommend(data) {
  return request({
    url: '/vector/recommend',
    method: 'post',
    data,
    timeout: 300000
  })
}

// 向量搜索
export function vectorMatch(data) {
  return request({
    url: '/vector/match',
    method: 'post',
    data
  })
}

// 获取向量统计
export function getVectorStats() {
  return request({
    url: '/vector/stats',
    method: 'get'
  })
}

// 增量同步
export function syncVector() {
  return request({
    url: '/vector/sync',
    method: 'post',
    timeout: 300000
  })
}
