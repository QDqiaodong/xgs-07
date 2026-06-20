<template>
  <div class="notes-page">
    <h2 class="page-title">我的练习笔记</h2>

    <div class="calendar-section">
      <div class="section-header-row">
        <h3 class="section-title">
          <el-icon class="title-icon calendar-icon"><Calendar /></el-icon>
          练习日历
        </h3>
        <span class="section-subtitle">坚持训练，见证成长轨迹</span>
      </div>
      <PracticeCalendar ref="calendarRef" :userId="userId" @date-select="handleDateSelect" />
    </div>

    <div class="review-section" v-if="emotionTrendData.length > 0">
      <div class="section-header-row">
        <h3 class="section-title">
          <el-icon class="title-icon"><TrendCharts /></el-icon>
          情绪控制复盘
        </h3>
        <span class="section-subtitle">表达稳定性变化</span>
      </div>

      <div class="stats-cards">
        <div class="stat-card stat-average">
          <div class="stat-icon">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">平均评分</span>
            <span class="stat-value">{{ averageScore }}</span>
            <span class="stat-unit">分</span>
          </div>
        </div>
        <div class="stat-card stat-count">
          <div class="stat-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">已评分文稿</span>
            <span class="stat-value">{{ emotionTrendData.length }}</span>
            <span class="stat-unit">篇</span>
          </div>
        </div>
        <div class="stat-card stat-highest">
          <div class="stat-icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">最高评分</span>
            <span class="stat-value">{{ highestScore }}</span>
            <span class="stat-unit">分</span>
          </div>
        </div>
        <div class="stat-card stat-stability">
          <div class="stat-icon">
            <el-icon><MagicStick /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">稳定性</span>
            <span class="stat-value">{{ stabilityLevel }}</span>
          </div>
        </div>
      </div>

      <div class="trend-chart-card">
        <div class="chart-header">
          <span class="chart-title">各文稿情绪评分趋势</span>
          <span class="chart-hint">按更新时间排序</span>
        </div>
        <div class="chart-container">
          <div class="chart-y-axis">
            <span>10</span>
            <span>8</span>
            <span>6</span>
            <span>4</span>
            <span>2</span>
            <span>0</span>
          </div>
          <div class="chart-content">
            <div class="chart-grid">
              <div class="grid-line" v-for="i in 5" :key="i"></div>
            </div>
            <div class="chart-bars">
              <div
                class="bar-item"
                v-for="(item, index) in displayTrendData"
                :key="item.id"
                @click="goManuscript(item.manuscriptId)"
              >
                <div class="bar-wrapper">
                  <div
                    class="bar-fill"
                    :class="getScoreClass(item.emotionControlScore)"
                    :style="{ height: (item.emotionControlScore * 10) + '%' }"
                  >
                    <span class="bar-value">{{ item.emotionControlScore }}</span>
                  </div>
                </div>
                <div class="bar-label" :title="item.manuscriptTitle">
                  {{ item.manuscriptTitle?.length > 6 ? item.manuscriptTitle.slice(0, 6) + '...' : item.manuscriptTitle }}
                </div>
              </div>
            </div>
            <div class="trend-line" v-if="displayTrendData.length > 1">
              <svg :viewBox="`0 0 ${chartWidth} 100`" preserveAspectRatio="none">
                <polyline
                  :points="trendLinePoints"
                  fill="none"
                  stroke="#409eff"
                  stroke-width="2"
                  stroke-dasharray="4,4"
                />
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="list-content" v-loading="loading">
      <el-empty v-if="!loading && list.length === 0" description="暂无练习笔记" />
      <div v-for="note in list" :key="note.id" class="note-card-wrapper">
        <el-card class="note-main-card">
          <div class="note-header">
            <div class="note-title" @click="goManuscript(note.manuscriptId)">
              <el-icon><Document /></el-icon>
              <span>{{ note.manuscriptTitle || '查看文稿' }}</span>
            </div>
            <div class="note-actions">
              <el-button type="primary" size="small" @click="editNote(note)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="danger" size="small" :loading="deletingId === note.id" @click="deleteNoteItem(note.id)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
          
          <div class="progress-summary" v-if="note.paragraphProgress">
            <div class="progress-title">
              <el-icon><TrendCharts /></el-icon>
              训练进度统计
            </div>
            <div class="progress-stats-inline">
              <span class="stat-item mastered">
                <span class="stat-dot"></span>
                已熟练 {{ note.paragraphProgress.mastered || 0 }}
              </span>
              <span class="stat-item strengthen">
                <span class="stat-dot"></span>
                需加强 {{ note.paragraphProgress.strengthen || 0 }}
              </span>
              <span class="stat-item skip">
                <span class="stat-dot"></span>
                暂不练 {{ note.paragraphProgress.skip || 0 }}
              </span>
              <span class="stat-item total">
                共 {{ note.paragraphProgress.total || 0 }} 段
              </span>
            </div>
            <div class="progress-bar-small">
              <div class="progress-fill-mastered" :style="{ width: note.paragraphProgress.percent + '%' }"></div>
            </div>
            <div class="extra-stats-row">
              <span class="extra-stat-item">
                <el-icon class="extra-stat-icon"><Operation /></el-icon>
                练习次数：<strong>{{ note.paragraphProgress.totalPracticeCount || 0 }}</strong> 次
              </span>
              <span class="extra-stat-item" v-if="note.practiceSessionStats?.sessionCount > 0">
                <el-icon class="extra-stat-icon"><Timer /></el-icon>
                会话次数：<strong>{{ note.practiceSessionStats.sessionCount || 0 }}</strong> 次
              </span>
              <span class="extra-stat-item" v-if="note.practiceSessionStats?.totalDurationSeconds > 0">
                <el-icon class="extra-stat-icon"><Clock /></el-icon>
                累计时长：<strong>{{ formatDuration(note.practiceSessionStats.totalDurationSeconds || 0) }}</strong>
              </span>
              <span class="extra-stat-item" v-if="note.practiceSessionStats?.averageScore > 0">
                <el-icon class="extra-stat-icon"><Star /></el-icon>
                平均评分：<strong>{{ Number(note.practiceSessionStats.averageScore).toFixed(1) }}</strong> 分
              </span>
              <span class="extra-stat-item">
                <el-icon class="extra-stat-icon"><Clock /></el-icon>
                最近练习：<strong>{{ formatRelativeTime(note.paragraphProgress.lastPracticeTime) }}</strong>
              </span>
            </div>
          </div>
          
          <div class="note-time">
            更新于：{{ formatTime(note.updateTime || note.paragraphProgress?.lastPracticeTime) }}
          </div>
        </el-card>
        
        <div class="note-sections">
          <el-card class="section-card section-difficulty" v-if="note.difficultyPoints">
            <div class="section-header">
              <el-icon class="section-icon"><Warning /></el-icon>
              <span class="section-title">难点句</span>
            </div>
            <div class="section-content">{{ note.difficultyPoints }}</div>
          </el-card>
          
          <el-card class="section-card section-tone" v-if="note.toneControl">
            <div class="section-header">
              <el-icon class="section-icon"><Microphone /></el-icon>
              <span class="section-title">语气控制</span>
            </div>
            <div class="section-content">{{ note.toneControl }}</div>
          </el-card>
          
          <el-card class="section-card section-emotion" v-if="note.emotionExpression">
            <div class="section-header">
              <el-icon class="section-icon"><MagicStick /></el-icon>
              <span class="section-title">情感表达</span>
            </div>
            <div class="section-content">{{ note.emotionExpression }}</div>
          </el-card>
          
          <el-card class="section-card section-other" v-if="note.otherNotes">
            <div class="section-header">
              <el-icon class="section-icon"><EditPen /></el-icon>
              <span class="section-title">其他记录</span>
            </div>
            <div class="section-content">{{ note.otherNotes }}</div>
          </el-card>

          <el-card class="section-card section-emotion-score" v-if="note.emotionControlScore">
            <div class="section-header">
              <el-icon class="section-icon"><Star /></el-icon>
              <span class="section-title">情绪控制评分</span>
              <span class="score-badge" :class="getScoreClass(note.emotionControlScore)">{{ note.emotionControlScore }}分</span>
            </div>
            <div class="section-content">
              <div class="score-bar">
                <div class="score-fill" :class="getScoreClass(note.emotionControlScore)" :style="{ width: (note.emotionControlScore * 10) + '%' }"></div>
              </div>
              <div class="score-note" v-if="note.emotionControlNote">{{ note.emotionControlNote }}</div>
            </div>
          </el-card>
          
          <div class="empty-sections-tip" v-if="!note.difficultyPoints && !note.toneControl && !note.emotionExpression && !note.otherNotes && !note.emotionControlScore">
            <el-icon><InfoFilled /></el-icon>
            <span>暂无详细笔记记录，点击编辑开始记录</span>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 30]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadList"
        @current-change="loadList"
      />
    </div>

    <el-dialog v-model="showEditDialog" title="编辑练习笔记" width="600px">
      <el-form :model="editForm" label-width="100px">
        <div class="form-section-title">
          <el-icon><Warning /></el-icon>
          <span>朗读难点记录</span>
        </div>
        <el-form-item label="难点句">
          <el-input v-model="editForm.difficultyPoints" type="textarea" :rows="2" placeholder="记录练习中遇到的难读句子、发音难点等" />
        </el-form-item>
        
        <div class="form-section-title">
          <el-icon><Microphone /></el-icon>
          <span>语气控制要点</span>
        </div>
        <el-form-item label="语气控制">
          <el-input v-model="editForm.toneControl" type="textarea" :rows="2" placeholder="记录语气、语调、节奏等控制要点" />
        </el-form-item>
        
        <div class="form-section-title">
          <el-icon><MagicStick /></el-icon>
          <span>情感表达要点</span>
        </div>
        <el-form-item label="情感表达">
          <el-input v-model="editForm.emotionExpression" type="textarea" :rows="2" placeholder="记录情感表达、情绪把控要点" />
        </el-form-item>
        
        <div class="form-section-title">
          <el-icon><EditPen /></el-icon>
          <span>其他训练记录</span>
        </div>
        <el-form-item label="其他记录">
          <el-input v-model="editForm.otherNotes" type="textarea" :rows="2" placeholder="其他练习心得、感悟等" />
        </el-form-item>

        <div class="form-section-title">
          <el-icon><Star /></el-icon>
          <span>情绪控制评分</span>
        </div>
        <el-form-item label="情绪评分">
          <div class="emotion-score-input">
            <el-rate
              v-model="editForm.emotionControlScore"
              :max="10"
              :low-threshold="3"
              :high-threshold="7"
              :colors="['#f56c6c', '#e6a23c', '#67c23a']"
              :texts="['很差', '较差', '一般', '较好', '很好']"
              show-text
              allow-half
            />
            <span class="score-num" v-if="editForm.emotionControlScore">{{ editForm.emotionControlScore }} 分</span>
          </div>
        </el-form-item>
        <el-form-item label="评分说明">
          <el-input v-model="editForm.emotionControlNote" type="textarea" :rows="2" placeholder="记录本次练习情绪控制的要点、感受或改进方向" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingEdit" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Edit, Delete, TrendCharts, Warning, Microphone, MagicStick, EditPen, InfoFilled, Clock, Operation, Star, Calendar, Timer } from '@element-plus/icons-vue'
