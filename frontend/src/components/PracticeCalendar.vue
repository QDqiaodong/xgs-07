<template>
  <div class="practice-calendar">
    <div class="calendar-header">
      <div class="month-nav">
        <el-button class="nav-btn" circle @click="prevMonth">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <span class="month-title">{{ currentYear }}年{{ currentMonth }}月</span>
        <el-button class="nav-btn" circle @click="nextMonth">
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button size="small" type="primary" plain @click="goToday" class="today-btn">今天</el-button>
      </div>
      <div class="calendar-stats" v-if="monthStats.totalDays > 0">
        <div class="stat-item">
          <span class="stat-label">本月练习</span>
          <span class="stat-value highlight">{{ monthStats.totalDays }}</span>
          <span class="stat-unit">天</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">累计时长</span>
          <span class="stat-value">{{ monthStats.totalMinutes }}</span>
          <span class="stat-unit">分钟</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">练习文稿</span>
          <span class="stat-value">{{ monthStats.totalManuscripts }}</span>
          <span class="stat-unit">篇</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">连续打卡</span>
          <span class="stat-value streak">{{ streakDays }}</span>
          <span class="stat-unit">天</span>
        </div>
      </div>
    </div>

    <div class="calendar-body">
      <div class="weekday-row">
        <div v-for="day in weekDays" :key="day" class="weekday-cell">{{ day }}</div>
      </div>
      <div class="days-grid">
        <div
          v-for="(cell, index) in calendarCells"
          :key="index"
          class="day-cell"
          :class="getCellClass(cell)"
          @click="selectDate(cell)"
        >
          <div class="day-number">{{ cell.day }}</div>
          <div class="day-content" v-if="cell.hasPractice">
            <div class="practice-badge" :class="getBadgeClass(cell.practiceData)">
              <el-icon v-if="cell.practiceData.manuscriptCount >= 3"><Trophy /></el-icon>
              <el-icon v-else-if="cell.practiceData.manuscriptCount >= 2"><Star /></el-icon>
              <el-icon v-else><Collection /></el-icon>
            </div>
            <div class="practice-info">
              <span class="practice-count" v-if="cell.practiceData.totalPracticeCount > 0">
                {{ cell.practiceData.totalPracticeCount }}次
              </span>
              <span class="practice-minutes" v-if="cell.practiceData.estimatedMinutes > 0">
                {{ cell.practiceData.estimatedMinutes }}分
              </span>
            </div>
          </div>
          <div class="today-marker" v-if="cell.isToday"></div>
        </div>
      </div>
    </div>

    <div class="legend-row">
      <div class="legend-item">
        <span class="legend-dot level-1"></span>
        <span>少量练习</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot level-2"></span>
        <span>中等练习</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot level-3"></span>
        <span>高强度练习</span>
      </div>
    </div>

    <el-dialog v-model="showDayDetail" :title="selectedDateStr" width="560px" class="day-detail-dialog">
      <div v-if="selectedDateData" class="day-detail-content">
        <div class="detail-summary">
          <div class="summary-card summary-manuscripts">
            <el-icon class="summary-icon"><Collection /></el-icon>
            <div class="summary-text">
              <span class="summary-value">{{ selectedDateData.manuscriptCount }}</span>
              <span class="summary-label">练习文稿</span>
            </div>
          </div>
          <div class="summary-card summary-practice">
            <el-icon class="summary-icon"><Operation /></el-icon>
            <div class="summary-text">
              <span class="summary-value">{{ selectedDateData.totalPracticeCount }}</span>
              <span class="summary-label">总练习次数</span>
            </div>
          </div>
          <div class="summary-card summary-time">
            <el-icon class="summary-icon"><Clock /></el-icon>
            <div class="summary-text">
              <span class="summary-value">{{ selectedDateData.estimatedMinutes }}</span>
              <span class="summary-label">预估时长(分钟)</span>
            </div>
          </div>
        </div>

        <div class="keypoints-section" v-if="selectedDateData.keyPoints">
          <div class="section-header">
            <el-icon class="section-icon"><MagicStick /></el-icon>
            <span class="section-title">当日重点</span>
          </div>
          <div class="keypoints-content">{{ selectedDateData.keyPoints }}</div>
        </div>

        <div class="manuscripts-section">
          <div class="section-header">
            <el-icon class="section-icon"><Document /></el-icon>
            <span class="section-title">练习文稿详情</span>
          </div>
          <div class="manuscript-list">
            <div
              class="manuscript-item"
              v-for="ms in selectedDateData.manuscripts"
              :key="ms.manuscriptId"
              @click="goManuscriptDetail(ms.manuscriptId)"
            >
              <div class="manuscript-header">
                <span class="manuscript-title">{{ ms.manuscriptTitle }}</span>
                <div class="manuscript-badges">
                  <el-tag v-if="ms.practiceCount > 0" size="small" type="info" effect="light">
                    <el-icon><Operation /></el-icon>
                    {{ ms.practiceCount }}次
                  </el-tag>
                  <el-tag
                    v-if="ms.emotionScore"
                    size="small"
                    :type="getScoreTagType(ms.emotionScore)"
                    effect="light"
                  >
                    <el-icon><Star /></el-icon>
                    {{ ms.emotionScore }}分
                  </el-tag>
                </div>
              </div>
              <div class="manuscript-note" v-if="ms.noteSummary">
                <el-icon class="note-icon"><EditPen /></el-icon>
                <span>{{ ms.noteSummary }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-day">
        <el-empty description="当日暂无练习记录" :image-size="100" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowLeft,
  ArrowRight,
  Trophy,
  Star,
  Collection,
  Operation,
  Clock,
  MagicStick,
  Document,
  EditPen
} from '@element-plus/icons-vue'
import { getPracticeCalendar } from '@/api'
import { getCurrentUserId } from '@/utils/storage'

