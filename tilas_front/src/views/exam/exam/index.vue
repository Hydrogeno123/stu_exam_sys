<template>
  <div class="container">
    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="考试名称">
          <el-input v-model="searchForm.examName" placeholder="请输入考试名称" clearable />
        </el-form-item>
        <el-form-item label="考试状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="未开始" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已结束" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试时间">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="clear">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="operation-container">
      <el-button type="primary" @click="addExam">新增考试</el-button>
      <el-button type="success" @click="publishExam" :disabled="selectedIds.length === 0">
        批量发布
      </el-button>
      <el-button type="danger" @click="batchDelete" :disabled="selectedIds.length === 0">
        批量删除
      </el-button>
    </div>

    <!-- 表格区域 -->
    <div class="table-container">
      <el-table
        :data="tableData"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="examName" label="考试名称" min-width="150" />
        <el-table-column prop="bankName" label="所属题库" width="120" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column prop="duration" label="时长(分钟)" width="100" />
        <el-table-column prop="choiceCount" label="选择题数量" width="120" />
        <el-table-column prop="fillCount" label="填空题数量" width="120" />
        <el-table-column prop="clazzName" label="所属班级" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="400" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              @click="editExam(scope.row)"
              :disabled="scope.row.status === 2 || scope.row.status === 3 || (scope.row.status === 1 && new Date() >= new Date(scope.row.startTime))"
            >
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="warning" 
              @click="editPaperQuestions(scope.row)"
              :disabled="scope.row.status === 1 || scope.row.status === 2 || scope.row.status === 3"
            >
              编辑题目
            </el-button>
            <el-button 
              size="small" 
              type="success" 
              @click="publishSingleExam(scope.row)"
              :disabled="scope.row.status !== 0"
            >
              发布
            </el-button>
            <el-button size="small" type="danger" @click="deleteExam(scope.row.id)">
              删除
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="考试名称" prop="examName">
          <el-input v-model="form.examName" placeholder="请输入考试名称" />
        </el-form-item>
        <el-form-item label="所属题库" prop="bankId">
          <el-select v-model="form.bankId" placeholder="请选择题库" style="width: 100%">
            <el-option
              v-for="bank in questionBanks"
              :key="bank.id"
              :label="bank.bankName"
              :value="bank.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考试总分" prop="totalScore">
          <el-input-number v-model="form.totalScore" :min="1" :max="1000" :disabled="form.choiceCount > 0 || form.fillCount > 0" />
          <span v-if="form.choiceCount > 0 || form.fillCount > 0" style="margin-left: 10px; color: #909399; font-size: 12px;">
            (根据选择题目自动计算)
          </span>
        </el-form-item>
        <el-form-item label="考试时长" prop="duration">
          <el-input-number v-model="form.duration" :min="1" :max="480" />
          <span style="margin-left: 10px">分钟</span>
        </el-form-item>
        <el-form-item label="选择题数量" prop="choiceCount">
          <el-input-number v-model="form.choiceCount" :min="0" :max="100" @change="onQuestionCountChange" />
        </el-form-item>
        <el-form-item label="填空题数量" prop="fillCount">
          <el-input-number v-model="form.fillCount" :min="0" :max="100" @change="onQuestionCountChange" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="所属班级" prop="clazzId">
          <el-select v-model="form.clazzId" placeholder="请选择班级" style="width: 100%">
            <el-option
              v-for="clazz in clazzList"
              :key="clazz.id"
              :label="clazz.clazzName"
              :value="clazz.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 成绩查看对话框 -->
    <el-dialog v-model="scoreDialogVisible" title="考试成绩" width="800px">
      <el-table :data="scoreData" border>
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="score" label="得分" width="100" />
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column prop="passScore" label="及格分" width="100" />
        <el-table-column label="是否及格" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.score >= scope.row.passScore ? 'success' : 'danger'">
              {{ scope.row.score >= scope.row.passScore ? '及格' : '不及格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewAnswerDetails(scope.row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 编辑试卷题目对话框 -->
    <el-dialog v-model="editPaperDialogVisible" :title="editPaperDialogTitle" width="900px">
      <div style="margin-bottom: 20px; padding: 15px; background-color: #f5f7fa; border-radius: 4px;">
        <p style="color: #606266; margin-bottom: 10px;">
          <strong>考试信息：</strong>{{ currentEditExam?.examName }} 
          （需要添加 {{ currentEditExam?.choiceCount }} 道选择题，{{ currentEditExam?.fillCount }} 道填空题）
        </p>
        <p style="color: #606266; margin-bottom: 10px;">
          <strong>已添加题目：</strong><el-tag type="info">{{ examPapers.length }}</el-tag>
          <strong style="margin-left: 10px;">还需添加：</strong>
          <el-tag :type="getRequiredCount() === 0 ? 'success' : 'danger'">{{ getRequiredCount() }}</el-tag>
        </p>
        <p style="color: #606266;">
          <strong>试卷总分：</strong><el-tag type="primary">{{ calculateTotalScore() }} 分</el-tag>
        </p>
      </div>

      <!-- 已添加的题目列表 -->
      <div style="margin-bottom: 20px;">
        <h4>已添加题目列表</h4>
        <el-table :data="examPapers" border max-height="300">
          <el-table-column type="selection" width="50" />
          <el-table-column prop="question.content" label="题目内容" min-width="200" show-overflow-tooltip />
          <el-table-column label="题型" width="80">
            <template #default="scope">
              {{ scope.row.question.type === 1 ? '选择题' : '填空题' }}
            </template>
          </el-table-column>
          <el-table-column prop="question.score" label="分值" width="60" />
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button size="small" type="danger" @click="removeQuestion(scope.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 可添加的题目列表 -->
      <div style="margin-bottom: 20px;">
        <h4>可添加题目</h4>
        <el-table 
          :data="availableQuestions" 
          border 
          max-height="300"
          @selection-change="handleQuestionSelectionChange"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="content" label="题目内容" min-width="200" show-overflow-tooltip />
          <el-table-column label="题型" width="80">
            <template #default="scope">
              {{ scope.row.type === 1 ? '选择题' : '填空题' }}
            </template>
          </el-table-column>
          <el-table-column prop="score" label="分值" width="60" />
        </el-table>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editPaperDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addSelectedQuestions">添加选中题目</el-button>
          <el-button type="success" @click="saveEditPaper">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  queryPageApi,
  addExamApi,
  updateExamApi,
  deleteExamByIdsApi,
  updateExamStatusApi,
  generateExamPaperApi,
  getExamPapersApi,
  saveExamPapersApi
} from '@/api/exam/exam'
import { queryAllApi } from '@/api/exam/questionBank'
import { queryByExamIdApi } from '@/api/exam/examScore'
import { queryQuestionsByBankIdApi } from '@/api/exam/question'
import { getClazzByTeacherIdApi, getAllClazzApi } from '@/api/clazz'

const searchForm = reactive({
  examName: '',
  status: '',
  timeRange: []
})

// 表格数据
const tableData = ref([])
const selectedIds = ref([])

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 题库列表
const questionBanks = ref([])

// 班级列表
const clazzList = ref([])

// 对话框相关
const dialogVisible = ref(false)
const scoreDialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const isEditing = ref(false)

// 当前教师ID
const currentTeacherId = ref(null)

// 获取当前教师信息
const getCurrentTeacher = () => {
  const user = JSON.parse(localStorage.getItem('token'))
  if (user && user.id) {
    currentTeacherId.value = user.id
  }
}
const form = reactive({
  id: null,
  examName: '',
  bankId: null,
  totalScore: 100,
  duration: 60,
  choiceCount: 0,
  fillCount: 0,
  startTime: '',
  status: 0,
  teacherId: null,
  clazzId: null
})

// 成绩数据
const scoreData = ref([])
const currentExamId = ref(null)

// 编辑试卷相关
const editPaperDialogVisible = ref(false)
const editPaperDialogTitle = ref('编辑试卷题目')
const examPapers = ref([])
const availableQuestions = ref([])
const selectedQuestionIds = ref([])
const currentEditExam = ref(null)

// 表单验证规则
const rules = {
  examName: [
    { required: true, message: '请输入考试名称', trigger: 'blur' }
  ],
  bankId: [
    { required: true, message: '请选择题库', trigger: 'change' }
  ],
  totalScore: [
    { required: true, message: '请输入总分', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入考试时长', trigger: 'blur' }
  ],
  choiceCount: [
    { required: true, message: '请输入选择题数量', trigger: 'blur' }
  ],
  fillCount: [
    { required: true, message: '请输入填空题数量', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  clazzId: [
    { required: true, message: '请选择班级', trigger: 'change' }
  ]
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    1: '未开始',
    2: '进行中',
    3: '已结束'
  }
  return statusMap[status] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    1: 'info',
    2: 'success',
    3: 'warning'
  }
  return typeMap[status] || 'info'
}

// 查询数据
const search = async () => {
  try {
    const params = {
      examName: searchForm.examName,
      status: searchForm.status,
      startTime: searchForm.timeRange?.[0] || '',
      endTime: searchForm.timeRange?.[1] || '',
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    const result = await queryPageApi(params)
    if (result.code === 1) {
      tableData.value = result.data.rows
      total.value = result.data.total
    }
  } catch (error) {
    ElMessage.error('查询失败')
  }
}

// 清空搜索条件
const clear = () => {
  searchForm.examName = ''
  searchForm.status = ''
  searchForm.timeRange = []
  search()
}

// 加载题库列表和班级列表
const loadQuestionBanks = async () => {
  try {
    const result = await queryAllApi()
    if (result.code) {
      questionBanks.value = result.data
    }
  } catch (error) {
    ElMessage.error('加载题库列表失败')
  }
}

// 加载班级列表
const loadClazzList = async () => {
  try {
    // 优先获取当前教师的班级
    const result = await getClazzByTeacherIdApi(currentTeacherId.value)
    if (result.code === 1) {
      clazzList.value = result.data || []
    } else if (result.data && result.data.length > 0) {
      clazzList.value = result.data
    } else {
      // 如果教师没有班级，加载所有班级
      const allResult = await getAllClazzApi()
      if (allResult.code === 1) {
        clazzList.value = allResult.data || []
      }
    }
  } catch (error) {
    ElMessage.warning('加载班级列表失败，可能欠少教师班级信息')
    console.error(error)
  }
}

// 新增考试
const addExam = () => {
  dialogTitle.value = '新增考试'
  isEditing.value = false
  dialogVisible.value = true
  Object.assign(form, {
    id: null,
    examName: '',
    bankId: null,
    totalScore: 100,
    duration: 60,
    choiceCount: 0,
    fillCount: 0,
    startTime: '',
    status: 0,
    teacherId: currentTeacherId.value,
    clazzId: null
  })
}

// 编辑考试
const editExam = (row) => {
  // 检查是否允许编辑：只能编辑状态为0（未发布）或状态为1且还未开始的考试
  if (row.status !== 0 && row.status !== 1) {
    ElMessage.warning('只能编辑未发布或未开始的考试')
    return
  }
  
  // 如果是已发布但未开始（状态为1），检查是否已经开始
  if (row.status === 1) {
    const now = new Date()
    const startTime = new Date(row.startTime)
    if (now >= startTime) {
      ElMessage.warning('考试已开始，无法编辑')
      return
    }
  }
  
  dialogTitle.value = '编辑考试'
  isEditing.value = true
  dialogVisible.value = true
  Object.assign(form, {
    id: row.id,
    examName: row.examName,
    bankId: row.bankId,
    totalScore: row.totalScore,
    duration: row.duration,
    choiceCount: row.choiceCount,
    fillCount: row.fillCount,
    startTime: row.startTime,
    status: row.status,
    clazzId: row.clazzId,
    teacherId: row.teacherId
  })
}

// 根据题目数量变化自动计算总分
const onQuestionCountChange = async () => {
  if (!form.bankId) {
    ElMessage.warning('请先选择题库')
    form.totalScore = 0
    return
  }
  
  try {
    // 获取该题库的所有题目
    const result = await queryQuestionsByBankIdApi(form.bankId)
    if (result.code === 1 && result.data) {
      const questions = result.data
      
      // 分别获取选择题和填空题
      const choiceQuestions = questions.filter(q => q.type === 1)
      const fillQuestions = questions.filter(q => q.type === 2)
      
      let totalScore = 0
      
      // 计算选择题总分
      if (form.choiceCount > 0 && choiceQuestions.length > 0) {
        const selectedChoices = choiceQuestions.slice(0, form.choiceCount)
        const choiceTotal = selectedChoices.reduce((sum, q) => sum + (q.score || 0), 0)
        totalScore += choiceTotal
      }
      
      // 计算填空题总分
      if (form.fillCount > 0 && fillQuestions.length > 0) {
        const selectedFills = fillQuestions.slice(0, form.fillCount)
        const fillTotal = selectedFills.reduce((sum, q) => sum + (q.score || 0), 0)
        totalScore += fillTotal
      }
      
      form.totalScore = totalScore || 0
    }
  } catch (error) {
    ElMessage.error('自动计算总分失败，请手动设置')
    console.error(error)
  }
}

// 保存考试
const save = async () => {
  if (!formRef.value) return
  
  // 确保 teacherId 被设置
  if (!form.teacherId) {
    form.teacherId = currentTeacherId.value
  }
  
  if (!form.teacherId) {
    ElMessage.error('无法获取教师信息，请重新登录')
    return
  }
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      // 如果是编辑已发布的考试，需要检查是否已开始
      if (isEditing.value && form.status === 1) {
        const now = new Date()
        const startTime = new Date(form.startTime)
        if (now >= startTime) {
          ElMessage.warning('考试已开始，无法编辑')
          return
        }
      }
      
      let result
      if (form.id) {
        result = await updateExamApi(form)
      } else {
        result = await addExamApi(form)
      }
      
      if (result.code === 1) {
        ElMessage.success('保存成功')
        dialogVisible.value = false
        search()
      }
    } catch (error) {
      ElMessage.error('保存失败')
      console.error(error)
    }
  })
}