import { getUserNotes, deleteNote as apiDeleteNote, saveNote, getManuscriptById, getUserTrainingProgressList, getEmotionScoreTrend, getPracticeSessionStats } from '@/api'
import { getCurrentUserId } from '@/utils/storage'
import PracticeCalendar from '@/components/PracticeCalendar.vue'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const showEditDialog = ref(false)
const editForm = ref({
  id: null,
  manuscriptId: null,
  difficultyPoints: '',
  toneControl: '',
  emotionExpression: '',
  otherNotes: '',
  emotionControlScore: null,
  emotionControlNote: ''
})

const userId = getCurrentUserId()
const savingEdit = ref(false)
const deletingId = ref(null)
const emotionTrendData = ref([])
const loadingTrend = ref(false)
const calendarRef = ref(null)

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const formatRelativeTime = (time) => {
  if (!time) return '暂无'
  const now = new Date()
  const target = new Date(time)
  const diff = now - target
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  return formatTime(time)
}

const formatDuration = (seconds) => {
  if (!seconds || seconds === 0) return '0分钟'
  const hours = Math.floor(seconds / 3600)
  const mins = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  if (hours > 0) {
    return `${hours}小时${mins}分钟`
  } else if (mins > 0) {
    return `${mins}分钟${secs > 0 ? secs + '秒' : ''}`
  }
  return `${secs}秒`
}

