<template>
  <div class="admin-exams-management">
    <div class="header">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增考试
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm">
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
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格区域 -->
    <div class="table-area">
      <el-table :data="tableData" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="examName" label="考试名称" min-width="150" />
        <el-table-column prop="teacherName" label="出题教师" width="120" align="center" />
        <el-table-column prop="bankName" label="题库" width="120" align="center" />
        <el-table-column prop="startTime" label="开始时间" width="180" align="center" />
        <el-table-column prop="duration" label="时长(分钟)" width="100" align="center" />
        <el-table-column prop="totalScore" label="总分" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="350" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="warning" @click="editPaperQuestions(scope.row)">编辑题目</el-button>
            <el-button size="small" type="info" @click="viewExamScores(scope.row)">学生成绩</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页区域 -->
    <div class="pagination-area">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

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
    <!-- 学生成绩对话框 -->
    <el-dialog v-model="scoresDialogVisible" :title="scoresDialogTitle" width="900px">
      <el-table :data="studentScores" border stripe max-height="500">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="studentName" label="学生姓名" width="120" align="center" />
        <el-table-column prop="totalScore" label="成绩" width="100" align="center" />
        <el-table-column label="是否及格" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.totalScore >= 60 ? 'success' : 'danger'">
              {{ scope.row.totalScore >= 60 ? '及格' : '不及格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" align="center" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button size="small" type="danger" @click="deleteStudentRecord(scope.row)">
              删除记录
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="考试名称" prop="examName">
          <el-input v-model="form.examName" placeholder="请输入考试名称" />
        </el-form-item>
        <el-form-item label="出题教师" prop="teacherId">
          <el-select v-model="form.teacherId" placeholder="请选择出题教师" style="width: 100%">
            <el-option
              v-for="teacher in teacherOptions"
              :key="teacher.id"
              :label="teacher.name"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题库" prop="bankId">
          <el-select v-model="form.bankId" placeholder="请选择题库" style="width: 100%">
            <el-option
              v-for="bank in bankOptions"
              :key="bank.id"
              :label="bank.bankName"
              :value="bank.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="请选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="考试时长" prop="duration">
          <el-input-number v-model="form.duration" placeholder="请输入考试时长（分钟）" :min="1" :max="480" />
        </el-form-item>
        <el-form-item label="选择题数" prop="choiceCount">
          <el-input-number v-model="form.choiceCount" placeholder="请输入选择题数" :min="0" />
        </el-form-item>
        <el-form-item label="填空题数" prop="fillCount">
          <el-input-number v-model="form.fillCount" placeholder="请输入填空题数" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAdminExamsApi, addAdminExamApi, updateAdminExamApi, deleteAdminExamApi, getAdminExamByIdApi, getAdminQuestionBanksApi } from '@/api/admin'
import { findAllTeachersApi } from '@/api/teacher'
import { getExamPapersApi, saveExamPapersApi } from '@/api/exam/exam'
import { queryQuestionsByBankIdApi } from '@/api/exam/question'
import { queryScoresByExamIdApi, deleteStudentExamRecordApi } from '@/api/exam/examScore'

// 搜索表单
const searchForm = reactive({
  examName: '',
  status: ''
})

// 表格数据
const tableData = ref([])

// 教师和题库选项
const teacherOptions = ref([])
const bankOptions = ref([])

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

// 编辑试卷相关
const editPaperDialogVisible = ref(false)
const editPaperDialogTitle = ref('编辑试卷题目')
const examPapers = ref([])
const availableQuestions = ref([])
const selectedQuestionIds = ref([])
const currentEditExam = ref(null)

// 学生成绩对话框相关
const scoresDialogVisible = ref(false)
const scoresDialogTitle = ref('')
const studentScores = ref([])
const currentExamForScores = ref(null)

// 表单数据
const form = reactive({
  id: '',
  examName: '',
  teacherId: '',
  bankId: '',
  startTime: '',
  duration: 120,
  totalScore: 100,
  choiceCount: 0,
  fillCount: 0
})

// 表单验证规则
const rules = {
  examName: [
    { required: true, message: '请输入考试名称', trigger: 'blur' }
  ],
  teacherId: [
    { required: true, message: '请选择出题教师', trigger: 'change' }
  ],
  bankId: [
    { required: true, message: '请选择题库', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请输入考试时长', trigger: 'change' }
  ],
  totalScore: [
    { required: true, message: '请输入总分', trigger: 'change' }
  ]
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
  
  // 更新总分
  form.totalScore = calculateTotalScore()
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
    // 更新总分
    form.totalScore = calculateTotalScore()
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
    const questionsRes = await queryQuestionsByBankIdApi(row.bankId)
    if (questionsRes.code === 1) {
      // 过滤掉已添加的题目
      const addedIds = new Set(examPapers.value.map(p => p.questionId))
      availableQuestions.value = (questionsRes.data || []).filter(q => !addedIds.has(q.id))
    }
    
    editPaperDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载试卷题目失败')
    console.error(error)
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
      // 更新总分
      form.totalScore = calculateTotalScore()
      editPaperDialogVisible.value = false
      // 刚做了不好的事，需要调電调调loadData来刷新表单
      loadData()
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

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    1: '未开始',
    2: '进行中',
    3: '已结束'
  }
  return statusMap[status] || '未知'
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const tagTypeMap = {
    1: 'info',
    2: 'warning',
    3: 'success'
  }
  return tagTypeMap[status] || 'info'
}

// 加载选项数据
const loadOptions = async () => {
  try {
    const [teachersRes, banksRes] = await Promise.all([
      findAllTeachersApi(),
      getAdminQuestionBanksApi({ page: 1, pageSize: 100 })
    ])
    
    if (teachersRes.code === 1) {
      teacherOptions.value = teachersRes.data || []
    }
    if (banksRes.code === 1) {
      bankOptions.value = banksRes.data.rows || []
    }
  } catch (error) {
    console.error('加载选项数据异常:', error)
  }
}

// 加载考试数据
const loadData = async () => {
  try {
    const params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    if (searchForm.examName) params.examName = searchForm.examName
    if (searchForm.status !== '') params.status = searchForm.status

    const response = await getAdminExamsApi(params)
    if (response.code === 1) {
      tableData.value = response.data.rows
      pagination.total = response.data.total
    } else {
      ElMessage.error(response.msg || '加载考试列表失败')
    }
  } catch (error) {
    ElMessage.error('加载考试列表异常')
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadData()
}

// 处理重置
const handleReset = () => {
  searchForm.examName = ''
  searchForm.status = ''
  pagination.currentPage = 1
  loadData()
}

// 处理分页大小改变
const handleSizeChange = () => {
  pagination.currentPage = 1
  loadData()
}

// 处理分页页码改变
const handleCurrentChange = () => {
  loadData()
}

// 处理新增
const handleAdd = () => {
  dialogTitle.value = '新增考试'
  form.id = ''
  form.examName = ''
  form.teacherId = ''
  form.bankId = ''
  form.startTime = ''
  form.duration = 120
  form.totalScore = 100
  form.choiceCount = 0
  form.fillCount = 0
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = async (row) => {
  try {
    const response = await getAdminExamByIdApi(row.id)
    if (response.code === 1) {
      dialogTitle.value = '编辑考试'
      Object.assign(form, response.data)
      dialogVisible.value = true
    } else {
      ElMessage.error(response.msg || '加载考试信息失败')
    }
  } catch (error) {
    ElMessage.error('加载考试信息异常')
  }
}

// 处理删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除考试 ${row.examName} 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        const response = await deleteAdminExamApi([row.id])
        if (response.code === 1) {
          ElMessage.success('删除成功')
          loadData()
        } else {
          ElMessage.error(response.msg || '删除失败')
        }
      } catch (error) {
        ElMessage.error('删除异常')
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
}

// 处理表单提交
const handleSubmit = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      let response
      if (form.id) {
        response = await updateAdminExamApi(form)
      } else {
        response = await addAdminExamApi(form)
      }

      if (response.code === 1) {
        ElMessage.success(form.id ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(response.msg || (form.id ? '编辑失败' : '新增失败'))
      }
    } catch (error) {
      ElMessage.error(form.id ? '编辑异常' : '新增异常')
    }
  })
}

