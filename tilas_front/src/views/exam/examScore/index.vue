<template>
  <div class="container">
    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="考试名称">
          <el-select v-model="searchForm.examId" placeholder="请选择考试" clearable style="width: 200px">
            <el-option
              v-for="exam in examList"
              :key="exam.id"
              :label="exam.examName"
              :value="exam.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-input v-model="searchForm.studentName" placeholder="请输入学生姓名" clearable />
        </el-form-item>
        <el-form-item label="是否及格">
          <el-select v-model="searchForm.isPass" placeholder="请选择" clearable>
            <el-option label="及格" :value="true" />
            <el-option label="不及格" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="clear">重置</el-button>
          <el-button type="success" @click="exportExcel">导出表格</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计信息 -->
    <div class="statistics-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-label">总考试人数</div>
              <div class="stat-value">{{ statistics.totalStudents }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-label">及格人数</div>
              <div class="stat-value success">{{ statistics.passStudents }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-label">不及格人数</div>
              <div class="stat-value danger">{{ statistics.failStudents }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-label">及格率</div>
              <div class="stat-value">{{ statistics.passRate }}%</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 表格区域 -->
    <div class="table-container">
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="examName" label="考试名称" min-width="150" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="clazzName" label="班级名称" width="120" />
        <el-table-column prop="totalScore" label="得分" width="100">
          <template #default="scope">
            <span :class="{ 'success-text': scope.row.totalScore >= getPassScore(scope.row.exam), 'danger-text': scope.row.totalScore < getPassScore(scope.row.exam) }">
              {{ scope.row.totalScore }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="totalQuestions" label="总题数" width="80" />
        <el-table-column label="是否及格" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.totalScore >= getPassScore(scope.row.exam) ? 'success' : 'danger'">
              {{ scope.row.totalScore >= getPassScore(scope.row.exam) ? '及格' : '不及格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewAnswerDetails(scope.row)">
              答题详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页区域 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 答题详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="答题详情" width="900px">
      <div v-if="currentRecord">
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="学生姓名">{{ currentRecord.studentName }}</el-descriptions-item>
          <el-descriptions-item label="考试名称">{{ currentRecord.examName }}</el-descriptions-item>
          <el-descriptions-item label="得分">{{ currentRecord.totalScore }}</el-descriptions-item>
        <el-descriptions-item label="是否及格">
          <el-tag :type="currentRecord.totalScore >= getPassScore(currentRecord.exam) ? 'success' : 'danger'">
            {{ currentRecord.totalScore >= getPassScore(currentRecord.exam) ? '及格' : '不及格' }}
          </el-tag>
        </el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ currentRecord.startTime }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentRecord.submitTime }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider />
        
        <div class="answer-details">
          <h3>答题记录</h3>
          <div v-for="(answer, index) in answerDetails" :key="index" class="answer-item">
            <div class="question-title">
              <span class="question-index">{{ index + 1 }}.</span>
              <span>{{ answer.questionContent }}</span>
              <span class="question-score">({{ answer.score }}分)</span>
            </div>
            <div class="answer-content">
              <p><strong>学生答案：</strong>{{ convertAnswerToDisplay(answer.studentAnswer, answer.questionType) || '未作答' }}</p>
              <p><strong>正确答案：</strong>{{ convertAnswerToDisplay(answer.correctAnswer, answer.questionType) }}</p>
              <p><strong>得分：</strong>
                <span :class="{ 'success-text': answer.isCorrect, 'danger-text': !answer.isCorrect }">
                  {{ answer.actualScore }}
                </span>
                / {{ answer.score }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  queryPageApi,
  updateExamScoreApi,
  getStatisticsApi
} from '@/api/exam/examScore'
import { queryAllApi, getScoreDetailsApi } from '@/api/exam/exam'

// 搜索表单
const searchForm = reactive({
  examId: '',
  studentName: '',
  isPass: ''
})

// 表格数据
const tableData = ref([])

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 考试列表
const examList = ref([])

// 统计信息
const statistics = reactive({
  totalStudents: 0,
  passStudents: 0,
  failStudents: 0,
  passRate: 0,
  passScore: 0  // 及格线分数
})

// 对话框相关
const detailDialogVisible = ref(false)
const gradeDialogVisible = ref(false)
const currentRecord = ref(null)
const answerDetails = ref([])

// 手动判卷表单
const gradeForm = reactive({
  adjustScore: 0,
  reason: ''
})

const gradeFormRef = ref()

// 手动判卷验证规则
const gradeRules = {
  reason: [
    { required: true, message: '请输入调整原因', trigger: 'blur' }
  ]
}

// 转换答案显示格式（数字->字母）
const convertAnswerToDisplay = (answer, questionType) => {
  if (!answer) return answer
  
  // 只有选择题需要转换（questionType=1）
  if (questionType === 1) {
    const answerNum = parseInt(answer)
    if (answerNum >= 1 && answerNum <= 4) {
      return String.fromCharCode(64 + answerNum) // 64+'A'=65
    }
  }
  
  return answer
}

// 计算及格率
const passRate = computed(() => {
  if (statistics.totalStudents === 0) return 0
  return ((statistics.passStudents / statistics.totalStudents) * 100).toFixed(2)
})

// 计算及格线（总分的60%取整数）
const getPassScore = (exam) => {
  if (!exam) return 60
  return Math.floor((exam.totalScore || 100) * 0.6)
}

// 查询数据
const search = async () => {
  try {
    const params = {
      examId: searchForm.examId,
      studentName: searchForm.studentName,
      isPass: searchForm.isPass,
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 並行调用分页数据和统计数据
    const [pageResult, statsResult] = await Promise.all([
      queryPageApi(params),
      getStatisticsApi({
        examId: searchForm.examId,
        studentName: searchForm.studentName,
        isPass: searchForm.isPass
      })
    ])
    
    if (pageResult.code) {
      tableData.value = pageResult.data.rows
      total.value = pageResult.data.total
      
      // 为每条数据添加exam对象（用于计算及格线）
      tableData.value.forEach(item => {
        const exam = examList.value.find(e => e.id === item.examId)
        item.exam = exam
      })
    }
    
    // 使用全量统计数据更新统计信息
    if (statsResult.code) {
      statistics.totalStudents = statsResult.data.totalStudents
      statistics.passStudents = statsResult.data.passStudents
      statistics.failStudents = statsResult.data.failStudents
      statistics.passRate = statsResult.data.passRate
      // 从考试总分的60%获取及格线
      statistics.passScore = statsResult.data.passScore || Math.floor((examList.value.find(e => e.id === searchForm.examId)?.totalScore || 100) * 0.6)
    }
  } catch (error) {
    ElMessage.error('查询失败')
  }
}



// 清空搜索条件
const clear = () => {
  searchForm.examId = ''
  searchForm.studentName = ''
  searchForm.isPass = ''
  search()
}

// 加载考试列表
const loadExamList = async () => {
  try {
    const result = await queryAllApi()
    if (result.code) {
      examList.value = result.data
    }
  } catch (error) {
    ElMessage.error('加载考试列表失败')
  }
}

// 查看答题详情
const viewAnswerDetails = async (row) => {
  try {
    currentRecord.value = row
    // 调用API获取答题详情
    const result = await getScoreDetailsApi(row.examId, row.studentId)
    if (result.code === 1) {
      answerDetails.value = result.data
    } else {
      ElMessage.error(result.message || '加载答题详情失败')
    }
    
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载答题详情失败')
  }
}

// 手动判卷
const manualGrade = (row) => {
  currentRecord.value = row
  gradeForm.adjustScore = row.score
  gradeForm.reason = ''
  gradeDialogVisible.value = true
}

// 保存手动判卷结果
const saveGrade = async () => {
  if (!gradeFormRef.value) return
  
  await gradeFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      const result = await updateExamScoreApi(currentRecord.value.id, {
        score: gradeForm.adjustScore,
        reason: gradeForm.reason
      })
      
      if (result.code) {
        ElMessage.success('成绩调整成功')
        gradeDialogVisible.value = false
        search()
      }
    } catch (error) {
      ElMessage.error('成绩调整失败')
    }
  })
}

// 导出Excel
const exportExcel = async () => {
  try {
    // 获取所有搜索结果的数据（不分页）
    const params = {
      examId: searchForm.examId,
      studentName: searchForm.studentName,
      isPass: searchForm.isPass,
      page: 1,
      pageSize: 10000  // 使用一个足够大的分页大小来获取所有数据
    }
    
    const result = await queryPageApi(params)
    if (!result.code || !result.data.rows || result.data.rows.length === 0) {
      ElMessage.warning('没有数据可以导出')
      return
    }
    
    const allData = result.data.rows
    // 为每条数据添加exam对象（用于计算及格线）
    allData.forEach(item => {
      const exam = examList.value.find(e => e.id === item.examId)
      item.exam = exam
    })
    
    // 表头
    const headers = ['考试名称', '学生姓名', '班级名称', '得分', '总题数', '是否及格', '提交时间']
    
    // 数据行
    const rows = allData.map(item => [
      item.examName,
      item.studentName,
      item.clazzName,
      item.totalScore,
      item.totalQuestions,
      item.totalScore >= getPassScore(item.exam) ? '及格' : '不及格',
      item.submitTime
    ])
    
    // 构建CSV内容
    let csvContent = '\uFEFF'  // 添加BOM以支持中文
    // 添加表头
    csvContent += headers.map(h => `"${h}"`).join(',') + '\r\n'
    // 添加数据行
    rows.forEach(row => {
      csvContent += row.map(cell => `"${cell}"`).join(',') + '\r\n'
    })
    
    // 使用Blob方式下载，支持较大的数据量
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `考试成绩_${new Date().getTime()}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)  // 释放对象URL
    
    ElMessage.success(`成功导出${allData.length}条记录`)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败: ' + (error.message || ''))
  }
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  search()
}

// 当前页变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  search()
}

// 组件挂载
onMounted(() => {
  search()
  loadExamList()
})
</script>

<style scoped>
.container {
  padding: 20px;
}

.search-container {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.statistics-container {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.danger {
  color: #f56c6c;
}

.table-container {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
}

.answer-details {
  max-height: 400px;
  overflow-y: auto;
}

.answer-item {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.question-title {
  font-weight: bold;
  margin-bottom: 10px;
}

.question-index {
  color: #409eff;
  margin-right: 5px;
}

.question-score {
  color: #909399;
  font-size: 12px;
  margin-left: 10px;
}

.answer-content {
  margin-left: 20px;
}

.success-text {
  color: #67c23a;
}

.danger-text {
  color: #f56c6c;
}
</style>