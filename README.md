## 快速总结

本项目已成功添加了**管理员（Administrator）**角色和**AI 智能助手**功能，系统现支持三种身份：

| 角色   | 账号        | 密码   | 功能                 |
| ------ | ----------- | ------ | -------------------- |
| 管理员 | root        | root   | 全系统数据管理       |
| 教师   | zhanglaoshi | 123456 | 班级、考试、成绩管理 |
| 学生   | zhangsan    | 123456 | 参加考试、查看成绩   |

---

## AI 智能助手功能

本项目集成了基于大语言模型的 AI 智能助手，为不同角色提供定制化的智能服务：

### 🤖 功能特性

- **角色差异化提示词**：管理员、教师、学生登录时自动加载对应角色的预制提示词
- **快捷问答**：提供常用问题的快捷按钮
- **对话历史**：保留对话上下文
- **消息操作**：支持复制、重新生成、清空对话

### 🔧 配置说明

在 `tlias-backend/src/main/resources/application.yml` 中配置：

```yaml
ai:
  enabled: true
  api-key: sk-xxxxx # 替换为你的 API Key

langchain4j:
  open-ai:
    chat-model:
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      api-key: ${ai.api-key}
      model-name: qwen-plus # 或其他模型名称
```

### 💬 各角色 AI 助手

| 角色 | AI 助手功能 |
|------|-------------|
| **管理员** | 系统管理帮助、班级/学生/题库/考试管理指南 |
| **教师** | 考试创建指导、成绩分析、导出报表说明 |
| **学生** | 参加考试教程、成绩查询、错题复习帮助 |

### 📁 相关文件

- **后端**：`tlias-backend/src/main/java/com/itheima/ai/AiAssistant.java`
- **后端服务**：`tlias-backend/src/main/java/com/itheima/service/impl/AiAssistantServiceImpl.java`
- **前端组件**：`tilas_front/src/components/AiAssistant.vue`
- **API 接口**：`tilas_front/src/api/ai/aiAssistant.js`

---

## 项目结构

```
front_back_list/
├── database/
│   ├── database0.sql          # ✨ 已更新：添加管理员初始账户
│   └── test1.sql
│   └── stu_exam1.sql          # 数据库结构及测试数据，用于phpmyadmin导入
├── tlias-backend/             # 后端Java项目
│   └── src/main/java/com/itheima/
│       ├── ai/                # ✨ 新增：AI 助手模块
│       │   └── AiAssistant.java
│       ├── controller/
│       │   └── AiAssistantController.java
│       ├── service/
│       │   ├── AiAssistantService.java
│       │   └── impl/
│       │       └── AiAssistantServiceImpl.java
│       └── pojo/User.java     # ✨ 已更新：添加管理员角色
├── tilas_front/               # 前端Vue项目
│   └── src/
│       ├── api/
│       │   ├── admin.js       # ✨ 新增：管理员API
│       │   ├── teacher.js     # ✨ 新增：教师API
│       │   └── ai/            # ✨ 新增：AI 助手 API
│       │       └── aiAssistant.js
│       ├── components/
│       │   └── AiAssistant.vue # ✨ 新增：AI 助手组件
│       ├── router/
│       │   └── index.js       # ✨ 已更新：添加管理员路由
│       └── views/
│           ├── login/
│           │   └── index.vue  # ✨ 已更新：添加管理员登录选项
│           ├── layout/
│           │   └── index.vue  # ✨ 已更新：管理员菜单
│           └── admin/         # ✨ 新增：管理员专属页面
│               ├── dashboard/
│               │   └── index.vue
│               ├── users/
│               │   └── index.vue
│               ├── clazz/
│               │   └── index.vue
│               ├── questions/
│               │   └── index.vue
│               └── exams/
│                   └── index.vue
└── ADMIN_FEATURE_GUIDE.md     # ✨ 新增：详细功能说明
```

---

## 安装与部署

### 1. 数据库初始化

运行数据库脚本：

```bash
# 使用MySQL客户端运行以下脚本
mysql -u root -p < database/database0.sql
```

**创建的管理员账户：**

- 用户名：`root`
- 密码：`root`
- 名称：系统管理员

### 2. 后端配置

```bash
cd tlias-backend
# Maven构建
mvn clean install

# 运行项目
mvn spring-boot:run
```

