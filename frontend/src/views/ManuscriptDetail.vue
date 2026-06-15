<template>
  <div class="detail-page" v-loading="loading">
    <div class="detail-header" v-if="manuscript">
      <el-page-header @back="$router.back()" content="返回列表">
        <template #extra>
          <el-button :type="rhythmPanelVisible ? 'success' : 'primary'" @click="toggleRhythmPanel">
            <el-icon><Tickets /></el-icon>
            节奏板
          </el-button>
          <el-button :type="isFavorited ? 'success' : 'primary'" @click="toggleFavorite">
            <el-icon><Star :fill="isFavorited ? '#67c23a' : 'none'" /></el-icon>
            {{ isFavorited ? '已收藏' : '收藏' }}
          </el-button>
          <el-button type="primary" @click="showNoteDialog = true">
            <el-icon><EditPen /></el-icon>
            记录笔记
          </el-button>
        </template>
      </el-page-header>
    </div>

    <div class="main-content" v-if="manuscript">
      <div class="rhythm-panel" :class="{ collapsed: !rhythmPanelVisible }">
        <div class="rhythm-header">
          <span class="rhythm-title">朗读节奏板</span>
          <el-button type="text" size="small" @click="rhythmPanelVisible = false" class="collapse-btn">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
        </div>
        <div class="rhythm-list">
          <div
            class="rhythm-item"
            v-for="(section, index) in paragraphSections"
            :key="index"
            :class="{ active: activeParagraphIndex === index }"
            @click="scrollToParagraph(index)"
          >
            <div class="rhythm-item-header">
              <span class="paragraph-num">第 {{ index + 1 }} 段</span>
              <span :class="['status-badge', 'status-' + getParagraphStatus(index)]">{{ getStatusLabel(getParagraphStatus(index)) }}</span>
              <el-button type="text" size="small" @click.stop="openRhythmEditor(index)">
                <el-icon><Edit /></el-icon>
              </el-button>
            </div>
            <div class="rhythm-item-content">
              <div v-if="rhythmData[index]?.pause" class="rhythm-tag pause">
                <span class="tag-label">停顿</span>
                <span class="tag-value">{{ rhythmData[index].pause }}</span>
              </div>
              <div v-if="rhythmData[index]?.stress" class="rhythm-tag stress">
                <span class="tag-label">重音</span>
                <span class="tag-value">{{ rhythmData[index].stress }}</span>
              </div>
              <div v-if="rhythmData[index]?.speed" class="rhythm-tag speed">
                <span class="tag-label">语速</span>
                <span class="tag-value">{{ rhythmData[index].speed }}</span>
              </div>
              <div v-if="!hasRhythmData(index)" class="no-rhythm-tip">
                点击编辑添加节奏提示
              </div>
            </div>
            <div class="rhythm-item-preview">{{ section.content.slice(0, 30) }}{{ section.content.length > 30 ? '...' : '' }}</div>
          </div>
        </div>
      </div>

      <div class="rhythm-expand-btn" v-if="!rhythmPanelVisible" @click="rhythmPanelVisible = true">
        <el-icon><Tickets /></el-icon>
        <span>节奏板</span>
      </div>

      <div class="content-column">
        <div class="manuscript-content" :class="'type-' + manuscriptType">
          <div class="article-header">
            <h1 class="article-title">{{ manuscript.title }}</h1>
            <div class="article-meta">
              <el-tag type="primary" size="small">{{ manuscript.categoryName }}</el-tag>
              <el-tag size="small" :type="manuscriptType === 'poetry' ? 'warning' : 'info'">
                {{ manuscriptType === 'poetry' ? '古诗词' : '散文' }}
              </el-tag>
              <span v-if="manuscript.difficulty" class="meta-item">难度：{{ manuscript.difficulty }}</span>
              <span v-if="manuscript.author" class="meta-item">作者：{{ manuscript.author }}</span>
              <span class="meta-item">浏览：{{ manuscript.viewCount }}</span>
              <span class="meta-item">收藏：{{ manuscript.favoriteCount }}</span>
            </div>
            <div class="progress-stats" v-if="paragraphSections.length > 0">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
              </div>
              <div class="progress-info">
                <span class="progress-item mastered">
                  <span class="progress-dot"></span>
                  已熟练 {{ progressStats.mastered }}
                </span>
                <span class="progress-item strengthen">
                  <span class="progress-dot"></span>
                  需加强 {{ progressStats.strengthen }}
                </span>
                <span class="progress-item skip">
                  <span class="progress-dot"></span>
                  暂不练 {{ progressStats.skip }}
                </span>
                <span class="progress-item total">
                  共 {{ paragraphSections.length }} 段
                </span>
              </div>
            </div>
            <p v-if="manuscript.introduction" class="introduction">{{ manuscript.introduction }}</p>
          </div>

          <div class="article-body">
            <div
              class="content-section"
              v-for="(section, index) in contentSections"
              :key="index"
              :id="'paragraph-' + index"
              :class="{
                'paragraph-highlight': isParagraphActive(index),
                'paragraph-status-mastered': section.type === 'paragraph' && getParagraphStatus(getParagraphIndex(index)) === 'mastered',
                'paragraph-status-strengthen': section.type === 'paragraph' && getParagraphStatus(getParagraphIndex(index)) === 'strengthen',
                'paragraph-status-skip': section.type === 'paragraph' && getParagraphStatus(getParagraphIndex(index)) === 'skip'
              }"
              @mouseenter="onParagraphHover(index)"
              @mouseleave="onParagraphLeave"
            >
              <div class="paragraph-wrapper" v-if="section.type === 'paragraph'">
                <div class="paragraph-status-bar">
                  <span class="status-label">第 {{ getParagraphIndex(index) + 1 }} 段</span>
                  <div class="status-actions">
                    <el-button
                      :type="getParagraphStatus(getParagraphIndex(index)) === 'mastered' ? 'success' : 'default'"
                      size="small"
                      @click.stop="setParagraphStatus(getParagraphIndex(index), 'mastered')"
                    >
                      <el-icon><Check /></el-icon>
                      已熟练
                    </el-button>
                    <el-button
                      :type="getParagraphStatus(getParagraphIndex(index)) === 'strengthen' ? 'warning' : 'default'"
                      size="small"
                      @click.stop="setParagraphStatus(getParagraphIndex(index), 'strengthen')"
                    >
                      <el-icon><Warning /></el-icon>
                      需加强
                    </el-button>
                    <el-button
                      :type="getParagraphStatus(getParagraphIndex(index)) === 'skip' ? 'info' : 'default'"
                      size="small"
                      @click.stop="setParagraphStatus(getParagraphIndex(index), 'skip')"
                    >
                      <el-icon><Clock /></el-icon>
                      暂不练
                    </el-button>
                  </div>
                </div>
                <div
                  class="paragraph"
                  :style="{ marginBottom: getParagraphMargin(getParagraphIndex(index)) }"
                >{{ section.content }}</div>
              </div>
              <div v-else-if="section.type === 'heading'" class="section-heading">{{ section.content }}</div>
              <div v-if="section.type === 'paragraph' && rhythmData[getParagraphIndex(index)]?.note" class="rhythm-note-inline">
                💡 {{ rhythmData[getParagraphIndex(index)].note }}
              </div>
            </div>
          </div>
        </div>

        <div class="notes-section" v-if="manuscript && notes.length > 0">
          <h3 class="section-title">大家的练习笔记</h3>
          <el-card v-for="note in notes" :key="note.id" class="note-card">
            <div class="note-content">
              <div v-if="note.difficultyPoints" class="note-item">
                <span class="note-label">难点要点：</span>
                <span class="note-text">{{ note.difficultyPoints }}</span>
              </div>
              <div v-if="note.toneControl" class="note-item">
                <span class="note-label">语气把控：</span>
                <span class="note-text">{{ note.toneControl }}</span>
              </div>
              <div v-if="note.emotionExpression" class="note-item">
                <span class="note-label">情感表达：</span>
                <span class="note-text">{{ note.emotionExpression }}</span>
              </div>
              <div v-if="note.otherNotes" class="note-item">
                <span class="note-label">其他心得：</span>
                <span class="note-text">{{ note.otherNotes }}</span>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <el-dialog v-model="showNoteDialog" title="记录练习笔记" width="600px">
      <el-form :model="noteForm" label-width="100px">
        <el-form-item label="难点要点">
          <el-input v-model="noteForm.difficultyPoints" type="textarea" :rows="2" placeholder="记录练习中的难点要点" />
        </el-form-item>
        <el-form-item label="语气把控">
          <el-input v-model="noteForm.toneControl" type="textarea" :rows="2" placeholder="记录语气把控要点" />
        </el-form-item>
        <el-form-item label="情感表达">
          <el-input v-model="noteForm.emotionExpression" type="textarea" :rows="2" placeholder="记录情感表达要点" />
        </el-form-item>
        <el-form-item label="其他心得">
          <el-input v-model="noteForm.otherNotes" type="textarea" :rows="2" placeholder="其他练习心得" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showNoteDialog = false">取消</el-button>
        <el-button type="primary" @click="saveNote">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showRhythmEditor" title="编辑段落节奏" width="500px">
      <el-form :model="rhythmForm" label-width="80px">
        <el-form-item label="停顿">
          <el-select v-model="rhythmForm.pause" placeholder="选择停顿方式" clearable style="width: 100%">
            <el-option v-for="item in pauseOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="重音">
          <el-select v-model="rhythmForm.stress" placeholder="选择重音方式" clearable style="width: 100%">
            <el-option v-for="item in stressOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="语速">
          <el-select v-model="rhythmForm.speed" placeholder="选择语速" clearable style="width: 100%">
            <el-option v-for="item in speedOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="rhythmForm.note" type="textarea" :rows="3" placeholder="输入节奏备注提示" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRhythmEditor = false">取消</el-button>
        <el-button type="primary" @click="saveRhythmData">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Tickets, ArrowLeft, Edit, Check, Warning, Clock } from '@element-plus/icons-vue'
