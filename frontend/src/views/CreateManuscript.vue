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
          <el-input v-model="form.title" placeholder="请输入文稿标题" maxlength="100" show-word-limit />
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
              <el-select v-model="form.difficulty" placeholder="请选择难度" style="width: 100%" popper-class="difficulty-select-popper">
                <el-option v-for="opt in difficultyOptions" :key="opt.value" :label="opt.label" :value="opt.value">
                  <div class="difficulty-option">
                    <span :class="['diff-tag', 'diff-' + opt.level]">{{ opt.label }} Lv.{{ opt.level }}</span>
                    <span class="diff-desc">{{ opt.description }}</span>
                  </div>
                </el-option>
              </el-select>
              <div class="difficulty-assist">
                <el-button size="small" link type="primary" :loading="assessing" @click="recommendDifficulty">
                  <el-icon><MagicStick /></el-icon> 智能推荐
                </el-button>
                <div v-if="assessment" class="assessment-result">
                  <div class="assessment-recommend">
                    <span class="assessment-label">推荐</span>
                    <span :class="['diff-tag', 'diff-' + assessment.recommendedLevelNum]" @click="applyRecommendation">
                      {{ assessment.recommendedLevel }} Lv.{{ assessment.recommendedLevelNum }}
                    </span>
                    <span class="assessment-score">综合分 {{ assessment.overallScore }}</span>
                    <el-button size="small" link type="primary" @click="applyRecommendation">采用</el-button>
                  </div>
                  <div class="assessment-dimensions">
                    <div v-for="dim in assessment.dimensions" :key="dim.dimension" class="dim-item">
                      <span class="dim-label">{{ dim.dimensionLabel }}</span>
                      <span class="dim-metric">{{ dim.metricLabel }}</span>
                      <span :class="['dim-level', 'diff-' + dim.levelNum]">{{ dim.level }}</span>
                      <span class="dim-weight">权重 {{ Math.round(dim.weight * 100) }}%</span>
                    </div>
                  </div>
                </div>
              </div>
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
            <span class="word-count">字数：{{ contentLength }} / 20000</span>
          </div>
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="15"
            maxlength="20000"
            show-word-limit
            placeholder="请输入文稿内容..."
            @input="onContentInput"
            resize="vertical"
          />
          <div class="content-length-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>篇幅参考：短诗建议 ≤ 2000 字，散文 ≤ 8000 字，演讲稿 ≤ 20000 字，超长内容可能影响页面正常展示。</span>
          </div>
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
import { getCategories, createManuscript, updateManuscript, getManuscriptById, assessDifficulty } from '@/api'
import { saveDraft, getDraft, removeDraft, getCurrentUserId } from '@/utils/storage'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const categories = ref([])
const isEdit = computed(() => !!route.params.id)
const assessing = ref(false)
const assessment = ref(null)

const difficultyOptions = [
  { value: '入门', label: '入门', level: 1, description: '短句多、用词简单' },
  { value: '初级', label: '初级', level: 2, description: '句式常规、无生僻字' },
  { value: '中级', label: '中级', level: 3, description: '含长句、绕口词' },
  { value: '高级', label: '高级', level: 4, description: '多音字多、情感复杂' }
]
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
    const data = await getManuscriptById(route.params.id, getCurrentUserId())
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

const recommendDifficulty = async () => {
  const content = form.value.content
  if (!content || !content.trim()) {
    ElMessage.warning('请先输入文稿内容')
    return
  }
  assessing.value = true
  try {
    assessment.value = await assessDifficulty(content)
    ElMessage.success(`推荐难度：${assessment.value.recommendedLevel}（综合分 ${assessment.value.overallScore}）`)
  } catch (e) {
    console.error(e)
  } finally {
    assessing.value = false
  }
}

const applyRecommendation = () => {
  if (!assessment.value) return
  form.value.difficulty = assessment.value.recommendedLevel
  ElMessage.success(`已采用推荐难度：${assessment.value.recommendedLevel}`)
}

watch(() => form.value.content, (val) => {
  if (assessment.value && (!val || !val.trim())) {
    assessment.value = null
  }
})

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

.content-length-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding: 8px 12px;
  background: #fdf6ec;
  border-radius: 6px;
  font-size: 12px;
  color: #b88230;
  line-height: 1.6;
}

.content-length-tip .el-icon {
  flex-shrink: 0;
  font-size: 14px;
}

.difficulty-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.diff-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid;
  white-space: nowrap;
  flex-shrink: 0;
}

.diff-tag.diff-1 {
  color: #67c23a;
  background: #f0f9eb;
  border-color: #c2e7b0;
}

.diff-tag.diff-2 {
  color: #409eff;
  background: #ecf5ff;
  border-color: #b3d8ff;
}

.diff-tag.diff-3 {
  color: #e6a23c;
  background: #fdf6ec;
  border-color: #f5dab1;
}

.diff-tag.diff-4 {
  color: #f56c6c;
  background: #fef0f0;
  border-color: #fbc4c4;
}

.diff-desc {
  font-size: 13px;
  color: #606266;
}

.difficulty-assist {
  margin-top: 8px;
  width: 100%;
}

.assessment-result {
  margin-top: 8px;
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.assessment-recommend {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.assessment-label {
  font-size: 13px;
  color: #909399;
}

.assessment-recommend .diff-tag {
  cursor: pointer;
}

.assessment-score {
  font-size: 12px;
  color: #909399;
}

.assessment-dimensions {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.dim-item {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
}

.dim-label {
  font-weight: 600;
  color: #303133;
  min-width: 64px;
}

.dim-metric {
  color: #606266;
}

.dim-level {
  display: inline-block;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  border: 1px solid;
}

.dim-level.diff-1 {
  color: #67c23a;
  background: #f0f9eb;
  border-color: #c2e7b0;
}

.dim-level.diff-2 {
  color: #409eff;
  background: #ecf5ff;
  border-color: #b3d8ff;
}

.dim-level.diff-3 {
  color: #e6a23c;
  background: #fdf6ec;
  border-color: #f5dab1;
}

.dim-level.diff-4 {
  color: #f56c6c;
  background: #fef0f0;
  border-color: #fbc4c4;
}

.dim-weight {
  color: #909399;
  margin-left: auto;
}
</style>

<style>
.difficulty-select-popper .el-select-dropdown__item {
  height: auto;
  padding: 8px 16px;
  line-height: 1.5;
}
</style>
