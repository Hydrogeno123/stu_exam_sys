import request from '@/utils/request'

// 查询所有教师
export const findAllTeachersApi = () => {
  return request({
    url: '/users/teachers',
    method: 'get'
  })
}

// 根据ID查询教师
export const getTeacherByIdApi = (id) => {
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}