const getScoreClass = (score) => {
  if (!score) return ''
  if (score >= 8) return 'score-high'
  if (score >= 5) return 'score-medium'
  return 'score-low'
}

const averageScore = computed(() => {
  if (emotionTrendData.value.length === 0) return '0.0'
  const sum = emotionTrendData.value.reduce((acc, item) => acc + (item.emotionControlScore || 0), 0)
  return (sum / emotionTrendData.value.length).toFixed(1)
})

const highestScore = computed(() => {
  if (emotionTrendData.value.length === 0) return '0'
  const scores = emotionTrendData.value.map(item => item.emotionControlScore || 0)
  return Math.max(...scores)
})

const stabilityLevel = computed(() => {
  if (emotionTrendData.value.length < 2) return '暂无数据'
  const scores = emotionTrendData.value.map(item => item.emotionControlScore || 0)
  const avg = scores.reduce((a, b) => a + b, 0) / scores.length
  const variance = scores.reduce((a, b) => a + Math.pow(b - avg, 2), 0) / scores.length
  const stdDev = Math.sqrt(variance)
  if (stdDev < 1) return '很稳定'
  if (stdDev < 2) return '较稳定'
  if (stdDev < 3) return '一般'
  return '需提升'
})

const displayTrendData = computed(() => {
  const data = [...emotionTrendData.value].reverse()
  return data.slice(-10)
})

