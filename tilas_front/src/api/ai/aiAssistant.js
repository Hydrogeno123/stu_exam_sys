import request from '@/utils/request'

export const chatApi = (data) => {
  return request({
    url: '/ai/chat',
    method: 'post',
    data
  })
}

export const chatStreamApi = (params) => {
  const loginUser = JSON.parse(localStorage.getItem('token'))
  const token = loginUser?.token || ''
  return fetch(`/api/ai/chat/stream?message=${encodeURIComponent(params.message)}${params.userId ? '&userId=' + params.userId : ''}${params.userRole ? '&userRole=' + params.userRole : ''}`, {
    method: 'GET',
    headers: {
      'token': token,
      'Accept': 'text/event-stream'
    }
  })
}

export const getAiConfigApi = () => {
  return request({
    url: '/ai/config',
    method: 'get'
  })
}

export const updateAiConfigApi = (data) => {
  return request({
    url: '/ai/config',
    method: 'post',
    data
  })
}

export const analyzeExamDataApi = (examId) => {
  return request({
    url: `/ai/analyze/exam/${examId}`,
    method: 'get'
  })
}

export const analyzeStudentDataApi = (studentId) => {
  return request({
    url: `/ai/analyze/student/${studentId}`,
    method: 'get'
  })
}

export const answerQuestionApi = (questionId) => {
  return request({
    url: `/ai/answer/${questionId}`,
    method: 'get'
  })
}
