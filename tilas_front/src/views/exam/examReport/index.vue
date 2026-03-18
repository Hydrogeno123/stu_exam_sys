<template>
  <div class="exam-report-container">
    <div class="header">
    </div>
    
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-col>
        <!-- <el-col :span="4">
          <el-select v-model="examType" placeholder="考试类型" style="width: 100%">
            <el-option label="全部" value=""></el-option>
            <el-option label="期中考试" value="1"></el-option>
            <el-option label="期末考试" value="2"></el-option>
            <el-option label="水平测试" value="3"></el-option>
          </el-select>
        </el-col> -->
        <el-col :span="4">
          <el-select v-model="selectedBank" placeholder="题库" style="width: 100%" clearable>
            <el-option label="全部题库" value=""></el-option>
            <el-option
              v-for="bank in questionBanks"
              :key="bank.id"
              :label="bank.name"
              :value="bank.id"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-col>
      </el-row>
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
                <div class="stat-value">{{ statistics.totalExams }}</div>
                <div class="stat-label">总考试次数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-icon" style="color: #67C23A;">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statistics.totalParticipants }}</div>
                <div class="stat-label">参与人数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-icon" style="color: #E6A23C;">
                <el-icon><Trophy /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statistics.avgScore }}分</div>
                <div class="stat-label">平均分</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-icon" style="color: #F56C6C;">
                <el-icon><Histogram /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statistics.passRate }}%</div>
                <div class="stat-label">及格率</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="chart-section">
      <el-card>
        <template #header>
          <span>考试趋势分析</span>
        </template>
        <div id="examTrendChart" style="height: 300px;"></div>
      </el-card>
    </div>

    <div class="table-section">
      <el-card>
        <template #header>
          <span>考试详情列表</span>
        </template>
        <el-table :data="examList" style="width: 100%">
          <el-table-column prop="examName" label="考试名称" width="200"></el-table-column>
          <el-table-column prop="subject" label="科目" width="100"></el-table-column>
          <el-table-column prop="participants" label="参与人数" width="100"></el-table-column>
          <el-table-column prop="avgScore" label="平均分" width="100"></el-table-column>
          <el-table-column prop="highestScore" label="最高分" width="100"></el-table-column>
          <el-table-column prop="lowestScore" label="最低分" width="100"></el-table-column>
          <el-table-column prop="passRate" label="及格率" width="100">
            <template #default="scope">
              {{ scope.row.passRate }}%
            </template>
          </el-table-column>
          <el-table-column prop="examDate" label="考试时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.examDate) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document, User, Trophy, Histogram } from '@element-plus/icons-vue'
import { getExamReportDataApi, getExamTrendDataApi, getOverallStatisticsApi } from '@/api/exam/examScore'
import { queryAllApi } from '@/api/exam/questionBank'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const dateRange = ref([])
const examType = ref('')
const selectedBank = ref('')
const questionBanks = ref([])

const statistics = ref({
  totalExams: 0,
  totalParticipants: 0,
  avgScore: 0,
  passRate: 0
})

const examList = ref([])
const examTrendChart = ref(null)
let chartInstance = null

const loadData = async () => {
  try {
    // 准备查询参数
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = formatDate(dateRange.value[0])
      params.endDate = formatDate(dateRange.value[1])
    }
    if (examType.value) {
      params.examName = getExamTypeName(examType.value)
    }
    if (selectedBank.value) {
      params.bankId = selectedBank.value
    }

    // 并行加载所有数据
    const [examReportData, examTrendData, overallStatistics] = await Promise.all([
      getExamReportDataApi(params),
      getExamTrendDataApi(params),
      getOverallStatisticsApi(params)
    ])

    // 更新统计数据
    if (overallStatistics.data) {
      statistics.value = {
        totalExams: overallStatistics.data.totalExams || 0,
        totalParticipants: overallStatistics.data.totalParticipants || 0,
        avgScore: overallStatistics.data.avgScore ? Math.round(overallStatistics.data.avgScore * 100) / 100 : 0,
        passRate: overallStatistics.data.passRate ? Math.round(overallStatistics.data.passRate * 100) / 100 : 0
      }
    }

    // 更新考试列表
    examList.value = examReportData.data || []

    // 更新图表
    updateExamTrendChart(examTrendData.data || [])

  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
    console.error('加载考试统计报告数据失败:', error)
  }
}

const formatDate = (date) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

const loadQuestionBanks = async () => {
  try {
    const response = await queryAllApi()
    if (response.data) {
      questionBanks.value = response.data.map(bank => ({
        id: bank.id,
        name: bank.bankName
      }))
    }
  } catch (error) {
    ElMessage.error('加载题库列表失败: ' + error.message)
    console.error('加载题库列表失败:', error)
  }
}

const getExamTypeName = (type) => {
  const typeMap = {
    '1': '期中',
    '2': '期末',
    '3': '水平测试'
  }
  return typeMap[type] || ''
}

const updateExamTrendChart = (trendData) => {
  if (!chartInstance) {
    chartInstance = echarts.init(document.getElementById('examTrendChart'))
  }

  const dates = trendData.map(item => item.examDate)
  const examCounts = trendData.map(item => item.examCount)
  const avgScores = trendData.map(item => item.avgScore || 0)
  const passRates = trendData.map(item => item.passRate || 0)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['考试次数', '平均分', '及格率']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: '次数/分数',
        position: 'left'
      },
      {
        type: 'value',
        name: '及格率(%)',
        position: 'right',
        axisLabel: {
          formatter: '{value}%'
        }
      }
    ],
    series: [
      {
        name: '考试次数',
        type: 'line',
        data: examCounts,
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '平均分',
        type: 'line',
        data: avgScores,
        itemStyle: {
          color: '#67C23A'
        }
      },
      {
        name: '及格率',
        type: 'line',
        yAxisIndex: 1,
        data: passRates,
        itemStyle: {
          color: '#E6A23C'
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

const resetFilter = () => {
  dateRange.value = []
  examType.value = ''
  selectedBank.value = ''
  loadData()
}

onMounted(() => {
  loadQuestionBanks()
  loadData()
  
  // 添加窗口大小变化监听，确保图表自适应
  window.addEventListener('resize', () => {
    if (chartInstance) {
      chartInstance.resize()
    }
  })
})

// 清理工作
import { onUnmounted } from 'vue'

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  window.removeEventListener('resize', () => {
    if (chartInstance) {
      chartInstance.resize()
    }
  })
})
</script>

<style scoped>
.exam-report-container {
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

.filter-section {
  margin-bottom: 20px;
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

.chart-section, .table-section {
  margin-bottom: 20px;
}
</style>