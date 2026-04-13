import request from '@/utils/request'

/**
 * 工单相关API
 */

// 分页查询工单列表
export function getWorkOrderList(params) {
  return request({
    url: '/workorder/list',
    method: 'get',
    params
  })
}

// 获取工单详情
export function getWorkOrderDetail(id) {
  return request({
    url: `/workorder/detail/${id}`,
    method: 'get'
  })
}

// 导入工单Excel
export function importWorkOrder(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/workorder/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除工单
export function deleteWorkOrder(id) {
  return request({
    url: `/workorder/${id}`,
    method: 'delete'
  })
}

// 批量删除工单
export function batchDeleteWorkOrder(ids) {
  return request({
    url: '/workorder/batchDelete',
    method: 'post',
    data: ids
  })
}
