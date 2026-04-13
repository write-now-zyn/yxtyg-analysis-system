import request from '@/utils/request'

// 分页查询会议纪要
export function getReviewList(params) {
  return request({
    url: '/review/list',
    method: 'get',
    params
  })
}

// 获取会议纪要详情
export function getReview(id) {
  return request({
    url: `/review/${id}`,
    method: 'get'
  })
}

// 新增会议纪要
export function addReview(data) {
  return request({
    url: '/review/add',
    method: 'post',
    data
  })
}

// 更新会议纪要
export function updateReview(data) {
  return request({
    url: '/review/update',
    method: 'put',
    data
  })
}

// 删除会议纪要
export function deleteReview(id) {
  return request({
    url: `/review/delete/${id}`,
    method: 'delete'
  })
}

// 导出Excel
export function exportReview(params) {
  window.open(`/api/review/export?${new URLSearchParams(params).toString()}`)
}

// AI解析与会人员
export function parseParticipants(text) {
  return request({
    url: '/ai/parse-participants',
    method: 'post',
    data: { text },
    timeout: 300000
  })
}
