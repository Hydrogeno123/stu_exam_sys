<template>
  <div class="admin-questions-management">
    <div class="header">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增试题
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="题库">
          <el-select v-model="searchForm.bankId" placeholder="请选择题库" clearable style="width: 200px">
            <el-option
              v-for="bank in bankOptions"
              :key="bank.id"
              :label="bank.bankName"
              :value="bank.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="searchForm.type" placeholder="请选择题型" clearable style="width: 150px">
            <el-option label="选择题" :value="1" />
            <el-option label="填空题" :value="2" />
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
        <el-table-column prop="bankName" label="题库" width="120" align="center" />
        <el-table-column prop="type" label="题型" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.type === 1 ? 'success' : 'info'">
              {{ scope.row.type === 1 ? '选择题' : '填空题' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题目内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="score" label="分值" width="80" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
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

    <!-- 添加/编辑试题对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :before-close="handleClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
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
        <el-form-item label="题型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">选择题</el-radio>
            <el-radio :label="2">填空题</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="form.content" type="textarea" placeholder="请输入题目内容" rows="4" />
        </el-form-item>
        <el-form-item label="分值" prop="score">
          <el-input-number v-model="form.score" placeholder="请输入分值" :min="1" :max="100" />
        </el-form-item>
        
        <!-- 选择题选项 -->
        <div v-if="form.type === 1">
          <el-form-item label="选项">
            <div class="options-container">
              <div v-for="(option, index) in form.options" :key="index" class="option-item">
                <el-input v-model="option.content" placeholder="请输入选项内容" style="margin-bottom: 8px" />
                <el-checkbox v-model="option.isCorrect">正确答案</el-checkbox>
                <el-button type="danger" size="small" @click="removeOption(index)" style="margin-left: 10px">删除</el-button>
              </div>
              <el-button @click="addOption" type="dashed" style="width: 100%">添加选项</el-button>
            </div>
          </el-form-item>
        </div>

        <!-- 填空题答案 -->
        <div v-else>
          <el-form-item label="正确答案" prop="answer">
            <el-input v-model="form.answer" placeholder="请输入正确答案" />
          </el-form-item>
        </div>
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
import { getAdminQuestionsApi, addAdminQuestionApi, updateAdminQuestionApi, deleteAdminQuestionApi, getAdminQuestionByIdApi, getAdminQuestionBanksApi } from '@/api/admin'

// 搜索表单
const searchForm = reactive({
  bankId: '',
  type: ''
})

// 表格数据
const tableData = ref([])

// 题库选项
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

// 表单数据
const form = reactive({
  id: '',
  bankId: '',
  type: 1,
  content: '',
  score: 5,
  answer: '',
  options: [
    { content: '', isCorrect: false },
    { content: '', isCorrect: false },
    { content: '', isCorrect: false },
    { content: '', isCorrect: false }
  ]
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
    { required: true, message: '请输入分值', trigger: 'change' }
  ],
  answer: [
    { required: true, message: '请输入正确答案', trigger: 'blur' }
  ]
}

// 加载题库选项
const loadBankOptions = async () => {
  try {
    const response = await getAdminQuestionBanksApi({ page: 1, pageSize: 100 })
    if (response.code === 1) {
      bankOptions.value = response.data.rows || []
    } else {
      console.error('加载题库列表失败:', response.msg)
    }
  } catch (error) {
    console.error('加载题库列表异常:', error)
  }
}

// 加载试题数据
const loadData = async () => {
  try {
    const params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    if (searchForm.bankId) params.bankId = searchForm.bankId
    if (searchForm.type !== '') params.type = searchForm.type

    const response = await getAdminQuestionsApi(params)
    if (response.code === 1) {
      tableData.value = response.data.rows
      pagination.total = response.data.total
    } else {
      ElMessage.error(response.msg || '加载试题列表失败')
    }
  } catch (error) {
    ElMessage.error('加载试题列表异常')
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadData()
}

// 处理重置
const handleReset = () => {
  searchForm.bankId = ''
  searchForm.type = ''
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

// 添加选项
const addOption = () => {
  form.options.push({ content: '', isCorrect: false })
}

// 删除选项
const removeOption = (index) => {
  if (form.options.length > 2) {
    form.options.splice(index, 1)
  } else {
    ElMessage.warning('至少需要2个选项')
  }
}

// 处理新增
const handleAdd = () => {
  dialogTitle.value = '新增试题'
  form.id = ''
  form.bankId = ''
  form.type = 1
  form.content = ''
  form.score = 5
  form.answer = ''
  form.options = [
    { content: '', isCorrect: false },
    { content: '', isCorrect: false },
    { content: '', isCorrect: false },
    { content: '', isCorrect: false }
  ]
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = async (row) => {
  try {
    const response = await getAdminQuestionByIdApi(row.id)
    if (response.code === 1) {
      dialogTitle.value = '编辑试题'
      const data = response.data
      form.id = data.id
      form.bankId = data.bankId
      form.type = data.type
      form.content = data.content
      form.score = data.score
      form.answer = data.answer
      if (data.type === 1 && data.options) {
        form.options = JSON.parse(data.options)
      } else {
        form.options = [
          { content: '', isCorrect: false },
          { content: '', isCorrect: false },
          { content: '', isCorrect: false },
          { content: '', isCorrect: false }
        ]
      }
      dialogVisible.value = true
    } else {
      ElMessage.error(response.msg || '加载试题信息失败')
    }
  } catch (error) {
    ElMessage.error('加载试题信息异常')
  }
}

// 处理删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除此试题吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        const response = await deleteAdminQuestionApi([row.id])
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
      let submitData = { ...form }
      
      // 处理选择题的选项
      if (form.type === 1) {
        const hasCorrect = form.options.some(opt => opt.isCorrect)
        if (!hasCorrect) {
          ElMessage.error('请至少选择一个正确答案')
          return
        }
        submitData.options = JSON.stringify(form.options)
        // 设置正确答案的索引
        submitData.answer = (form.options.findIndex(opt => opt.isCorrect) + 1).toString()
      }

      let response
      if (form.id) {
        response = await updateAdminQuestionApi(submitData)
      } else {
        response = await addAdminQuestionApi(submitData)
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

// 处理关闭对话框
const handleClose = () => {
  dialogVisible.value = false
}

// 组件挂载时加载数据
onMounted(() => {
  loadBankOptions()
  loadData()
})
</script>

<style scoped>
.admin-questions-management {
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

.options-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 12px;
}

.option-item {
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.option-item:last-child {
  border-bottom: none;
}
</style>
