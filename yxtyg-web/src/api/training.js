import request from '@/utils/request'

// 分页查询上报记录
export function getTrainingReportList(params) {
  return request({
    url: '/training/report/list',
    method: 'get',
    params
  })
}

// 获取上报详情
export function getTrainingReportDetail(id) {
  return request({
    url: `/training/report/detail/${id}`,
    method: 'get'
  })
}

// 新增培训上报
export function addTrainingReport(data) {
  return request({
    url: '/training/report/add',
    method: 'post',
    data
  })
}

// 更新培训上报
export function updateTrainingReport(data) {
  return request({
    url: '/training/report/update',
    method: 'put',
    data
  })
}

// 删除培训上报
export function deleteTrainingReport(id) {
  return request({
    url: `/training/report/delete/${id}`,
    method: 'delete'
  })
}

// 导出Excel
export function exportTraining(params) {
  window.open(`/api/training/export?${new URLSearchParams(params).toString()}`)
}

// 获取培训截图
export function getTrainingImage(id) {
  return `/api/training/record/image/${id}`
}

// AI解析培训记录
export function parseTrainingRecords(text) {
  return request({
    url: '/ai/parse-training',
    method: 'post',
    data: { text },
    timeout: 300000
  })
}
