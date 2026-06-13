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
              <p>{{ cat.description }}</p>
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
import { getHotManuscripts, getCategories } from '@/api'
import ManuscriptCard from '@/components/ManuscriptCard.vue'

const router = useRouter()
const hotList = ref([])
const categories = ref([])

const loadData = async () => {
  try {
    const [hot, cats] = await Promise.all([
      getHotManuscripts(),
      getCategories()
    ])
    hotList.value = hot.slice(0, 8)
    categories.value = cats
  } catch (e) {
    console.error(e)
  }
}

const goCategory = (categoryId) => {
  router.push({ path: '/manuscripts', query: { category: categoryId } })
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
  padding: 20px 10px;
}

.cat-icon {
  color: #409eff;
  margin-bottom: 12px;
}

.cat-content h3 {
  font-size: 18px;
  margin-bottom: 8px;
  color: #303133;
}

.cat-content p {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}
</style>
