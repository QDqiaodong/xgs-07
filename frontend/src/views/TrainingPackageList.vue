<template>
  <div class="training-packages-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <el-icon><Collection /></el-icon>
          文稿训练包
        </h1>
        <p class="page-subtitle">系统化阶段性练习，从入门到精通循序渐进</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="goCreate">创建训练包</el-button>
    </div>

    <div class="filter-bar">
      <div class="filter-group">
        <span class="filter-label">分类：</span>
        <el-radio-group v-model="activeCategory" @change="loadList" size="default">
          <el-radio-button :label="''">全部分类</el-radio-button>
          <el-radio-button v-for="cat in categories" :key="cat.id" :label="String(cat.id)">
            {{ cat.name }}
          </el-radio-button>
        </el-radio-group>
      </div>
      <div class="filter-group">
        <span class="filter-label">难度：</span>
        <el-radio-group v-model="activeDifficulty" @change="loadList" size="default">
          <el-radio-button :label="''">全部难度</el-radio-button>
          <el-radio-button v-for="d in difficultyLevels" :key="d" :label="d">
            {{ d }}
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div class="section-tabs">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="全部训练包" name="all" />
        <el-tab-pane label="我的进度" name="mine" />
      </el-tabs>
    </div>

    <div class="list-content" v-loading="loading">
      <el-empty v-if="!loading && displayList.length === 0"
        :description="activeTab === 'mine' ? '暂无训练进度，去选一个训练包开始吧' : '暂无训练包'" />
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="pkg in displayList" :key="pkg.id">
          <div class="package-card" @click="goDetail(pkg.id)">
            <div class="card-cover" :style="getCoverStyle(pkg)">
              <div class="cover-overlay">
                <DifficultyBadge v-if="pkg.difficulty" :difficulty="pkg.difficulty" />
              </div>
              <div class="cover-category" v-if="pkg.categoryName">{{ pkg.categoryName }}</div>
            </div>
            <div class="card-body">
              <h3 class="card-title">{{ pkg.name }}</h3>
              <p class="card-desc">{{ pkg.description || '暂无描述' }}</p>
              <div class="card-goals" v-if="getGoals(pkg).length > 0">
                <el-tag size="small" type="info" v-for="(g, i) in getGoals(pkg).slice(0, 3)" :key="i">{{ g }}</el-tag>
              </div>
              <div class="card-meta">
                <span class="meta-item">
                  <el-icon><Document /></el-icon>
                  {{ pkg._manuscriptCount || 0 }} 篇文稿
                </span>
                <span class="meta-item" v-if="pkg.targetDays">
                  <el-icon><Calendar /></el-icon>
                  {{ pkg.targetDays }}天
                </span>
              </div>
              <div class="card-progress" v-if="pkg._progress">
                <div class="progress-bar-mini">
                  <div class="progress-fill-mini"
                    :style="{ width: Math.round(pkg._progress.completionPercent || 0) + '%' }"></div>
                </div>
                <div class="progress-info">
                  <span :class="['progress-status', 'status-' + pkg._progress.status]">
                    {{ getProgressStatusLabel(pkg._progress.status) }}
                  </span>
                  <span class="progress-num">
                    {{ pkg._progress.completedManuscripts || 0 }}/{{ pkg._progress.totalManuscripts || pkg._manuscriptCount || 0 }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTrainingPackages, getCategories, getDifficultyLevels, getUserTrainingPackageProgressList } from '@/api'
import { getCurrentUserId } from '@/utils/storage'
import { Collection, Plus, Document, Calendar } from '@element-plus/icons-vue'
import DifficultyBadge from '@/components/DifficultyBadge.vue'

const router = useRouter()
const userId = getCurrentUserId()

const loading = ref(false)
const allPackages = ref([])
const userProgressMap = ref({})
const categories = ref([])
const difficultyLevels = ref([])
const activeCategory = ref('')
const activeDifficulty = ref('')
const activeTab = ref('all')

const loadCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch (e) {
    console.error(e)
  }
}

const loadDifficulties = async () => {
  try {
    const levels = await getDifficultyLevels()
    difficultyLevels.value = levels || ['简单', '中等', '困难', '挑战']
  } catch (e) {
    difficultyLevels.value = ['简单', '中等', '困难', '挑战']
  }
}

