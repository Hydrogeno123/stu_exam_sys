import request from '@/utils/request'

// 根据考试ID查询成绩列表
export const queryScoresByExamIdApi = (examId) => {
  return request({
    url: `/examScores/exam/${examId}`,
    method: 'get'
  })
}

// 根据学生ID查询成绩列表
export const queryScoresByStudentIdApi = (studentId) => {
  return request({
    url: `/examScores/student/${studentId}`,
    method: 'get'
  })
}

// 根据考试ID和学生ID查询成绩
export const queryScoreApi = (examId, studentId) => {
  return request({
    url: '/examScores/exam/' + examId + '/student/' + studentId,
    method: 'get'
  })
}

// 保存成绩
export const saveExamScoreApi = (data) => {
  return request({
    url: '/examScores',
    method: 'post',
    data
  })
}

// 修改成绩
export const updateExamScoreApi = (data) => {
  return request({
    url: '/examScores',
    method: 'put',
    data
  })
}

// 获取考试排名
export const getExamRankingApi = (examId) => {
  return request({
    url: `/examScores/ranking/${examId}`,
    method: 'get'
  })
}

// 根据考试ID查询成绩列表（兼容旧导入）
export const queryByExamIdApi = (examId) => {
  return request({
    url: `/examScores/exam/${examId}`,
    method: 'get'
  })
}

// 获取学生成绩（兼容旧导入）
export const getStudentScoresApi = (studentId) => {
  return request({
    url: `/examScores/student/${studentId}`,
    method: 'get'
  })
}

// 分页查询成绩（兼容旧导入）
export const queryPageApi = (params) => {
  return request({
    url: '/examScores/page',
    method: 'get',
    params
  })
}

// 查询全量统计数据（不分页，用于统计块）
export const getStatisticsApi = (params) => {
  return request({
    url: '/examScores/statistics',
    method: 'get',
    params
  })
}

// 获取考试统计报告数据
export const getExamReportDataApi = (params) => {
  return request({
    url: '/examScores/report/exam-report',
    method: 'get',
    params: { ...params, bankId: params.bankId }
  })
}

// 获取考试趋势统计数据
export const getExamTrendDataApi = (params) => {
  return request({
    url: '/examScores/report/exam-trend',
    method: 'get',
    params: { ...params, bankId: params.bankId }
  })
}

// 获取总体统计数据
export const getOverallStatisticsApi = (params) => {
  return request({
    url: '/examScores/report/overall-statistics',
    method: 'get',
    params: { ...params, bankId: params.bankId }
  })
}

// 获取成绩分数段统计
export const getScoreSegmentsApi = (params) => {
  return request({
    url: '/examScores/report/score-segments',
    method: 'get',
    params
  })
}

// 删除学生的考试记录
export const deleteStudentExamRecordApi = (examId, studentId) => {
  return request({
    url: `/examScores/student/${studentId}/exam/${examId}`,
    method: 'delete'
  })
}