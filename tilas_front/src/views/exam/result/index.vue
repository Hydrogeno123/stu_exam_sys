<template>
  <div class="exam-result-container">
    <!-- 成绩信息卡片 -->
    <div v-if="!loading" class="result-header">
      <div class="score-card">
        <div :class="['score-display', { pass: isPassed }]">
          <div class="score-number">{{ score }}</div>
          <div class="score-label">{{ isPassed ? '及格' : '不及格' }}</div>
        </div>
        <div class="score-info">
          <div class="info-item">
            <span class="label">总分</span>
            <span class="value">{{ examScore.totalScore }} / {{ exam.totalScore }}</span>
          </div>
          <div class="info-item">
            <span class="label">正确题数</span>
            <span class="value">{{ examScore.correctCount }} / {{ papers.length }}</span>
          </div>
          <div class="info-item">
            <span class="label">提交时间</span>
            <span class="value">{{ formatTime(examScore.submitTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">及格线</span>
            <span class="value">{{ exam.passScore }}分</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 答题详情 -->
    <div class="result-content">
      <div class="result-title">
        <h3>答题详情</h3>
      </div>

      <div v-if="loading" style="text-align: center; padding: 20px">
        <el-skeleton :rows="5" animated />
      </div>

      <template v-else>
        <div 
          v-for="(answer, index) in answers" 
          :key="answer.id"
          :class="['answer-item', { correct: answer.isCorrect === 1 }]"
        >
          <div class="answer-header">
            <span class="question-num">第 {{ index + 1 }} 题</span>
            <span class="question-type">{{ getQuestionType(answer.question.type) }}</span>
            <span class="question-score">({{ answer.score }} 分)</span>
            <el-tag :type="answer.isCorrect === 1 ? 'success' : 'danger'" style="margin-left: auto">
              {{ answer.isCorrect === 1 ? '正确' : '错误' }}
            </el-tag>
          </div>

          <div class="answer-content">
            <div class="content-item">
              <label>题目内容:</label>
              <p>{{ answer.question.content }}</p>
            </div>

            <div class="content-item">
              <label>你的答案:</label>
              <p :class="{ 'wrong-answer': answer.isCorrect === 0 }">
                {{ getDisplayAnswer(answer) || '(未作答)' }}
              </p>
            </div>

            <div v-if="answer.isCorrect === 0" class="content-item">
              <label>正确答案:</label>
              <p class="correct-answer">{{ getCorrectAnswer(answer) }}</p>
            </div>

            <div v-if="answer.question.type === 1" class="content-item">
              <label>选项:</label>
              <div class="options-list">
                <div v-for="(option, idx) in parseOptions(answer.question.options)" :key="idx">
                  {{ option }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 操作按钮 -->
    <div class="result-footer">
      <el-button @click="goBack">返回考试列表</el-button>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { getStudentAnswersApi } from '@/api/exam/exam'
import { queryExamByIdApi } from '@/api/exam/exam'
import { queryScoreApi } from '@/api/exam/examScore'

const router = useRouter()
const route = useRoute()

// 数据
const exam = ref({
  examName: '',
  totalScore: 100
})

const examScore = ref({
  totalScore: 0,
  correctCount: 0,
  submitTime: ''
})

const answers = ref([])
const papers = ref([])
const loading = ref(false)

const examId = ref(parseInt(route.params.examId))
const studentId = ref(parseInt(route.params.studentId) || 1)

// 成绩
const score = computed(() => examScore.value.totalScore)
const isPassed = computed(() => {
  const passScore = exam.value.passScore || Math.floor((exam.value.totalScore || 100) * 0.6)
  return score.value >= passScore
})

// 获取答题详情
const loadAnswerDetails = async () => {
  loading.value = true
  try {
    // 获取答题记录
    const answerResult = await getStudentAnswersApi(examId.value, studentId.value)
    if (answerResult && answerResult.code) {
      answers.value = answerResult.data || []
      // 将answers转换为papers格式用于显示
      papers.value = answers.value.map(answer => ({
        question: answer.question
      }))
      
      // 从答题记录中计算总分和正确题数
      let totalScore = 0
      let correctCount = 0
      answers.value.forEach(answer => {
        totalScore += answer.score || 0
        if (answer.isCorrect === 1) {
          correctCount++
        }
      })
      
      // 更新examScore对象
      examScore.value = {
        totalScore: totalScore,
        correctCount: correctCount,
        totalQuestions: answers.value.length
      }
    } else {
      ElMessage.error('获取答题详情失败')
    }

    // 获取考试信息
    const examResult = await queryExamByIdApi(examId.value)
    if (examResult && examResult.code) {
      exam.value = examResult.data || {}
      // 计算及格线（总分的60%取整数）
      const passScore = Math.floor((exam.value.totalScore || 100) * 0.6)
      exam.value.passScore = passScore
    }
  } catch (error) {
    ElMessage.error('加载成绩详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 解析选项
const parseOptions = (optionsStr) => {
  try {
    const options = JSON.parse(optionsStr)
    // 仅返回选项内容（content）而不是整个JSON对象
    return options.map(option => option.content || option)
  } catch {
    return []
  }
}

// 获取显示的答案
// 对于选择题，转换选项序号（1-4）为选项内容或字母
// 对于填空题，直接返回答案
const getDisplayAnswer = (answer) => {
  if (!answer.studentAnswer) return ''
  
  if (answer.question.type === 1) {
    // 选择题：学生答案是选项序号（1、2、3、4）
    const answerNum = parseInt(answer.studentAnswer)
    
    // 优先转换为字母
    if (answerNum >= 1 && answerNum <= 4) {
      return String.fromCharCode(64 + answerNum) // 64+'A'=65
    }
    return answer.studentAnswer
  } else {
    // 填空题：直接返回答案
    return answer.studentAnswer
  }
}

// 获取正确答案的显示值
const getCorrectAnswer = (answer) => {
  if (answer.question.type === 1) {
    // 选择题：正确答案是选项序号（1-4），需要转换为选项内容或字母
    const answerNum = parseInt(answer.question.answer)
    
    // 优先转换为字母
    if (answerNum >= 1 && answerNum <= 4) {
      const letter = String.fromCharCode(64 + answerNum) // 64+'A'=65
      return letter
    }
    return answer.question.answer
  } else {
    // 填空题：直接返回答案
    return answer.question.answer
  }
}

// 获取题目类型
const getQuestionType = (type) => {
  return type === 1 ? '选择题' : '填空题'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

// 返回
const goBack = () => {
  router.push({ name: 'studentExam' })
}

// 返回首页
const goHome = () => {
  router.push({ name: 'index' })
}

// 组件挂载
onMounted(() => {
  loadAnswerDetails()
})
</script>

<style scoped>
.exam-result-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.result-header {
  background: white;
  border-radius: 4px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.score-card {
  display: flex;
  gap: 40px;
  align-items: center;
}

.score-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 150px;
  height: 150px;
  background: linear-gradient(135deg, #f5222d, #ff4d4f);
  border-radius: 50%;
  color: white;
  box-shadow: 0 4px 12px rgba(245, 34, 45, 0.3);
}

.score-display.pass {
  background: linear-gradient(135deg, #52c41a, #85ce61);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

.score-number {
  font-size: 48px;
  font-weight: bold;
}

.score-label {
  font-size: 14px;
  margin-top: 10px;
  opacity: 0.9;
}

.score-info {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-item .label {
  color: #666;
  font-size: 12px;
  margin-bottom: 5px;
}

.info-item .value {
  color: #333;
  font-size: 16px;
  font-weight: bold;
}

.result-content {
  flex: 1;
  background: white;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.result-title {
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.result-title h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.answer-item {
  padding: 20px;
  margin-bottom: 15px;
  background: #fafbfc;
  border-left: 4px solid #dcdfe6;
  border-radius: 3px;
  transition: all 0.3s;
}

.answer-item.correct {
  border-left-color: #67c23a;
  background: #f0f9ff;
}

.answer-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  gap: 12px;
  font-weight: bold;
}

.question-num {
  color: #409eff;
}

.question-type {
  background: #f0f9ff;
  color: #0284c7;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 12px;
  font-weight: normal;
}

.question-score {
  color: #666;
  font-size: 12px;
  font-weight: normal;
}

.answer-content {
  padding: 10px 0;
}

.content-item {
  margin-bottom: 15px;
}

.content-item label {
  display: block;
  color: #666;
  font-size: 12px;
  margin-bottom: 5px;
  font-weight: bold;
}

.content-item p {
  margin: 0;
  color: #333;
  padding: 10px;
  background: white;
  border-radius: 3px;
  line-height: 1.6;
}

.content-item p.wrong-answer {
  background: #fef0f0;
  color: #f56c6c;
}

.content-item p.correct-answer {
  background: #f0f9ff;
  color: #67c23a;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px;
  background: white;
  border-radius: 3px;
}

.options-list div {
  padding: 8px;
  background: #f5f7fa;
  border-radius: 3px;
  font-size: 14px;
}

.result-footer {
  display: flex;
  justify-content: center;
  gap: 15px;
  padding: 20px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>
