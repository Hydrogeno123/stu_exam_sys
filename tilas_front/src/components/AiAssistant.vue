<template>
  <div class="ai-assistant">
    <div class="ai-float-btn" @click="toggleChat" v-if="!isOpen">
      <el-icon :size="28"><ChatDotRound /></el-icon>
      <span class="ai-label">AI助手</span>
      <div class="ai-badge" v-if="unreadCount > 0">{{ unreadCount }}</div>
    </div>

    <transition name="slide-up">
      <div class="ai-chat-window" v-if="isOpen">
        <div class="ai-header">
          <div class="ai-title">
            <el-icon><ChatDotRound /></el-icon>
            <span>AI智能助手</span>
            <el-tag size="small" :type="aiStatus === 'online' ? 'success' : 'danger'" class="status-tag">
              {{ aiStatus === 'online' ? '在线' : '离线' }}
            </el-tag>
          </div>
          <div class="header-actions">
            <el-tooltip content="清空对话" placement="bottom">
              <el-icon class="action-btn" @click="clearConversation"><Delete /></el-icon>
            </el-tooltip>
            <el-icon class="close-btn" @click="toggleChat"><Close /></el-icon>
          </div>
        </div>

        <div class="ai-messages" ref="messagesContainer">
          <div 
            v-for="(msg, index) in messages" 
            :key="index" 
            :class="['message', msg.role]"
          >
            <div class="message-avatar">
              <el-avatar v-if="msg.role === 'user'" :size="32" icon="User" />
              <el-avatar v-else :size="32" class="ai-avatar">
                <el-icon><ChatDotRound /></el-icon>
              </el-avatar>
            </div>
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(msg.content)"></div>
              <div class="message-actions" v-if="msg.role === 'assistant' && !msg.loading">
                <el-button text size="small" @click="copyMessage(msg.content)">
                  <el-icon><CopyDocument /></el-icon> 复制
                </el-button>
                <el-button text size="small" @click="regenerateMessage(index)">
                  <el-icon><RefreshRight /></el-icon> 重新生成
                </el-button>
              </div>
            </div>
          </div>
          <div v-if="loading && streamingContent" class="message assistant">
            <div class="message-avatar">
              <el-avatar :size="32" class="ai-avatar">
                <el-icon><ChatDotRound /></el-icon>
              </el-avatar>
            </div>
            <div class="message-content">
              <div class="message-text streaming" v-html="formatMessage(streamingContent)"></div>
              <span class="cursor-blink">|</span>
            </div>
          </div>
          <div v-else-if="loading" class="message assistant">
            <div class="message-avatar">
              <el-avatar :size="32" class="ai-avatar">
                <el-icon><ChatDotRound /></el-icon>
              </el-avatar>
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <div class="ai-quick-actions" v-if="messages.length <= 1">
          <el-button v-for="btn in quickActions" :key="btn.text" size="small" type="primary" plain @click="askQuickQuestion(btn.text)">
            {{ btn.icon }} {{ btn.text }}
          </el-button>
        </div>

        <div class="ai-input-area">
          <el-input
            v-model="inputMessage"
            :placeholder="inputPlaceholder"
            @keyup.enter="sendMessage"
            :disabled="loading"
            :autosize="{ minRows: 1, maxRows: 4 }"
            type="textarea"
          >
          </el-input>
          <div class="input-actions">
            <el-button 
              @click="sendMessage" 
              :loading="loading" 
              :disabled="!inputMessage.trim() || loading"
              type="primary"
              circle
              size="small"
            >
              <el-icon><Promotion /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed } from 'vue'
import { ChatDotRound, Close, User, Delete, CopyDocument, RefreshRight, Promotion } from '@element-plus/icons-vue'
import { chatApi, getAiConfigApi } from '@/api/ai/aiAssistant'
import { ElMessage } from 'element-plus'

const isOpen = ref(false)
const inputMessage = ref('')
const loading = ref(false)
const streamingContent = ref('')
const unreadCount = ref(0)
const aiStatus = ref('unknown')
const userInfo = ref(null)

const aiConfig = ref({
  enabled: false,
  hasApiKey: false,
  baseUrl: '',
  modelName: ''
})

const currentRole = ref(2)

