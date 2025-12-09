import request from '@/utils/request'

// 分页查询学员列表
export const getStuListApi = (params) => {
  return request({
    url: '/users',
    method: 'get',
    params: {
      ...params,
      role: 2 // 只查询学员（学生）
    }
  })
}

// 新增学员
export const saveStuApi = (data) => {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

// 根据ID查询学员
export const getStuByIdApi = (id) => {
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}

// 修改学员
export const updateStuApi = (data) => {
  return request({
    url: '/users',
    method: 'put',
    data
  })
}

// 删除学员
export const deleteStuApi = (ids) => {
  return request({
    url: `/users/${ids}`,
    method: 'delete'
  })
}

// 查询所有学员（用于下拉选择）
export const getAllStuApi = () => {
  return request({
    url: '/users/students',
    method: 'get'
  })
}

// 根据班级ID查询学员列表
export const getStuByClazzIdApi = (clazzId) => {
  return request({
    url: `/users/students/clazz/${clazzId}`,
    method: 'get'
  })
}

// 更新学员班级信息
export const updateStuClazzApi = (studentId, clazzId) => {
  return request({
    url: `/users/students/${studentId}/clazz/${clazzId}`,
    method: 'put'
  })
}

// 统计班级学员数量
export const getStuCountByClazzIdApi = (clazzId) => {
  return request({
    url: `/users/students/clazz/${clazzId}/count`,
    method: 'get'
  })
}