<script setup>
  import { ref, onMounted } from 'vue'
  import {loginApi} from '@/api/login'
  import { ElMessage } from 'element-plus'
  import { useRouter } from 'vue-router'


  let loginForm = ref({username:'', password:'', role: '0'})
  const router = useRouter()

// 角色选项
const roleOptions = [
  { label: '管理员', value: '0' },
  { label: '教师', value: '1' },
  { label: '学生', value: '2' }
]

// 获取角色文本
const getRoleText = (role) => {
  const roleMap = {
    '0': '管理员',
    '1': '教师',
    '2': '学生'
  }
  return roleMap[role] || '未知角色'
}

//登录
const login = async () =>{
  // 验证表单
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  if (!loginForm.value.role) {
    ElMessage.warning('请选择角色')
    return
  }
  
  try {
    const result = await loginApi(loginForm.value); //调用登录接口
    if(result.code){
      // 验证角色匹配
      if (result.data.role && result.data.role.toString() !== loginForm.value.role) {
        ElMessage.error(`当前账号为${getRoleText(result.data.role.toString())}，请选择正确的角色登录`)
        return
      }
      
      //1 提示登录成功
      ElMessage.success('登录成功');
      //存储用户的登录信息（包含角色信息）
      localStorage.setItem('token', JSON.stringify(result.data));  //登录信息是json 格式的 转换为字符串 存储  
      localStorage.setItem('userRole', loginForm.value.role); // 存储用户选择的角色
      
        // 2 根据角色跳转到不同页面
      if (loginForm.value.role === '0') {
        // 管理员跳转到管理员首页
        router.push('/admin/dashboard');
      } else if (loginForm.value.role === '1') {
        // 教师跳转到首页
        router.push('/index');
      } else {
        // 学生跳转到考试页面
        router.push('/studentExam');
      }
    }else{
      //3 提示登录失败
      ElMessage.error('用户名或密码错误');
    }
  } catch (error) {
    ElMessage.error('登录失败，请检查网络连接')
  }

}
//清空
const clear = () =>{
  loginForm.value = {username:'', password:'', role: '0'}

}

// 组件挂载时检查是否已登录
onMounted(() => {
  // 路由守卫会处理跳转逻辑，此处不再自动跳转
})
  
</script>

<template>
  <div class="education-login">
    <div class="login-form">
      <el-form label-width="80px">
        <p class="title">学生考试系统</p>
        
        <!-- 角色选择 -->
        <el-form-item label="身份" prop="role">
          <el-radio-group v-model="loginForm.role">
            <el-radio-button label="0">管理员</el-radio-button>
            <el-radio-button label="1">教师</el-radio-button>
            <el-radio-button label="2">学生</el-radio-button>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="loginForm.password" placeholder="请输入密码"></el-input>
        </el-form-item>

        <el-form-item>
          <div class="button-container">
            <el-button class="button" type="primary" @click="login">登 录</el-button>
            <el-button class="button" type="info" @click="clear">重 置</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
@import url('@/assets/education-theme.css');

.button-container {
  display: flex;
  gap: 16px;
  margin-top: 12px;
  width: 100%;
  justify-content: center;
  align-items: center;
}

.button {
  width: 120px !important;
  height: 40px !important;
  padding: 0 !important;
  margin: 0 !important;
  font-size: 15px !important;
  font-weight: 600 !important;
  line-height: 1 !important;
  text-align: center !important;
  box-sizing: border-box !important;
  border: 2px solid transparent !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  min-width: 120px !important;
  max-width: 120px !important;
}

.role-radio-group {
  width: 100%;
}

.role-radio-group .el-radio-button {
  flex: 1;
}

.role-radio-group .el-radio-button__inner {
  width: 100%;
  text-align: center;
}
</style>