// 删除考试
const deleteExam = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该考试吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await deleteExamByIdsApi([id])
    if (result.code) {
      ElMessage.success('删除成功')
      search()
    }
  } catch (error) {
    // 用户取消删除
  }
}

// 编辑试卷题目
const editPaperQuestions = async (row) => {
  try {
    currentEditExam.value = row
    editPaperDialogTitle.value = `编辑试卷题目 - ${row.examName}`
    selectedQuestionIds.value = []
    
    // 获取试卷已有的题目
    const paperResult = await getExamPapersApi(row.id)
    if (paperResult.code === 1) {
      examPapers.value = paperResult.data || []
    }
    
    // 获取题库中的可用题目
    const questionsResult = await queryQuestionsByBankIdApi(row.bankId)
    if (questionsResult.code === 1) {
      // 过滤掉已添加的题目
      const addedIds = new Set(examPapers.value.map(p => p.questionId))
      availableQuestions.value = (questionsResult.data || []).filter(q => !addedIds.has(q.id))
    }
    
    editPaperDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载试卷题目失败')
    console.error(error)
  }
}

// 获取还需添加的题目数量（按题型分别统计）
const getRequiredCount = () => {
  if (!currentEditExam.value) return 0
  const total = (currentEditExam.value.choiceCount || 0) + (currentEditExam.value.fillCount || 0)
  return Math.max(0, total - examPapers.value.length)
}

