<template>
  <div class="admin-clazz-management">
    <div class="header">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增班级
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="班级编号">
          <el-input v-model="searchForm.clazzCode" placeholder="请输入班级编号" clearable />
        </el-form-item>
        <el-form-item label="班级名称">
          <el-input v-model="searchForm.clazzName" placeholder="请输入班级名称" clearable />
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
        <el-table-column prop="clazzCode" label="班级编号" width="120" align="center" />
        <el-table-column prop="clazzName" label="班级名称" width="150" align="center" />
        <el-table-column prop="teacherName" label="班主任" width="120" align="center" />
        <el-table-column prop="description" label="描述" min-width="150" />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
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

    <!-- 添加/编辑班级对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :before-close="handleClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="班级编号" prop="clazzCode">
          <el-input v-model="form.clazzCode" placeholder="请输入班级编号" :disabled="form.id ? true : false" />
        </el-form-item>
        <el-form-item label="班级名称" prop="clazzName">
          <el-input v-model="form.clazzName" placeholder="请输入班级名称" />
        </el-form-item>
        <el-form-item label="班主任" prop="teacherId">
          <el-select v-model="form.teacherId" placeholder="请选择班主任" style="width: 100%">
            <el-option
              v-for="teacher in teacherOptions"
              :key="teacher.id"
              :label="teacher.name"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班级描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入班级描述" rows="3" />
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
import { getAdminClazzesApi, addAdminClazzApi, updateAdminClazzApi, deleteAdminClazzApi, getAdminClazzByIdApi } from '@/api/admin'
import { findAllTeachersApi } from '@/api/teacher'

// 搜索表单
const searchForm = reactive({
  clazzCode: '',
  clazzName: ''
})

// 表格数据
const tableData = ref([])

// 教师选项
const teacherOptions = ref([])

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
  clazzCode: '',
  clazzName: '',
  teacherId: '',
  description: ''
})

// 表单验证规则
const rules = {
  clazzCode: [
    { required: true, message: '请输入班级编号', trigger: 'blur' },
    { min: 2, max: 20, message: '班级编号长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  clazzName: [
    { required: true, message: '请输入班级名称', trigger: 'blur' },
    { min: 2, max: 50, message: '班级名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  teacherId: [
    { required: true, message: '请选择班主任', trigger: 'change' }
  ]
}

// 加载教师选项
const loadTeacherOptions = async () => {
  try {
    const response = await findAllTeachersApi()
    if (response.code === 1) {
      teacherOptions.value = response.data || []
    } else {
      console.error('加载教师列表失败:', response.msg)
    }
  } catch (error) {
    console.error('加载教师列表异常:', error)
  }
}

// 加载班级数据
const loadData = async () => {
  try {
    const params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    if (searchForm.clazzCode) params.clazzCode = searchForm.clazzCode
    if (searchForm.clazzName) params.clazzName = searchForm.clazzName

    const response = await getAdminClazzesApi(params)
    if (response.code === 1) {
      tableData.value = response.data.rows
      pagination.total = response.data.total
    } else {
      ElMessage.error(response.msg || '加载班级列表失败')
    }
  } catch (error) {
    ElMessage.error('加载班级列表异常')
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadData()
}

// 处理重置
const handleReset = () => {
  searchForm.clazzCode = ''
  searchForm.clazzName = ''
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
  dialogTitle.value = '新增班级'
  form.id = ''
  form.clazzCode = ''
  form.clazzName = ''
  form.teacherId = ''
  form.description = ''
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = async (row) => {
  try {
    const response = await getAdminClazzByIdApi(row.id)
    if (response.code === 1) {
      dialogTitle.value = '编辑班级'
      Object.assign(form, response.data)
      dialogVisible.value = true
    } else {
      ElMessage.error(response.msg || '加载班级信息失败')
    }
  } catch (error) {
    ElMessage.error('加载班级信息异常')
  }
}

// 处理删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除班级 ${row.clazzName} 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        const response = await deleteAdminClazzApi([row.id])
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
        // 编辑班级
        response = await updateAdminClazzApi(form)
      } else {
        // 新增班级
        response = await addAdminClazzApi(form)
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
  loadTeacherOptions()
  loadData()
})
</script>

<style scoped>
.admin-clazz-management {
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