const roleConfig = {
  0: {
    name: '管理员',
    welcomeMsg: '您好！我是 **AI智能助手**，专为管理员服务。\n\n📋 **系统说明：**\n- 左侧导航栏包含：班级管理、学生管理、题库管理、题目管理、考试管理、成绩管理\n- 首页展示系统数据统计（班级数、学生数、考试数等）\n\n🔧 **我可以帮助您：**\n- 🏫 班级和学生管理：创建班级、添加学生、批量导入\n- 📚 题库和题目管理：创建题库、添加题目、批量导入题目\n- 📊 系统数据概览：查看统计报表\n\n请问有什么可以帮助您的？',
    placeholder: '请输入管理相关问题，如"如何创建班级？"',
    quickActions: [
      { icon: '🏫', text: '如何创建班级？' },
      { icon: '👥', text: '如何添加学生？' },
      { icon: '📚', text: '如何创建题库？' },
      { icon: '❓', text: '如何添加题目？' }
    ]
  },
  1: {
    name: '教师',
    welcomeMsg: '您好！我是 **AI智能助手**，专为教师服务。\n\n📋 **系统说明：**\n- 左侧导航栏包含：考试管理、成绩管理、题库管理、学生管理\n- 可以创建考试、发布考试、查看成绩、导出成绩报表\n\n🔧 **我可以帮助您：**\n- 📝 创建和管理考试：配置考试时间、选择题目、发布考试\n- 📊 查看和分析成绩：查看班级排名、平均分、及格率\n- 📋 导出成绩报表：一键导出Excel格式的成绩单\n\n请问有什么可以帮助您的？',
    placeholder: '请输入教学相关问题，如"如何创建考试？"',
    quickActions: [
      { icon: '📝', text: '如何创建考试？' },
      { icon: '📊', text: '如何查看成绩？' },
      { icon: '📋', text: '如何导出成绩？' },
      { icon: '💡', text: '教学使用帮助' }
    ]
  },
  2: {
    name: '学生',
    welcomeMsg: '您好！我是 **AI智能助手**，专为学生服务。\n\n📋 **系统说明：**\n- 首页展示：考试列表（可参加的考试）\n- 我的成绩：查看所有已完成考试的成绩和错题\n\n🔧 **我可以帮助您：**\n- 🎯 参加在线考试：在考试列表选择考试，开始作答并提交\n- 📈 查看我的成绩：查看得分、班级排名、正确答案\n- 📝 错题复习和解析：查看错题、学习解题思路\n\n加油学习！请问有什么可以帮助您的？',
    placeholder: '请输入学习相关问题，如"如何参加考试？"',
    quickActions: [
      { icon: '🎯', text: '如何参加考试？' },
      { icon: '📈', text: '如何查看成绩？' },
      { icon: '📝', text: '如何查看错题？' },
      { icon: '💡', text: '系统使用帮助' }
    ]
  }
}

const currentRoleConfig = computed(() => roleConfig[currentRole.value] || roleConfig[2])
const inputPlaceholder = computed(() => currentRoleConfig.value.placeholder)
const quickActions = computed(() => currentRoleConfig.value.quickActions)

const messages = ref([])

onMounted(() => {
  const user = JSON.parse(localStorage.getItem('token'))
  const userRoleStr = localStorage.getItem('userRole')
  console.log('从 localStorage 获取的用户信息:', user)
  console.log('从 localStorage 获取的用户选择角色:', userRoleStr)
  userInfo.value = user
  if (userRoleStr !== null) {
    currentRole.value = parseInt(userRoleStr, 10)
  } else if (user?.role !== undefined) {
    currentRole.value = user.role
  }
  initWelcomeMessage()
  checkAiStatus()
})

const initWelcomeMessage = () => {
  messages.value = [
    {
      role: 'assistant',
      content: currentRoleConfig.value.welcomeMsg
    }
  ]
}

const checkAiStatus = async () => {
  try {
    const result = await getAiConfigApi()
    if (result.code === 1) {
      aiConfig.value = result.data
      aiStatus.value = result.data.enabled ? 'online' : 'offline'
    }
  } catch (e) {
    aiStatus.value = 'offline'
  }
}

