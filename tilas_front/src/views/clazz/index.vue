<template>
  <div class="clazz-management">
    <!-- 移除新增班级按钮 -->
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="班级名称">
          <el-input v-model="searchForm.clazzName" placeholder="请输入班级名称" clearable />
        </el-form-item>
        <el-form-item label="班级编号">
          <el-input v-model="searchForm.clazzCode" placeholder="请输入班级编号" clearable />
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
        <el-table-column prop="clazzName" label="班级名称" min-width="150" />
        <el-table-column prop="teacherName" label="班主任" width="120" align="center" />
        <el-table-column prop="studentCount" label="学生人数" width="100" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleViewStudents(scope.row)">查看学员</el-button>
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

    <!-- 移除新增/编辑对话框 -->

    <!-- 查看学员对话框 -->
    <el-dialog v-model="studentDialogVisible" title="班级学员" width="700px">
      <div class="student-list-container">
        <el-table :data="studentList" border stripe v-loading="studentListLoading">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="username" label="学号" width="120" align="center" />
          <el-table-column prop="name" label="姓名" width="120" align="center" />
          <el-table-column prop="phone" label="手机号" min-width="150" />
          <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getClazzListApi } from '@/api/clazz'
import { getStuByClazzIdApi } from '@/api/stu'

// 搜索表单
const searchForm = reactive({
  clazzName: '',
  clazzCode: ''
})

// 表格数据
const tableData = ref([])

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框相关 - 旧版本中应会被移除
// const dialogVisible = ref(false)
// const dialogTitle = ref('')
// const formRef = ref()

// 表单数据 - 旧版本中应会被移除
// const form = reactive({
//   id: '',
//   clazzCode: '',
//   clazzName: '',
//   teacherId: '',
//   teacherName: '',
//   description: '',
//   status: 1
// })

// 教师选项
const teacherOptions = ref([])

// 学员对话框相关
const studentDialogVisible = ref(false)
const studentList = ref([])
const studentListLoading = ref(false)

// 表单验证规则 - 旧版本中不再需要
// const rules = {
//   clazzCode: [
//     { required: true, message: '请输入班级编号', trigger: 'blur' }
//   ],
//   clazzName: [
//     { required: true, message: '请输入班级名称', trigger: 'blur' }
//   ],
//   teacherId: [
//     { required: true, message: '请选择班主任', trigger: 'change' }
//   ]
// }

// 加载教师选项
const loadTeacherOptions = async () => {
  // 旧版本中此函数不再使用
}

