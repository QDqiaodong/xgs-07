<template>
  <div class="detail-page" v-loading="loading">
    <div class="detail-header" v-if="manuscript">
      <el-page-header @back="$router.back()" content="返回列表">
        <template #extra>
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

    <div class="manuscript-content" v-if="manuscript">
      <div class="article-header">
        <h1 class="article-title">{{ manuscript.title }}</h1>
        <div class="article-meta">
          <el-tag type="primary" size="small">{{ manuscript.categoryName }}</el-tag>
          <span v-if="manuscript.difficulty" class="meta-item">难度：{{ manuscript.difficulty }}</span>
          <span v-if="manuscript.author" class="meta-item">作者：{{ manuscript.author }}</span>
          <span class="meta-item">浏览：{{ manuscript.viewCount }}</span>
          <span class="meta-item">收藏：{{ manuscript.favoriteCount }}</span>
        </div>
        <p v-if="manuscript.introduction" class="introduction">{{ manuscript.introduction }}</p>
      </div>

      <div class="article-body">
        <div class="content-section" v-for="(section, index) in contentSections" :key="index">
          <div v-if="section.type === 'paragraph'" class="paragraph">{{ section.content }}</div>
          <div v-else-if="section.type === 'heading'" class="section-heading">{{ section.content }}</div>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getManuscriptDetail, addFavorite, removeFavorite, checkFavorite, getManuscriptNotes, saveNote as saveNoteApi } from '@/api'
import { getCurrentUserId } from '@/utils/storage'

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

const contentSections = computed(() => {
  if (!manuscript.value?.content) return []
  const lines = manuscript.value.content.split(/\n+/).filter(line => line.trim())
  const sections = []
  lines.forEach(line => {
    const trimmed = line.trim()
    if (trimmed.length < 15 && (trimmed.endsWith('：') || trimmed.endsWith(':') || /^[第零一二三四五六七八九十\d]+/.test(trimmed))) {
      sections.push({ type: 'heading', content: trimmed })
    } else {
      sections.push({ type: 'paragraph', content: trimmed })
    }
  })
  return sections
})

const loadDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    manuscript.value = await getManuscriptDetail(id)
    await Promise.all([
      checkFavoriteStatus(),
      loadNotes()
    ])
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
.detail-page {
  max-width: 900px;
  margin: 0 auto;
}

.detail-header {
  margin-bottom: 24px;
}

.manuscript-content {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  margin-bottom: 32px;
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
  gap: 20px;
  flex-wrap: wrap;
  margin-bottom: 20px;
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
}

.article-body {
  font-size: 17px;
  line-height: 2;
  color: #303133;
}

.paragraph {
  margin-bottom: 20px;
  text-indent: 2em;
}

.section-heading {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
  margin: 32px 0 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #ecf5ff;
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
</style>
