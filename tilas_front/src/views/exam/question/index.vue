<template>
  <div class="container">
    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="题库">
          <el-select v-model="searchForm.bankId" placeholder="请选择题库" clearable>
            <el-option
              v-for="bank in questionBanks"
              :key="bank.id"
              :label="bank.bankName"
              :value="bank.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题目类型">
          <el-select v-model="searchForm.type" placeholder="请选择题型" clearable>
            <el-option label="单选题" :value="1" />
            <el-option label="填空题" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容">
          <el-input v-model="searchForm.content" placeholder="请输入题目内容" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="clear">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="operation-container">
      <el-button type="primary" @click="addQuestion">新增题目</el-button>
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
        <el-table-column prop="bankName" label="所属题库" width="120" />
        <el-table-column prop="content" label="题目内容" min-width="200" />
        <el-table-column prop="type" label="题型" width="100">
          <template #default="scope">
            <span>{{ getTypeText(scope.row.type) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="editQuestion(scope.row)">
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="deleteQuestion(scope.row.id)">
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
        <el-form-item label="所属题库" prop="bankId">
          <el-select v-model="form.bankId" placeholder="请选择题库">
            <el-option
              v-for="bank in questionBanks"
              :key="bank.id"
              :label="bank.bankName"
              :value="bank.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题目类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择题型" @change="handleTypeChange">
            <el-option label="单选题" :value="1" />
            <el-option label="填空题" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题目内容" />
        </el-form-item>
        <el-form-item label="选项" v-if="showOptions">
          <div v-for="(option, index) in form.options" :key="index" class="option-item">
            <el-input
              v-model="option.content"
              :placeholder="`选项 ${String.fromCharCode(65 + index)}`"
              style="width: 300px; margin-right: 10px;"
            />
            <el-radio v-model="form.correctOptionIndex" :label="index">
              正确答案
            </el-radio>
          </div>
        </el-form-item>
        <el-form-item label="参考答案" prop="referenceAnswer" v-if="form.type === 2">
          <el-input v-model="form.referenceAnswer" type="textarea" :rows="3" placeholder="请输入参考答案" />
        </el-form-item>
        <el-form-item label="分值" prop="score">
          <el-input-number v-model="form.score" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="解析" prop="analysis">
          <el-input v-model="form.analysis" type="textarea" :rows="2" placeholder="请输入题目解析" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  queryPageApi,
  queryAllQuestionsApi,
  addQuestionApi,
  updateQuestionApi,
  deleteQuestionByIdsApi
} from '@/api/exam/question'
import { queryAllQuestionBanksApi } from '@/api/exam/questionBank'

// 搜索表单
const searchForm = reactive({
  bankId: null,
  type: null,
  content: ''
})

// 题库列表
const questionBanks = ref([])

// 表格数据
const tableData = ref([])
const selectedIds = ref([])

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = reactive({
  id: null,
  bankId: null,
  type: null,
  content: '',
  options: [{ content: '', isCorrect: false }, { content: '', isCorrect: false }, { content: '', isCorrect: false }, { content: '', isCorrect: false }],
  correctOptionIndex: 0,
  referenceAnswer: '',
  score: 5,
  analysis: ''
})

// 计算属性
const showOptions = computed(() => {
  return form.type === 1 || form.type === 2
})

// 表单验证规则
const rules = {
  bankId: [
    { required: true, message: '请选择题库', trigger: 'change' }
  ],
  type: [
    { required: true, message: '请选择题型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入题目内容', trigger: 'blur' }
  ],
  score: [
    { required: true, message: '请输入分值', trigger: 'blur' }
  ]
}

// 获取题型文本
const getTypeText = (type) => {
  const typeMap = {
    1: '单选题',
    2: '填空题'
  }
  return typeMap[type] || '未知'
}

// 查询数据
const search = async () => {
  try {
    // ... existing code ...
    const result = await queryPageApi(
      searchForm.bankId || null,
      searchForm.type || null,
      searchForm.content ? searchForm.content.trim() : null,
      currentPage.value,
      pageSize.value
    )
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
  searchForm.bankId = null
  searchForm.type = null
  searchForm.content = ''
  currentPage.value = 1  // 重置页码
  search()
}

// 新增题目
const addQuestion = () => {
  dialogTitle.value = '新增题目'
  dialogVisible.value = true
  Object.assign(form, {
    id: null,
    bankId: null,
    type: null,
    content: '',
    options: [{ content: '', isCorrect: false }, { content: '', isCorrect: false }, { content: '', isCorrect: false }, { content: '', isCorrect: false }],
    correctAnswer: '',
    referenceAnswer: '',
    score: 5,
    analysis: ''
  })
}

// 编辑题目
const editQuestion = (row) => {
  dialogTitle.value = '编辑题目'
  dialogVisible.value = true
  
  // 初始化表单，只复制必要的字段，排除非数据库字段
  form.id = row.id
  form.bankId = row.bankId
  form.type = row.type
  form.content = row.content
  form.answer = row.answer
  form.score = row.score
  form.analysis = row.analysis || ''
  
  // 处理不同题型的答案
  if (form.type === 1) {
    // 单选题
    // 处理选项数据，确保有4个选项
    if (row.options) {
      form.options = JSON.parse(row.options)
      // 如果选项数量不足4个，补充到4个
      while (form.options.length < 4) {
        form.options.push({ content: '', isCorrect: false })
      }
      // 如果选项数量超过4个，截断到4个
      if (form.options.length > 4) {
        form.options = form.options.slice(0, 4)
      }
    } else {
      // 默认4个选项
      form.options = [
        { content: '', isCorrect: false },
        { content: '', isCorrect: false },
        { content: '', isCorrect: false },
        { content: '', isCorrect: false }
      ]
    }
    
    // 设置正确选项索引
    const correctIndex = form.options.findIndex(option => option.content === row.answer)
    form.correctOptionIndex = correctIndex !== -1 ? correctIndex : 0
    
    form.referenceAnswer = ''
  } else if (form.type === 2) {
    // 填空题
    form.options = []
    form.correctOptionIndex = 0
    form.referenceAnswer = row.answer
  }
}

// 保存题目
const save = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      // 构建提交数据，只包含必要的字段
      const submitData = {
        id: form.id,
        bankId: form.bankId,
        type: form.type,
        content: form.content,
        score: form.score,
        analysis: form.analysis
      }
      
      // 处理不同题型的答案
      if (form.type === 1) {
        // 单选题
        submitData.options = JSON.stringify(form.options)
        
        // 使用选中的选项索引作为正确答案（存储为序号1-4，而不是选项内容）
        if (form.correctOptionIndex >= 0 && form.correctOptionIndex < form.options.length) {
          submitData.answer = String(form.correctOptionIndex + 1)  // 索引转换为序号（0->1, 1->2, 2->3, 3->4）
        } else {
          // 默认第一个选项为正确答案
          submitData.answer = '1'
        }
      } else if (form.type === 2) {
        // 填空题
        submitData.answer = form.referenceAnswer
      }
      
      let result
      if (form.id) {
        result = await updateQuestionApi(submitData)
      } else {
        result = await addQuestionApi(submitData)
      }
      
      if (result.code === 1) {
        ElMessage.success('保存成功')
        dialogVisible.value = false
        search()
      }
    } catch (error) {
      ElMessage.error('保存失败')
    }
  })
}

