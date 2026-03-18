import request from '@/utils/request'

// 分页查询题目列表
export const queryPageApi = (bankId, type, content, page, size) => {
  return request({
    url: '/questions',
    method: 'get',
    params: {
      bankId,
      type,
      content,
      page,
      pageSize: size
    }
  })
}

// 查询所有题目
export const queryAllQuestionsApi = () => {
  return request({
    url: '/questions/all',
    method: 'get'
  })
}

// 新增题目
export const addQuestionApi = (data) => {
  return request({
    url: '/questions',
    method: 'post',
    data
  })
}

// 根据ID查询题目
export const queryQuestionByIdApi = (id) => {
  return request({
    url: `/questions/${id}`,
    method: 'get'
  })
}

// 修改题目
export const updateQuestionApi = (data) => {
  return request({
    url: '/questions',
    method: 'put',
    data
  })
}

// 批量删除题目
export const deleteQuestionByIdsApi = (ids) => {
  return request({
    url: `/questions/${ids.join(',')}`,
    method: 'delete'
  })
}

// 根据题库ID查询题目列表
export const queryQuestionsByBankIdApi = (bankId) => {
  return request({
    url: `/questions/bank/${bankId}`,
    method: 'get'
  })
}

// 随机获取题目
export const getRandomQuestionsApi = (bankId, count) => {
  return request({
    url: '/questions/random',
    method: 'get',
    params: {
      bankId,
      count
    }
  })
}