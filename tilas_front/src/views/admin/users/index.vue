<template>
  <div class="admin-users-management">
    <div class="header">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增用户
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户姓名">
          <el-input v-model="searchForm.name" placeholder="请输入用户姓名" clearable />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable>
            <el-option label="管理员" :value="0" />
            <el-option label="教师" :value="1" />
            <el-option label="学生" :value="2" />
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
        <el-table-column prop="username" label="用户名" width="120" align="center" />
        <el-table-column prop="name" label="姓名" width="100" align="center" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.role)">{{ getRoleText(scope.row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="150" align="center" />
        <el-table-column prop="clazzId" label="班级" width="100" align="center">
          <template #default="scope">
            {{ getClazzName(scope.row.clazzId) }}
          </template>
        </el-table-column>
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

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :before-close="handleClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" :placeholder="isEditing ? '不修改密码请保持空白' : '请输入密码'" show-password />
          <div v-if="isEditing" class="password-hint" style="margin-top: 8px; color: #909399; font-size: 12px;">
            原密码：{{ originalPassword }}
          </div>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" :value="0" />
            <el-option label="教师" :value="1" />
            <el-option label="学生" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.role === 1 || form.role === 2" label="班级" prop="clazzId">
          <el-select v-model="form.clazzId" placeholder="请选择班级" style="width: 100%" clearable>
            <el-option v-for="clazz in clazzOptions" :key="clazz.id" :label="clazz.clazzName" :value="clazz.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
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
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAdminUsersApi, addAdminUserApi, updateAdminUserApi, deleteAdminUserApi, getAdminUserByIdApi, getAdminClazzesApi } from '@/api/admin'

// 搜索表单
const searchForm = reactive({
  name: '',
  username: '',
  role: ''
})

// 表格数据
const tableData = ref([])

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

// 班级列表
const clazzOptions = ref([])

// 是否为编辑状态
const isEditing = ref(false)

// 原始密码（我上一次的密码）
const originalPassword = ref('')

// 表单数据
const form = reactive({
  id: '',
  username: '',
  name: '',
  password: '',
  phone: '',
  role: 1,
  clazzId: null
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { 
      validator: (rule, value, callback) => {
        // 编辑时密码为空是允许的，新增时必填
        if (!isEditing.value && !value) {
          callback(new Error('请输入密码'))
        } else if (value && (value.length < 6 || value.length > 20)) {
          callback(new Error('密码长度在 6 到 20 个字符'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$|^$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 监听角色变化，当选择学生或教师时加载班级列表
watch(() => form.role, async (newRole) => {
  if (newRole === 1 || newRole === 2) {
    try {
      const response = await getAdminClazzesApi({ page: 1, pageSize: 1000 })
      if (response.code === 1) {
        clazzOptions.value = response.data.rows || []
      }
    } catch (error) {
      console.error('加载班级列表失败:', error)
    }
  }
})

// 获取班级名称
const getClazzName = (clazzId) => {
  if (!clazzId) return '-'
  const clazz = clazzOptions.value.find(c => c.id === clazzId)
  return clazz ? clazz.clazzName : '-'
}

// 获取角色文本
const getRoleText = (role) => {
  const roleMap = {
    0: '管理员',
    1: '教师',
    2: '学生'
  }
  return roleMap[role] || '未知'
}

// 获取角色标签类型
const getRoleTagType = (role) => {
  const tagTypeMap = {
    0: 'danger',
    1: 'warning',
    2: 'success'
  }
  return tagTypeMap[role] || 'info'
}

// 加载用户数据
const loadData = async () => {
  try {
    const params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.username) params.username = searchForm.username
    if (searchForm.role !== '') params.role = searchForm.role

    const response = await getAdminUsersApi(params)
    if (response.code === 1) {
      tableData.value = response.data.rows
      pagination.total = response.data.total
    } else {
      ElMessage.error(response.msg || '加载用户列表失败')
    }
  } catch (error) {
    ElMessage.error('加载用户列表异常')
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadData()
}

// 处理重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.username = ''
  searchForm.role = ''
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
  isEditing.value = false
  dialogTitle.value = '新增用户'
  form.id = ''
  form.username = ''
  form.name = ''
  form.password = ''
  form.phone = ''
  form.role = 1
  form.clazzId = null
  clazzOptions.value = []
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = async (row) => {
  isEditing.value = true
  try {
    const response = await getAdminUserByIdApi(row.id)
    if (response.code === 1) {
      dialogTitle.value = '编辑用户'
      Object.assign(form, response.data)
      originalPassword.value = response.data.password // 保存原始密码
      form.password = '' // 编辑时清空密码，允许不修改密码
      dialogVisible.value = true
    } else {
      ElMessage.error(response.msg || '加载用户信息失败')
    }
  } catch (error) {
    ElMessage.error('加载用户信息异常')
  }
}

// 处理删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除用户 ${row.name}（${row.username}） 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        const response = await deleteAdminUserApi([row.id])
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
      let submitData = { ...form }
      
      if (form.id) {
        // 编辑用户：如果密码为空，需要从数据中需除，并更改API为changePassword或管理员更新
        if (!submitData.password) {
          delete submitData.password
        }
        response = await updateAdminUserApi(submitData)
      } else {
        // 新增用户
        response = await addAdminUserApi(submitData)
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
  loadData()
  loadClazzes()
})

// 加载班级数据
const loadClazzes = async () => {
  try {
    const response = await getAdminClazzesApi({ page: 1, pageSize: 1000 })
    if (response.code === 1) {
      clazzOptions.value = response.data.rows || []
    }
  } catch (error) {
    console.error('加载班级列表失败:', error)
  }
}
</script>

<style scoped>
.admin-users-management {
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
