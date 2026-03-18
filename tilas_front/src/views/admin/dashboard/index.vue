<template>
  <div class="admin-dashboard">
    <div class="header">
      <h1>管理员仪表板</h1>
      <p class="subtitle">欢迎使用系统管理平台</p>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics-container">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">用户总数</p>
          <p class="stat-value">{{ statistics.totalUsers }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
          <el-icon><UserFilled /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">教师数量</p>
          <p class="stat-value">{{ statistics.teacherCount }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">学生总数</p>
          <p class="stat-value">{{ statistics.studentCount }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
          <el-icon><HomeFilled /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">班级总数</p>
          <p class="stat-value">{{ statistics.totalClazz }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #30cfd0 0%, #330867 100%)">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">考试总数</p>
          <p class="stat-value">{{ statistics.totalExams }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)">
          <el-icon><QuestionFilled /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">试题总数</p>
          <p class="stat-value">{{ statistics.totalQuestions }}</p>
        </div>
      </div>
    </div>

    <!-- 快速操作 -->
    <div class="quick-actions">
      <h2>快速操作</h2>
      <div class="actions-grid">
        <el-button type="primary" size="large" @click="goTo('/admin/users')">
          <el-icon><UserFilled /></el-icon>
          用户管理
        </el-button>
        <el-button type="primary" size="large" @click="goTo('/admin/clazz')">
          <el-icon><HomeFilled /></el-icon>
          班级管理
        </el-button>
        <el-button type="primary" size="large" @click="goTo('/admin/questions')">
          <el-icon><QuestionFilled /></el-icon>
          试题管理
        </el-button>
        <el-button type="primary" size="large" @click="goTo('/admin/exams')">
          <el-icon><Document /></el-icon>
          考试管理
        </el-button>
      </div>
    </div>

    <!-- 系统信息 -->
    <div class="system-info">
      <h2>系统信息</h2>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="系统名称">学生考试系统</el-descriptions-item>
        <el-descriptions-item label="当前版本">v1.0.0</el-descriptions-item>
        <el-descriptions-item label="最后更新">{{ formatTime(lastUpdateTime) }}</el-descriptions-item>
        <el-descriptions-item label="管理员">{{ adminName }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, UserFilled, HomeFilled, QuestionFilled, Document } from '@element-plus/icons-vue'
import { getAdminUsersApi, getAdminClazzesApi, getAdminExamsApi, getAdminQuestionsApi } from '@/api/admin'

const router = useRouter()

// 统计数据
const statistics = reactive({
  totalUsers: 0,
  teacherCount: 0,
  studentCount: 0,
  totalClazz: 0,
  totalExams: 0,
  totalQuestions: 0
})

const adminName = ref('')
const lastUpdateTime = ref(new Date())

// 格式化时间
const formatTime = (date) => {
  const d = new Date(date)
  return d.getFullYear() + '-' + 
         String(d.getMonth() + 1).padStart(2, '0') + '-' + 
         String(d.getDate()).padStart(2, '0') + ' ' +
         String(d.getHours()).padStart(2, '0') + ':' + 
         String(d.getMinutes()).padStart(2, '0') + ':' + 
         String(d.getSeconds()).padStart(2, '0')
}

// 导航到指定页面
const goTo = (path) => {
  router.push(path)
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const [usersRes, clazzRes, examsRes, questionsRes] = await Promise.all([
      getAdminUsersApi({ page: 1, pageSize: 1 }),
      getAdminClazzesApi({ page: 1, pageSize: 1 }),
      getAdminExamsApi({ page: 1, pageSize: 1 }),
      getAdminQuestionsApi({ page: 1, pageSize: 1 })
    ])

    if (usersRes.code === 1) {
      statistics.totalUsers = usersRes.data.total || 0
      // 计算教师和学生数量
      const users = usersRes.data.records || []
      statistics.teacherCount = users.filter(u => u.role === 1).length
      statistics.studentCount = users.filter(u => u.role === 2).length
    }

    if (clazzRes.code === 1) {
      statistics.totalClazz = clazzRes.data.total || 0
    }

    if (examsRes.code === 1) {
      statistics.totalExams = examsRes.data.total || 0
    }

    if (questionsRes.code === 1) {
      statistics.totalQuestions = questionsRes.data.total || 0
    }
  } catch (error) {
    console.error('加载统计数据异常:', error)
  }
}

// 组件挂载时加载数据
onMounted(() => {
  const loginUser = JSON.parse(localStorage.getItem('token') || '{}')
  adminName.value = loginUser.name || '管理员'
  loadStatistics()
})
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.header {
  margin-bottom: 30px;
}

.header h1 {
  margin: 0 0 5px 0;
  font-size: 28px;
  font-weight: bold;
  color: #1f2d3d;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

/* 统计卡片 */
.statistics-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-card:hover {
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 32px;
  margin-right: 15px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-label {
  margin: 0;
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-value {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #1f2d3d;
}

/* 快速操作 */
.quick-actions {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.quick-actions h2 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  color: #1f2d3d;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.actions-grid :deep(.el-button) {
  height: 48px;
  font-size: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* 系统信息 */
.system-info {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.system-info h2 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  color: #1f2d3d;
}

@media (max-width: 768px) {
  .statistics-container {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }

  .stat-card {
    flex-direction: column;
    text-align: center;
  }

  .stat-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
