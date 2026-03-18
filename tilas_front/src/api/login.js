import request from '@/utils/request'

// 用户登录
export const loginApi = (data) => {
  return request({
    url: '/users/login',
    method: 'post',
    data
  })
}

// 获取当前用户信息
export const getCurrentUserApi = () => {
  return request({
    url: '/users/current',
    method: 'get'
  })
}

// 修改密码
export const changePasswordApi = (data) => {
  return request({
    url: '/users/changePassword',
    method: 'put',
    data
  })
}

// 退出登录
export const logoutApi = () => {
  return request({
    url: '/users/logout',
    method: 'post'
  })
}