### 3. 前端配置

```bash
cd tilas_front

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 生产环境打包
npm run build
```

---

## 登录与使用

### 登录步骤

1. **打开应用** → 访问 `http://localhost:5173`（Vite开发服务器）或部署地址
2. **选择身份** → 点击"管理员"单选按钮
3. **输入凭证** → 
   - 用户名：`root`
   - 密码：`root`
4. **点击登录** → 自动跳转到管理员仪表板

### 管理员仪表板

登录后进入首页，显示：

- 📊 系统统计：用户总数、教师数、学生数、班级数、考试数、试题数
- 🚀 快速操作：一键跳转到各管理页面

---

## 管理员功能模块

### 📱 系统管理

#### 用户管理

- **访问路径**：系统管理 → 用户管理
- **操作**：
  - ✅ 查看所有用户（分页显示）
  - ✅ 新增用户（指定角色：管理员/教师/学生）
  - ✅ 编辑用户信息
  - ✅ 删除用户
- **过滤**：姓名、用户名、角色

#### 班级管理

- **访问路径**：系统管理 → 班级管理
- **操作**：
  - ✅ 查看所有班级
  - ✅ 新增班级（选择班主任教师）
  - ✅ 编辑班级信息
  - ✅ 删除班级
- **过滤**：班级编号、班级名称

### 📚 考试管理

#### 试题/试卷管理

- **访问路径**：考试管理 → 试题/试卷管理
- **操作**：
  - ✅ 查看所有试题
  - ✅ 新增试题
    - **选择题**：支持多个选项，标记正确答案
    - **填空题**：输入正确答案
  - ✅ 编辑试题
  - ✅ 删除试题
- **过滤**：题库、题型

#### 考试配置

- **访问路径**：考试管理 → 考试配置
- **操作**：
  - ✅ 查看所有考试
  - ✅ 新增考试
    - 指定出题教师
    - 选择题库
    - 设置考试时间
    - 配置考试时长、总分
  - ✅ 编辑考试信息
  - ✅ 删除考试
- **过滤**：考试名称、考试状态（未开始/进行中/已结束）

---

## 核心特性

### 🔐 权限隔离

- ✅ 管理员独占的菜单和页面
- ✅ 基于角色的路由保护
- ✅ 未授权访问会被重定向到首页

### 📊 全面管理

- ✅ 用户全生命周期管理
- ✅ 班级完整配置
- ✅ 试题和试卷管理
- ✅ 考试全流程管理

### 🎨 用户友好

- ✅ 简洁直观的界面
- ✅ 一键快速操作
- ✅ 完整的数据验证
- ✅ 友好的错误提示

### 🔄 三角色支持

```
管理员 (角色=0)
  ├── 用户管理
  ├── 班级管理
  ├── 试题管理
  └── 考试管理

教师 (角色=1)
  ├── 班级管理
  ├── 考试管理
  ├── 
  └── 成绩统计

学生 (角色=2)
  ├── 在线考试
  ├── 我的成绩
  └── 班级成绩
```

---

## API接口概览

### 管理员API模块 (`api/admin.js`)

**用户相关**

- `GET /users` - 分页查询用户
- `POST /users` - 新增用户
- `PUT /users` - 更新用户
- `DELETE /users/{ids}` - 删除用户

**班级相关**

- `GET /clazz` - 分页查询班级
- `POST /clazz` - 新增班级
- `PUT /clazz` - 更新班级
- `DELETE /clazz/{ids}` - 删除班级

**试题相关**

- `GET /questions` - 分页查询试题
- `POST /questions` - 新增试题
- `PUT /questions` - 更新试题
- `DELETE /questions/{ids}` - 删除试题

**考试相关**

- `GET /exams` - 分页查询考试
- `POST /exams` - 新增考试
- `PUT /exams` - 更新考试
- `DELETE /exams/{ids}` - 删除考试

---

## 文件修改清单

### ✨ 新增文件（12个）

**后端 AI 模块**（5个）
- `tlias-backend/src/main/java/com/itheima/ai/AiAssistant.java`
- `tlias-backend/src/main/java/com/itheima/controller/AiAssistantController.java`
- `tlias-backend/src/main/java/com/itheima/service/AiAssistantService.java`
- `tlias-backend/src/main/java/com/itheima/service/impl/AiAssistantServiceImpl.java`

