<template>
  <div class="container">
    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="题库名称">
          <el-input v-model="searchForm.bankName" placeholder="请输入题库名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="clear">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="operation-container">
      <el-button type="primary" @click="addQuestionBank">新增题库</el-button>
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
        <el-table-column prop="bankName" label="题库名称" min-width="150" />
        <el-table-column prop="description" label="题库描述" min-width="200" />
        <el-table-column prop="questionCount" label="题目数量" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="editQuestionBank(scope.row)">
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="deleteQuestionBank(scope.row.id)">
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="题库名称" prop="bankName">
          <el-input v-model="form.bankName" placeholder="请输入题库名称" />
        </el-form-item>
        <el-form-item label="题库描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入题库描述"
          />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  queryPageApi,
  queryAllQuestionBanksApi,
  addQuestionBankApi,
  updateQuestionBankApi,
  deleteQuestionBankByIdsApi
} from '@/api/exam/questionBank'

// 搜索表单
const searchForm = reactive({
  bankName: ''
})

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
  bankName: '',
  description: ''
})

// 表单验证规则
const rules = {
  bankName: [
    { required: true, message: '请输入题库名称', trigger: 'blur' }
  ]
}

// 查询数据
const search = async () => {
  try {
    // 如果有搜索条件，使用分页查询，否则查询所有
    let result
    if (searchForm.bankName && searchForm.bankName.trim()) {
      result = await queryPageApi(searchForm.bankName, currentPage.value, pageSize.value)
    } else {
      // 没有搜索条件时显示所有数据
      result = await queryAllQuestionBanksApi()
    }
    
    if (result.code) {
      if (searchForm.bankName && searchForm.bankName.trim()) {
        // 分页查询结果
        tableData.value = result.data.rows
        total.value = result.data.total
      } else {
        // 查询所有结果
        tableData.value = result.data
        total.value = result.data.length
      }
    }
  } catch (error) {
    ElMessage.error('查询失败')
  }
}

// 清空搜索条件
const clear = () => {
  searchForm.bankName = ''
  currentPage.value = 1
  search()
}

// 新增题库
const addQuestionBank = () => {
  dialogTitle.value = '新增题库'
  dialogVisible.value = true
  Object.assign(form, { id: null, bankName: '', description: '' })
}

// 编辑题库
const editQuestionBank = (row) => {
  dialogTitle.value = '编辑题库'
  dialogVisible.value = true
  Object.assign(form, row)
}

// 保存题库
const save = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      let result
      if (form.id) {
        result = await updateQuestionBankApi(form)
      } else {
        result = await addQuestionBankApi(form)
      }
      
      if (result.code) {
        ElMessage.success('保存成功')
        dialogVisible.value = false
        search()
      }
    } catch (error) {
      ElMessage.error('保存失败')
    }
  })
}

// 删除题库
const deleteQuestionBank = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该题库吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await deleteQuestionBankByIdsApi([id])
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
    await ElMessageBox.confirm('确定删除选中的题库吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await deleteQuestionBankByIdsApi(selectedIds.value)
    if (result.code) {
      ElMessage.success('删除成功')
      selectedIds.value = []
      search()
    }
  } catch (error) {
    // 用户取消删除
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

// 页面加载时查询数据
onMounted(() => {
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
</style>