<template>
  <div class="student-exam-container">
    <!-- 待参加考试列表 -->
    <div class="exam-list">
      <h2>待参加考试</h2>
      <div v-if="loading" style="text-align: center; padding: 20px">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else-if="pendingExams.length === 0" style="text-align: center; padding: 20px">
        <p>暂无待参加的考试</p>
      </div>
      <el-table v-else :data="pendingExams" border style="width: 100%">
        <el-table-column prop="examName" label="考试名称" width="200" />
        <el-table-column prop="bankName" label="所属题库" width="150" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column prop="duration" label="时长(分钟)" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              @click="goToExam(scope.row)"
              :disabled="isBeforeStartTime(scope.row)"
            >
              {{ isBeforeStartTime(scope.row) ? '未开始' : '进入考试' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 已考试记录 -->
    <div class="exam-history">
      <h2>考试记录</h2>
      <div v-if="historyLoading" style="text-align: center; padding: 20px">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else-if="examHistory.length === 0" style="text-align: center; padding: 20px">
        <p>暂无考试记录</p>
      </div>
      <el-table v-else :data="examHistory" border style="width: 100%">
        <el-table-column prop="examName" label="考试名称" width="200" />
        <el-table-column prop="totalScore" label="得分" width="100" />
        <el-table-column prop="correctCount" label="正确数" width="80" />
        <el-table-column label="是否及格" width="100">
          <template #default="scope">
            <el-tag :type="getPassStatus(scope.row)">
              {{ scope.row.totalScore >= (scope.row.passScore || 60) ? '及格' : '不及格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewExamResult(scope.row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getAvailableExamsApi, queryAllApi } from '@/api/exam/exam'
import { getStudentScoresApi } from '@/api/exam/examScore'

const router = useRouter()

// 待参加考试列表
const pendingExams = ref([])
const loading = ref(false)

// 考试记录
const examHistory = ref([])
const historyLoading = ref(false)

// 所有考试列表（用于查询考试总分和计算及格线）
const allExams = ref([])

// 学生ID，从登录信息中获取
const studentId = ref(null)

// 页面加载时获取学生ID
const getStudentId = () => {
  try {
    const token = localStorage.getItem('token')
    if (token) {
      const user = JSON.parse(token)
      if (user && user.id) {
        studentId.value = user.id
        console.log('获取学生ID成功:', studentId.value)
      } else {
        console.error('用户信息中没有ID字段:', user)
      }
    } else {
      console.error('未找到token')
    }
  } catch (error) {
    console.error('解析token失败:', error)
  }
}

// 获取可参加的考试列表
const loadPendingExams = async () => {
  if (!studentId.value) {
    console.error('学生ID为null，无法加载考试列表')
    ElMessage.error('无法获取学生信息')
    return
  }
  
  loading.value = true
  try {
    console.log('正在加载考试列表...学生ID:', studentId.value)
    const result = await getAvailableExamsApi(studentId.value)
    console.log('考试列表结果:', result)
    if (result && result.code) {
      pendingExams.value = result.data || []
      console.log('成功加载考试:', pendingExams.value.length)
    } else {
      ElMessage.error('加载考试列表失败')
    }
  } catch (error) {
    ElMessage.error('加载考试列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取考试记录
const loadExamHistory = async () => {
  if (!studentId.value) {
    console.error('学生ID为null，无法加载考试记录')
    ElMessage.error('无法获取学生信息')
    return
  }
  
  historyLoading.value = true
  try {
    console.log('正在加载考试记录...学生ID:', studentId.value)
    
    // 并行加载成绩记录和所有考试信息
    const [scoreResult, examsResult] = await Promise.all([
      getStudentScoresApi(studentId.value),
      queryAllApi()
    ])
    
    console.log('考试记录结果:', scoreResult)
    console.log('所有考试结果:', examsResult)
    
    if (scoreResult && scoreResult.code) {
      examHistory.value = scoreResult.data || []
      console.log('成功加载考试记录:', examHistory.value.length)
      
      // 保存所有考试信息
      if (examsResult && examsResult.code) {
        allExams.value = examsResult.data || []
      }
      
      // 为每条成绩记录添加考试信息和及格线
      examHistory.value.forEach(record => {
        const exam = allExams.value.find(e => e.id === record.examId)
        if (exam) {
          // 计算及格线（总分的60%取整数）
          record.passScore = Math.floor((exam.totalScore || 100) * 0.6)
        } else {
          // 如果找不到考试信息，默认60分
          record.passScore = 60
        }
      })
    } else {
      ElMessage.error('加载考试记录失败')
    }
  } catch (error) {
    ElMessage.error('加载考试记录失败')
    console.error(error)
  } finally {
    historyLoading.value = false
  }
}

// 检查是否在开始时间前
const isBeforeStartTime = (exam) => {
  const now = new Date()
  const startTime = new Date(exam.startTime)
  return now < startTime
}

// 获取及格状态类型
const getPassStatus = (record) => {
  // 使用提前计算好的passScore对比
  const passScore = record.passScore || 60
  return record.totalScore >= passScore ? 'success' : 'danger'
}

// 进入考试页面
const goToExam = async (exam) => {
  try {
    await ElMessageBox.confirm(`即将开始"${exam.examName}"考试，考试时长${exam.duration}分钟，确定要继续吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 跳转到答题页面
    router.push({
      name: 'ExamDoing',
      params: {
        examId: exam.id
      }
    })
  } catch (error) {
    // 用户取消
  }
}

// 查看考试结果详情
const viewExamResult = (record) => {
  router.push({
    name: 'ExamResult',
    params: {
      examId: record.examId,
      studentId: studentId.value
    }
  })
}

// 组件挂载
onMounted(() => {
  getStudentId()
  // 使用 nextTick 确保 studentId 已设置
  Promise.resolve().then(() => {
    loadPendingExams()
    loadExamHistory()
  })
})
</script>

<style scoped>
.student-exam-container {
  padding: 20px;
}

.exam-list {
  margin-bottom: 40px;
}

.exam-history {
  margin-top: 40px;
}

h1 {
  color: #333;
  margin-bottom: 20px;
}

h2 {
  color: #666;
  margin-bottom: 15px;
}
</style>