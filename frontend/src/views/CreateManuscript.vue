<template>
  <div class="create-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑文稿' : '录入新文稿' }}</span>
          <el-tag v-if="hasDraft" type="warning">有未保存的草稿</el-tag>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="文稿标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文稿标题" maxlength="200" show-word-limit />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="作者">
              <el-input v-model="form.author" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="难度">
              <el-select v-model="form.difficulty" placeholder="请选择难度" style="width: 100%">
                <el-option label="入门" value="入门" />
                <el-option label="初级" value="初级" />
                <el-option label="中级" value="中级" />
                <el-option label="高级" value="高级" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="简介">
          <el-input v-model="form.introduction" type="textarea" :rows="2" placeholder="请输入文稿简介（可选）" maxlength="500" show-word-limit />
        </el-form-item>

        <el-form-item label="文稿内容" prop="content">
          <div class="editor-toolbar">
            <el-button-group>
              <el-button size="small" @click="insertFormat('indent')">首行缩进</el-button>
              <el-button size="small" @click="insertFormat('paragraph')">分段</el-button>
              <el-button size="small" @click="clearFormat">清除格式</el-button>
            </el-button-group>
            <span class="word-count">字数：{{ contentLength }}</span>
          </div>
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="15"
            placeholder="请输入文稿内容..."
            @input="onContentInput"
            resize="vertical"
          />
        </el-form-item>

        <el-form-item label="发布设置">
          <el-switch v-model="form.isPublic" active-text="公开发布" inactive-text="仅自己可见" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitForm">
            <el-icon><Check /></el-icon>
            {{ isEdit ? '保存修改' : '保存文稿' }}
          </el-button>
          <el-button @click="saveToDraft">
            <el-icon><Document /></el-icon>
            保存草稿
          </el-button>
          <el-button @click="loadDraft" v-if="hasDraft">
            <el-icon><Refresh /></el-icon>
            恢复草稿
          </el-button>
          <el-button @click="resetForm">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategories, createManuscript, updateManuscript, getManuscriptById } from '@/api'
import { saveDraft, getDraft, removeDraft, getCurrentUserId } from '@/utils/storage'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const categories = ref([])
const isEdit = computed(() => !!route.params.id)
const draftKey = computed(() => isEdit.value ? `edit_${route.params.id}` : 'new')

const form = ref({
  title: '',
  content: '',
  introduction: '',
  categoryId: null,
  author: '',
  difficulty: '',
  isPublic: false
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文稿内容', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const contentLength = computed(() => {
  return form.value.content ? form.value.content.length : 0
})

const hasDraft = computed(() => {
  return !!getDraft(draftKey.value)
})

let autoSaveTimer = null

const loadCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch (e) {
    console.error(e)
  }
}

const loadManuscript = async () => {
  if (!isEdit.value) return
  try {
    const data = await getManuscriptById(route.params.id)
    form.value = {
      title: data.title,
      content: data.content,
      introduction: data.introduction || '',
      categoryId: data.categoryId,
      author: data.author || '',
      difficulty: data.difficulty || '',
      isPublic: data.isPublic
    }
  } catch (e) {
    console.error(e)
  }
}

const onContentInput = () => {
  if (autoSaveTimer) {
    clearTimeout(autoSaveTimer)
  }
  autoSaveTimer = setTimeout(() => {
    saveDraft(draftKey.value, form.value)
  }, 3000)
}

const saveToDraft = () => {
  saveDraft(draftKey.value, form.value)
  ElMessage.success('草稿已保存')
}

const loadDraft = () => {
  const draft = getDraft(draftKey.value)
  if (draft) {
    form.value = { ...form.value, ...draft }
    ElMessage.success('草稿已恢复')
  }
}

const insertFormat = (type) => {
  if (type === 'indent') {
    form.value.content = form.value.content.split('\n').map(line => {
      if (line.trim() && !line.startsWith('　　')) {
        return '　　' + line
      }
      return line
    }).join('\n')
  } else if (type === 'paragraph') {
    form.value.content += '\n\n'
  }
}

const clearFormat = () => {
  form.value.content = form.value.content.replace(/　　/g, '').replace(/\n{3,}/g, '\n\n')
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        form.value.createUser = 'user_' + getCurrentUserId()
        if (isEdit.value) {
          await updateManuscript(route.params.id, form.value)
          ElMessage.success('修改成功')
        } else {
          await createManuscript(form.value)
          ElMessage.success('保存成功')
        }
        removeDraft(draftKey.value)
        router.push('/manuscripts')
      } catch (e) {
        console.error(e)
      } finally {
        submitting.value = false
      }
    }
  })
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

onMounted(() => {
  loadCategories()
  if (isEdit.value) {
    loadManuscript()
  } else {
    const draft = getDraft(draftKey.value)
    if (draft) {
      ElMessageBox.confirm('发现未保存的草稿，是否恢复？', '提示', {
        confirmButtonText: '恢复',
        cancelButtonText: '丢弃'
      }).then(() => {
        form.value = { ...form.value, ...draft }
      }).catch(() => {
        removeDraft(draftKey.value)
      })
    }
  }
})

onUnmounted(() => {
  if (autoSaveTimer) {
    clearTimeout(autoSaveTimer)
  }
})
</script>

<style scoped>
.create-page {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.word-count {
  font-size: 13px;
  color: #909399;
}
</style>
