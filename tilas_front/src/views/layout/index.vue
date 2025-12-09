<script setup>
import {
  Avatar,
  Collection,
  Document,
  EditPen,
  HelpFilled,
  Histogram,
  InfoFilled,
  Menu,
  Notebook,
  QuestionFilled,
  Share,
  Tools,
  Trophy,
  User,
  SwitchButton,
  House,
  HomeFilled,
  UserFilled,
  Setting,
  Management,
} from "@element-plus/icons-vue";
//定义钩子函数 获取用户名
import { ref ,onMounted, computed} from "vue";
import { ElMessage, ElMessageBox} from "element-plus";
import { useRouter } from "vue-router";
import { changePasswordApi } from '@/api/login';


const loginName = ref("");
const userRole = ref(1);
const userId = ref(null);
const passwordDialogVisible = ref(false);
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});
const passwordFormRef = ref(null);

onMounted(() => {
  // 获取登录用户信息
  let loginUser = JSON.parse(localStorage.getItem('token'))
  if (loginUser) {
    loginName.value = loginUser.name
    userRole.value = loginUser.role
    userId.value = loginUser.id
  }
});

// 根据用户角色计算菜单项
const menuItems = computed(() => {
  const role = userRole.value;
  const baseMenu = [
    // 首页菜单
    { path: '/index', icon: House, label: '首页' }
  ];

  if (role === 0) {
    // 管理员菜单
    return [
      // 管理员仪表板
      { path: '/admin/dashboard', icon: House, label: '管理员仪表板' },
      // 系统管理
      { 
        type: 'submenu', 
        path: '/admin-management', 
        icon: Tools, 
        label: '系统管理',
        children: [
          { path: '/admin/users', icon: UserFilled, label: '用户管理' },
          { path: '/admin/clazz', icon: HomeFilled, label: '班级管理' }
        ]
      },
      // 考试系统管理
      { 
        type: 'submenu', 
        path: '/admin-exam-management', 
        icon: Document, 
        label: '考试管理',
        children: [
          { path: '/admin/questions', icon: QuestionFilled, label: '试题/试卷管理' },
          { path: '/admin/exams', icon: EditPen, label: '考试配置' }
        ]
      }
    ];
  } else if (role === 1) {
    // 教师菜单
    return [
      ...baseMenu,
      // 班级管理菜单
      { 
        type: 'submenu', 
        path: '/clazz-management', // 为子菜单添加唯一路径
        icon: Menu, 
        label: '班级学员管理',
        children: [
          { path: '/clazz', icon: HomeFilled, label: '班级管理' },
          { path: '/stu', icon: UserFilled, label: '学员管理' }
        ]
      },
      // 考试系统管理
      { 
        type: 'submenu', 
        path: '/exam-management', // 为子菜单添加唯一路径
        icon: Document, 
        label: '考试系统管理',
        children: [
          { path: '/questionBank', icon: Collection, label: '题库管理' },
          { path: '/question', icon: QuestionFilled, label: '题目管理' },
          { path: '/exam', icon: EditPen, label: '考试管理' },
          { path: '/examScore', icon: Trophy, label: '成绩管理' }
        ]
      },
      // 数据统计管理
      { 
        type: 'submenu', 
        path: '/statistics-management', // 为子菜单添加唯一路径
        icon: Histogram, 
        label: '数据统计管理',
        children: [
          { path: '/examReport', icon: InfoFilled, label: '考试统计' },
          { path: '/scoreReport', icon: Share, label: '成绩统计' }
        ]
      }
    ];
  } else {
    // 学生菜单
    return [
      ...baseMenu,
      // 考试相关
      { path: '/studentExam', icon: User, label: '在线考试' },
      { path: '/studentScore', icon: Trophy, label: '我的成绩' },
      { path: '/stuexamReport', icon: Histogram, label: '班级成绩查看' }
    ];
  }
});

//修改密码
const openPasswordDialog = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordDialogVisible.value = true
}

