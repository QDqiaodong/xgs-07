<template>
  <div class="training-page" v-loading="loading">
    <div class="not-allowed" v-if="notAllowed">
      <el-result icon="warning" title="无法访问" sub-title="该文稿不存在或您暂无权限查看">
        <template #extra>
          <el-button type="primary" @click="$router.back()">返回</el-button>
          <el-button @click="$router.push('/manuscripts')">去文稿列表</el-button>
        </template>
      </el-result>
    </div>

    <template v-else-if="trainingState">
      <header class="training-header">
        <div class="header-left">
          <el-button text @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回文稿
          </el-button>
          <div class="header-title">
            <h2>逐段跟读训练</h2>
            <span class="manuscript-name">{{ trainingState.manuscriptTitle || '文稿' }}</span>
          </div>
        </div>
        <div class="header-right">
          <div class="progress-summary">
            <span class="progress-label">已完成</span>
            <span class="progress-value">{{ completedList.length }}/{{ trainingState.totalParagraphs }}</span>
          </div>
          <div class="progress-ring">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
            </div>
            <span class="progress-percent">{{ progressPercent }}%</span>
          </div>
        </div>
      </header>

      <div class="training-empty" v-if="trainingState.totalParagraphs === 0">
        <el-empty description="该文稿暂无可练习的段落" />
      </div>

      <div class="training-body" v-else>
        <section class="current-section">
          <div class="current-card" :class="'card-status-' + (currentParagraph?.status || 'unmarked')">
            <div class="current-head">
              <div class="head-segment">
                <span class="segment-tag">第 {{ currentIndex + 1 }} 段</span>
                <span class="segment-total">/ 共 {{ trainingState.totalParagraphs }} 段</span>
              </div>
              <span class="status-badge" :class="'badge-' + (currentParagraph?.status || 'unmarked')">
                {{ getStatusLabel(currentParagraph?.status) }}
              </span>
            </div>

            <div class="current-content">{{ currentParagraph?.content }}</div>

            <div class="tip-block tip-reading" v-if="currentParagraph?.readingTip">
              <span class="tip-icon">💡</span>
              <div class="tip-body">
                <span class="tip-label">朗读提示</span>
                <span class="tip-text">{{ currentParagraph.readingTip }}</span>
              </div>
            </div>
            <div class="tip-block tip-focus" v-if="currentParagraph?.practiceFocus">
              <span class="tip-icon">🎯</span>
              <div class="tip-body">
                <span class="tip-label">练习重点</span>
                <span class="tip-text">{{ currentParagraph.practiceFocus }}</span>
              </div>
            </div>

            <div class="action-bar">
              <el-button :icon="ArrowLeft" :disabled="currentIndex === 0" @click="prev">上一段</el-button>
              <el-button type="success" :icon="Check" :loading="saving" @click="markMasteredAndNext">
                标记已熟练 · 下一段
              </el-button>
              <el-button type="warning" :icon="Warning" :loading="saving" @click="markStrengthen">
                标记需加强
              </el-button>
              <el-button type="info" :icon="Clock" :loading="saving" @click="markSkip">
                暂不练
              </el-button>
              <el-button text :disabled="!currentParagraph?.status" @click="clearMark">清除标记</el-button>
              <el-button :icon="ArrowRight" :disabled="currentIndex >= trainingState.totalParagraphs - 1" @click="next">下一段</el-button>
            </div>
          </div>

          <transition name="fade">
            <div class="complete-banner" v-if="allDone">
              <el-icon><CircleCheck /></el-icon>
              <span>全部段落已处理完毕，跟读训练完成！可在右侧列表点击任意段落回顾复习。</span>
            </div>
          </transition>
        </section>

        <aside class="side-panel">
          <div class="panel-group group-current">
            <div class="group-head">
              <span class="group-dot current"></span>
              <span class="group-title">当前段</span>
            </div>
            <div class="group-item current-item">
              <span class="item-num">第 {{ currentIndex + 1 }} 段</span>
              <span class="item-status" :class="'badge-' + (currentParagraph?.status || 'unmarked')">
                {{ getStatusLabel(currentParagraph?.status) }}
              </span>
            </div>
          </div>

          <div class="panel-group">
            <div class="group-head">
              <span class="group-dot mastered"></span>
              <span class="group-title">已完成段</span>
              <span class="group-count">{{ completedList.length }}</span>
            </div>
            <div class="group-items" v-if="completedList.length">
              <div class="group-item clickable" v-for="idx in completedList" :key="'c-' + idx" @click="jumpTo(idx)">
                <span class="item-num">第 {{ idx + 1 }} 段</span>
                <el-icon class="item-go"><ArrowRight /></el-icon>
              </div>
            </div>
            <div class="group-empty" v-else>暂无已完成段落</div>
          </div>

          <div class="panel-group">
            <div class="group-head">
              <span class="group-dot strengthen"></span>
              <span class="group-title">待加强段</span>
              <span class="group-count">{{ strengthenList.length }}</span>
            </div>
            <div class="group-items" v-if="strengthenList.length">
              <div class="group-item clickable" v-for="idx in strengthenList" :key="'s-' + idx" @click="jumpTo(idx)">
                <span class="item-num">第 {{ idx + 1 }} 段</span>
                <el-icon class="item-go"><ArrowRight /></el-icon>
              </div>
            </div>
            <div class="group-empty" v-else>暂无待加强段落</div>
          </div>

          <div class="panel-group">
            <div class="group-head">
              <span class="group-dot skip"></span>
              <span class="group-title">暂不练段</span>
              <span class="group-count">{{ skipList.length }}</span>
            </div>
            <div class="group-items" v-if="skipList.length">
              <div class="group-item clickable" v-for="idx in skipList" :key="'k-' + idx" @click="jumpTo(idx)">
                <span class="item-num">第 {{ idx + 1 }} 段</span>
                <el-icon class="item-go"><ArrowRight /></el-icon>
              </div>
            </div>
            <div class="group-empty" v-else>暂无跳过段落</div>
          </div>
        </aside>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, Check, Warning, Clock, CircleCheck } from '@element-plus/icons-vue'
