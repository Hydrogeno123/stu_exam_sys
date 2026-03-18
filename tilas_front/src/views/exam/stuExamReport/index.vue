<template>
  <div class="stu-exam-report-container">
    <div class="header">
      <h2>班级成绩查看</h2>
      <p>查看您所在班级的整体成绩情况和您的个人表现</p>
    </div>

    <div class="exam-selector-section">
      <el-card>
        <el-form :inline="true">
          <el-form-item label="考试选择">
            <el-select v-model="selectedExamId" placeholder="请选择考试" @change="handleExamChange">
              <el-option
                v-for="exam in examList"
                :key="exam.id"
                :label="exam.examName"
                :value="exam.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadClassExamData" :loading="loading">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <div v-if="selectedExamId" class="statistics-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-icon" style="color: #409EFF;">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ classStats.totalStudents }}</div>
                <div class="stat-label">班级参与总人数</div>
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
                <div class="stat-value">{{ classStats.avgScore }}</div>
                <div class="stat-label">班级平均分</div>
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
                <div class="stat-value">{{ classStats.passRate }}%</div>
                <div class="stat-label">班级及格率</div>
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
                <div class="stat-value">{{ myScore.rank || '-' }}</div>
                <div class="stat-label">我的排名</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-if="selectedExamId" class="score-distribution-section">
      <el-card>
        <template #header>
          <span>成绩分布</span>
        </template>
        <div class="distribution-content">
          <div class="distribution-bars">
            <div
              v-for="segment in scoreDistribution"
              :key="segment.label"
              class="distribution-bar"
            >
              <div class="segment-label">{{ segment.label }}</div>
              <div class="bar-container">
                <div
                  class="bar-fill"
                  :style="{ 
                    width: (segment.count / classStats.totalStudents * 100) + '%',
                    backgroundColor: segment.color
                  }"
                ></div>
                <div class="bar-text">{{ segment.count }}人 ({{ Math.round(segment.count / classStats.totalStudents * 100) }}%)</div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <div v-if="selectedExamId" class="class-scores-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>班级成绩列表</span>
            <el-tag type="info">班级人数：{{ classStats.totalStudents }}人</el-tag>
          </div>
        </template>
        <el-table :data="studentScores" v-loading="loading" style="width: 100%" :row-class-name="getRowClassName">
          <el-table-column type="index" label="排名" width="80">
            <template #default="scope">
              <span v-if="scope.row.studentId === currentUserId" class="my-rank">{{ scope.$index + 1 }}</span>
              <span v-else>{{ scope.$index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="studentName" label="姓名" width="120">
            <template #default="scope">
              <span v-if="scope.row.studentId === currentUserId" class="my-name">{{ scope.row.studentName }}</span>
              <span v-else>{{ scope.row.studentName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalScore" label="成绩" width="100">
            <template #default="scope">
              <span :class="{
                'excellent': scope.row.totalScore >= 90, 
                'good': scope.row.totalScore >= 80 && scope.row.totalScore < 90, 
                'pass': scope.row.totalScore >= passScore && scope.row.totalScore < 80, 
                'fail': scope.row.totalScore < passScore
              }">
                {{ scope.row.totalScore }}分
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="correctCount" label="正确题数" width="120">
            <template #default="scope">
              {{ scope.row.correctCount }}/{{ scope.row.totalQuestions || 'N/A' }}
            </template>
          </el-table-column>
          <el-table-column prop="totalScore" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.totalScore >= passScore ? 'success' : 'danger'">
                {{ scope.row.totalScore >= passScore ? '及格' : '不及格' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, Trophy, Histogram, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { queryAllApi } from '@/api/exam/exam'
import { queryPageApi as getScoresApi } from '@/api/exam/examScore'

// 响应式数据
const loading = ref(false)
const examList = ref([])
const selectedExamId = ref('')
const selectedExam = ref(null) // 保存当前选中的考试信息
const studentScores = ref([])
const passScore = ref(60) // 及格线
const classStats = ref({
  totalStudents: 0,
  avgScore: 0,
  passRate: 0
})
const myScore = ref({
  score: 0,
  rank: 0,
  studentId: 0
})
const currentUserId = ref('')
const classId = ref('')

// 成绩分布数据
const scoreDistribution = ref([])

// 获取当前用户信息
const getCurrentUser = () => {
  const token = localStorage.getItem('token')
  return token ? JSON.parse(token) : null
}

// 加载考试列表
const loadExams = async () => {
  try {
    const response = await queryAllApi()
    if (response && response.code) {
      examList.value = response.data || []
      // 默认选择第一个考试
      if (examList.value.length > 0 && !selectedExamId.value) {
        selectedExamId.value = examList.value[0].id
        await loadClassExamData()
      }
    }
  } catch (error) {
    console.error('获取考试列表失败:', error)
    ElMessage.error('获取考试列表失败')
  }
}

// 获取班级成绩数据
const loadClassExamData = async () => {
  if (!selectedExamId.value || !classId.value) return
  
  loading.value = true
  try {
    console.log(`加载班级成绩: examId=${selectedExamId.value}, clazzId=${classId.value}`)
    
    // 获取当前选中的考试信息
    selectedExam.value = examList.value.find(e => e.id === selectedExamId.value)
    console.log('当前选中的考试:', selectedExam.value)
    
    // 执行查询，获取该考试该班级的所有成绩
    const response = await getScoresApi({
      page: 1,
      pageSize: 1000,
      examId: selectedExamId.value,
      clazzId: classId.value
    })
    
    if (response && response.code) {
      const scores = response.data.rows || []
      console.log(`获取到 ${scores.length} 条成绩`)
      
      // 计算及格线：总分的60%取整
      if (selectedExam.value) {
        passScore.value = Math.floor((selectedExam.value.totalScore || 100) * 0.6)
      } else {
        passScore.value = 60
      }
      console.log(`及格线: ${passScore.value}`)
      
      // 为每条成绩记录添加及格线
      scores.forEach(s => {
        s.passScore = passScore.value
      })
      
      // 按总分降序排序
      scores.sort((a, b) => (b.totalScore || 0) - (a.totalScore || 0))
      studentScores.value = scores
      
      // 计算班级统计信息（及格线基于动态计算）
      calculateClassStats(scores)
      
      // 找到当前学生的信息并计算排名
      const currentUserScore = scores.find(s => s.studentId === currentUserId.value)
      if (currentUserScore) {
        const ranking = scores.findIndex(s => s.studentId === currentUserId.value) + 1
        myScore.value = {
          score: currentUserScore.totalScore,
          rank: ranking,
          studentId: currentUserScore.studentId,
          totalScore: currentUserScore.totalScore,
          correctCount: currentUserScore.correctCount
        }
        console.log(`当前学生排名: ${ranking}`)
      }
      
      // 生成成绩分布数据（基于当前考试最大分数）
      generateScoreDistribution(scores)
    } else {
      ElMessage.error('获取班级成绩数据失败')
    }
  } catch (error) {
    console.error('获取班级成绩数据失败:', error)
    ElMessage.error('获取班级成绩数据失败')
  } finally {
    loading.value = false
  }
}

// 计算班级统计信息（及格线基于动态计算60%）
const calculateClassStats = (scores) => {
  const total = scores.length
  const avgScore = total > 0 ? Math.round((scores.reduce((sum, s) => sum + (s.totalScore || 0), 0) / total) * 10) / 10 : 0
  const passCount = scores.filter(s => (s.totalScore || 0) >= passScore.value).length
  const passRate = total > 0 ? Math.round((passCount / total) * 100 * 10) / 10 : 0
  
  classStats.value = {
    totalStudents: total,
    avgScore: avgScore,
    passRate: passRate
  }
  
  console.log(`班级统计: 总人数=${total}, 平均分=${avgScore}, 及格率=${passRate}%`)
}

// 生成成绩分布数据（每10分一个分数段，最大值为当前考试最大分数）
const generateScoreDistribution = (scores) => {
  // 获取当前考试的最大分数
  let maxScore = selectedExam.value ? (selectedExam.value.totalScore || 100) : 100
  
  // 生成每10分一个分数段的分布
  const segments = []
  const colors = ['#909399', '#F56C6C', '#E6A23C', '#409EFF', '#67C23A', '#00A870', '#409EFF', '#E6A23C', '#F56C6C', '#909399']
  
  for (let i = 0; i <= maxScore; i += 10) {
    const min = i
    const max = Math.min(i + 9, maxScore)
    const label = `${min}-${max}分`
    const colorIndex = Math.floor(i / 10) % colors.length
    
    segments.push({
      label: label,
      min: min,
      max: max,
      count: 0,
      color: colors[colorIndex]
    })
  }
  
  scoreDistribution.value = segments
  
  // 统计每个分数段的人数
  scores.forEach(score => {
    const s = score.totalScore || 0
    const segment = scoreDistribution.value.find(seg => s >= seg.min && s <= seg.max)
    if (segment) {
      segment.count++
    }
  })
  
  console.log('成绩分布:', scoreDistribution.value)
}

// 处理考试选择
const handleExamChange = () => {
  if (selectedExamId.value) {
    loadClassExamData()
  }
}

// 重置筛选
const resetFilter = () => {
  if (examList.value.length > 0) {
    selectedExamId.value = examList.value[0].id
  }
  loadClassExamData()
}

// 表格行样式
const getRowClassName = ({ row }) => {
  return row.studentId === currentUserId.value ? 'my-row' : ''
}

// 页面加载时初始化
onMounted(async () => {
  const user = getCurrentUser()
  if (user) {
    currentUserId.value = user.id
    classId.value = user.clazzId // 从用户信息获取班级ID
    console.log(`\u5f53前用户: ID=${user.id}, \u73ed\u7ea7=${classId.value}`)
    if (!classId.value) {
      ElMessage.warning('\u65e0\u6cd5\u83b7\u53d6\u73ed\u7ea7\u4fe1\u606f')
      return
    }
  } else {
    ElMessage.error('\u7528\u6237\u672a\u767b\u5f55')
    return
  }
  await loadExams()
})
</script>

<style scoped>
.stu-exam-report-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.header {
  margin-bottom: 20px;
}

.header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.header p {
  margin: 0;
  color: #909399;
}

.exam-selector-section {
  margin-bottom: 20px;
}

.statistics-section {
  margin-bottom: 20px;
}

.stat-card {
  border: 1px solid #ebeef5;
}

.stat-item {
  display: flex;
  align-items: center;
}

.stat-icon {
  font-size: 32px;
  margin-right: 16px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.score-distribution-section {
  margin-bottom: 20px;
}

.distribution-content {
  padding: 20px 0;
}

.distribution-bars {
  max-width: 600px;
}

.distribution-bar {
  margin-bottom: 16px;
}

.segment-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.bar-container {
  position: relative;
  background-color: #f5f7fa;
  border-radius: 4px;
  height: 32px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  transition: width 0.3s ease;
  border-radius: 4px;
}

.bar-text {
  position: absolute;
  top: 0;
  left: 8px;
  height: 100%;
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #303133;
  font-weight: bold;
}

.class-scores-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.my-rank, .my-name {
  font-weight: bold;
  color: #409EFF;
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
}

.fail {
  color: #F56C6C;
  font-weight: bold;
}

:deep(.my-row) {
  background-color: #ecf5ff !important;
}
</style>