<template>
  <div class="exam-doing-container">
    <!-- 考试信息条 -->
    <div class="exam-header">
      <div class="exam-info">
        <h2>{{ exam.examName }}</h2>
        <p>总分: {{ exam.totalScore }} 分 | 时长: {{ exam.duration }} 分钟</p>
      </div>
      <div class="exam-timer">
        <div :class="['timer', { warning: remainingSeconds < 300, danger: remainingSeconds < 60 }]">
          {{ formatTime(remainingSeconds) }}
        </div>
        <p>剩余时间</p>
      </div>
    </div>

    <!-- 题目列表 -->
    <div class="exam-content">
      <div class="questions-container">
        <div v-if="loading" style="text-align: center; padding: 20px">
          <el-skeleton :rows="5" animated />
        </div>
        <template v-else>
          <div 
            v-for="(paper, index) in papers" 
            :key="paper.id" 
            class="question-item"
          >
            <div class="question-header">
              <span class="question-number">第 {{ index + 1 }} 题</span>
              <span class="question-type">{{ getQuestionType(paper.question.type) }}</span>
              <span class="question-score">({{ paper.question.score }} 分)</span>
            </div>

            <div class="question-content">
              {{ paper.question.content }}
            </div>

            <!-- 选择题 -->
            <div v-if="paper.question.type === 1" class="question-options">
              <div 
                v-for="(option, optIndex) in parseOptions(paper.question.options)" 
                :key="optIndex"
                class="option-item"
                :class="{ selected: answers[paper.question.id] === String(optIndex + 1) }"
                @click="selectOption(paper.question.id, optIndex + 1)"
              >
                <div class="option-label">
                  <span class="option-letter">{{ String.fromCharCode(65 + optIndex) }}</span>
                  <span class="option-text">{{ option.content }}</span>
                </div>
                <div class="option-checkbox" :class="{ checked: answers[paper.question.id] === String(optIndex + 1) }"></div>
              </div>
            </div>

            <!-- 填空题 -->
            <div v-else class="question-fill">
              <el-input 
                v-model="answers[paper.question.id]" 
                placeholder="请输入答案"
                type="textarea"
                :rows="3"
              />
            </div>
          </div>
        </template>
      </div>

      <!-- 右侧导航栏 -->
      <div class="questions-nav">
        <h4>题目导航</h4>
        <div class="nav-buttons">
          <button 
            v-for="(paper, index) in papers" 
            :key="paper.id"
            :class="['nav-btn', { 
              answered: answers[paper.question.id]
            }]"
            @click="goToQuestion(index)"
          >
            {{ index + 1 }}
          </button>
        </div>
        <div class="nav-legend">
          <div class="legend-item">
            <span class="legend-dot answered"></span>
            <span>已答</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot unanswered"></span>
            <span>未答</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="exam-footer">
      <el-button type="danger" @click="handleSubmitExam" :disabled="submitting">
        交卷
      </el-button>
    </div>

    <!-- 提交确认对话框 -->
    <el-dialog v-model="submitDialogVisible" title="提交考试" width="400px" :close-on-click-modal="false">
      <div>
        <p>已答题: {{ answeredCount }} / {{ papers.length }}</p>
        <p v-if="unansweredCount > 0" style="color: #f56c6c">
          还有 {{ unansweredCount }} 题未回答，是否确定要提交?
        </p>
        <p v-else style="color: #67c23a">
          所有题目已答，即将提交
        </p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="submitDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmSubmitExam" :loading="submitting">
            确定提交
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { startExamApi, submitExamApi } from '@/api/exam/exam'

const router = useRouter()
const route = useRoute()

// 考试信息
const exam = ref({
  examName: '',
  totalScore: 100,
  duration: 60
})

// 试卷数据
const papers = ref([])
const loading = ref(false)

// 答题数据 - 使用reactive确保响应式
const answers = reactive({})
const currentQuestionIndex = ref(0)

// 倒计时
const remainingSeconds = ref(3600)
let timerInterval = null

// 提交状态
const submitDialogVisible = ref(false)
const submitting = ref(false)

const examId = ref(parseInt(route.params.examId))
const studentId = ref(null)

// 获取当前学生ID
const getStudentId = () => {
  const user = JSON.parse(localStorage.getItem('token'))
  if (user && user.id) {
    studentId.value = user.id
  }
}

// 已答题数和未答题数
const answeredCount = computed(() => {
  return Object.values(answers).filter(a => a && a.toString().trim() !== '').length
})

const unansweredCount = computed(() => {
  return papers.value.length - answeredCount.value
})

