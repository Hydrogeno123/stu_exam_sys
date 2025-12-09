<template>
  <div class="student-score-container">
    <div class="header">
      <h2>我的成绩</h2>
      <p>查看您的考试成绩和学习表现</p>
    </div>
    
    <div class="statistics-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-icon" style="color: #409EFF;">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ myStats.totalExams }}</div>
                <div class="stat-label">考试总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-icon" style="color: #67C23A;">
                <el-icon><Trophy /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ myStats.avgScore }}</div>
                <div class="stat-label">平均成绩</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-icon" style="color: #E6A23C;">
                <el-icon><Histogram /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ myStats.passRate }}%</div>
                <div class="stat-label">及格率</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-icon" style="color: #F56C6C;">
                <el-icon><Star /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ myStats.classRanking }}</div>
                <div class="stat-label">班级排名</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="score-table-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>成绩详情</span>
            <el-button type="primary" @click="loadMyScores" :loading="loading">刷新</el-button>
          </div>
        </template>
        <el-table :data="myScores" v-loading="loading" style="width: 100%">
          <el-table-column type="index" label="序号" width="80"></el-table-column>
          <el-table-column prop="examName" label="考试名称" width="180"></el-table-column>
          <el-table-column prop="totalScore" label="总分" width="100"></el-table-column>
          <el-table-column prop="correctCount" label="正确题数" width="120"></el-table-column>
          <el-table-column prop="totalScore" label="我的成绩" width="100">
            <template #default="scope">
              <span :class="{
                'excellent': scope.row.totalScore >= 90, 
                'good': scope.row.totalScore >= 80 && scope.row.totalScore < 90, 
                'pass': scope.row.totalScore >= (scope.row.passScore || 60) && scope.row.totalScore < 80, 
                'fail': scope.row.totalScore < (scope.row.passScore || 60)
              }">
                {{ scope.row.totalScore }}分
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="classRanking" label="班级排名" width="120">
            <template #default="scope">
              第{{ scope.row.classRanking }}名
            </template>
          </el-table-column>
          <el-table-column prop="submitTime" label="考试时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.submitTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button type="primary" link @click="viewDetail(scope.row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 成绩详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="成绩详情" width="800px">
      <div v-if="currentScore">
        <el-row :gutter="20" style="margin-bottom: 20px;">
          <el-col :span="12">
            <h3>基本信息</h3>
            <p><strong>考试名称:</strong>{{ currentScore.examName }}</p>
            <p><strong>考试时间:</strong>{{ formatDate(currentScore.submitTime) }}</p>
            <p><strong>我的成绩:</strong>{{ currentScore.totalScore }}分</p>
          </el-col>
          <el-col :span="12">
            <h3>成绩分析</h3>
            <p><strong>班级排名:</strong>第{{ currentScore.classRanking }}名</p>
            <p><strong>正确题数:</strong>{{ currentScore.correctCount }}/{{ currentScore.totalQuestions || 'N/A' }}</p>
            <p><strong>及格状态:</strong>
              <el-tag :type="currentScore.totalScore >= (currentScore.passScore || 60) ? 'success' : 'danger'">
                {{ currentScore.totalScore >= (currentScore.passScore || 60) ? '及格' : '不及格' }}
              </el-tag>
            </p>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document, Trophy, Histogram, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getStudentScoresApi, getExamRankingApi } from '@/api/exam/examScore'
import { queryAllApi } from '@/api/exam/exam'

// 响应式数据
const loading = ref(false)
const myScores = ref([])
const allExams = ref([])
const myStats = ref({
  totalExams: 0,
  avgScore: 0,
  passRate: 0,
  classRanking: 0
})

const detailDialogVisible = ref(false)
const currentScore = ref(null)

// 获取当前用户信息
const getCurrentUser = () => {
  const user = localStorage.getItem('token')
  return user ? JSON.parse(user) : null
}

