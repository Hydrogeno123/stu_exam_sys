<template>
  <div class="score-report-container">
    <div class="header">
    </div>
    
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-select v-model="selectedExam" placeholder="选择考试（选择该选项显示统计结果）" style="width: 107%">
            <el-option label="全部考试" value=""></el-option>
            <el-option
              v-for="exam in examOptions"
              :key="exam.value"
              :label="exam.label"
              :value="exam.value">
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="selectedClass" placeholder="选择班级" style="width: 100%">
            <el-option label="全部班级" value=""></el-option>
            <el-option
              v-for="clazz in classOptions"
              :key="clazz.value"
              :label="clazz.label"
              :value="clazz.value">
            </el-option>
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
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statistics.totalStudents }}</div>
                <div class="stat-label">学生总数</div>
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
                <div class="stat-value">{{ statistics.avgScore }}分</div>
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
                <div class="stat-value">{{ statistics.passRate }}%</div>
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
                <div class="stat-value">{{ statistics.excellentRate }}%</div>
                <div class="stat-label">优秀率</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="score-distribution">
      <el-card>
        <template #header>
          <span>成绩分布统计</span>
        </template>
        <div id="scoreDistributionChart" style="height: 400px;"></div>
      </el-card>
    </div>

    <div class="ranking-section">
      <el-card>
        <template #header>
          <span>成绩排名</span>
        </template>
        <el-table :data="scoreRanking" style="width: 100%">
          <el-table-column type="index" label="排名" width="80"></el-table-column>
          <el-table-column prop="studentName" label="学生姓名" width="120"></el-table-column>
          <el-table-column prop="clazzName" label="班级" width="100"></el-table-column>
          <el-table-column prop="examName" label="考试名称" width="150"></el-table-column>
          <el-table-column prop="totalScore" label="成绩" width="100">
            <template #default="scope">
              <span :class="{'excellent': scope.row.totalScore >= 90, 'good': scope.row.totalScore >= 80 && scope.row.totalScore < 90, 'pass': scope.row.totalScore >= 60 && scope.row.totalScore < 80, 'fail': scope.row.totalScore < 60}">
                {{ scope.row.totalScore }}分
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="submitTime" label="提交时间" width="180"></el-table-column>
        </el-table>
        
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { User, Trophy, Histogram, Star } from '@element-plus/icons-vue'
import { queryPageApi as getScoreRankingApi } from '@/api/exam/examScore'
import { getScoreSegmentsApi } from '@/api/exam/examScore'
import { queryAllApi as getAllExamsApi } from '@/api/exam/exam'
import { getAllClazzApi as getAllClassesApi } from '@/api/clazz'
import * as echarts from 'echarts'

const selectedExam = ref('')
const selectedClass = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 考试和班级选项数据
const examOptions = ref([])
const classOptions = ref([])

// 图表实例
let scoreDistributionChart = null

const statistics = ref({
  totalStudents: 0,
  avgScore: 0,
  passRate: 0,
  excellentRate: 0,
  maxExamScore: 0  // 最大考试总分
})

const scoreSegments = ref([])

const scoreRanking = ref([])

const loadData = async () => {
  try {
    // 获取成绩排名数据
    const rankingParams = {
      page: currentPage.value,
      pageSize: pageSize.value,
      examId: selectedExam.value || null,
      clazzId: selectedClass.value || null
    }
    
    const rankingResponse = await getScoreRankingApi(rankingParams)
    scoreRanking.value = rankingResponse.data.rows || rankingResponse.data
    total.value = rankingResponse.data.total || scoreRanking.value.length
    
    // 获取分数段统计数据（不显示表格，仅用于图表显示）
    const segmentParams = {
      examId: selectedExam.value || null,
      clazzId: selectedClass.value || null
    }
    
    const segmentResponse = await getScoreSegmentsApi(segmentParams)
    
    // 确保正确处理响应数据
    if (segmentResponse && segmentResponse.data) {
      if (Array.isArray(segmentResponse.data)) {
        scoreSegments.value = segmentResponse.data
      } else if (typeof segmentResponse.data === 'object' && segmentResponse.data.rows) {
        scoreSegments.value = segmentResponse.data.rows
      } else {
        scoreSegments.value = []
      }
    } else {
      scoreSegments.value = []
    }
    
    // 确保所有分数段都存在，即使计数为0
    const allSegments = ['90-100', '80-89', '70-79', '60-69', '50-59', '40-49', '30-39', '20-29', '10-19', '0-9']
    const existingSegments = new Set(scoreSegments.value.map(item => item.segment))
    
    for (const segment of allSegments) {
      if (!existingSegments.has(segment)) {
        scoreSegments.value.push({ segment, count: 0, percentage: 0 })
      }
    }
    
    // 按照正确的顺序排序
    const segmentOrder = { '90-100': 1, '80-89': 2, '70-79': 3, '60-69': 4, '50-59': 5, '40-49': 6, '30-39': 7, '20-29': 8, '10-19': 9, '0-9': 10 }
    scoreSegments.value.sort((a, b) => segmentOrder[a.segment] - segmentOrder[b.segment])
    
    // 更新成绩分布图表
    updateScoreDistributionChart()
    
    // 计算统计信息
    if (scoreRanking.value.length > 0) {
      // 学生总数
      statistics.value.totalStudents = total.value
      
      if (selectedExam.value) {
        // 选中特定考试，计算统计信息
        // 计算平均分
        const totalScore = scoreRanking.value.reduce((sum, item) => sum + (item.totalScore || item.score || 0), 0)
        statistics.value.avgScore = totalScore > 0 ? (totalScore / scoreRanking.value.length).toFixed(1) : 0
        
        // 计算及格率：根据考试总成绩的60%向下取整
        let passThreshold = 60  // 默认值
        const selectedExamData = examOptions.value.find(e => e.value === selectedExam.value)
        if (selectedExamData && selectedExamData.totalScore) {
          passThreshold = Math.floor(selectedExamData.totalScore * 0.6)
        }
        const passCount = scoreRanking.value.filter(item => (item.totalScore || item.score || 0) >= passThreshold).length
        statistics.value.passRate = total.value > 0 ? ((passCount / total.value) * 100).toFixed(1) : 0
        
        // 计算优秀率：总成绩的 90%
        let excellentThreshold = 90  // 默认值
        const selectedExamDataForExcellent = examOptions.value.find(e => e.value === selectedExam.value)
        if (selectedExamDataForExcellent && selectedExamDataForExcellent.totalScore) {
          excellentThreshold = Math.floor(selectedExamDataForExcellent.totalScore * 0.9)
        }
        const excellentCount = scoreRanking.value.filter(item => (item.totalScore || item.score || 0) >= excellentThreshold).length
        statistics.value.excellentRate = total.value > 0 ? ((excellentCount / total.value) * 100).toFixed(1) : 0
      } else {
        // 未选中特定考试时，统计数据都设置为0
        statistics.value.avgScore = 0
        statistics.value.passRate = 0
        statistics.value.excellentRate = 0
      }
    }
  } catch (error) {
    console.error('加载成绩统计数据失败:', error)
  }
}

