<template>
  <div class="notes-page">
    <h2 class="page-title">我的练习笔记</h2>
    
    <div class="list-content" v-loading="loading">
      <el-empty v-if="!loading && list.length === 0" description="暂无练习笔记" />
      <el-card v-for="note in list" :key="note.id" class="note-card">
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
        <div class="note-body">
          <div class="note-grid">
            <div v-if="note.difficultyPoints" class="note-item">
              <div class="item-label">难点要点</div>
              <div class="item-content">{{ note.difficultyPoints }}</div>
            </div>
            <div v-if="note.toneControl" class="note-item">
              <div class="item-label">语气把控</div>
              <div class="item-content">{{ note.toneControl }}</div>
            </div>
            <div v-if="note.emotionExpression" class="note-item">
              <div class="item-label">情感表达</div>
              <div class="item-content">{{ note.emotionExpression }}</div>
            </div>
            <div v-if="note.otherNotes" class="note-item">
              <div class="item-label">其他心得</div>
              <div class="item-content">{{ note.otherNotes }}</div>
            </div>
          </div>
          <div class="note-time">
            更新于：{{ formatTime(note.updateTime) }}
          </div>
        </div>
      </el-card>
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

    <el-dialog v-model="showEditDialog" title="编辑笔记" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="难点要点">
          <el-input v-model="editForm.difficultyPoints" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="语气把控">
          <el-input v-model="editForm.toneControl" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="情感表达">
          <el-input v-model="editForm.emotionExpression" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="其他心得">
          <el-input v-model="editForm.otherNotes" type="textarea" :rows="2" />
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
import { getUserNotes, deleteNote as apiDeleteNote, saveNote, getManuscriptById } from '@/api'
import { getCurrentUserId } from '@/utils/storage'

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
    await apiDeleteNote(id)
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
  max-width: 1000px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  margin-bottom: 24px;
  color: #303133;
}

.note-card {
  margin-bottom: 20px;
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

.note-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.note-item {
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 6px;
}

.item-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.item-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}

.note-time {
  font-size: 12px;
  color: #c0c4cc;
  text-align: right;
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