const props = defineProps({
  userId: {
    type: [Number, String],
    default: () => getCurrentUserId()
  }
})

const emit = defineEmits(['date-select'])

const router = useRouter()

const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)
const calendarData = ref([])
const loadingCalendar = ref(false)
const showDayDetail = ref(false)
const selectedDate = ref(null)
const weekDays = ['日', '一', '二', '三', '四', '五', '六']

const loadCalendar = async () => {
  loadingCalendar.value = true
  try {
    const data = await getPracticeCalendar(props.userId, currentYear.value, currentMonth.value)
    calendarData.value = data || []
  } catch (e) {
    console.error('加载日历数据失败', e)
  } finally {
    loadingCalendar.value = false
  }
}

const calendarDataMap = computed(() => {
  const map = new Map()
  for (const item of calendarData.value) {
    map.set(item.date, item)
  }
  return map
})

const calendarCells = computed(() => {
  const cells = []
  const firstDay = new Date(currentYear.value, currentMonth.value - 1, 1)
  const lastDay = new Date(currentYear.value, currentMonth.value, 0)
  const firstDayOfWeek = firstDay.getDay()
  const totalDays = lastDay.getDate()
  const today = new Date()
  const todayStr = formatDateStr(today)

  const prevMonthLastDay = new Date(currentYear.value, currentMonth.value - 1, 0).getDate()
  for (let i = firstDayOfWeek - 1; i >= 0; i--) {
    const day = prevMonthLastDay - i
    const date = buildDateStr(currentYear.value, currentMonth.value - 1, day)
    cells.push({
      day,
      date,
      isCurrentMonth: false,
      isToday: date === todayStr,
      hasPractice: calendarDataMap.value.has(date),
      practiceData: calendarDataMap.value.get(date)
    })
  }

  for (let day = 1; day <= totalDays; day++) {
    const date = buildDateStr(currentYear.value, currentMonth.value, day)
    cells.push({
      day,
      date,
      isCurrentMonth: true,
      isToday: date === todayStr,
      hasPractice: calendarDataMap.value.has(date),
      practiceData: calendarDataMap.value.get(date)
    })
  }

  const remaining = 42 - cells.length
  for (let day = 1; day <= remaining; day++) {
    const date = buildDateStr(currentYear.value, currentMonth.value + 1, day)
    cells.push({
      day,
      date,
      isCurrentMonth: false,
      isToday: date === todayStr,
      hasPractice: calendarDataMap.value.has(date),
      practiceData: calendarDataMap.value.get(date)
    })
  }

  return cells
})