const resetFilter = () => {
  selectedExam.value = ''
  selectedClass.value = ''
  currentPage.value = 1
  loadData()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

onMounted(() => {
  loadExamAndClassData()
  loadData()
  // 初始化图表
  initCharts()
})

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    const chartDom = document.getElementById('scoreDistributionChart')
    if (chartDom) {
      scoreDistributionChart = echarts.init(chartDom)
      // 设置初始图表配置（使用动态分数段，由于loadData会更新）
      const option = {
        title: {
          text: '成绩分布'
        },
        tooltip: {},
        xAxis: {
          type: 'category',
          data: []
        },
        yAxis: {},
        series: [{
          type: 'bar',
          data: []
        }]
      }
      scoreDistributionChart.setOption(option)
    }
  })
}

// 更新成绩分布图表
const updateScoreDistributionChart = () => {
  if (scoreDistributionChart && scoreSegments.value.length > 0) {
    const segments = scoreSegments.value.map(item => item.segment)
    const counts = scoreSegments.value.map(item => item.count || 0)
    const percentages = scoreSegments.value.map(item => item.percentage || 0)
    
    const colorMap = {
      '90-100': '#67C23A',  // 绿色 - 优秀
      '80-89': '#409EFF',   // 蓝色 - 良好
      '70-79': '#E6A23C',   // 黄色 - 中等
      '60-69': '#F56C6C',   // 红色 - 及格
      '50-59': '#FF9C6E',   // 橘色
      '40-49': '#FF7A45',   // 橘红
      '30-39': '#FF6B6B',   // 深红
      '20-29': '#FF5252',   // 紅赐
      '10-19': '#909399',   // 灰色
      '0-9': '#CCCCCC'      // 浅灰色
    }
    
    const option = {
      tooltip: {
        trigger: 'axis',
        formatter: function(params) {
          if (params.length > 0) {
            const dataIndex = params[0].dataIndex
            return `${segments[dataIndex]}<br/>人数: ${counts[dataIndex]}<br/>占比: ${percentages[dataIndex]}%`
          }
          return ''
        }
      },
      xAxis: {
        type: 'category',
        data: segments,
        axisLabel: {
          fontSize: 12,
          color: '#666'
        },
        axisLine: {
          lineStyle: { color: '#E0E0E0' }
        }
      },
      yAxis: {
        type: 'value',
        name: '人数',
        axisLabel: {
          fontSize: 12,
          color: '#666'
        },
        splitLine: {
          lineStyle: { color: '#F0F0F0' }
        }
      },
      series: [{
        type: 'bar',
        data: counts,
        itemStyle: {
          color: function(params) {
            return colorMap[segments[params.dataIndex]] || '#409EFF'
          },
          borderRadius: [4, 4, 0, 0]
        },
        label: {
          show: true,
          position: 'top',
          formatter: function(params) {
            return params.value
          },
          fontSize: 12,
          color: '#333'
        }
      }]
    }
    scoreDistributionChart.setOption(option)
  }
}

// 加载考试和班级数据
const loadExamAndClassData = async () => {
  try {
    // 获取所有考试（需要包抬 totalScore 字段）
    const examResponse = await getAllExamsApi()
    examOptions.value = examResponse.data.map(exam => ({
      label: exam.examName,
      value: exam.id,
      totalScore: exam.totalScore || 100  // 确保获取考试总分
    }))
    
    // 获取所有班级
    const classResponse = await getAllClassesApi()
    classOptions.value = classResponse.data.map(clazz => ({
      label: clazz.clazzName,
      value: clazz.id
    }))
  } catch (error) {
    console.error('加载考试和班级数据失败:', error)
  }
}
</script>

<style scoped>
.score-report-container {
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

.score-distribution {
  margin-bottom: 20px;
}

.score-distribution :deep(.el-card) {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border: none;
  border-radius: 8px;
}

.ranking-section {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
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