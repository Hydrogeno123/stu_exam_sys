<template>
  <div class="stu-management">
    <!-- 移除新增学员按钮 -->
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="学员姓名">
          <el-input v-model="searchForm.name" placeholder="请输入学员姓名" clearable />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="searchForm.username" placeholder="请输入学号" clearable />
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="searchForm.clazzId" placeholder="请选择班级" clearable>
            <el-option
              v-for="clazz in clazzOptions"
              :key="clazz.id"
              :label="clazz.clazzName"
              :value="clazz.id"
            />
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
        <el-table-column prop="username" label="学号" width="120" align="center" />
        <el-table-column prop="name" label="姓名" width="100" align="center" />
        <el-table-column prop="phone" label="手机号" width="150" align="center" />
        <el-table-column prop="clazzName" label="班级" min-width="150" />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
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

    <!-- 移除新增/编辑对话框 -->
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStuListApi } from '@/api/stu'
import { getAllClazzApi } from '@/api/clazz'

// 搜索表单
const searchForm = reactive({
  name: '',
  username: '',
  clazzId: ''
})

// 班级选项
const clazzOptions = ref([])

// 表格数据
const tableData = ref([])

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框相关 - 教师不可使用
// const dialogVisible = ref(false)
// const dialogTitle = ref('')
// const formRef = ref()

// 表单数据 - 教师不可使用
// const form = reactive({
//   id: '',
//   username: '',
//   name: '',
//   password: '',
//   phone: '',
//   email: '',
//   clazzId: '',
//   role: 2,
//   status: 'active'
// })

// 表单验证规则 - 旧版本中不再需要
// const rules = {
//   name: [
//     { required: true, message: '请输入学员姓名', trigger: 'blur' },
//     { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
//   ],
//   username: [
//     { required: true, message: '请输入学号', trigger: 'blur' },
//     { min: 3, max: 20, message: '学号长度在 3 到 20 个字符', trigger: 'blur' }
//   ],
//   password: [
//     { required: true, message: '请输入密码', trigger: 'blur' },
//     { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
//   ],
//   phone: [
//     { required: true, message: '请输入手机号', trigger: 'blur' },
//     { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
//   ],
//   clazzId: [
//     { required: true, message: '请选择班级', trigger: 'change' }
//   ]
// }

// 加载班级选项
const loadClazzOptions = async () => {
  try {
    const response = await getAllClazzApi()
    console.log('班级选项响应:', response)
    if (response.code === 1) {
      clazzOptions.value = response.data || []
      console.log('班级选项加载成功:', clazzOptions.value)
    } else {
      console.error('班级选项API返回错误:', response.msg)
      ElMessage.error(response.msg || '加载班级选项失败')
    }
  } catch (error) {
    console.error('加载班级选项失败:', error)
    console.error('错误详情:', error.message)
    console.error('错误堆栈:', error.stack)
    ElMessage.error('加载班级选项失败')
  }
}

// 加载学员数据
const loadData = async () => {
  try {
    const params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      username: searchForm.username || undefined,
      clazzId: searchForm.clazzId || undefined
    }
    
    console.log('学员查询参数:', params)
    const response = await getStuListApi(params)
    console.log('学员数据响应:', response)
    
    if (response.code === 1) {
      const pageResult = response.data
      console.log('学员分页结果:', pageResult)
      tableData.value = pageResult.rows || []
      pagination.total = pageResult.total || 0
      console.log('学员表格数据已更新:', tableData.value)
    } else {
      console.error('学员数据API返回错误:', response.msg)
      ElMessage.error(response.msg || '加载数据失败')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    console.error('错误详情:', error.message)
    console.error('错误堆栈:', error.stack)
    ElMessage.error('加载数据失败')
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.currentPage = 1
  loadData()
}

// 重置搜索
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.currentPage = 1
  loadData()
}

// 新增学员 - 教师不可使用
// const handleAdd = () => {
//   dialogTitle.value = '\u65b0\u589e\u5b66\u5458'
//   Object.keys(form).forEach(key => {
//     form[key] = key === 'role' ? 2 : key === 'status' ? 'active' : ''
//   })
//   dialogVisible.value = true
// }

// 编辑学员 - 教师不可使用
// const handleEdit = (row) => {
//   dialogTitle.value = '\u7f16\u8f91\u5b66\u5458'
//   Object.keys(form).forEach(key => {
//     form[key] = row[key]
//   })
//   dialogVisible.value = true
// }

// 删除学员 - 教师不可使用
// const handleDelete = (row) => {
//   ElMessageBox.confirm('\u786e\u5b9a\u8981\u5220\u9664\u8fd9\u4e2a\u5b66\u5458\u5417\uff1f', '\u63d0\u793a', {
//     confirmButtonText: '\u786e\u5b9a',
//     cancelButtonText: '\u53d6\u6d88',
//     type: 'warning'
//   }).then(async () => {
//     try {
//       const response = await deleteStuApi([row.id])
//       console.log('\u5220\u9664\u5b66\u5458\u54cd\u5e94:', response)
//       if (response.code === 1) {
//         ElMessage.success('\u5220\u9664\u6210\u529f')
//         loadData()
//       } else {
//         ElMessage.error(response.msg || '\u5220\u9664\u5931\u8d25')
//       }
//     } catch (error) {
//       console.error('\u5220\u9664\u5931\u8d25:', error)
//       console.error('\u9519\u8bef\u8be6\u60c5:', error.message)
//       console.error('\u9519\u8bef\u5806\u6808:', error.stack)
//       ElMessage.error('\u5220\u9664\u5931\u8d25')
//     }
//   }).catch(() => {
//     ElMessage.info('\u5df2\u53d6\u6d88\u5220\u9664')
//   })
// }


// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadData()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadData()
}

// 对话框关闭 - 旧版本中不再需要
// const handleClose = (done) => {
//   ElMessageBox.confirm('确定要关闭吗？', '提示', {
//     confirmButtonText: '确定',
//     cancelButtonText: '取消',
//     type: 'warning'
//   }).then(() => {
//     done()
//   }).catch(() => {
//     // 取消关闭，不调用done函数
//   })
// }

// 表单提交 - 旧版本中不再使用
// const handleSubmit = async () => {
//   formRef.value.validate(async (valid) => {
//     if (valid) {
//       try {
//         let response
//         if (dialogTitle.value === '新增学员') {
//           response = await saveStuApi(form)
//         } else {
//           response = await updateStuApi(form)
//         }
//         
//         console.log('新增/编辑学员响应:', response)
//         
//         if (response.code === 1) {
//           ElMessage.success(dialogTitle.value + '成功')
//           dialogVisible.value = false
//           loadData()
//         } else {
//           ElMessage.error(response.msg || '操作失败')
//         }
//       } catch (error) {
//         console.error('操作失败:', error)
//         console.error('错误详情:', error.message)
//         console.error('错误堆栈:', error.stack)
//         ElMessage.error('操作失败')
//       }
//     }
//   })
// }

onMounted(() => {
  // 页面加载时初始化数据
  loadClazzOptions()
  loadData()
})
</script>

<style scoped>
.stu-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-area {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.table-area {
  margin-bottom: 20px;
}

.pagination-area {
  display: flex;
  justify-content: flex-end;
}
</style>