const chartWidth = computed(() => {
  const count = displayTrendData.value.length
  return Math.max(count * 60, 200)
})

const trendLinePoints = computed(() => {
  const data = displayTrendData.value
  if (data.length < 2) return ''
  const points = data.map((item, index) => {
    const x = (index / (data.length - 1)) * chartWidth.value
    const y = 100 - (item.emotionControlScore || 0) * 10
    return `${x},${y}`
  })
  return points.join(' ')
})

const loadEmotionTrend = async () => {
  loadingTrend.value = true
  try {
    const trendList = await getEmotionScoreTrend(userId)
    const result = []
    for (const note of trendList) {
      try {
        const manuscript = await getManuscriptById(note.manuscriptId, userId)
        if (manuscript && manuscript.status === 1) {
          result.push({
            ...note,
            manuscriptTitle: manuscript.title
          })
        }
      } catch (e) {
        console.error('加载文稿信息失败', e)
      }
    }
    emotionTrendData.value = result
  } catch (e) {
    console.error('加载情绪评分趋势失败', e)
  } finally {
    loadingTrend.value = false
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const [res, progressList] = await Promise.all([
      getUserNotes({
        userId,
        page: page.value - 1,
        size: size.value
      }),
      getUserTrainingProgressList(userId).catch(e => {
        console.error('加载训练进度失败', e)
        return []
      })
    ])
    
    const noteMap = new Map()
    for (const note of res.content) {
      if (!noteMap.has(note.manuscriptId)) {
        noteMap.set(note.manuscriptId, note)
      }
    }
    const allNotes = Array.from(noteMap.values())
    
    const progressMap = new Map()
    for (const prog of progressList) {
      progressMap.set(prog.manuscriptId, prog)
    }
    
    for (const prog of progressList) {
      if (!noteMap.has(prog.manuscriptId)) {
        allNotes.push({ manuscriptId: prog.manuscriptId, virtualNote: true })
      }
    }
    
    const validNotes = []
    for (let i = 0; i < allNotes.length; i++) {
      try {
        const manuscript = await getManuscriptById(allNotes[i].manuscriptId, userId)
        if (!manuscript || manuscript.status !== 1) {
          continue
        }
        allNotes[i].manuscriptTitle = manuscript?.title
        const progress = progressMap.get(allNotes[i].manuscriptId)
        
        if (progress) {
          allNotes[i].paragraphProgress = {
            mastered: progress.masteredCount || 0,
            strengthen: progress.strengthenCount || 0,
            skip: progress.skipCount || 0,
            total: progress.totalParagraphs || 0,
            percent: progress.progressPercent || 0,
            totalPracticeCount: progress.totalPracticeCount || 0,
            lastPracticeTime: progress.lastPracticeTime
          }
        } else {
          allNotes[i].paragraphProgress = {
            mastered: 0, strengthen: 0, skip: 0, total: 0, percent: 0,
            totalPracticeCount: 0, lastPracticeTime: null
          }
        }
        
        try {
          const sessionStats = await getPracticeSessionStats(userId, allNotes[i].manuscriptId)
          if (sessionStats) {
            allNotes[i].practiceSessionStats = sessionStats
          }
        } catch (e) {
          console.error('加载练习会话统计失败', e)
        }
        
        validNotes.push(allNotes[i])
      } catch (e) {
        console.error(e)
      }
    }
    
    validNotes.sort((a, b) => {
      const timeA = a.paragraphProgress?.lastPracticeTime || a.updateTime
      const timeB = b.paragraphProgress?.lastPracticeTime || b.updateTime
      if (!timeA && !timeB) return 0
      if (!timeA) return 1
      if (!timeB) return -1
      return new Date(timeB) - new Date(timeA)
    })
    
    list.value = validNotes
    total.value = validNotes.length
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const goManuscript = (manuscriptId) => {
  router.push(`/manuscript/${manuscriptId}`)
}

const editNote = (note) => {
  editForm.value = {
    id: note.id,
    manuscriptId: note.manuscriptId,
    difficultyPoints: note.difficultyPoints || '',
    toneControl: note.toneControl || '',
    emotionExpression: note.emotionExpression || '',
    otherNotes: note.otherNotes || '',
    emotionControlScore: note.emotionControlScore || null,
    emotionControlNote: note.emotionControlNote || ''
  }
  showEditDialog.value = true
}

const saveEdit = async () => {
  if (savingEdit.value) return
  savingEdit.value = true
  try {
    await saveNote({
      userId,
      manuscriptId: editForm.value.manuscriptId,
      difficultyPoints: editForm.value.difficultyPoints,
      toneControl: editForm.value.toneControl,
      emotionExpression: editForm.value.emotionExpression,
      otherNotes: editForm.value.otherNotes,
      emotionControlScore: editForm.value.emotionControlScore,
      emotionControlNote: editForm.value.emotionControlNote
    })
    ElMessage.success('保存成功')
    showEditDialog.value = false
    loadList()
    loadEmotionTrend()
    refreshCalendar()
  } catch (e) {
    console.error(e)
  } finally {
    savingEdit.value = false
  }
}

const deleteNoteItem = async (id) => {
  if (deletingId.value === id) return
  try {
    await ElMessageBox.confirm('确定要删除这条笔记吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    deletingId.value = id
    await apiDeleteNote(id, userId)
    ElMessage.success('删除成功')
    loadList()
    loadEmotionTrend()
    refreshCalendar()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  } finally {
    deletingId.value = null
  }
}

const handleDateSelect = (cell) => {
  if (cell && cell.hasPractice && cell.practiceData && cell.practiceData.manuscripts) {
    console.log('选中日期:', cell.date, '练习文稿:', cell.practiceData.manuscripts.length)
  }
}

const refreshCalendar = () => {
  if (calendarRef.value) {
    calendarRef.value.loadCalendar()
  }
}

onMounted(() => {
  loadList()
  loadEmotionTrend()
})
</script>

<style scoped>
.notes-page {
  max-width: 1100px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  margin-bottom: 24px;
  color: #303133;
}

.review-section {
  margin-bottom: 32px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border-radius: 16px;
  padding: 24px;
}

.section-header-row {
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 20px;
}

.section-header-row .section-title {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  color: #409eff;
  font-size: 24px;
}

.section-subtitle {
  font-size: 14px;
  color: #909399;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-average .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.stat-count .stat-icon {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
}

.stat-highest .stat-icon {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: #fff;
}

.stat-stability .stat-icon {
  background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%);
  color: #fff;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.stat-unit {
  font-size: 12px;
  color: #c0c4cc;
}

.trend-chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.chart-hint {
  font-size: 12px;
  color: #c0c4cc;
}

.chart-container {
  display: flex;
  gap: 12px;
  height: 220px;
}

.chart-y-axis {
  width: 30px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  font-size: 11px;
  color: #c0c4cc;
  text-align: right;
  padding: 6px 0;
  flex-shrink: 0;
}

.chart-content {
  flex: 1;
  position: relative;
  overflow-x: auto;
}

.chart-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  pointer-events: none;
}

.grid-line {
  height: 1px;
  background: #f0f2f5;
}

.chart-bars {
  display: flex;
  gap: 16px;
  height: calc(100% - 24px);
  align-items: flex-end;
  padding: 0 8px;
  min-width: 100%;
  position: relative;
  z-index: 2;
}

.bar-item {
  flex: 1;
  min-width: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.bar-item:hover {
  transform: scale(1.05);
}

.bar-wrapper {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.bar-fill {
  width: 28px;
  border-radius: 6px 6px 0 0;
  position: relative;
  transition: all 0.3s ease;
  min-height: 4px;
}

.bar-fill.score-high {
  background: linear-gradient(180deg, #85ce61 0%, #67c23a 100%);
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
}

.bar-fill.score-medium {
  background: linear-gradient(180deg, #f0c78a 0%, #e6a23c 100%);
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.3);
}

.bar-fill.score-low {
  background: linear-gradient(180deg, #f89898 0%, #f56c6c 100%);
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.3);
}

.bar-value {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 11px;
  font-weight: 600;
  color: #606266;
  white-space: nowrap;
}

.bar-label {
  font-size: 11px;
  color: #909399;
  text-align: center;
  max-width: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.trend-line {
  position: absolute;
  top: 0;
  left: 8px;
  right: 8px;
  height: calc(100% - 24px);
  pointer-events: none;
  z-index: 1;
}

.trend-line svg {
  width: 100%;
  height: 100%;
}

.note-card-wrapper {
  margin-bottom: 28px;
}

.note-main-card {
  margin-bottom: 16px;
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.note-title {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.note-title:hover {
  text-decoration: underline;
}

.note-actions {
  display: flex;
  gap: 8px;
}

.progress-summary {
  background: linear-gradient(135deg, #f0f9eb 0%, #fdf6ec 100%);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}

.progress-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.progress-stats-inline {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
}

.stat-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.stat-item.mastered .stat-dot {
  background: #67c23a;
}

.stat-item.strengthen .stat-dot {
  background: #e6a23c;
}

.stat-item.skip .stat-dot {
  background: #909399;
}

.stat-item.total {
  color: #909399;
}

.progress-bar-small {
  height: 6px;
  background: #e4e7ed;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill-mastered {
  height: 100%;
  background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.extra-stats-row {
  display: flex;
  gap: 24px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed rgba(103, 194, 58, 0.3);
  flex-wrap: wrap;
}

.extra-stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.extra-stat-icon {
  font-size: 16px;
  color: #e6a23c;
}

.extra-stat-item strong {
  color: #303133;
  font-weight: 600;
}

.note-time {
  font-size: 12px;
  color: #c0c4cc;
  text-align: right;
}

.note-sections {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.section-card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.section-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.section-card :deep(.el-card__body) {
  padding: 0;
}

.section-header {
  padding: 14px 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
}

.section-icon {
  font-size: 18px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
}

.section-content {
  padding: 16px 20px 20px;
  font-size: 14px;
  color: #303133;
  line-height: 1.8;
  background: #fff;
}

.section-difficulty .section-header {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  color: #f56c6c;
}

.section-difficulty .section-content {
  background: linear-gradient(135deg, #ffffff 0%, #fef5f5 100%);
  border-left: 3px solid #f56c6c;
}

.section-tone .section-header {
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
  color: #409eff;
}

.section-tone .section-content {
  background: linear-gradient(135deg, #ffffff 0%, #f5f9ff 100%);
  border-left: 3px solid #409eff;
}

.section-emotion .section-header {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
  color: #67c23a;
}

.section-emotion .section-content {
  background: linear-gradient(135deg, #ffffff 0%, #f8fcf5 100%);
  border-left: 3px solid #67c23a;
}

.section-other .section-header {
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
  color: #e6a23c;
}

.section-other .section-content {
  background: linear-gradient(135deg, #ffffff 0%, #fefaf5 100%);
  border-left: 3px solid #e6a23c;
}

.section-emotion-score .section-header {
  background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
  color: #fa8c16;
}

.section-emotion-score .section-content {
  background: linear-gradient(135deg, #ffffff 0%, #fffbf0 100%);
  border-left: 3px solid #fa8c16;
}

.score-badge {
  margin-left: auto;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.score-badge.score-high {
  background: #f0f9eb;
  color: #67c23a;
}

.score-badge.score-medium {
  background: #fdf6ec;
  color: #e6a23c;
}

.score-badge.score-low {
  background: #fef0f0;
  color: #f56c6c;
}

.score-bar {
  height: 8px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 10px;
}

.score-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.score-fill.score-high {
  background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%);
}

.score-fill.score-medium {
  background: linear-gradient(90deg, #e6a23c 0%, #f0c78a 100%);
}

.score-fill.score-low {
  background: linear-gradient(90deg, #f56c6c 0%, #f89898 100%);
}

.score-note {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.emotion-score-input {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.score-num {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.empty-sections-tip {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #909399;
  font-size: 14px;
}

.form-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 20px 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.form-section-title:first-child {
  margin-top: 0;
}

.list-content {
  min-height: 300px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.calendar-section {
  margin-bottom: 32px;
}

.calendar-icon {
  color: #67c23a !important;
}
</style>
