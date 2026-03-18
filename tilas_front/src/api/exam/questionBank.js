import request from '@/utils/request'

// 分页查询题库列表
export const queryPageApi = (bankName, page, size) => {
  return request({
    url: '/questionBanks',
    method: 'get',
    params: {
      bankName,
      page,
      pageSize: size
    }
  })
}

// 新增题库
export const addQuestionBankApi = (data) => {
  return request({
    url: '/questionBanks',
    method: 'post',
    data
  })
}

// 根据ID查询题库
export const queryQuestionBankByIdApi = (id) => {
  return request({
    url: `/questionBanks/${id}`,
    method: 'get'
  })
}

// 修改题库
export const updateQuestionBankApi = (data) => {
  return request({
    url: '/questionBanks',
    method: 'put',
    data
  })
}

// 批量删除题库
export const deleteQuestionBankByIdsApi = (ids) => {
  return request({
    url: `/questionBanks/${ids.join(',')}`,
    method: 'delete'
  })
}

// 查询所有题库
export const queryAllQuestionBanksApi = () => {
  return request({
    url: '/questionBanks/all',
    method: 'get'
  })
}

// 查询所有题库（兼容旧导入）
export const queryAllApi = () => {
  return request({
    url: '/questionBanks/all',
    method: 'get'
  })
}