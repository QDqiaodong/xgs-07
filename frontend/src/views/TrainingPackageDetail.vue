<template>
  <div class="training-package-detail-page" v-loading="loading">
    <div class="back-bar">
      <el-page-header @back="$router.back()" content="返回训练包列表" />
    </div>

    <div v-if="packageData" class="detail-content">
      <div class="package-hero" :style="getCoverStyle(packageData)">
        <div class="hero-content">
          <div class="hero-tags">
            <DifficultyBadge v-if="packageData.difficulty" :difficulty="packageData.difficulty" />
            <el-tag v-if="packageData.categoryName" type="info" effect="dark">{{ packageData.categoryName }}</el-tag>
          </div>
          <h1 class="hero-title">{{ packageData.name }}</h1>
          <p class="hero-desc">{{ packageData.description }}</p>
          <div class="hero-stats">
            <div class="stat-item">
              <el-icon><Document /></el-icon>
              <span class="stat-num">{{ packageData.items?.length || 0 }}</span>
              <span class="stat-label">篇文稿</span>
            </div>
            <div class="stat-item" v-if="packageData.targetDays">
              <el-icon><Calendar /></el-icon>
              <span class="stat-num">{{ packageData.targetDays }}</span>
              <span class="stat-label">目标天数</span>
            </div>
            <div class="stat-item" v-if="packageData.targetDuration">
              <el-icon><Timer /></el-icon>
              <span class="stat-num">{{ packageData.targetDuration }}</span>
              <span class="stat-label">分钟/次</span>
            </div>
          </div>
        </div>
        <div class="hero-progress-card" v-if="progress">
          <div class="progress-card-header">
            <span class="progress-card-title">我的训练进度</span>
            <el-tag :type="getProgressTagType(progress.status)" effect="light">
              {{ getProgressStatusLabel(progress.status) }}
            </el-tag>
          </div>
          <el-progress
            :percentage="Math.round(progress.completionPercent || 0)"
            :status="getProgressElStatus(progress.status)"
            :stroke-width="10"
            :show-text="true"
          />
          <div class="progress-detail">
            <div class="pd-row">
              <span>文稿完成</span>
              <span class="pd-num">{{ progress.completedManuscripts || 0 }} / {{ progress.totalManuscripts || packageData.items?.length || 0 }}</span>
            </div>
            <div class="pd-row" v-if="progress.totalPracticeCount">
              <span>累计练习</span>
              <span class="pd-num">{{ progress.totalPracticeCount }} 次</span>
            </div>
            <div class="pd-row" v-if="progress.totalPracticeMinutes">
              <span>累计时长</span>
              <span class="pd-num">{{ progress.totalPracticeMinutes }} 分钟</span>
            </div>
            <div class="pd-row" v-if="progress.startDate">
              <span>开始日期</span>
              <span class="pd-num">{{ progress.startDate }}</span>
            </div>
          </div>
          <div class="progress-actions">
            <el-button v-if="progress.status !== 'completed'" type="primary"
              @click="goCurrentManuscript" :icon="VideoPlay">
              {{ progress.status === 'not_started' ? '开始训练' : '继续训练' }}
            </el-button>
            <el-button v-else type="success" :icon="CircleCheck" disabled>
              训练完成
            </el-button>
            <el-button plain @click="recordPractice" :icon="Timer">
              记录练习
            </el-button>
          </div>
        </div>
        <div class="hero-progress-card" v-else>
          <div class="progress-card-header">
            <span class="progress-card-title">开始你的训练</span>
          </div>
          <p class="empty-progress-tip">系统化的训练计划，帮助你循序渐进地提升朗诵水平</p>
          <el-button type="primary" size="large" @click="startTraining" :icon="VideoPlay">
            立即开始训练
          </el-button>
        </div>
      </div>

      <div class="goals-section" v-if="getGoals().length > 0">
        <h2 class="section-heading">
          <el-icon><Flag /></el-icon>
          训练目标
        </h2>
        <div class="goals-list">
          <div v-for="(goal, idx) in getGoals()" :key="idx" class="goal-item">
            <span class="goal-index">{{ idx + 1 }}</span>
            <span class="goal-text">{{ goal }}</span>
          </div>
        </div>
      </div>

      <div class="goals-section" v-if="packageData.tips">
        <h2 class="section-heading">
          <el-icon><MagicStick /></el-icon>
          训练提示
        </h2>
        <div class="tips-box">
          {{ packageData.tips }}
        </div>
      </div>

      <div class="manuscripts-section">
        <h2 class="section-heading">
          <el-icon><Reading /></el-icon>
          训练文稿序列
          <span class="heading-sub">共 {{ packageData.items?.length || 0 }} 篇，按顺序练习效果更佳</span>
        </h2>

        <div class="manuscripts-timeline">
          <div
            class="ms-item"
            v-for="(item, idx) in packageData.items"
            :key="item.id"
            :class="getItemClass(idx)"
          >
            <div class="ms-timeline">
              <div class="ms-node" :class="getNodeClass(idx)">
                <span v-if="isCompleted(idx)"><Check /></span>
                <span v-else-if="isCurrent(idx)" class="pulse-dot"></span>
                <span v-else>{{ idx + 1 }}</span>
              </div>
              <div class="ms-connector" v-if="idx < (packageData.items?.length || 0) - 1"></div>
            </div>
            <div class="ms-content" @click="goManuscript(item.manuscriptId)">
              <div class="ms-header">
                <h3 class="ms-title">{{ item.manuscriptTitle || '文稿 ' + (idx + 1) }}</h3>
                <div class="ms-stage" v-if="item.stage" :class="'stage-' + getStageKey(item.stage)">
                  <el-icon><Promotion /></el-icon>
                  {{ item.stage }}
                </div>
              </div>
              <div class="ms-meta">
                <span v-if="item.manuscriptAuthor" class="ms-meta-item">
                  <el-icon><User /></el-icon>
                  {{ item.manuscriptAuthor }}
                </span>
                <span v-if="item.manuscriptDifficulty" class="ms-meta-item">
                  <DifficultyBadge :difficulty="item.manuscriptDifficulty" size="small" />
                </span>
              </div>
              <div class="ms-note" v-if="item.stageNote">
                <el-icon><InfoFilled /></el-icon>
                {{ item.stageNote }}
              </div>
              <div class="ms-status-bar" v-if="progress">
                <span :class="['ms-status', getManuscriptStatusClass(idx)]">
                  {{ getManuscriptStatusLabel(idx) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Document, Calendar, Timer, VideoPlay, CircleCheck, Flag, MagicStick, Reading,
  Check, Promotion, User, InfoFilled
} from '@element-plus/icons-vue'
import { getTrainingPackageWithProgress, startTrainingPackage, recordTrainingPractice, getParagraphProgress } from '@/api'
import { getCurrentUserId } from '@/utils/storage'
import DifficultyBadge from '@/components/DifficultyBadge.vue'

const route = useRoute()
const router = useRouter()
const userId = getCurrentUserId()

const loading = ref(false)
const packageData = ref(null)
const progress = ref(null)
const manuscriptProgressMap = ref({})

const loadDetail = async () => {
  loading.value = true
  try {
    const data = await getTrainingPackageWithProgress(route.params.id, userId)
    packageData.value = data.package
    progress.value = data.progress
    if (packageData.value?.items) {
      for (const item of packageData.value.items) {
        try {
          const pg = await getParagraphProgress(userId, item.manuscriptId)
          manuscriptProgressMap.value[item.manuscriptId] = pg || {}
        } catch {}
      }
    }
  } catch (e) {
    console.error('加载训练包详情失败', e)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const getGoals = () => {
  try {
    if (!packageData.value?.trainingGoals) return []
    const g = JSON.parse(packageData.value.trainingGoals)
    return Array.isArray(g) ? g : []
  } catch {
    return []
  }
}

const coverColors = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
]

const getCoverStyle = (pkg) => {
  if (pkg.coverImage) {
    return { backgroundImage: `url(${pkg.coverImage})`, backgroundSize: 'cover', backgroundPosition: 'center' }
  }
  const idx = (pkg.id || 0) % coverColors.length
  return { background: coverColors[idx] }
}

const getProgressStatusLabel = (status) => {
  return { 'not_started': '未开始', 'in_progress': '进行中', 'completed': '已完成' }[status] || '未开始'
}

const getProgressTagType = (status) => {
  return { 'not_started': 'info', 'in_progress': 'primary', 'completed': 'success' }[status] || 'info'
}

const getProgressElStatus = (status) => {
  if (status === 'completed') return 'success'
  if (status === 'in_progress') return ''
  return 'exception'
}

const startTraining = async () => {
  try {
    progress.value = await startTrainingPackage(route.params.id, userId)
    ElMessage.success('训练已开始，加油！')
  } catch (e) {
    console.error(e)
    ElMessage.error('开始训练失败')
  }
}

const recordPractice = async () => {
  try {
    progress.value = await recordTrainingPractice(route.params.id, userId, 15)
    ElMessage.success('已记录练习 15 分钟')
  } catch (e) {
    console.error(e)
    ElMessage.error('记录失败')
  }
}

const goCurrentManuscript = () => {
  if (!packageData.value?.items?.length) return
  const idx = Math.min(progress.value?.currentManuscriptIndex || 0, packageData.value.items.length - 1)
  goManuscript(packageData.value.items[idx].manuscriptId)
}

const goManuscript = (id) => {
  if (!id) return
  router.push(`/manuscript/${id}`)
}

const manuscriptMasteredMap = computed(() => {
  const map = {}
  packageData.value?.items?.forEach(item => {
    const pg = manuscriptProgressMap.value[item.manuscriptId]
    if (pg && Object.keys(pg).length > 0) {
      const values = Object.values(pg)
      const mastered = values.filter(v => v === 'mastered').length
      map[item.manuscriptId] = {
        mastered,
        total: values.length,
        percent: values.length > 0 ? (mastered / values.length) : 0
      }
    } else {
      map[item.manuscriptId] = { mastered: 0, total: 0, percent: 0 }
    }
  })
  return map
})

const isCompleted = (idx) => {
  const item = packageData.value?.items?.[idx]
  if (!item) return false
  const info = manuscriptMasteredMap.value[item.manuscriptId]
  return info && info.total > 0 && info.mastered >= info.total
}

const isCurrent = (idx) => {
  if (!progress.value) return idx === 0
  if (progress.value.status === 'completed') return false
  const curr = progress.value.currentManuscriptIndex || 0
  return idx === curr
}

const getItemClass = (idx) => {
  if (isCompleted(idx)) return 'item-completed'
  if (isCurrent(idx)) return 'item-current'
  return 'item-pending'
}

const getNodeClass = (idx) => {
  if (isCompleted(idx)) return 'node-completed'
  if (isCurrent(idx)) return 'node-current'
  return 'node-pending'
}

const getStageKey = (stage) => {
  const map = { '铺垫': 'intro', '发展': 'dev', '高潮': 'climax', '收束': 'end' }
  return map[stage] || 'intro'
}

const getManuscriptStatusClass = (idx) => {
  const item = packageData.value?.items?.[idx]
  if (!item) return ''
  const info = manuscriptMasteredMap.value[item.manuscriptId]
  if (!info || info.total === 0) return 'status-pending'
  if (info.mastered >= info.total) return 'status-mastered'
  return 'status-training'
}

const getManuscriptStatusLabel = (idx) => {
  const item = packageData.value?.items?.[idx]
  if (!item) return ''
  const info = manuscriptMasteredMap.value[item.manuscriptId]
  if (!info || info.total === 0) return '未开始'
  if (info.mastered >= info.total) return '已熟练'
  return `练习中 ${info.mastered}/${info.total}段`
}

onMounted(loadDetail)
</script>

<style scoped>
.training-package-detail-page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 20px;
}
.back-bar {
  margin-bottom: 16px;
  background: #fff;
  padding: 10px 16px;
  border-radius: 10px;
}

.package-hero {
  border-radius: 20px;
  padding: 36px 40px;
  position: relative;
  overflow: hidden;
  display: flex;
  gap: 32px;
  align-items: flex-start;
  margin-bottom: 28px;
  color: #fff;
  min-height: 220px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}
.package-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(0,0,0,0.15) 0%, rgba(0,0,0,0.35) 100%);
  z-index: 0;
}
.hero-content {
  flex: 1;
  position: relative;
  z-index: 1;
}
.hero-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}
.hero-title {
  font-size: 30px;
  font-weight: 700;
  margin: 0 0 10px;
  line-height: 1.3;
  text-shadow: 0 2px 8px rgba(0,0,0,0.2);
}
.hero-desc {
  font-size: 15px;
  opacity: 0.92;
  margin: 0 0 20px;
  line-height: 1.7;
  max-width: 560px;
}
.hero-stats {
  display: flex;
  gap: 28px;
  flex-wrap: wrap;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255,255,255,0.18);
  backdrop-filter: blur(6px);
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
}
.stat-item .el-icon { font-size: 16px; }
.stat-num {
  font-size: 18px;
  font-weight: 700;
  font-family: 'Courier New', monospace;
}
.stat-label { opacity: 0.9; }

