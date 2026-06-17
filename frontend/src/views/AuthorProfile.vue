<template>
  <div class="author-profile-page" v-loading="loading">
    <div v-if="profile" class="profile-content">
      <div class="profile-header">
        <el-page-header @back="$router.back()" content="返回">
          <template #title>
            <span>作者档案</span>
          </template>
        </el-page-header>
        <div class="author-info">
          <div class="author-avatar">{{ profile.author?.charAt(0) || '?' }}</div>
          <div class="author-details">
            <h1 class="author-name">{{ profile.author }}</h1>
            <div class="author-stats">
              <span class="stat-item">
                <el-icon><Document /></el-icon>
                {{ profile.totalManuscripts }} 篇文稿
              </span>
              <span class="stat-item">
                <el-icon><View /></el-icon>
                {{ profile.totalViews }} 次浏览
              </span>
              <span class="stat-item">
                <el-icon><Star /></el-icon>
                {{ profile.totalFavorites }} 次收藏
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="distribution-section">
        <div class="dist-card genre-card">
          <div class="dist-card-header">
            <el-icon class="dist-icon"><Collection /></el-icon>
            <span class="dist-title">体裁分布</span>
          </div>
          <div class="dist-card-body">
            <div v-if="profile.genreDistribution && profile.genreDistribution.length > 0" class="bar-chart">
              <div v-for="item in profile.genreDistribution" :key="item.name" class="bar-row">
                <span class="bar-label">{{ item.name }}</span>
                <div class="bar-track">
                  <div class="bar-fill genre-fill" :style="{ width: item.percentage + '%' }"></div>
                </div>
                <span class="bar-value">{{ item.count }}篇 ({{ item.percentage }}%)</span>
              </div>
            </div>
            <div v-else class="empty-tip">暂无体裁数据</div>
          </div>
        </div>

        <div class="dist-card difficulty-card">
          <div class="dist-card-header">
            <el-icon class="dist-icon"><Warning /></el-icon>
            <span class="dist-title">难度分布</span>
          </div>
          <div class="dist-card-body">
            <div v-if="profile.difficultyDistribution && profile.difficultyDistribution.length > 0" class="bar-chart">
              <div v-for="item in profile.difficultyDistribution" :key="item.name" class="bar-row">
                <span class="bar-label" :class="'diff-label-' + item.level">{{ item.name }}</span>
                <div class="bar-track">
                  <div class="bar-fill" :class="'diff-fill-' + item.level" :style="{ width: item.percentage + '%' }"></div>
                </div>
                <span class="bar-value">{{ item.count }}篇 ({{ item.percentage }}%)</span>
              </div>
            </div>
            <div v-else class="empty-tip">暂无难度数据</div>
          </div>
        </div>

        <div class="dist-card expression-card">
          <div class="dist-card-header">
            <el-icon class="dist-icon"><MagicStick /></el-icon>
            <span class="dist-title">适合训练的表达类型</span>
          </div>
          <div class="dist-card-body">
            <div v-if="profile.expressionDistribution && profile.expressionDistribution.length > 0" class="expression-list">
              <div v-for="item in profile.expressionDistribution" :key="item.type" class="expression-item">
                <span class="emotion-color-dot" :style="{ background: getEmotionColor(item.type) }"></span>
                <span class="expression-label">{{ item.label }}</span>
                <div class="expression-bar-track">
                  <div class="expression-bar-fill" :style="{ width: item.percentage + '%', background: getEmotionColor(item.type) }"></div>
                </div>
                <span class="expression-count">{{ item.count }}次</span>
              </div>
            </div>
            <div v-else class="empty-tip">
              <el-icon><InfoFilled /></el-icon>
              <span>暂无情感训练数据，用户标记情感后将自动聚合</span>
            </div>
          </div>
        </div>
      </div>

      <div class="manuscripts-section">
        <div class="section-header">
          <el-icon><Document /></el-icon>
          <span class="section-title">{{ profile.author }} 的文稿</span>
          <span class="section-count">共 {{ profile.totalManuscripts }} 篇</span>
        </div>
        <div class="manuscript-list">
          <div v-for="m in profile.manuscripts" :key="m.id" class="manuscript-item" @click="goManuscript(m.id)">
            <div class="manuscript-item-header">
              <span class="manuscript-title">{{ m.title }}</span>
              <div class="manuscript-tags">
                <el-tag size="small" v-if="m.categoryName">{{ m.categoryName }}</el-tag>
                <DifficultyBadge v-if="m.difficulty" :difficulty="m.difficulty" size="small" />
              </div>
            </div>
            <div class="manuscript-item-footer">
              <span class="stat"><el-icon><View /></el-icon> {{ m.viewCount || 0 }}</span>
              <span class="stat"><el-icon><Star /></el-icon> {{ m.favoriteCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && !profile" description="未找到该作者的文稿档案" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, View, Star, Warning, Collection, MagicStick, InfoFilled } from '@element-plus/icons-vue'
import { getAuthorProfile } from '@/api'
import DifficultyBadge from '@/components/DifficultyBadge.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const profile = ref(null)

const emotionColorMap = {
  calm: '#909399',
  passionate: '#f56c6c',
  low: '#606266',
  bright: '#e6a23c',
  gentle: '#f48fb1',
  sad: '#409eff',
  joyful: '#67c23a',
  solemn: '#9b59b6'
}

const getEmotionColor = (type) => {
  return emotionColorMap[type] || '#909399'
}

const goManuscript = (id) => {
  router.push(`/manuscript/${id}`)
}

const loadProfile = async () => {
  loading.value = true
  try {
    const name = decodeURIComponent(route.params.name)
    const res = await getAuthorProfile(name)
    profile.value = res
  } catch (e) {
    console.error('加载作者档案失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.author-profile-page {
  max-width: 1100px;
  margin: 0 auto;
}

.profile-header {
  margin-bottom: 32px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-top: 24px;
  padding: 28px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: #fff;
}

.author-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: 700;
  flex-shrink: 0;
  backdrop-filter: blur(4px);
  border: 3px solid rgba(255, 255, 255, 0.4);
}

.author-details {
  flex: 1;
}

.author-name {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
  color: #fff;
}

.author-stats {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.author-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.9);
}

.distribution-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 36px;
}

.dist-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.dist-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.dist-card-header {
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
}

.genre-card .dist-card-header {
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
  color: #409eff;
}

.difficulty-card .dist-card-header {
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
  color: #e6a23c;
}

.expression-card .dist-card-header {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
  color: #67c23a;
}

.dist-icon {
  font-size: 20px;
}

.dist-title {
  font-weight: 600;
}

.dist-card-body {
  padding: 20px;
}

.bar-chart {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.bar-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.bar-label {
  width: 56px;
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  text-align: right;
  flex-shrink: 0;
}

.bar-track {
  flex: 1;
  height: 20px;
  background: #f0f2f5;
  border-radius: 10px;
  overflow: hidden;
  min-width: 0;
}

.bar-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 0.6s ease;
  min-width: 4px;
}

.genre-fill {
  background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%);
}