// 获取每种题型需要添加的数量
const getRequiredByType = () => {
  if (!currentEditExam.value) return { choice: 0, fill: 0 }
  
  // 计算已添加的每种题型数量
  const addedChoice = examPapers.value.filter(p => p.question.type === 1).length
  const addedFill = examPapers.value.filter(p => p.question.type === 2).length
  
  return {
    choice: Math.max(0, (currentEditExam.value.choiceCount || 0) - addedChoice),
    fill: Math.max(0, (currentEditExam.value.fillCount || 0) - addedFill)
  }
}

// 试卷题目选择变化事件
const handleQuestionSelectionChange = (selection) => {
  selectedQuestionIds.value = selection.map(item => item.id)
}

// 添加选中的题目
const addSelectedQuestions = () => {
  if (selectedQuestionIds.value.length === 0) {
    ElMessage.warning('请先选择要添加的题目')
    return
  }
  
  // 获取选中的题目数据
  const selectedData = availableQuestions.value.filter(q => selectedQuestionIds.value.includes(q.id))
  
  // 统计选中题目中的每种题型
  const selectedChoice = selectedData.filter(q => q.type === 1).length
  const selectedFill = selectedData.filter(q => q.type === 2).length
  
  // 获取需要的每种题目数量
  const required = getRequiredByType()
  
  // 检查选择题数量是否超过
  if (selectedChoice > required.choice) {
    ElMessage.warning(`选择题最多还能添加 ${required.choice} 道，但您选择了 ${selectedChoice} 道`)
    return
  }
  
  // 检查填空题数量是否超过
  if (selectedFill > required.fill) {
    ElMessage.warning(`填空题最多还能添加 ${required.fill} 道，但您选择了 ${selectedFill} 道`)
    return
  }
  
  // 添加到已添加列表
  const nextOrder = examPapers.value.length + 1
  selectedData.forEach((q, index) => {
    examPapers.value.push({
      questionId: q.id,
      question: q,
      questionOrder: nextOrder + index,
      examId: currentEditExam.value.id
    })
  })
  
  // 从可用列表中移除
  availableQuestions.value = availableQuestions.value.filter(q => !selectedQuestionIds.value.includes(q.id))
  selectedQuestionIds.value = []
  
  ElMessage.success(`成功添加 ${selectedData.length} 道题目（选择题: ${selectedChoice}道, 填空题: ${selectedFill}道）`)
}