// 获取考试信息和试卷
const loadExamPaper = async () => {
  if (!studentId.value) {
    ElMessage.error('无法获取学生ID')
    router.back()
    return
  }
  
  loading.value = true
  try {
    const result = await startExamApi(examId.value, studentId.value)
    if (result && result.code) {
      exam.value = result.data.exam
      papers.value = result.data.papers || []
      
      // 初始化倒计时
      remainingSeconds.value = exam.value.duration * 60
      startTimer()
    } else {
      ElMessage.error('获取试卷失败')
      router.back()
    }
  } catch (error) {
    ElMessage.error('获取试卷失败')
    console.error(error)
    router.back()
  } finally {
    loading.value = false
  }
}

// 解析选项
const parseOptions = (optionsStr) => {
  try {
    const options = JSON.parse(optionsStr)
    return options.map(option => ({
      content: option.content,
      isCorrect: option.isCorrect
    }))
  } catch {
    return []
  }
}

// 选择选项
const selectOption = (questionId, optionIndex) => {
  console.log('selected option:', { questionId, optionIndex })
  answers[questionId] = String(optionIndex) // 存储选项序号（1, 2, 3, 4）
  console.log('answers after selection:', answers)
}

// 获取题目类型文本
const getQuestionType = (type) => {
  return type === 1 ? '选择题' : '填空题'
}