const loadUserProgress = async () => {
  try {
    const list = await getUserTrainingPackageProgressList(userId)
    const map = {}
    list.forEach(p => { map[p.packageId] = p })
    userProgressMap.value = map
  } catch (e) {
    console.error('加载用户进度失败', e)
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const params = {}
    if (activeCategory.value) params.categoryId = activeCategory.value
    if (activeDifficulty.value) params.difficulty = activeDifficulty.value
    params.userId = userId
    const list = await getTrainingPackages(params)
    allPackages.value = (list || []).map(pkg => ({
      ...pkg,
      _manuscriptCount: pkg.items ? pkg.items.length : 0,
      _progress: userProgressMap.value[pkg.id] || null
    }))
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const onTabChange = async (tab) => {
  if (tab === 'mine') {
    await loadUserProgress()
    await loadList()
  }
}

const displayList = computed(() => {
  if (activeTab.value === 'mine') {
    return allPackages.value.filter(p => p._progress)
  }
  return allPackages.value
})

const getGoals = (pkg) => {
  try {
    if (!pkg.trainingGoals) return []
    const goals = JSON.parse(pkg.trainingGoals)
    return Array.isArray(goals) ? goals : []
  } catch {
    return []
  }
}

const getProgressStatusLabel = (status) => {
  const map = {
    'not_started': '未开始',
    'in_progress': '进行中',
    'completed': '已完成'
  }
  return map[status] || '未开始'
}

const coverColors = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
  'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
  'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)'
]

const getCoverStyle = (pkg) => {
  if (pkg.coverImage) {
    return { backgroundImage: `url(${pkg.coverImage})`, backgroundSize: 'cover', backgroundPosition: 'center' }
  }
  const idx = (pkg.id || 0) % coverColors.length
  return { background: coverColors[idx] }
}

const goDetail = (id) => router.push(`/training-packages/${id}`)
const goCreate = () => ElMessage.info('创建功能即将开放')

import { ElMessage } from 'element-plus'

onMounted(async () => {
  await Promise.all([loadCategories(), loadDifficulties(), loadUserProgress()])
  loadList()
})
</script>

<style scoped>
.training-packages-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}
.header-left { flex: 1; }
.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 6px;
}
.page-title .el-icon { color: #667eea; }
.page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}
.filter-bar {
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.filter-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  min-width: 48px;
}
.section-tabs {
  background: #fff;
  border-radius: 12px 12px 0 0;
  padding: 0 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border-bottom: 1px solid #f0f2f5;
}
.list-content {
  background: #fff;
  padding: 20px;
  border-radius: 0 0 12px 12px;
  min-height: 300px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  margin-bottom: 24px;
}
.package-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
}
.package-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.12);
  border-color: #667eea;
}
.card-cover {
  height: 120px;
  position: relative;
  overflow: hidden;
}
.cover-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
}
.cover-category {
  position: absolute;
  bottom: 10px;
  left: 12px;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  background: rgba(0,0,0,0.35);
  border-radius: 10px;
  backdrop-filter: blur(4px);
}
.card-body { padding: 14px 16px 16px; }
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 6px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  margin: 0 0 10px;
  height: 39px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.card-goals {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}
.card-goals .el-tag {
  border: none;
  background: #f5f7fa;
  color: #606266;
}
.card-meta {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}
.meta-item .el-icon { font-size: 13px; }
.card-progress {
  padding-top: 10px;
  border-top: 1px dashed #f0f2f5;
}
.progress-bar-mini {
  height: 6px;
  background: #f0f2f5;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 6px;
}
.progress-fill-mini {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 3px;
  transition: width 0.3s;
}
.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}
.progress-status {
  padding: 1px 8px;
  border-radius: 10px;
  font-weight: 500;
}
.progress-status.status-not_started { background: #f4f4f5; color: #909399; }
.progress-status.status-in_progress { background: #ecf5ff; color: #409eff; }
.progress-status.status-completed { background: #f0f9eb; color: #67c23a; }
.progress-num { color: #606266; font-family: 'Courier New', monospace; }
</style>
