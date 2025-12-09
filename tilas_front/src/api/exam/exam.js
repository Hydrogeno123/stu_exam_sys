import request from '@/utils/request'

// 分页查询考试列表
export const queryPageApi = (params) => {
  return request({
    url: '/exams',
    method: 'get',
    params
  })
}

// 新增考试
export const addExamApi = (data) => {
  return request({
    url: '/exams',
    method: 'post',
    data
  })
}

// 根据ID查询考试
export const queryExamByIdApi = (id) => {
  return request({
    url: `/exams/${id}`,
    method: 'get'
  })
}

// 修改考试
export const updateExamApi = (data) => {
  return request({
    url: '/exams',
    method: 'put',
    data
  })
}

// 批量删除考试
export const deleteExamByIdsApi = (ids) => {
  return request({
    url: `/exams/${ids.join(',')}`,
    method: 'delete'
  })
}

// 根据教师ID查询考试列表
export const queryExamsByTeacherIdApi = (teacherId) => {
  return request({
    url: `/exams/teacher/${teacherId}`,
    method: 'get'
  })
}

// 查询学生可参加的考试
export const queryAvailableExamsApi = (studentId) => {
  return request({
    url: `/exams/student/${studentId}`,
    method: 'get'
  })
}

// 生成试卷
export const generateExamPaperApi = (examId) => {
  return request({
    url: `/exams/generate/${examId}`,
    method: 'post'
  })
}

// 更新考试状态
export const updateExamStatusApi = (examId, status) => {
  return request({
    url: `/exams/status/${examId}`,
    method: 'put',
    params: { status }
  })
}

// 查询所有考试（兼容旧导入）
export const queryAllApi = () => {
  return request({
    url: '/exams/all',
    method: 'get'
  })
}

// 查询学生可参加的考试（兼容旧导入）
export const getAvailableExamsApi = (studentId) => {
  return request({
    url: `/exams/student/${studentId}`,
    method: 'get'
  })
}

// 学生开始考试，获取试卷
export const startExamApi = (examId, studentId) => {
  return request({
    url: `/exams/start/${examId}/student/${studentId}`,
    method: 'post'
  })
}

// 学生提交答题
export const submitExamApi = (data) => {
  return request({
    url: '/exams/submit',
    method: 'post',
    data
  })
}

// 获取学生的答题详情
export const getStudentAnswersApi = (examId, studentId) => {
  return request({
    url: `/exams/answer/${examId}/student/${studentId}`,
    method: 'get'
  })
}

// 获取学生答题记录详情（用于成绩查看）
export const getScoreDetailsApi = (examId, studentId) => {
  return request({
    url: `/exams/score-details/${examId}/student/${studentId}`,
    method: 'get'
  })
}

// 保存考试的试卷题目
export const saveExamPapersApi = (examId, papers) => {
  return request({
    url: `/exams/${examId}/exam-papers`,
    method: 'post',
    data: papers
  })
}

// 获取考试的试卷题目
export const getExamPapersApi = (examId) => {
  return request({
    url: `/exams/${examId}/exam-papers`,
    method: 'get'
  })
}