import { getManuscriptDetail, addFavorite, removeFavorite, checkFavorite, getManuscriptNotes, saveNote as saveNoteApi, saveParagraphProgress, getParagraphProgress } from '@/api'
import { getCurrentUserId, getRhythm, saveRhythm, getProgress, saveProgress } from '@/utils/storage'
import { splitContentSections, getParagraphSections, getParagraphIndex as calcParagraphIndex } from '@/utils/manuscript'

const route = useRoute()
const manuscript = ref(null)
const loading = ref(false)
const isFavorited = ref(false)
const notes = ref([])
const showNoteDialog = ref(false)
const noteForm = ref({
  difficultyPoints: '',
  toneControl: '',
  emotionExpression: '',
  otherNotes: ''
})

const userId = getCurrentUserId()

const rhythmPanelVisible = ref(true)
const rhythmData = ref({})
const activeParagraphIndex = ref(-1)
const showRhythmEditor = ref(false)
const editingParagraphIndex = ref(-1)
const rhythmForm = ref({
  pause: '',
  stress: '',
  speed: '',
  note: ''
})

const paragraphProgress = ref({})

const statusOptions = [
  { value: 'mastered', label: '已熟练', type: 'success' },
  { value: 'strengthen', label: '需加强', type: 'warning' },
  { value: 'skip', label: '暂不练', type: 'info' }
]