// 删除题目
const deleteQuestion = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该题目吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await deleteQuestionByIdsApi([id])
    if (result.code) {
      ElMessage.success('删除成功')
      search()
    }
  } catch (error) {
    // 用户取消删除
  }
}

// 批量删除
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm('确定删除选中的题目吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await deleteQuestionByIdsApi(selectedIds.value)
    if (result.code) {
      ElMessage.success('删除成功')
      selectedIds.value = []
      search()
    }
  } catch (error) {
    // 用户取消删除
  }
}



// 处理题型变化
const handleTypeChange = (type) => {
  if (type === 1) {
    // 单选题
    form.options = [
      { content: '', isCorrect: false }, 
      { content: '', isCorrect: false }, 
      { content: '', isCorrect: false }, 
      { content: '', isCorrect: false }
    ]
    form.correctOptionIndex = 0
    form.referenceAnswer = ''
    form.answer = ''
  } else if (type === 2) {
    // 填空题
    form.options = []
    form.correctOptionIndex = 0
    form.referenceAnswer = ''
    form.answer = ''
  }
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  search()
}

// 当前页变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  search()
}

// 加载题库列表
const loadQuestionBanks = async () => {
  try {
    const result = await queryAllQuestionBanksApi()
    if (result.code) {
      questionBanks.value = result.data
    }
  } catch (error) {
    ElMessage.error('加载题库列表失败')
  }
}

// 页面加载时查询数据
onMounted(() => {
  loadQuestionBanks()
  search()
})
</script>

<style scoped>
.container {
  padding: 20px;
}

.search-container {
  margin-bottom: 20px;
}

.operation-container {
  margin-bottom: 20px;
}

.table-container {
  margin-bottom: 20px;
}

.pagination-container {
  text-align: center;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
</style>