.hero-progress-card {
  width: 280px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 14px;
  padding: 18px;
  position: relative;
  z-index: 1;
  box-shadow: 0 6px 18px rgba(0,0,0,0.15);
  color: #303133;
}
.progress-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.progress-card-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.empty-progress-tip {
  font-size: 13px;
  color: #909399;
  margin: 0 0 16px;
  line-height: 1.6;
}
.progress-detail {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed #ebeef5;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.pd-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #606266;
}
.pd-num {
  font-weight: 600;
  color: #303133;
  font-family: 'Courier New', monospace;
}
.progress-actions {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-heading {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px;
}
.section-heading .el-icon { color: #667eea; }
.heading-sub {
  font-size: 13px;
  font-weight: 400;
  color: #909399;
  margin-left: 6px;
}

.goals-section, .manuscripts-section {
  background: #fff;
  border-radius: 14px;
  padding: 24px 28px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.goals-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
}
.goal-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border-radius: 10px;
  transition: all 0.2s;
}
.goal-item:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
.goal-index {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}
.goal-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  padding-top: 2px;
}
.tips-box {
  padding: 16px 20px;
  background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
  border-left: 4px solid #faad14;
  border-radius: 0 10px 10px 0;
  font-size: 14px;
  color: #614700;
  line-height: 1.8;
}