const pauseOptions = ['短停', '中停', '长停', '换气', '停顿强调']
const stressOptions = ['重音', '轻读', '拖音', '颤音', '气声']
const speedOptions = ['慢速', '稍慢', '正常', '稍快', '快速']

const manuscriptType = computed(() => {
  if (!manuscript.value) return 'prose'
  const categoryName = manuscript.value.categoryName || ''
  const content = manuscript.value.content || ''
  
  const poetryKeywords = ['诗', '词', '曲', '赋', '古诗', '唐诗', '宋词', '元曲', '绝句', '律诗', '乐府']
  const isPoetryCategory = poetryKeywords.some(kw => categoryName.includes(kw))
  
  if (isPoetryCategory) return 'poetry'
  
  const lines = content.split(/\n+/).filter(l => l.trim())
  const shortLineCount = lines.filter(l => l.trim().length <= 12).length
  const isPoetryContent = lines.length >= 4 && shortLineCount / lines.length >= 0.6
  
  return isPoetryContent ? 'poetry' : 'prose'
})

const pauseMarginMap = {
  '短停': '1em',
  '中停': '1.5em',
  '长停': '2.5em',
  '换气': '1.2em',
  '停顿强调': '2em'
}

const getParagraphMargin = (paraIndex) => {
  const data = rhythmData.value[paraIndex]
  if (data?.pause && pauseMarginMap[data.pause]) {
    return pauseMarginMap[data.pause]
  }
  return manuscriptType.value === 'poetry' ? '0.8em' : '1.5em'
}