// 格式化时间
const formatTime = (seconds) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  if (hours > 0) {
    return `${hours}:${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
  }
  return `${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

// 开始倒计时
const startTimer = () => {
  timerInterval = setInterval(() => {
    remainingSeconds.value--
    
    if (remainingSeconds.value <= 0) {
      clearInterval(timerInterval)
      ElMessage.warning('考试时间已到，自动提交')
      confirmSubmitExam()
    }
  }, 1000)
}

// 上一题
const handlePrevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

// 下一题
const handleNextQuestion = () => {
  if (currentQuestionIndex.value < papers.value.length - 1) {
    currentQuestionIndex.value++
  }
}

// 跳转到指定题目
const goToQuestion = (index) => {
  currentQuestionIndex.value = index
}



// 处理提交考试
const handleSubmitExam = () => {
  console.log('点击交卷按钮，当前状态:', {
    submitting: submitting.value,
    papersCount: papers.value.length,
    answeredCount: answeredCount.value
  })
  
  if (submitting.value) {
    ElMessage.warning('正在提交中，请稍候...')
    return
  }
  
  if (papers.value.length === 0) {
    ElMessage.warning('题目加载中，请稍候...')
    return
  }
  
  submitDialogVisible.value = true
  console.log('打开提交对话框，状态:', submitDialogVisible.value)
}

// 确认提交考试
const confirmSubmitExam = async () => {
  console.log('开始提交考试...')
  
  // 清除倒计时
  if (timerInterval) {
    clearInterval(timerInterval)
  }
  
  submitting.value = true
  try {
    // 构建答题数据
    const submitData = {
      examId: examId.value,
      studentId: studentId.value,
      answers: papers.value.map(paper => ({
        questionId: paper.question.id,
        answer: answers[paper.question.id] || ''
      }))
    }
    
    console.log('提交数据:', submitData)
    
    const result = await submitExamApi(submitData)
    console.log('提交结果:', result)
    
    if (result && result.code) {
      ElMessage.success('提交成功')
      submitDialogVisible.value = false
      
      // 跳转到成绩页面
      setTimeout(() => {
        router.push({
          name: 'ExamResult',
          params: {
            examId: examId.value,
            studentId: studentId.value
          }
        })
      }, 1000)
    } else {
      ElMessage.error('提交失败')
      submitting.value = false
    }
  } catch (error) {
    ElMessage.error('提交失败: ' + error.message)
    console.error('提交错误:', error)
    submitting.value = false
  }
}

// 强制退出时提交当前答题
const submitCurrentAnswers = async (to, next) => {
  console.log('强制退出，保存当前答题...')
  
  // 清除倒计时
  if (timerInterval) {
    clearInterval(timerInterval)
  }
  
  try {
    // 构建答题数据（仅提交学生已有的答辦）
    const submitData = {
      examId: examId.value,
      studentId: studentId.value,
      answers: papers.value.map(paper => ({
        questionId: paper.question.id,
        answer: answers[paper.question.id] || ''
      }))
    }
    
    console.log('强制退出提交数据:', submitData)
    
    const result = await submitExamApi(submitData)
    console.log('强制退出提交结果:', result)
    
    if (result && result.code) {
      ElMessage.success('答题已保存，正在退出')
    } else {
      ElMessage.warning('正在退出')
    }
  } catch (error) {
    console.error('强制退出错误:', error)
    ElMessage.warning('正在退出')
  }
  
  // 不管是否提交成功，都離開答题頁面
  next()
}

// 组件挂载
onMounted(() => {
  getStudentId()
  // 延迟加载以不保证studentId已设置
  Promise.resolve().then(() => {
    loadExamPaper()
  })
  
  // 防止用户在考试期间刷新或关闭页面
  window.addEventListener('beforeunload', handleBeforeUnload)
  // 防止通过浏览器返回按颁离开
  router.beforeEach((to, from, next) => {
    if (from.name === 'ExamDoing' && to.name !== 'ExamResult') {
      ElMessageBox.confirm('考试期间无法离开，请先交卷！', '提示', {
        confirmButtonText: '继续考试',
        cancelButtonText: '强制退出',
        type: 'warning'
      }).then(() => {
        // 取消离开
      }).catch(() => {
        // 强制退出：保存当前答题记录並跳转
        submitCurrentAnswers(to, next)
      })
    } else {
      next()
    }
  })
})

// 处理刷新和关闭页面
const handleBeforeUnload = (e) => {
  if (loading.value === false && papers.value.length > 0) {
    e.preventDefault()
    e.returnValue = ''
    return ''
  }
}

// 组件卸载，清除倒计时
onUnmounted(() => {
  if (timerInterval) {
    clearInterval(timerInterval)
  }
  // 移除事件监听
  window.removeEventListener('beforeunload', handleBeforeUnload)
})
</script>

<style scoped>
.exam-doing-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fa;
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #409EFF;
  color: white;
}

.exam-info h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.exam-info p {
  margin: 6px 0 0 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.exam-timer {
  text-align: center;
  background: rgba(255, 255, 255, 0.2);
  padding: 12px 20px;
  border-radius: 8px;
}

.timer {
  font-size: 28px;
  font-weight: 600;
  font-family: monospace;
}

.timer.warning {
  color: #E6A23C;
}

.timer.danger {
  color: #F56C6C;
}

.exam-timer p {
  margin: 6px 0 0 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
}

.exam-content {
  display: flex;
  flex: 1;
  overflow: hidden;
  gap: 20px;
  padding: 20px;
}

.questions-container {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  overflow-y: auto;
  padding: 20px;
  border: 1px solid #EBEEF5;
}

.question-item {
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  background: #fff;
  border: 1px solid #EBEEF5;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  font-weight: 600;
  flex-wrap: wrap;
}

.question-number {
  color: #409EFF;
  font-size: 16px;
  font-weight: 600;
}

.question-type {
  background: #ecf5ff;
  color: #409EFF;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 13px;
}

.question-score {
  color: #909399;
  font-size: 14px;
}

.question-content {
  margin-bottom: 16px;
  line-height: 1.6;
  color: #303133;
  font-size: 15px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.question-options {
  margin-top: 16px;
}

.option-item {
  margin-bottom: 12px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 6px;
  border: 1px solid #DCDFE6;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.option-item:hover {
  border-color: #409EFF;
  background: #ecf5ff;
}

.option-item.selected {
  border-color: #409EFF;
  background: #ecf5ff;
}

.option-label {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.option-letter {
  font-weight: 600;
  color: #409EFF;
  font-size: 14px;
  min-width: 24px;
  background: #ecf5ff;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.option-text {
  color: #303133;
  font-size: 14px;
  line-height: 1.5;
}

.option-checkbox {
  width: 20px;
  height: 20px;
  border: 1px solid #DCDFE6;
  border-radius: 50%;
  flex-shrink: 0;
  background: #fff;
}

.option-checkbox.checked {
  background: #409EFF;
  border-color: #409EFF;
  position: relative;
}

.option-checkbox.checked::after {
  content: '\2713';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-weight: bold;
  font-size: 12px;
}

.questions-nav {
  width: 180px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #EBEEF5;
  display: flex;
  flex-direction: column;
}

.questions-nav h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  text-align: center;
}

.nav-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 16px;
  max-height: calc(100vh - 400px);
  overflow-y: auto;
}

.nav-btn {
  width: 100%;
  height: 36px;
  border: 1px solid #DCDFE6;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
}

.nav-btn:hover {
  border-color: #409EFF;
  color: #409EFF;
}

.nav-btn.answered {
  background: #67C23A;
  color: white;
  border-color: #67C23A;
}

.nav-legend {
  border-top: 1px solid #EBEEF5;
  padding-top: 12px;
  margin-top: auto;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #909399;
}

.legend-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
}

.legend-dot.answered {
  background: #67C23A;
}

.legend-dot.unanswered {
  background: #DCDFE6;
}

.exam-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border-top: 1px solid #EBEEF5;
}

.exam-footer button {
  padding: 10px 32px;
  font-size: 14px;
  font-weight: 500;
}
</style>