// 查看学生成绩
const viewExamScores = async (row) => {
  try {
    currentExamForScores.value = row
    scoresDialogTitle.value = `${row.examName} - 学生成绩`
    
    // 加载该考试的所有学生成绩
    const result = await queryScoresByExamIdApi(row.id)
    if (result.code === 1) {
      studentScores.value = result.data || []
    } else {
      ElMessage.error(result.msg || '加载学生成绩失败')
    }
    
    scoresDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载学生成绩异常')
    console.error(error)
  }
}

// 删除学生的考试记录
const deleteStudentRecord = (row) => {
  ElMessageBox.confirm(`确定删除学生 ${row.studentName} 的考试记录吗？删除后学生可以重新参加考试。`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        const response = await deleteStudentExamRecordApi(currentExamForScores.value.id, row.studentId)
        if (response.code === 1) {
          ElMessage.success('删除成功，学生可以重新参加考试')
          // 刷新学生成绩列表
          await viewExamScores(currentExamForScores.value)
        } else {
          ElMessage.error(response.msg || '删除失败')
        }
      } catch (error) {
        ElMessage.error('删除异常')
        console.error(error)
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
}

// 处理关闭对话框
const handleClose = () => {
  dialogVisible.value = false
}

// 组件挂载时加载数据
onMounted(() => {
  loadOptions()
  loadData()
})
</script>

<style scoped>
.admin-exams-management {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.search-area {
  background-color: #f5f7fa;
  padding: 20px;
  margin-bottom: 20px;
  border-radius: 4px;
}

.table-area {
  margin-bottom: 20px;
}

.pagination-area {
  display: flex;
  justify-content: flex-end;
  padding: 20px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
