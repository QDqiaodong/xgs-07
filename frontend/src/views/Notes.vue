<template>
  <div class="notes-page">
    <h2 class="page-title">我的练习笔记</h2>
    
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
              <el-button type="danger" size="small" @click="deleteNoteItem(note.id)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
          
          <div class="progress-summary" v-if="note.paragraphProgress">
            <div class="progress-title">
              <el-icon><TrendCharts /></el-icon>
              段落掌握情况
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
          </div>
          
          <div class="note-time">
            更新于：{{ formatTime(note.updateTime) }}
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
          
          <div class="empty-sections-tip" v-if="!note.difficultyPoints && !note.toneControl && !note.emotionExpression && !note.otherNotes">
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
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Edit, Delete, TrendCharts, Warning, Microphone, MagicStick, EditPen, InfoFilled } from '@element-plus/icons-vue'
import { getUserNotes, deleteNote as apiDeleteNote, saveNote, getManuscriptById, getParagraphProgressList } from '@/api'
import { getCurrentUserId } from '@/utils/storage'
import { getParagraphCount, detectManuscriptType } from '@/utils/manuscript'

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
  otherNotes: ''
})

const userId = getCurrentUserId()

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getUserNotes({
      userId,
      page: page.value - 1,
      size: size.value
    })
    list.value = res.content
    total.value = res.totalElements
    
    for (let i = 0; i < list.value.length; i++) {
      try {
        const manuscript = await getManuscriptById(list.value[i].manuscriptId)
        list.value[i].manuscriptTitle = manuscript?.title
        const content = manuscript?.content || ''
        const categoryName = manuscript?.categoryName || ''
        const mType = detectManuscriptType(categoryName, content)
        const total = getParagraphCount(content, mType)
        
        try {
          const progressList = await getParagraphProgressList(userId, list.value[i].manuscriptId)
          const stats = { mastered: 0, strengthen: 0, skip: 0, total }
          if (progressList && progressList.length > 0) {
            progressList.forEach(p => {
              if (p.status === 'mastered') stats.mastered++
              else if (p.status === 'strengthen') stats.strengthen++
              else if (p.status === 'skip') stats.skip++
            })
          }
          stats.percent = total > 0 ? Math.round((stats.mastered / total) * 100) : 0
          list.value[i].paragraphProgress = stats
        } catch (e) {
          console.error('加载段落进度失败', e)
          list.value[i].paragraphProgress = { mastered: 0, strengthen: 0, skip: 0, total, percent: 0 }
        }
      } catch (e) {
        console.error(e)
      }
    }
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
    otherNotes: note.otherNotes || ''
  }
  showEditDialog.value = true
}

const saveEdit = async () => {
  try {
    await saveNote({
      userId,
      manuscriptId: editForm.value.manuscriptId,
      difficultyPoints: editForm.value.difficultyPoints,
      toneControl: editForm.value.toneControl,
      emotionExpression: editForm.value.emotionExpression,
      otherNotes: editForm.value.otherNotes
    })
    ElMessage.success('保存成功')
    showEditDialog.value = false
    loadList()
  } catch (e) {
    console.error(e)
  }
}

const deleteNoteItem = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条笔记吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await apiDeleteNote(id, userId)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

onMounted(() => {
  loadList()
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
</style>