const contentSections = computed(() => {
  return splitContentSections(manuscript.value?.content)
})

const paragraphSections = computed(() => {
  return getParagraphSections(manuscript.value?.content)
})

const getParagraphIndex = (sectionIndex) => {
  return calcParagraphIndex(contentSections.value, sectionIndex)
}

const hasRhythmData = (index) => {
  const data = rhythmData.value[index]
  return data && (data.pause || data.stress || data.speed || data.note)
}

const progressStats = computed(() => {
  const stats = { mastered: 0, strengthen: 0, skip: 0, total: paragraphSections.value.length }
  for (let i = 0; i < paragraphSections.value.length; i++) {
    const status = paragraphProgress.value[i]
    if (status === 'mastered') stats.mastered++
    else if (status === 'strengthen') stats.strengthen++
    else if (status === 'skip') stats.skip++
  }
  return stats
})

const progressPercent = computed(() => {
  const total = paragraphSections.value.length
  if (total === 0) return 0
  return Math.round((progressStats.value.mastered / total) * 100)
})

const getParagraphStatus = (index) => {
  return paragraphProgress.value[index] || ''
}

const getStatusLabel = (status) => {
  const map = {
    mastered: '已熟练',
    strengthen: '需加强',
    skip: '暂不练'
  }
  return map[status] || '未标记'
}

const setParagraphStatus = async (paraIndex, status) => {
  const currentStatus = paragraphProgress.value[paraIndex]
  if (currentStatus === status) {
    delete paragraphProgress.value[paraIndex]
  } else {
    paragraphProgress.value[paraIndex] = status
  }
  saveProgress(route.params.id, paragraphProgress.value)
  try {
    await saveParagraphProgress({
      userId,
      manuscriptId: route.params.id,
      paragraphIndex: paraIndex,
      status: paragraphProgress.value[paraIndex] || null
    })
  } catch (e) {
    console.error('保存段落进度失败', e)
  }
  ElMessage.success(currentStatus === status ? '已取消标记' : '状态已更新')
}

const isParagraphActive = (sectionIndex) => {
  if (contentSections.value[sectionIndex]?.type !== 'paragraph') return false
  const paraIndex = getParagraphIndex(sectionIndex)
  return activeParagraphIndex.value === paraIndex
}

const toggleRhythmPanel = () => {
  rhythmPanelVisible.value = !rhythmPanelVisible.value
}

const scrollToParagraph = (paraIndex) => {
  let sectionIndex = 0
  let paraCount = 0
  for (let i = 0; i < contentSections.value.length; i++) {
    if (contentSections.value[i].type === 'paragraph') {
      if (paraCount === paraIndex) {
        sectionIndex = i
        break
      }
      paraCount++
    }
  }
  const el = document.getElementById('paragraph-' + sectionIndex)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
    activeParagraphIndex.value = paraIndex
    setTimeout(() => {
      activeParagraphIndex.value = -1
    }, 2000)
  }
}

const onParagraphHover = (sectionIndex) => {
  if (contentSections.value[sectionIndex]?.type === 'paragraph') {
    activeParagraphIndex.value = getParagraphIndex(sectionIndex)
  }
}

const onParagraphLeave = () => {
  activeParagraphIndex.value = -1
}

const openRhythmEditor = (paraIndex) => {
  editingParagraphIndex.value = paraIndex
  const data = rhythmData.value[paraIndex] || {}
  rhythmForm.value = {
    pause: data.pause || '',
    stress: data.stress || '',
    speed: data.speed || '',
    note: data.note || ''
  }
  showRhythmEditor.value = true
}

const saveRhythmData = () => {
  const paraIndex = editingParagraphIndex.value
  if (rhythmForm.value.pause || rhythmForm.value.stress || rhythmForm.value.speed || rhythmForm.value.note) {
    rhythmData.value[paraIndex] = { ...rhythmForm.value }
  } else {
    delete rhythmData.value[paraIndex]
  }
  saveRhythm(route.params.id, rhythmData.value)
  showRhythmEditor.value = false
  ElMessage.success('节奏提示已保存')
}