import { getParagraphTrainingState, saveParagraphProgress, deleteParagraphProgress } from '@/api'
import { getCurrentUserId, getTrainingCurrent, saveTrainingCurrent } from '@/utils/storage'

const route = useRoute()
const router = useRouter()
const userId = getCurrentUserId()
const loading = ref(false)
const notAllowed = ref(false)
const trainingState = ref(null)
const currentIndex = ref(0)
const saving = ref(false)

const paragraphs = computed(() => trainingState.value?.paragraphs || [])
const total = computed(() => trainingState.value?.totalParagraphs || 0)
const currentParagraph = computed(() => paragraphs.value[currentIndex.value])
const completedList = computed(() => trainingState.value?.completedParagraphs || [])
const strengthenList = computed(() => trainingState.value?.strengthenParagraphs || [])
const skipList = computed(() => trainingState.value?.skipParagraphs || [])
const progressPercent = computed(() => trainingState.value?.progressPercent || 0)
const allDone = computed(() => {
  if (total.value === 0) return false
  return (completedList.value.length + skipList.value.length) >= total.value
})

const getStatusLabel = (status) => {
  const map = { mastered: '已熟练', strengthen: '需加强', skip: '暂不练' }
  return map[status] || '未标记'
}

const goBack = () => router.push('/manuscript/' + route.params.id)

const persistCurrent = () => {
  saveTrainingCurrent(userId, route.params.id, currentIndex.value)
}

const prev = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
    persistCurrent()
  }
}
const next = () => {
  if (currentIndex.value < total.value - 1) {
    currentIndex.value++
    persistCurrent()
  }
}
const jumpTo = (idx) => {
  if (idx >= 0 && idx < total.value) {
    currentIndex.value = idx
    persistCurrent()
  }
}
const advance = () => {
  if (currentIndex.value < total.value - 1) {
    currentIndex.value++
  }
  persistCurrent()
}

const applyStatus = (idx, status) => {
  const p = paragraphs.value[idx]
  if (p) {
    p.status = status
  }
  refreshLists()
}

const refreshLists = () => {
  if (!trainingState.value) return
  const completed = []
  const strengthen = []
  const skip = []
  paragraphs.value.forEach((p, i) => {
    if (p.status === 'mastered') completed.push(i)
    else if (p.status === 'strengthen') strengthen.push(i)
    else if (p.status === 'skip') skip.push(i)
  })
  trainingState.value.completedParagraphs = completed
  trainingState.value.strengthenParagraphs = strengthen
  trainingState.value.skipParagraphs = skip
  const t = total.value
  trainingState.value.progressPercent = t > 0 ? Math.round((completed.length * 100) / t) : 0
}