const monthStats = computed(() => {
  let totalDays = 0
  let totalMinutes = 0
  let totalManuscripts = 0
  const manuscriptSet = new Set()

  for (const cell of calendarCells.value) {
    if (cell.isCurrentMonth && cell.hasPractice) {
      totalDays++
      totalMinutes += cell.practiceData.estimatedMinutes || 0
      totalManuscripts += cell.practiceData.manuscriptCount || 0
      if (cell.practiceData.manuscripts) {
        for (const ms of cell.practiceData.manuscripts) {
          manuscriptSet.add(ms.manuscriptId)
        }
      }
    }
  }

  return {
    totalDays,
    totalMinutes,
    totalManuscripts: manuscriptSet.size
  }
})

const streakDays = computed(() => {
  const today = new Date()
  let streak = 0
  for (let i = 0; i < 365; i++) {
    const checkDate = new Date(today)
    checkDate.setDate(today.getDate() - i)
    const dateStr = formatDateStr(checkDate)
    if (calendarDataMap.value.has(dateStr)) {
      streak++
    } else if (i > 0) {
      break
    }
  }
  return streak
})

const selectedDateData = computed(() => {
  if (!selectedDate.value) return null
  return calendarDataMap.value.get(selectedDate.value) || null
})

const selectedDateStr = computed(() => {
  if (!selectedDate.value) return ''
  const d = new Date(selectedDate.value)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 练习详情`
})

const buildDateStr = (year, month, day) => {
  const d = new Date(year, month - 1, day)
  return formatDateStr(d)
}

const formatDateStr = (date) => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const prevMonth = () => {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value--
  } else {
    currentMonth.value--
  }
}

const nextMonth = () => {
  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value++
  } else {
    currentMonth.value++
  }
}

const goToday = () => {
  const now = new Date()
  currentYear.value = now.getFullYear()
  currentMonth.value = now.getMonth() + 1
}

const getCellClass = (cell) => {
  const classes = []
  if (!cell.isCurrentMonth) classes.push('other-month')
  if (cell.isToday) classes.push('today')
  if (cell.hasPractice) classes.push('has-practice')
  if (cell.hasPractice && cell.isCurrentMonth) {
    const level = getPracticeLevel(cell.practiceData)
    classes.push(`practice-level-${level}`)
  }
  return classes
}

const getPracticeLevel = (data) => {
  if (!data) return 0
  const count = data.totalPracticeCount || 0
  if (count >= 10) return 3
  if (count >= 5) return 2
  return 1
}

const getBadgeClass = (data) => {
  const level = getPracticeLevel(data)
  return `badge-level-${level}`
}

const selectDate = (cell) => {
  selectedDate.value = cell.date
  showDayDetail.value = true
  emit('date-select', cell)
}

const goManuscriptDetail = (manuscriptId) => {
  showDayDetail.value = false
  router.push(`/manuscript/${manuscriptId}`)
}

const getScoreTagType = (score) => {
  if (!score) return 'info'
  if (score >= 8) return 'success'
  if (score >= 5) return 'warning'
  return 'danger'
}

watch(
  () => [currentYear.value, currentMonth.value],
  () => {
    loadCalendar()
  }
)

onMounted(() => {
  loadCalendar()
})

defineExpose({
  loadCalendar,
  goToday
})
</script>

<style scoped>
.practice-calendar {
  background: linear-gradient(135deg, #ffffff 0%, #f8faff 100%);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.08);
  border: 1px solid rgba(64, 158, 255, 0.1);
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.month-nav {
  display: flex;
  align-items: center;
  gap: 12px;
}

.month-title {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  min-width: 140px;
  text-align: center;
}

.nav-btn {
  background: #f0f5ff;
  border: none;
  color: #409eff;
}

.nav-btn:hover {
  background: #409eff;
  color: #fff;
}

.today-btn {
  margin-left: 8px;
}

.calendar-stats {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: baseline;
  gap: 4px;
  background: #fff;
  padding: 10px 16px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #606266;
}

.stat-value.highlight {
  color: #409eff;
}

.stat-value.streak {
  color: #e6a23c;
}

.stat-unit {
  font-size: 12px;
  color: #c0c4cc;
}

.calendar-body {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
}

.weekday-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 12px;
}

.weekday-cell {
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #909399;
  padding: 8px 0;
}

.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}

.day-cell {
  min-height: 72px;
  padding: 8px;
  border-radius: 10px;
  cursor: pointer;
  position: relative;
  transition: all 0.25s ease;
  border: 1px solid transparent;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #fafbfc;
}

.day-cell:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.day-cell.other-month {
  opacity: 0.35;
  background: transparent;
}

.day-cell.today {
  border-color: #409eff;
  background: #ecf5ff;
}

.day-cell.today .day-number {
  color: #409eff;
  font-weight: 700;
}

.day-cell.has-practice {
  border-color: transparent;
}

.day-cell.practice-level-1 {
  background: linear-gradient(135deg, #e1f3d8 0%, #f0f9eb 100%);
  border-color: rgba(103, 194, 58, 0.3);
}

.day-cell.practice-level-2 {
  background: linear-gradient(135deg, #d9ecff 0%, #ecf5ff 100%);
  border-color: rgba(64, 158, 255, 0.3);
}

.day-cell.practice-level-3 {
  background: linear-gradient(135deg, #fde2e2 0%, #fef0f0 50%, #fff7e6 100%);
  border-color: rgba(245, 108, 108, 0.3);
}

.day-number {
  font-size: 15px;
  color: #606266;
  line-height: 1.2;
  margin-bottom: 4px;
}

.day-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  flex: 1;
  justify-content: center;
}

.practice-badge {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #fff;
}

.badge-level-1 {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.badge-level-2 {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

.badge-level-3 {
  background: linear-gradient(135deg, #f56c6c 0%, #e6a23c 100%);
}

.practice-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
  font-size: 10px;
  color: #606266;
}

.practice-count,
.practice-minutes {
  line-height: 1.2;
  font-weight: 500;
}

.today-marker {
  position: absolute;
  bottom: 4px;
  right: 4px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #409eff;
}

.legend-row {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #ebeef5;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
}

.legend-dot {
  width: 14px;
  height: 14px;
  border-radius: 4px;
}

.legend-dot.level-1 {
  background: linear-gradient(135deg, #e1f3d8 0%, #f0f9eb 100%);
  border: 1px solid rgba(103, 194, 58, 0.3);
}

.legend-dot.level-2 {
  background: linear-gradient(135deg, #d9ecff 0%, #ecf5ff 100%);
  border: 1px solid rgba(64, 158, 255, 0.3);
}

.legend-dot.level-3 {
  background: linear-gradient(135deg, #fde2e2 0%, #fef0f0 50%, #fff7e6 100%);
  border: 1px solid rgba(245, 108, 108, 0.3);
}

.day-detail-dialog :deep(.el-dialog__body) {
  padding-top: 8px;
}

.day-detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.summary-card {
  padding: 16px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.summary-icon {
  font-size: 28px;
}

.summary-card.summary-manuscripts {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.summary-card.summary-practice {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
}

.summary-card.summary-time {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: #fff;
}

.summary-text {
  display: flex;
  flex-direction: column;
}

.summary-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.1;
}

.summary-label {
  font-size: 12px;
  opacity: 0.9;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.section-icon {
  font-size: 18px;
  color: #409eff;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.keypoints-content {
  background: linear-gradient(135deg, #fff7e6 0%, #fffaf0 100%);
  padding: 14px 16px;
  border-radius: 10px;
  border-left: 3px solid #e6a23c;
  font-size: 14px;
  color: #606266;
  line-height: 1.7;
}

.manuscript-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.manuscript-item {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 14px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.manuscript-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
  transform: translateY(-1px);
}

.manuscript-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  gap: 12px;
}

.manuscript-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}

.manuscript-badges {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.manuscript-note {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding-top: 8px;
  border-top: 1px dashed #ebeef5;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.note-icon {
  color: #909399;
  font-size: 14px;
  flex-shrink: 0;
  margin-top: 2px;
}

.empty-day {
  padding: 24px 0;
}
</style>