// 加载我的成绩数据
const loadMyScores = async () => {
  const user = getCurrentUser()
  if (!user) {
    ElMessage.error('用户未登录')
    return
  }

  loading.value = true
  try {
    console.log('正在加载学生成绩...')
    
    // 并行加载学生成绩和所有考试
    const [scoresResult, examsResult] = await Promise.all([
      getStudentScoresApi(user.id),
      queryAllApi()
    ])
    
    console.log('成绩数据结果:', scoresResult)
    console.log('考试数据结果:', examsResult)
    
    if (scoresResult && scoresResult.code) {
      myScores.value = scoresResult.data || []
      console.log('成功加载成绩:', myScores.value.length)
      
      // 保存所有考试信息
      if (examsResult && examsResult.code) {
        allExams.value = examsResult.data || []
      }
      
      // 为每条成绩记录添加考试信息和及格线
      myScores.value.forEach(record => {
        const exam = allExams.value.find(e => e.id === record.examId)
        if (exam) {
          // 计算及格线（总分的60%取整数）
          record.passScore = Math.floor((exam.totalScore || 100) * 0.6)
          record.examName = exam.examName
          record.totalQuestions = exam.choiceCount + exam.fillCount
          record.examTotalScore = exam.totalScore // 添加考试总分字段
        } else {
          // 如果找不到考试信息，默认60分
          record.passScore = 60
          record.examTotalScore = 100
        }
      })
      
      // 并行加载每个考试的排名信息，然后计算班级排名
      const rankingPromises = myScores.value.map(record => 
        getExamRankingApi(record.examId).then(result => {
          if (result && result.code && result.data && Array.isArray(result.data)) {
            // 在返回的排名列表中找到当前学生的位置（排名是数组索引+1）
            const rankingIndex = result.data.findIndex(r => r.studentId === user.id)
            record.classRanking = rankingIndex >= 0 ? (rankingIndex + 1) : '-'
            console.log(`考试ID ${record.examId} 排名列表:`, result.data)
            console.log(`学生ID ${user.id} 在排名中位置: ${rankingIndex}, 排名: ${record.classRanking}`)
          } else {
            record.classRanking = '-'
          }
        }).catch(error => {
          console.error('获取排名信息失败:', error)
          record.classRanking = '-'
        })
      )
      
      // 所有排名加载完成后计算统计数据
      await Promise.all(rankingPromises)
      calculateStats()
    } else {
      ElMessage.error('获取成绩数据失败')
    }
  } catch (error) {
    console.error('获取成绩数据失败:', error)
    ElMessage.error('获取成绩数据失败')
  } finally {
    loading.value = false
  }
}

// 计算统计数据
const calculateStats = () => {
  if (myScores.value.length === 0) return

  const totalExams = myScores.value.length
  const avgScore = Math.round(myScores.value.reduce((sum, score) => sum + score.totalScore, 0) / totalExams)
  
  // 根据不同考试的及格线计算及格数
  const passCount = myScores.value.filter(score => {
    const passScore = score.passScore || 60
    return score.totalScore >= passScore
  }).length
  const passRate = Math.round((passCount / totalExams) * 100)

  // 获取最新排名（从最近提交的考试中获取）
  let latestRanking = '-'
  const sortedBySubmitTime = [...myScores.value].sort((a, b) => {
    const timeA = new Date(a.submitTime || 0).getTime()
    const timeB = new Date(b.submitTime || 0).getTime()
    return timeB - timeA // 按提交时间降序排序
  })
  if (sortedBySubmitTime.length > 0) {
    const ranking = sortedBySubmitTime[0].classRanking
    if (ranking && ranking !== '-') {
      latestRanking = ranking
    }
  }

  myStats.value = {
    totalExams,
    avgScore,
    passRate,
    classRanking: latestRanking
  }
}

// 查看成绩详情
const viewDetail = (score) => {
  currentScore.value = score
  detailDialogVisible.value = true
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 组件挂载时加载数据
onMounted(() => {
  loadMyScores()
})
</script>

<style scoped>
.student-score-container {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #303133;
}

.header p {
  margin: 5px 0 0 0;
  color: #909399;
}

.statistics-section {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
}

.stat-item {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  font-size: 40px;
  margin-right: 15px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.score-table-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.excellent {
  color: #67C23A;
  font-weight: bold;
}

.good {
  color: #409EFF;
  font-weight: bold;
}

.pass {
  color: #E6A23C;
  font-weight: bold;
}

.fail {
  color: #F56C6C;
  font-weight: bold;
}
</style>