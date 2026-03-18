import request from '@/utils/request'

// 分页查询班级列表
export const getClazzListApi = (params) => {
  return request({
    url: '/clazz',
    method: 'get',
    params
  })
}

// 新增班级
export const saveClazzApi = (data) => {
  return request({
    url: '/clazz',
    method: 'post',
    data
  })
}

// 根据ID查询班级
export const getClazzByIdApi = (id) => {
  return request({
    url: `/clazz/${id}`,
    method: 'get'
  })
}

// 修改班级
export const updateClazzApi = (data) => {
  return request({
    url: '/clazz',
    method: 'put',
    data
  })
}

// 删除班级
export const deleteClazzApi = (ids) => {
  return request({
    url: `/clazz/${ids}`,
    method: 'delete'
  })
}

// 根据教师ID查询班级列表
export const getClazzByTeacherIdApi = (teacherId) => {
  return request({
    url: `/clazz/teacher/${teacherId}`,
    method: 'get'
  })
}

// 查询所有班级（用于下拉选择）
export const getAllClazzApi = () => {
  return request({
    url: '/clazz/all',
    method: 'get'
  })
}

// 统计班级数量
export const getClazzCountApi = () => {
  return request({
    url: '/clazz/count',
    method: 'get'
  })
}