const loadRhythmData = () => {
  const data = getRhythm(route.params.id)
  if (data) {
    rhythmData.value = data
  }
}

const loadProgressData = async () => {
  const localData = getProgress(route.params.id)
  if (localData) {
    paragraphProgress.value = localData
  }
  try {
    const serverData = await getParagraphProgress(userId, route.params.id)
    if (serverData) {
      paragraphProgress.value = serverData
      saveProgress(route.params.id, serverData)
    }
  } catch (e) {
    console.error('加载段落进度失败', e)
  }
}

const loadDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    manuscript.value = await getManuscriptDetail(id)
    await Promise.all([
      checkFavoriteStatus(),
      loadNotes()
    ])
    loadRhythmData()
    loadProgressData()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const checkFavoriteStatus = async () => {
  try {
    isFavorited.value = await checkFavorite(userId, route.params.id)
  } catch (e) {
    console.error(e)
  }
}

const toggleFavorite = async () => {
  try {
    if (isFavorited.value) {
      await removeFavorite(userId, route.params.id)
      isFavorited.value = false
      manuscript.value.favoriteCount--
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(userId, route.params.id)
      isFavorited.value = true
      manuscript.value.favoriteCount++
      ElMessage.success('收藏成功')
    }
  } catch (e) {
    console.error(e)
  }
}

const loadNotes = async () => {
  try {
    notes.value = await getManuscriptNotes(route.params.id)
  } catch (e) {
    console.error(e)
  }
}