.manuscripts-timeline {
  padding-left: 8px;
}
.ms-item {
  display: flex;
  gap: 14px;
  margin-bottom: 16px;
  position: relative;
}
.ms-timeline {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 36px;
  flex-shrink: 0;
}
.ms-node {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  z-index: 2;
  transition: all 0.3s;
}
.ms-node.node-pending {
  background: #f4f4f5;
  color: #909399;
  border: 2px solid #dcdfe6;
}
.ms-node.node-current {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: 2px solid #667eea;
  box-shadow: 0 0 0 4px rgba(102,126,234,0.15);
}
.ms-node.node-completed {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: #fff;
  border: 2px solid #67c23a;
}
.pulse-dot {
  width: 10px;
  height: 10px;
  background: #fff;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.7); }
}
.ms-connector {
  flex: 1;
  width: 3px;
  background: #e4e7ed;
  margin: 4px 0;
  min-height: 20px;
}
.item-completed .ms-connector {
  background: linear-gradient(180deg, #67c23a 0%, #85ce61 100%);
}
.item-current .ms-connector {
  background: linear-gradient(180deg, #667eea 0%, #e4e7ed 100%);
}

.ms-content {
  flex: 1;
  background: #fafbfc;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px 18px;
  cursor: pointer;
  transition: all 0.25s ease;
}
.ms-content:hover {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102,126,234,0.1);
  transform: translateX(4px);
}
.item-completed .ms-content {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
  border-color: #c2e7b0;
}
.item-current .ms-content {
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
  border-color: #a0cfff;
}
.ms-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  gap: 10px;
}
.ms-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  line-height: 1.4;
}
.ms-stage {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
  padding: 3px 10px;
  border-radius: 12px;
  flex-shrink: 0;
}
.ms-stage .el-icon { font-size: 12px; }
.ms-stage.stage-intro { background: #f4f4f5; color: #606266; }
.ms-stage.stage-dev { background: #ecf5ff; color: #409eff; }
.ms-stage.stage-climax { background: #fef0f0; color: #f56c6c; }
.ms-stage.stage-end { background: #f0f9eb; color: #67c23a; }

.ms-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.ms-meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}
.ms-note {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 8px 12px;
  background: rgba(102,126,234,0.08);
  border-radius: 8px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}
.ms-note .el-icon {
  color: #667eea;
  flex-shrink: 0;
  margin-top: 2px;
  font-size: 14px;
}
.ms-status-bar {
  padding-top: 6px;
}
.ms-status {
  font-size: 12px;
  font-weight: 500;
  padding: 3px 10px;
  border-radius: 10px;
}
.ms-status.status-pending { background: #f4f4f5; color: #909399; }
.ms-status.status-training { background: #ecf5ff; color: #409eff; }
.ms-status.status-mastered { background: #f0f9eb; color: #67c23a; }
</style>