**前端 AI 模块**（2个）
- `tilas_front/src/api/ai/aiAssistant.js`
- `tilas_front/src/components/AiAssistant.vue`

**管理员模块**（5个）
- `tilas_front/src/api/admin.js`
- `tilas_front/src/api/teacher.js`
- `tilas_front/src/views/admin/dashboard/index.vue`
- `tilas_front/src/views/admin/users/index.vue`
- `tilas_front/src/views/admin/clazz/index.vue`
- `tilas_front/src/views/admin/questions/index.vue`
- `tilas_front/src/views/admin/exams/index.vue`

### 🔄 修改文件（7个）

- `database/database0.sql` - 添加管理员初始账户
- `tlias-backend/src/main/java/com/itheima/pojo/User.java` - 更新角色注释
- `tlias-backend/src/main/resources/application.yml` - 添加 AI 配置
- `tilas_front/src/views/login/index.vue` - 添加管理员登录选项
- `tilas_front/src/router/index.js` - 添加管理员路由
- `tilas_front/src/views/layout/index.vue` - 添加管理员菜单和 AI 助手
- `pom.xml` - 添加 langchain4j 依赖

---

## 测试场景

### 场景1：管理员登录

1. 选择"管理员"身份
2. 输入 root/root
3. ✅ 成功进入管理员仪表板

### 场景2：创建用户

1. 进入 系统管理 → 用户管理
2. 点击"新增用户"
3. 填写用户信息，选择角色
4. ✅ 成功创建用户

### 场景3：创建考试

1. 进入 考试管理 → 考试配置
2. 点击"新增考试"
3. 选择教师、题库，设置时间
4. ✅ 成功创建考试

### 场景4：权限验证

1. 用学生身份登录
2. 尝试访问管理员页面 (如 /admin/users)
3. ✅ 被重定向到首页

### 场景5：AI 助手功能测试

1. 以任意角色登录
2. 点击右下角的 "AI助手" 按钮
3. ✅ 显示对应角色的欢迎语和快捷按钮
4. 输入问题或点击快捷按钮
5. ✅ AI 返回相关回答

---

## 常见问题

**Q: 管理员如何修改密码？**
A: 在任何页面的右上角，点击"修改密码"即可修改当前用户的密码。

**Q: 如何添加更多管理员账户？**
A: 通过"用户管理"新增用户，选择角色为"管理员"即可。

**Q: 删除教师后，其班级怎么办？**
A: 建议先更换班主任后再删除教师，或手动更新班级的班主任。

**Q: 试题支持哪些格式？**
A: 支持选择题（4个选项）和填空题（文本答案）。

**Q: 如何批量导入用户？**
A: 当前版本需单个创建，可后期扩展为CSV批量导入功能。

**Q: 如何配置 AI 助手？**
A: 在 `tlias-backend/src/main/resources/application.yml` 中配置 `ai.api-key`，使用阿里云百炼或其他兼容 OpenAI 格式的 API 服务。

**Q: AI 助手不工作怎么办？**
A: 检查：1) application.yml 中的 AI 配置是否正确；2) API Key 是否有效；3) 网络是否能访问 API 服务。

**Q: 不同角色的 AI 助手有什么区别？**
A: 每个角色都有专属的预制提示词，管理员侧重系统管理、教师侧重教学、学生侧重学习指导。

---

## 后续开发建议

1. **数据导入导出**
   - 支持Excel格式批量导入用户
   - 支持数据导出为CSV文件

2. **高级统计**
   - 班级成绩排行
   - 考试参与率统计
   - 学生进度跟踪

3. **系统设置**
   - 考试规则配置
   - 系统参数设置
   - 操作日志记录

4. **权限细化**
   - 管理员权限等级划分
   - 审计日志
   - 操作历史追踪

5. **AI 助手增强**
   - 接入考试/成绩数据分析
   - 支持语音输入
   - 支持多轮对话记忆
   - 支持知识库检索

---

## 支持与反馈

如有任何问题或建议，请联系开发团队。

**项目版本：** v1.1
**支持角色：** 管理员、教师、学生
**新增功能：** AI 智能助手
**最后更新：** 2026年4月