// 加载数据
const loadData = async () => {
  try {
    // 清理搜索参数，去除首尾空格
    const cleanClazzName = searchForm.clazzName ? searchForm.clazzName.trim() : ''
    const cleanClazzCode = searchForm.clazzCode ? searchForm.clazzCode.trim() : ''
    
    const params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
      clazzName: cleanClazzName || undefined,
      clazzCode: cleanClazzCode || undefined
    }
    
    console.log('发送请求参数:', params)
    console.log('清理后的班级名称:', cleanClazzName)
    console.log('清理后的班级编号:', cleanClazzCode)
    console.log('请求URL:', '/api/clazz')
    
    const response = await getClazzListApi(params)
    console.log('收到响应数据:', response)
    console.log('响应code:', response.code)
    console.log('响应data:', response.data)
    
    if (response.code === 1) {
      const pageResult = response.data
      console.log('分页结果:', pageResult)
      console.log('分页列表数据:', pageResult.rows)
      console.log('分页总数:', pageResult.total)
      
      tableData.value = pageResult.rows || []
      pagination.total = pageResult.total || 0
      
      console.log('表格数据已更新:', tableData.value)
      console.log('表格数据长度:', tableData.value.length)
    } else {
      console.error('API返回错误:', response.msg)
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

// 新增班级 - 教师不可使用
// const handleAdd = () => {
//   dialogTitle.value = '\u65b0\u589e\u73ed\u7ea7'
//   Object.keys(form).forEach(key => {
//     form[key] = key === 'status' ? 1 : ''
//   })
//   dialogVisible.value = true
// }

// 编辑班级 - 教师不可使用
// const handleEdit = (row) => {
//   dialogTitle.value = '\u7f16\u8f91\u73ed\u7ea7'
//   Object.keys(form).forEach(key => {
//     form[key] = row[key]
//   })
//   dialogVisible.value = true
// }

// 删除班级 - 教师不可使用
// const handleDelete = (row) => {
//   ElMessageBox.confirm('\u786e\u5b9a\u8981\u5220\u9664\u8fd9\u4e2a\u73ed\u7ea7\u5417\uff1f', '\u63d0\u793a', {
//     confirmButtonText: '\u786e\u5b9a',
//     cancelButtonText: '\u53d6\u6d88',
//     type: 'warning'
//   }).then(async () => {
//     try {
//       const response = await deleteClazzApi([row.id])
//       if (response.code === 1) {
//         ElMessage.success('\u5220\u9664\u6210\u529f')
//         loadData()
//       } else {
//         ElMessage.error(response.msg || '\u5220\u9664\u5931\u8d25')
//       }
//     } catch (error) {
//       console.error('\u5220\u9664\u5931\u8d25:', error)
//       ElMessage.error('\u5220\u9664\u5931\u8d25')
//     }
//   }).catch(() => {
//     ElMessage.info('\u5df2\u53d6\u6d88\u5220\u9664')
//   })
// }

// 查看学员
const handleViewStudents = async (row) => {
  try {
    studentListLoading.value = true
    const response = await getStuByClazzIdApi(row.id)
    if (response.code === 1) {
      studentList.value = response.data || []
      studentDialogVisible.value = true
    } else {
      ElMessage.error(response.msg || '加载学员列表失败')
    }
  } catch (error) {
    console.error('加载学员列表失败:', error)
    ElMessage.error('加载学员列表失败')
  } finally {
    studentListLoading.value = false
  }
}

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
//   ElMessageBox.confirm('\u786e\u5b9a\u8981\u5173\u95ed\u5417\uff1f', '\u63d0\u793a', {
//     confirmButtonText: '\u786e\u5b9a',
//     cancelButtonText: '\u53d6\u6d88',
//     type: 'warning'
//   }).then(() => {
//     done()
//   }).catch(() => {
//     // \u53d6\u6d88\u5173\u95ed
//   })
// }

// 表单提\u4ea4 - 旧版本中不\u518d使\u7528
// const handleSubmit = async () => {
//   formRef.value.validate(async (valid) => {
//     if (valid) {
//       try {
//         // \u6839\u636e\u9009\u62e9\u7684\u6559\u5e08ID\u83b7\u53d6\u6559\u5e08\u59d3\u540d
//         const selectedTeacher = teacherOptions.value.find(teacher => teacher.id === form.teacherId)
//         const submitData = {
//           ...form,
//           teacherName: selectedTeacher ? selectedTeacher.name : ''
//         }
//         
//         console.log('\u63d0\u4ea4\u6570\u636e:', submitData)
//         
//         let response
//         if (dialogTitle.value === '\u65b0\u589e\u73ed\u7ea7') {
//           response = await saveClazzApi(submitData)
//         } else {
//           response = await updateClazzApi(submitData)
//         }
//         
//         console.log('API\u54cd\u5e94:', response)
//         
//         if (response.code === 1) {
//           ElMessage.success(dialogTitle.value + '\u6210\u529f')
//           dialogVisible.value = false
//           loadData()
//         } else {
//           ElMessage.error(response.msg || '\u64cd\u4f5c\u5931\u8d25')
//         }
//       } catch (error) {
//         console.error('\u64cd\u4f5c\u5931\u8d25:', error)
//         console.error('\u9519\u8bef\u8be6\u60c5:', error.message)
//         console.error('\u9519\u8bef\u5806\u6808:', error.stack)
//         ElMessage.error('\u64cd\u4f5c\u5931\u8d25')
//       }
//     }
//   })
// }

onMounted(() => {
  // 页面加载时初始化数据
  loadTeacherOptions()
  loadData()
})
</script>

<style scoped>
.clazz-management {
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

.student-list-container {
  padding: 10px 0;
}
</style>