const saveNote = async () => {
  try {
    await saveNoteApi({
      userId,
      manuscriptId: route.params.id,
      ...noteForm.value
    })
    ElMessage.success('笔记保存成功')
    showNoteDialog.value = false
    loadNotes()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.manuscript-content {
  --prose-font-size: 17px;
  --prose-line-height: 2.2;
  --prose-letter-spacing: 0.05em;
  --prose-max-width: 720px;
  --prose-padding-x: 60px;
  
  --poetry-font-size: 18px;
  --poetry-line-height: 1.8;
  --poetry-letter-spacing: 0.1em;
  --poetry-max-width: 600px;
  --poetry-padding-x: 80px;
}

.article-header {
  text-align: center;
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid #ebeef5;
}

.article-title {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 20px;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 20px;
  align-items: center;
}

.meta-item {
  font-size: 14px;
  color: #909399;
}

.introduction {
  font-size: 15px;
  color: #606266;
  line-height: 1.8;
  padding: 16px 24px;
  background: #f5f7fa;
  border-radius: 8px;
  font-style: italic;
  max-width: var(--prose-max-width);
  margin: 0 auto;
}

.progress-stats {
  margin: 20px auto 0;
  max-width: var(--prose-max-width);
}

.progress-bar {
  height: 8px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 12px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-info {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.progress-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.progress-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.progress-item.mastered .progress-dot {
  background: #67c23a;
}

.progress-item.strengthen .progress-dot {
  background: #e6a23c;
}

.progress-item.skip .progress-dot {
  background: #909399;
}

.progress-item.total {
  color: #909399;
}

.type-prose .article-body {
  font-size: var(--prose-font-size);
  line-height: var(--prose-line-height);
  letter-spacing: var(--prose-letter-spacing);
  color: #303133;
  max-width: var(--prose-max-width);
  margin: 0 auto;
  padding: 0 var(--prose-padding-x);
}

.type-poetry .article-body {
  font-size: var(--poetry-font-size);
  line-height: var(--poetry-line-height);
  letter-spacing: var(--poetry-letter-spacing);
  color: #303133;
  max-width: var(--poetry-max-width);
  margin: 0 auto;
  padding: 0 var(--poetry-padding-x);
  text-align: center;
}

.type-prose .paragraph {
  text-indent: 2em;
  text-align: justify;
}

.type-poetry .paragraph {
  text-indent: 0;
  text-align: center;
}

.section-heading {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
  margin: 32px 0 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #ecf5ff;
  text-align: left;
  text-indent: 0;
  letter-spacing: 0;
}

.notes-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 20px;
  margin-bottom: 16px;
  color: #303133;
}

.note-card {
  margin-bottom: 16px;
}

.note-item {
  margin-bottom: 12px;
  line-height: 1.8;
}

.note-label {
  font-weight: 600;
  color: #606266;
}

.note-text {
  color: #303133;
}

.main-content {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.content-column {
  flex: 1;
  min-width: 0;
}

.rhythm-panel {
  width: 280px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  position: sticky;
  top: 20px;
  max-height: calc(100vh - 40px);
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.rhythm-panel.collapsed {
  width: 0;
  opacity: 0;
  overflow: hidden;
}

.rhythm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.rhythm-title {
  font-size: 16px;
  font-weight: 600;
}

.collapse-btn {
  color: #fff !important;
  padding: 4px;
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.rhythm-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.rhythm-item {
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
  background: #f5f7fa;
}

.rhythm-item:hover {
  background: #ecf5ff;
  border-color: #b3d8ff;
}

.rhythm-item.active {
  background: #ecf5ff;
  border-color: #409eff;
}

.rhythm-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.paragraph-num {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.status-badge {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.status-badge.status-mastered {
  background: #f0f9eb;
  color: #67c23a;
}

.status-badge.status-strengthen {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-badge.status-skip {
  background: #f4f4f5;
  color: #909399;
}

.status-badge.status- {
  display: none;
}

.rhythm-item-content {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.rhythm-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.rhythm-tag.pause {
  background: #fdf6ec;
  color: #e6a23c;
}

.rhythm-tag.stress {
  background: #fef0f0;
  color: #f56c6c;
}

.rhythm-tag.speed {
  background: #f0f9eb;
  color: #67c23a;
}

.tag-label {
  opacity: 0.8;
}

.tag-value {
  font-weight: 600;
}

.no-rhythm-tip {
  font-size: 12px;
  color: #c0c4cc;
  font-style: italic;
}

.rhythm-item-preview {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.manuscript-content {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  margin-bottom: 32px;
}

.content-section {
  transition: all 0.3s ease;
  border-radius: 8px;
  padding: 4px 8px;
  margin: 0 -8px;
}

.paragraph-highlight {
  background: #ecf5ff;
  box-shadow: 0 0 0 2px #409eff;
}

.paragraph-wrapper {
  position: relative;
}

.paragraph-status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding: 4px 0;
  opacity: 0.7;
  transform: translateY(0);
  transition: all 0.2s ease;
}

.content-section:hover .paragraph-status-bar {
  opacity: 1;
}

@media (hover: none) and (pointer: coarse) {
  .paragraph-status-bar {
    opacity: 1;
  }
}

.status-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.status-actions {
  display: flex;
  gap: 6px;
}

.paragraph-status-mastered {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.08) 0%, rgba(133, 206, 97, 0.05) 100%);
  border-left: 3px solid #67c23a;
  padding-left: 12px !important;
  margin-left: -12px !important;
  border-radius: 4px;
}

.paragraph-status-strengthen {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.08) 0%, rgba(245, 205, 135, 0.05) 100%);
  border-left: 3px solid #e6a23c;
  padding-left: 12px !important;
  margin-left: -12px !important;
  border-radius: 4px;
}

.paragraph-status-skip {
  background: linear-gradient(135deg, rgba(144, 147, 153, 0.06) 0%, rgba(192, 196, 204, 0.04) 100%);
  border-left: 3px solid #909399;
  padding-left: 12px !important;
  margin-left: -12px !important;
  border-radius: 4px;
  opacity: 0.6;
}

.rhythm-note-inline {
  margin: 8px 0 20px;
  padding: 12px 16px;
  background: #fdf6ec;
  border-left: 4px solid #e6a23c;
  border-radius: 4px;
  font-size: 14px;
  color: #b88230;
  line-height: 1.6;
}

.rhythm-expand-btn {
  position: fixed;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 16px 8px;
  border-radius: 0 8px 8px 0;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  writing-mode: vertical-rl;
  z-index: 100;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  transition: all 0.2s ease;
}

.rhythm-expand-btn:hover {
  padding-right: 12px;
}

.detail-page {
  max-width: 1280px;
  margin: 0 auto;
}

.detail-header {
  margin-bottom: 24px;
}
</style>
