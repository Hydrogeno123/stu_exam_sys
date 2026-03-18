import { createRouter, createWebHistory } from 'vue-router'

import LayoutView from '@/views/layout/index.vue'
import LoginView from '@/views/login/index.vue'
import IndexView from '@/views/index/index.vue'

// 管理员相关视图
import AdminDashboardView from '@/views/admin/dashboard/index.vue'
import AdminUsersView from '@/views/admin/users/index.vue'
import AdminExamsView from '@/views/admin/exams/index.vue'
import AdminQuestionsView from '@/views/admin/questions/index.vue'
import AdminClazzView from '@/views/admin/clazz/index.vue'

// 基础管理相关视图
import ClazzView from '@/views/clazz/index.vue'
import StuView from '@/views/stu/index.vue'

// 考试系统相关视图
import QuestionBankView from '@/views/exam/questionBank/index.vue'
import QuestionView from '@/views/exam/question/index.vue'
import ExamView from '@/views/exam/exam/index.vue'
import ExamScoreView from '@/views/exam/examScore/index.vue'
import StudentExamView from '@/views/exam/studentExam/index.vue'
import StudentScoreView from '@/views/exam/studentScore/index.vue'
import StuExamReportView from '@/views/exam/stuExamReport/index.vue'
import ExamReportView from '@/views/exam/examReport/index.vue'
import ScoreReportView from '@/views/exam/scoreReport/index.vue'
import ExamDoingView from '@/views/exam/doing/index.vue'
import ExamResultView from '@/views/exam/result/index.vue'

import { ElMessage } from 'element-plus'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
     path: '/', 
     name: '',
     component: LayoutView,
     redirect: '/index', //重定向
     children: [
      // 首页路由
      {path: '/index', name: 'index', component: IndexView},
      // 管理员相关路由
      {path: '/admin/dashboard', name: 'adminDashboard', component: AdminDashboardView, meta: { roles: [0] }},
      {path: '/admin/users', name: 'adminUsers', component: AdminUsersView, meta: { roles: [0] }},
      {path: '/admin/exams', name: 'adminExams', component: AdminExamsView, meta: { roles: [0] }},
      {path: '/admin/questions', name: 'adminQuestions', component: AdminQuestionsView, meta: { roles: [0] }},
      {path: '/admin/clazz', name: 'adminClazz', component: AdminClazzView, meta: { roles: [0] }},
      // 基础管理相关路由
      {path: '/clazz', name: 'clazz', component: ClazzView, meta: { roles: [1] }},
      {path: '/stu', name: 'stu', component: StuView, meta: { roles: [1] }},
      // 考试系统相关路由
      {path: '/questionBank', name: 'questionBank', component: QuestionBankView, meta: { roles: [1] }},
      {path: '/question', name: 'question', component: QuestionView, meta: { roles: [1] }},
      {path: '/exam', name: 'exam', component: ExamView, meta: { roles: [1] }},
      {path: '/examScore', name: 'examScore', component: ExamScoreView, meta: { roles: [1] }},
      {path: '/studentExam', name: 'studentExam', component: StudentExamView, meta: { roles: [2] }},
      {path: '/exam/doing/:examId', name: 'ExamDoing', component: ExamDoingView, meta: { roles: [2] }},
      {path: '/exam/result/:examId/:studentId', name: 'ExamResult', component: ExamResultView, meta: { roles: [2] }},
      {path: '/studentScore', name: 'studentScore', component: StudentScoreView, meta: { roles: [2] }},
      {path: '/stuexamReport', name: 'stuexamReport', component: StuExamReportView, meta: { roles: [2] }},
      {path: '/examReport', name: 'examReport', component: ExamReportView, meta: { roles: [1] }},
      {path: '/scoreReport', name: 'scoreReport', component: ScoreReportView, meta: { roles: [1] }},
     ]
    },
    {path: '/login', name: 'login', component: LoginView}
  ]
})

// 路由守卫 - 检查用户是否已登录及权限
router.beforeEach((to, from, next) => {
  // 检查是否前往登录页面
  if (to.path === '/login') {
    next()
    return
  }
  
  // 检查是否有token
  const token = localStorage.getItem('token')
  if (!token) {
    // 如果没有token，重定向到登录页面
    next('/login')
    return
  }

  // 检查用户角色权限
  const user = JSON.parse(token)
  const userRole = user.role
  
  // 如果路由有权限要求
  if (to.meta.roles) {
    const requiredRoles = to.meta.roles
    if (!requiredRoles.includes(userRole)) {
      // 权限不足，跳转到首页或错误页面
      ElMessage.warning('您没有访问该页面的权限')
      next('/index')
      return
    }
  }
  
  next()
})

export default router