// 移除题目
const removeQuestion = (paper) => {
  const index = examPapers.value.findIndex(p => p.questionId === paper.questionId)
  if (index > -1) {
    const removed = examPapers.value.splice(index, 1)[0]
    // 加回到可用列表
    availableQuestions.value.push(removed.question)
    // 重新排序
    examPapers.value.forEach((p, idx) => {
      p.questionOrder = idx + 1
    })
  }
}

// 保存编辑的试卷
const saveEditPaper = async () => {
  const required = getRequiredCount()
  if (required > 0) {
    ElMessage.error(`试卷题目不足，还需添加 ${required} 道题目`)
    return
  }
  
  try {
    // 准备保存的数据
    const papersToSave = examPapers.value.map((p, idx) => ({
      examId: p.examId,
      questionId: p.questionId,
      questionOrder: idx + 1,
      createTime: p.createTime || new Date()
    }))
    
    const result = await saveExamPapersApi(currentEditExam.value.id, papersToSave)
    if (result.code === 1) {
      ElMessage.success('保存成功')
      editPaperDialogVisible.value = false
    } else {
      ElMessage.error(result.msg || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存试卷失败')
    console.error(error)
  }
}

// 计算试卷总分
const calculateTotalScore = () => {
  if (examPapers.value.length === 0) {
    return 0
  }
  return examPapers.value.reduce((total, paper) => {
    return total + (paper.question?.score || 0)
  }, 0)
}

// 发布单个考试
const publishSingleExam = async (row) => {
  try {
    // 先检查试卷题目是否满足要求
    const paperResult = await getExamPapersApi(row.id)
    if (paperResult.code === 1) {
      const papers = paperResult.data || []
      const required = (row.choiceCount || 0) + (row.fillCount || 0)
      if (papers.length !== required) {
        ElMessage.error(`试卷题目数量不符，需要 ${required} 道题，实际有 ${papers.length} 道`)
        return
      }
    }
    
    await ElMessageBox.confirm('确定发布该考试吗？发布后学生将可以参加考试。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 更新考试状态为发布
    const result = await updateExamStatusApi(row.id, '1')
    if (result.code) {
      ElMessage.success('发布成功')
      search()
    } else {
      ElMessage.error(result.msg || '发布失败')
    }
  } catch (error) {
    if (error && error.response) {
      ElMessage.error(error.response.data?.msg || '发布失败')
    }
    // 用户取消发布
  }
}

// 批量发布考试
const publishExam = async () => {
  try {
    // 先检查所有选中考试的试卷题目
    for (const examId of selectedIds.value) {
      const exam = tableData.value.find(e => e.id === examId)
      if (!exam) continue
      
      const paperResult = await getExamPapersApi(examId)
      if (paperResult.code === 1) {
        const papers = paperResult.data || []
        const required = (exam.choiceCount || 0) + (exam.fillCount || 0)
        if (papers.length !== required) {
          ElMessage.error(`考试"${exam.examName}"的试卷题目数量不符，需要 ${required} 道题，实际有 ${papers.length} 道`)
          return
        }
      }
    }
    
    await ElMessageBox.confirm('确定发布选中的考试吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 更新所有考试的状态
    const promises = selectedIds.value.map(id => updateExamStatusApi(id, '1'))
    await Promise.all(promises)
    ElMessage.success('批量发布成功')
    search()
  } catch (error) {
    if (error && error.response) {
      ElMessage.error(error.response.data?.msg || '发布失败')
    }
    // 用户取消发布
  }
}



// 查看答题详情
const viewAnswerDetails = (row) => {
  ElMessage.info('查看答题详情功能待开发')
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
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
  getCurrentTeacher()
  loadQuestionBanks()
  loadClazzList()
  search()
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

.operation-container {
  margin-bottom: 20px;
}

.table-container {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
}
</style>