const setStatus = async (idx, status, shouldAdvance) => {
  if (saving.value) return
  saving.value = true
  const prevStatus = paragraphs.value[idx]?.status || ''
  applyStatus(idx, status)
  try {
    await saveParagraphProgress({
      userId,
      manuscriptId: route.params.id,
      paragraphIndex: idx,
      status
    })
    ElMessage.success('状态已更新')
    if (shouldAdvance) advance()
  } catch (e) {
    applyStatus(idx, prevStatus)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const markMasteredAndNext = () => setStatus(currentIndex.value, 'mastered', true)
const markStrengthen = () => setStatus(currentIndex.value, 'strengthen', true)
const markSkip = () => setStatus(currentIndex.value, 'skip', true)

const clearMark = async () => {
  if (saving.value) return
  const idx = currentIndex.value
  const prevStatus = paragraphs.value[idx]?.status || ''
  if (!prevStatus) return
  saving.value = true
  applyStatus(idx, '')
  try {
    await deleteParagraphProgress(userId, route.params.id, idx)
    ElMessage.success('已清除标记')
  } catch (e) {
    applyStatus(idx, prevStatus)
    ElMessage.error('清除标记失败，请重试')
  } finally {
    saving.value = false
  }
}

const loadTraining = async () => {
  loading.value = true
  notAllowed.value = false
  try {
    const state = await getParagraphTrainingState(userId, route.params.id)
    if (!state) {
      notAllowed.value = true
      return
    }
    trainingState.value = state
    const saved = getTrainingCurrent(userId, route.params.id)
    if (saved !== null && saved !== undefined && saved >= 0 && saved < state.totalParagraphs) {
      currentIndex.value = saved
    } else {
      currentIndex.value = state.currentParagraphIndex || 0
    }
  } catch (e) {
    console.error('加载跟读训练状态失败', e)
    notAllowed.value = true
  } finally {
    loading.value = false
  }
}

onMounted(loadTraining)
</script>

<style scoped>
.training-page {
  min-height: 100vh;
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.not-allowed {
  padding: 80px 0;
}

.training-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title h2 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.manuscript-name {
  font-size: 13px;
  color: #909399;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.progress-summary {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.progress-label {
  font-size: 12px;
  color: #909399;
}

.progress-value {
  font-size: 16px;
  font-weight: 600;
  color: #67c23a;
}

.progress-ring {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 180px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #67c23a, #95d475);
  border-radius: 4px;
  transition: width 0.4s ease;
}

.progress-percent {
  font-size: 13px;
  font-weight: 600;
  color: #67c23a;
  min-width: 36px;
  text-align: right;
}

.training-empty {
  padding: 60px 0;
}

.training-body {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  align-items: start;
}

.current-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.current-card {
  background: #fff;
  border-radius: 14px;
  padding: 28px 32px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06);
  border-top: 4px solid #c0c4cc;
  transition: border-color 0.3s ease;
}

.current-card.card-status-mastered {
  border-top-color: #67c23a;
}
.current-card.card-status-strengthen {
  border-top-color: #e6a23c;
}
.current-card.card-status-skip {
  border-top-color: #909399;
}

.current-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.head-segment {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.segment-tag {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

.segment-total {
  font-size: 14px;
  color: #909399;
}

.status-badge {
  font-size: 13px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 12px;
}

.badge-mastered {
  background: #f0f9eb;
  color: #67c23a;
}
.badge-strengthen {
  background: #fdf6ec;
  color: #e6a23c;
}
.badge-skip {
  background: #f4f4f5;
  color: #909399;
}
.badge-unmarked {
  background: #f4f4f5;
  color: #c0c4cc;
}

.current-content {
  font-size: 22px;
  line-height: 2.1;
  color: #303133;
  letter-spacing: 0.5px;
  white-space: pre-wrap;
  padding: 12px 0 20px;
  min-height: 120px;
}

.tip-block {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  margin-bottom: 12px;
  font-size: 14px;
}

.tip-reading {
  background: #ecf5ff;
}

.tip-focus {
  background: #fdf6ec;
}

.tip-icon {
  font-size: 18px;
  line-height: 1.6;
}

.tip-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.tip-label {
  font-size: 12px;
  font-weight: 600;
  color: #606266;
}

.tip-text {
  color: #303133;
  line-height: 1.6;
}

.action-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px dashed #ebeef5;
}

.complete-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  background: linear-gradient(90deg, #f0f9eb, #ecf5ff);
  border-radius: 12px;
  color: #67c23a;
  font-size: 15px;
  font-weight: 600;
}

.complete-banner .el-icon {
  font-size: 22px;
}

.side-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 24px;
}

.panel-group {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.group-current {
  border-left: 4px solid #409eff;
}

.group-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.group-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.group-dot.current {
  background: #409eff;
}
.group-dot.mastered {
  background: #67c23a;
}
.group-dot.strengthen {
  background: #e6a23c;
}
.group-dot.skip {
  background: #909399;
}

.group-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}

.group-count {
  font-size: 13px;
  color: #909399;
  background: #f4f4f5;
  padding: 1px 8px;
  border-radius: 10px;
}

.group-items {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.group-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 8px;
  background: #f9fafc;
  font-size: 13px;
  color: #606266;
}

.group-item.clickable {
  cursor: pointer;
  transition: background 0.2s ease;
}

.group-item.clickable:hover {
  background: #ecf5ff;
  color: #409eff;
}

.current-item {
  background: #ecf5ff;
  color: #303133;
  font-weight: 600;
}

.item-num {
  flex: 1;
}

.item-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}

.item-go {
  font-size: 13px;
  color: #c0c4cc;
}

.group-empty {
  font-size: 13px;
  color: #c0c4cc;
  padding: 6px 12px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 900px) {
  .training-body {
    grid-template-columns: 1fr;
  }
  .side-panel {
    position: static;
  }
}
</style>
