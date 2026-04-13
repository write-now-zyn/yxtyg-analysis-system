import request from '@/utils/request'

/**
 * 看板相关API
 */

// 获取看板概览数据
export function getDashboardOverview(params) {
  return request({
    url: '/dashboard/overview',
    method: 'get',
    params
  })
}