const closePasswordDialog = () => {
  passwordDialogVisible.value = false
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
}

const submitPasswordChange = async () => {
  // 验证表单
  if (!passwordForm.value.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!passwordForm.value.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (!passwordForm.value.confirmPassword) {
    ElMessage.warning('请确认新密码')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  if (passwordForm.value.newPassword === passwordForm.value.oldPassword) {
    ElMessage.warning('新密码不能与原密码相同')
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }

  try {
    const response = await changePasswordApi({
      id: userId.value,
      password: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    if (response.code === 1) {
      ElMessage.success('密码修改成功')
      closePasswordDialog()
    } else {
      ElMessage.error(response.msg || '密码修改失败')
    }
  } catch (error) {
    ElMessage.error('修改密码异常')
  }
}

//退出登录
const router = useRouter();
const logout =() =>{
  ElMessageBox.confirm("您确定要退出登录吗?", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      //提示成功信息
      ElMessage.success("退出登录成功");
      //清除token
      localStorage.removeItem("token");  
      localStorage.removeItem("userRole"); 
      //跳转到登录页面
      router.push("/login");
   
    })
    .catch(() => {
      //点击取消按钮
      //点击取消按钮不做任何操作
      ElMessage.info("已取消退出登录");
    });

}
</script>

<template>
  <div class="education-layout">
    <el-container>
      <!-- Header 区域 -->
      <el-header class="education-header">
        <span class="title">学生考试系统</span>
        <span class="right_tool">
          <a @click="openPasswordDialog" href="javascript:void(0)">
            <el-icon><EditPen /></el-icon> 修改密码 &nbsp;&nbsp;&nbsp; |
            &nbsp;&nbsp;&nbsp;
          </a>
          <a @click="logout" href="javascript:void(0)">
            <el-icon><SwitchButton /></el-icon> 退出登录【{{loginName}}】
          </a>
        </span>

        <!-- 修改密码对话框 -->
        <el-dialog
          v-model="passwordDialogVisible"
          title="修改密码"
          width="400px"
          @close="closePasswordDialog"
        >
          <el-form ref="passwordFormRef" :model="passwordForm" label-width="80px">
            <el-form-item label="原密码">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="closePasswordDialog">取消</el-button>
              <el-button type="primary" @click="submitPasswordChange">确定</el-button>
            </span>
          </template>
        </el-dialog>
      </el-header>

      <el-container>
        <!-- 左侧菜单 -->
        <el-aside width="200px" class="education-sidebar">
          <el-menu 
            router 
            class="education-menu"
            :unique-opened="true">
            <!-- 动态渲染菜单项 -->
            <template v-for="item in menuItems" :key="item.path">
              <!-- 普通菜单项 -->
              <el-menu-item 
                v-if="!item.type || item.type === 'item'" 
                :index="item.path">
                <el-icon><component :is="item.icon" /></el-icon> 
                {{ item.label }}
              </el-menu-item>
              
              <!-- 子菜单项 -->
              <el-sub-menu 
                v-else-if="item.type === 'submenu'" 
                :index="item.path">
                <template #title>
                  <el-icon><component :is="item.icon" /></el-icon> 
                  {{ item.label }}
                </template>
                <el-menu-item 
                  v-for="child in item.children" 
                  :key="child.path" 
                  :index="child.path">
                  <el-icon><component :is="child.icon" /></el-icon> 
                  {{ child.label }}
                </el-menu-item>
              </el-sub-menu>
            </template>
          </el-menu>
        </el-aside>

        <el-main> 
         <router-view></router-view>  
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
@import url('@/assets/education-theme.css');

.title {
  color: white;
  font-size: 32px;
  font-family: 'Microsoft YaHei', sans-serif;
  line-height: 60px;
  font-weight: bold;
}

.right_tool {
  float: right;
  line-height: 60px;
}

a {
  color: white;
  text-decoration: none;
  transition: opacity 0.3s ease;
}

a:hover {
  opacity: 0.8;
}
</style>