const toggleChat = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    unreadCount.value = 0
    nextTick(() => scrollToBottom())
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatMessage = (content) => {
  if (!content) return ''
  let html = escapeHtml(content)
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/\*(.*?)\*/g, '<em>$1</em>')
  html = html.replace(/`(.*?)`/g, '<code>$1</code>')
  html = html.replace(/^### (.*$)/gm, '<h4>$1</h4>')
  html = html.replace(/^## (.*$)/gm, '<h3>$1</h3>')
  html = html.replace(/^# (.*$)/gm, '<h2>$1</h2>')
  html = html.replace(/^\s*[-*] (.*$)/gm, '<li>$1</li>')
  html = html.replace(/\n/g, '<br>')
  return html
}

const escapeHtml = (text) => {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return

  const userMessage = inputMessage.value.trim()
  messages.value.push({
    role: 'user',
    content: userMessage
  })
  
  inputMessage.value = ''
  scrollToBottom()

  loading.value = true
  streamingContent.value = ''

  try {
    const result = await chatApi({
      message: userMessage,
      userId: userInfo.value?.id,
      userRole: currentRole.value
    })

    if (result.code === 1) {
      messages.value.push({
        role: 'assistant',
        content: result.data
      })
    } else {
      messages.value.push({
        role: 'assistant',
        content: '抱歉，我暂时无法回答这个问题，请稍后再试。'
      })
    }
  } catch (error) {
    ElMessage.error('AI服务暂时不可用')
    messages.value.push({
      role: 'assistant',
      content: '抱歉，服务暂时不可用，请稍后再试。'
    })
  } finally {
    loading.value = false
    streamingContent.value = ''
    scrollToBottom()
    
    if (!isOpen.value) {
      unreadCount.value++
    }
  }
}

const askQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

const clearConversation = () => {
  messages.value = [
    {
      role: 'assistant',
      content: '对话已清空。有什么我可以帮助您的？'
    }
  ]
}

const copyMessage = (content) => {
  navigator.clipboard.writeText(content).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const regenerateMessage = async (index) => {
  if (index < 1 || loading.value) return
  
  const lastUserMsg = messages.value[index - 1]
  if (!lastUserMsg || lastUserMsg.role !== 'user') return
  
  messages.value.splice(index)
  inputMessage.value = lastUserMsg.content
  sendMessage()
}
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 9999;
}

.ai-float-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  color: white;
  position: relative;
}

.ai-float-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.ai-label {
  font-size: 10px;
  margin-top: 2px;
}

.ai-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #ff4757;
  color: white;
  font-size: 10px;
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.ai-chat-window {
  width: 420px;
  height: 600px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ai-header {
  height: 54px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  color: white;
  flex-shrink: 0;
}

.ai-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
}

.status-tag {
  margin-left: 4px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  cursor: pointer;
  font-size: 16px;
  opacity: 0.85;
  transition: opacity 0.2s;
}

.action-btn:hover {
  opacity: 1;
}

.close-btn {
  cursor: pointer;
  font-size: 18px;
  opacity: 0.85;
  transition: opacity 0.2s;
}

.close-btn:hover {
  opacity: 1;
}

.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f8f9fa;
  scroll-behavior: smooth;
}

.message {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.ai-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.message-content {
  max-width: 78%;
  position: relative;
}

.message-text {
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}

.message-text :deep(strong) {
  color: #667eea;
  font-weight: 600;
}

.message-text :deep(code) {
  background: #f0f0f0;
  padding: 1px 5px;
  border-radius: 4px;
  font-size: 13px;
  color: #e74c3c;
}

.message-text :deep(h2), .message-text :deep(h3), .message-text :deep(h4) {
  margin: 8px 0 4px 0;
  color: #333;
}

.message.text :deep(li) {
  list-style-position: inside;
}

.message.user .message-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message.user .message-text :deep(strong),
.message.user .message-text :deep(code) {
  color: #fff;
  background: rgba(255,255,255,0.2);
}

.message.assistant .message-text {
  background: white;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.streaming {
  border: 1px solid #e0e0e0;
}

.cursor-blink {
  color: #667eea;
  font-weight: bold;
  animation: blink 0.8s infinite;
  margin-left: 2px;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.message-actions {
  display: flex;
  gap: 4px;
  margin-top: 6px;
  padding: 0 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.message-content:hover .message-actions {
  opacity: 1;
}

.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 14px 18px;
  background: white;
  border-radius: 14px;
  width: fit-content;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #667eea;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

.ai-quick-actions {
  padding: 10px 16px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  background: white;
  border-top: 1px solid #eee;
  flex-shrink: 0;
}

.ai-quick-actions .el-button {
  font-size: 12px;
}

.ai-input-area {
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #eee;
  display: flex;
  gap: 8px;
  align-items: flex-end;
  flex-shrink: 0;
}

.ai-input-area .el-textarea {
  --el-input-border-radius: 20px;
  flex: 1;
}

.input-actions {
  display: flex;
  align-items: center;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}
</style>