.diff-fill-1 {
  background: linear-gradient(90deg, #67c23a 0%, #95d475 100%);
}

.diff-fill-2 {
  background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%);
}

.diff-fill-3 {
  background: linear-gradient(90deg, #e6a23c 0%, #f0c78a 100%);
}

.diff-fill-4 {
  background: linear-gradient(90deg, #f56c6c 0%, #f89898 100%);
}

.diff-fill-0 {
  background: linear-gradient(90deg, #909399 0%, #c0c4cc 100%);
}

.diff-label-1 { color: #67c23a; }
.diff-label-2 { color: #409eff; }
.diff-label-3 { color: #e6a23c; }
.diff-label-4 { color: #f56c6c; }
.diff-label-0 { color: #909399; }

.bar-value {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  flex-shrink: 0;
  min-width: 80px;
}

.expression-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.expression-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.emotion-color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
}

.expression-label {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  width: 36px;
  flex-shrink: 0;
}

.expression-bar-track {
  flex: 1;
  height: 16px;
  background: #f0f2f5;
  border-radius: 8px;
  overflow: hidden;
  min-width: 0;
}

.expression-bar-fill {
  height: 100%;
  border-radius: 8px;
  transition: width 0.6s ease;
  min-width: 4px;
  opacity: 0.8;
}

.expression-count {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  flex-shrink: 0;
  min-width: 36px;
}

.empty-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  color: #c0c4cc;
  font-size: 13px;
}

.manuscripts-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.manuscripts-section .section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.manuscripts-section .section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.section-count {
  font-size: 13px;
  color: #909399;
  margin-left: auto;
}

.manuscript-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.manuscript-item {
  padding: 16px 20px;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.2s ease;
}

.manuscript-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.manuscript-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  gap: 12px;
}

.manuscript-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.manuscript-tags {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.manuscript-item-footer {
  display: flex;
  gap: 16px;
}

.manuscript-item-footer .stat {
  font-size: 12px;
  color: #c0c4cc;
  display: flex;
  align-items: center;
  gap: 4px;
}

@media (max-width: 900px) {
  .distribution-section {
    grid-template-columns: 1fr;
  }

  .manuscript-list {
    grid-template-columns: 1fr;
  }

  .author-info {
    flex-direction: column;
    text-align: center;
  }

  .author-stats {
    justify-content: center;
  }
}
</style>
