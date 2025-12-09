import request from '@/utils/request'

// ===================== 用户管理 API =====================

// 分页查询用户列表（包括所有角色）
export const getAdminUsersApi = (params) => {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

// 新增用户
export const addAdminUserApi = (data) => {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

// 修改用户信息
export const updateAdminUserApi = (data) => {
  return request({
    url: '/users',
    method: 'put',
    data
  })
}

// 删除用户
export const deleteAdminUserApi = (ids) => {
  return request({
    url: `/users/${ids}`,
    method: 'delete'
  })
}

// 根据ID查询用户详情
export const getAdminUserByIdApi = (id) => {
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}

// ===================== 考试管理 API =====================

// 分页查询所有考试
export const getAdminExamsApi = (params) => {
  return request({
    url: '/exams',
    method: 'get',
    params
  })
}

// 新增考试
export const addAdminExamApi = (data) => {
  return request({
    url: '/exams',
    method: 'post',
    data
  })
}

// 修改考试信息
export const updateAdminExamApi = (data) => {
  return request({
    url: '/exams',
    method: 'put',
    data
  })
}

// 删除考试
export const deleteAdminExamApi = (ids) => {
  return request({
    url: `/exams/${ids}`,
    method: 'delete'
  })
}

// 根据ID查询考试详情
export const getAdminExamByIdApi = (id) => {
  return request({
    url: `/exams/${id}`,
    method: 'get'
  })
}

// ===================== 试卷管理 API =====================

// 分页查询试卷
export const getAdminExamPapersApi = (params) => {
  return request({
    url: '/exam-papers',
    method: 'get',
    params
  })
}

// 添加试题到试卷
export const addQuestionToExamApi = (data) => {
  return request({
    url: '/exam-papers',
    method: 'post',
    data
  })
}

// 从试卷删除试题
export const deleteQuestionFromExamApi = (examId, questionId) => {
  return request({
    url: `/exam-papers/${examId}/${questionId}`,
    method: 'delete'
  })
}

// ===================== 试题管理 API =====================

// 分页查询所有试题
export const getAdminQuestionsApi = (params) => {
  return request({
    url: '/questions',
    method: 'get',
    params
  })
}

// 新增试题
export const addAdminQuestionApi = (data) => {
  return request({
    url: '/questions',
    method: 'post',
    data
  })
}

// 修改试题
export const updateAdminQuestionApi = (data) => {
  return request({
    url: '/questions',
    method: 'put',
    data
  })
}

// 删除试题
export const deleteAdminQuestionApi = (ids) => {
  return request({
    url: `/questions/${ids}`,
    method: 'delete'
  })
}

// 根据ID查询试题详情
export const getAdminQuestionByIdApi = (id) => {
  return request({
    url: `/questions/${id}`,
    method: 'get'
  })
}

// 分页查询题库
export const getAdminQuestionBanksApi = (params) => {
  return request({
    url: '/questionBanks',
    method: 'get',
    params
  })
}

// ===================== 班级管理 API =====================

// 分页查询班级列表
export const getAdminClazzesApi = (params) => {
  return request({
    url: '/clazz',
    method: 'get',
    params
  })
}

// 新增班级
export const addAdminClazzApi = (data) => {
  return request({
    url: '/clazz',
    method: 'post',
    data
  })
}

// 修改班级信息
export const updateAdminClazzApi = (data) => {
  return request({
    url: '/clazz',
    method: 'put',
    data
  })
}

// 删除班级
export const deleteAdminClazzApi = (ids) => {
  return request({
    url: `/clazz/${ids}`,
    method: 'delete'
  })
}

// 根据ID查询班级详情
export const getAdminClazzByIdApi = (id) => {
  return request({
    url: `/clazz/${id}`,
    method: 'get'
  })
}

// 查询所有班级（用于下拉选择）
export const getAllAdminClazzesApi = () => {
  return request({
    url: '/clazz/all',
    method: 'get'
  })
}
