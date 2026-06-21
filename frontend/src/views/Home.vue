<template>
  <div class="home-page">
    <el-row class="banner" type="flex" justify="center" align="middle">
      <div class="banner-content">
        <h1>语音朗诵与文稿记录空间</h1>
        <p>服务朗诵、朗读爱好者，录入文稿、记录感悟、分享心得</p>
        <el-button type="primary" size="large" @click="$router.push('/create')">
          <el-icon><EditPen /></el-icon>
          录入文稿
        </el-button>
        <el-button size="large" @click="$router.push('/manuscripts')">
          浏览文稿
        </el-button>
        <el-button size="large" type="success" @click="$router.push('/training-packages')">
          <el-icon><Collection /></el-icon>
          训练包
        </el-button>
      </div>
    </el-row>

    <div class="section">
      <div class="section-header">
        <h2>热门文稿</h2>
        <el-button type="text" @click="$router.push('/manuscripts')">查看更多</el-button>
      </div>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in hotList" :key="item.id">
          <ManuscriptCard :manuscript="item" />
        </el-col>
      </el-row>
    </div>

    <div class="section" v-if="packages.length > 0">
      <div class="section-header">
        <h2>
          <el-icon style="color:#67c23a"><Collection /></el-icon>
          训练包推荐
        </h2>
        <el-button type="text" @click="$router.push('/training-packages')">查看更多</el-button>
      </div>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6" :lg="6" v-for="pkg in packages" :key="pkg.id">
          <div class="package-card" @click="$router.push('/training-packages/' + pkg.id)">
            <div class="pkg-cover" :style="getPkgCoverStyle(pkg)">
              <div class="pkg-category" v-if="pkg.categoryName">{{ pkg.categoryName }}</div>
            </div>
            <div class="pkg-body">
              <div class="pkg-head">
                <h3>{{ pkg.name }}</h3>
                <DifficultyBadge v-if="pkg.difficulty" :difficulty="pkg.difficulty" size="small" />
              </div>
              <p class="pkg-desc">{{ pkg.description }}</p>
              <div class="pkg-meta">
                <span><Document /> {{ pkg.items?.length || 0 }} 篇</span>
                <span v-if="pkg.targetDays"><Calendar /> {{ pkg.targetDays }}天</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="section">
      <div class="section-header">
        <h2>文稿分类</h2>
      </div>
      <el-row :gutter="16">
        <el-col :xs="12" :sm="8" :md="6" :lg="3" v-for="cat in categories" :key="cat.id">
          <el-card class="category-card" shadow="hover" @click="goCategory(cat.id)">
            <div class="cat-content">
              <div class="cat-icon">
                <el-icon :size="32"><Collection /></el-icon>
              </div>
              <h3>{{ cat.name }}</h3>
              <p class="cat-desc">{{ cat.description }}</p>
              <div class="cat-meta">
                <div v-if="cat.genre" class="cat-meta-item">
                  <span class="cat-meta-label">体裁</span>
                  <span class="cat-meta-value">{{ cat.genre }}</span>
                </div>
                <div v-if="cat.targetAudience" class="cat-meta-item">
                  <span class="cat-meta-label">适合</span>
                  <span class="cat-meta-value">{{ cat.targetAudience }}</span>
                </div>
                <div v-if="cat.trainingFocus" class="cat-meta-item">
                  <span class="cat-meta-label">训练</span>
                  <span class="cat-meta-value">{{ cat.trainingFocus }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHotManuscripts, getCategories, getTrainingPackages } from '@/api'
import { getCurrentUserId } from '@/utils/storage'
import ManuscriptCard from '@/components/ManuscriptCard.vue'
import { Collection, Document, Calendar } from '@element-plus/icons-vue'
import DifficultyBadge from '@/components/DifficultyBadge.vue'

const router = useRouter()
const userId = getCurrentUserId()
const hotList = ref([])
const categories = ref([])
const packages = ref([])

const loadData = async () => {
  try {
    const [hot, cats, pkgs] = await Promise.all([
      getHotManuscripts(),
      getCategories(),
      getTrainingPackages({ userId })
    ])
    hotList.value = hot.slice(0, 8)
    categories.value = cats
    packages.value = (pkgs || []).slice(0, 4)
  } catch (e) {
    console.error(e)
  }
}

const goCategory = (categoryId) => {
  router.push({ path: '/manuscripts', query: { category: categoryId } })
}

const pkgCoverColors = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #30cfd0 0%, #330867 100%)'
]

const getPkgCoverStyle = (pkg) => {
  if (pkg.coverImage) {
    return { backgroundImage: `url(${pkg.coverImage})`, backgroundSize: 'cover', backgroundPosition: 'center' }
  }
  const idx = (pkg.id || 0) % pkgCoverColors.length
  return { background: pkgCoverColors[idx] }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 80px 20px;
  border-radius: 12px;
  margin-bottom: 40px;
}

.banner-content {
  text-align: center;
}

.banner-content h1 {
  font-size: 36px;
  margin-bottom: 16px;
}

.banner-content p {
  font-size: 18px;
  margin-bottom: 32px;
  opacity: 0.9;
}

.banner-content .el-button {
  margin: 0 8px;
}

.section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 24px;
  color: #303133;
}

.category-card {
  cursor: pointer;
  text-align: center;
}

.cat-content {
  padding: 16px 12px;
  text-align: left;
}

.cat-icon {
  color: #409eff;
  margin-bottom: 10px;
  text-align: center;
}

.cat-content h3 {
  font-size: 18px;
  margin-bottom: 6px;
  color: #303133;
  text-align: center;
}

.cat-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  margin-bottom: 12px;
  text-align: center;
  min-height: 40px;
}

.cat-meta {
  border-top: 1px dashed #ebeef5;
  padding-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cat-meta-item {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 12px;
  line-height: 1.5;
}

.cat-meta-label {
  flex-shrink: 0;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 600;
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
  color: #409eff;
}

.cat-meta-value {
  color: #606266;
  flex: 1;
  word-break: break-all;
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
  border-color: #67c23a;
}
.pkg-cover {
  height: 110px;
  position: relative;
  overflow: hidden;
}
.pkg-category {
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
.pkg-body {
  padding: 14px 16px 16px;
}
.pkg-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 6px;
}
.pkg-head h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}
.pkg-desc {
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
.pkg-meta {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: #909399;
  padding-top: 10px;
  border-top: 1px dashed #f0f2f5;
}
.pkg-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}
.pkg-meta .el-icon { font-size: 13px; }
</style>
