import axios from 'axios'
import { ElMessage } from 'element-plus'  
import router from '../router'   //创建路由对象




//创建axios实例对象
const request = axios.create({
  baseURL: '/api',
  timeout: 600000 //请求超时时间
})

//axios 的 request 拦截器 - 获取localStorage中的token，在请求头中的token中添加token,添加失败的回调
request.interceptors.request.use(
  (config) => {
    const loginUser = JSON.parse(localStorage.getItem('token'))   //localStorage.setItem('token', JSON.stringify(result.data));
    if (loginUser&& loginUser.token) {
      config.headers.token = loginUser.token
    }
    return config
  }
)


//axios的响应 response 拦截器
request.interceptors.response.use(
  (response) => { //成功回调
    return response.data
  },
  (error) => { //失败回调
   //如果响应的状态码为401, 则路由到登录页面
   if (error.response.status === 401) {
    ElMessage.error('登录失效, 请重新登录')
    router.push('/login')  //路由到登录页面  同时会更新浏览器的历史记录 可以返回
  }else{
    ElMessage.error('接口访问异常')
  }
  }
)

export default request
