<template>
  <div class="manuscript-list-page">
    <div class="filter-bar">
      <div class="filter-row">
        <el-radio-group v-model="listMode" @change="onModeChange" class="mode-switcher">
          <el-radio-button label="public">
            <el-icon><Reading /></el-icon>
            公共文稿
          </el-radio-button>
          <el-radio-button label="my">
            <el-icon><User /></el-icon>
            我的文稿
          </el-radio-button>
        </el-radio-group>
        <el-radio-group v-model="activeCategory" @change="onCategoryChange">
          <el-radio-button :label="0">全部分类</el-radio-button>
          <el-radio-button v-for="cat in categories" :key="cat.id" :label="cat.id">
            {{ cat.name }}
          </el-radio-button>
        </el-radio-group>
      </div>
      <div v-if="listMode === 'my'" class="my-list-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>我的文稿包含所有您创建的文稿，无论是否公开。未公开发布的文稿仅您本人可见。</span>
      </div>
    </div>

    <div v-if="currentCategory" class="category-detail-banner">
      <div class="cat-banner-header">
        <h2 class="cat-banner-title">{{ currentCategory.name }}</h2>
        <p class="cat-banner-desc" v-if="currentCategory.description">{{ currentCategory.description }}</p>
      </div>
      <div class="cat-banner-meta">
        <div v-if="currentCategory.genre" class="cat-banner-item">
          <span class="cat-banner-label">体裁</span>
          <span class="cat-banner-value">{{ currentCategory.genre }}</span>
        </div>
        <div v-if="currentCategory.targetAudience" class="cat-banner-item">
          <span class="cat-banner-label">适合人群</span>
          <span class="cat-banner-value">{{ currentCategory.targetAudience }}</span>
        </div>
        <div v-if="currentCategory.trainingFocus" class="cat-banner-item">
          <span class="cat-banner-label">训练重点</span>
          <span class="cat-banner-value">{{ currentCategory.trainingFocus }}</span>
        </div>
      </div>
    </div>

    <div class="list-content" v-loading="loading">
      <el-empty v-if="!loading && list.length === 0" description="暂无文稿" />
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in list" :key="item.id">
          <ManuscriptCard :manuscript="item" />
        </el-col>
      </el-row>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getManuscripts, getCategories, getMyManuscripts } from '@/api'
import ManuscriptCard from '@/components/ManuscriptCard.vue'
import { getCurrentUserId } from '@/utils/storage'
import { Reading, User, InfoFilled } from '@element-plus/icons-vue'

const route = useRoute()
const loading = ref(false)
const list = ref([])
const categories = ref([])
const activeCategory = ref(0)
const page = ref(1)
const size = ref(12)
const total = ref(0)
const listMode = ref('public')

const userId = getCurrentUserId()

const currentCategory = computed(() => {
  if (activeCategory.value <= 0) return null
  return categories.value.find(c => c.id === activeCategory.value) || null
})

const loadCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch (e) {
    console.error(e)
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value - 1,
      size: size.value
    }
    if (activeCategory.value > 0) {
      params.categoryId = activeCategory.value
    }
    let res
    if (listMode.value === 'my') {
      params.userId = userId
      res = await getMyManuscripts(params)
    } else {
      res = await getManuscripts(params)
    }
    list.value = res.content
    total.value = res.totalElements
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const onCategoryChange = () => {
  page.value = 1
  loadList()
}

const onModeChange = () => {
  page.value = 1
  loadList()
}

watch(() => route.query, (query) => {
  if (query.category) {
    activeCategory.value = Number(query.category)
    page.value = 1
    loadList()
  }
}, { immediate: true })

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.filter-bar {
  background: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.filter-row {
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.mode-switcher {
  margin-right: 8px;
}

.mode-switcher .el-radio-button__inner {
  display: flex;
  align-items: center;
  gap: 4px;
}

.my-list-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  padding: 8px 14px;
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f9eb 100%);
  border-radius: 6px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.my-list-tip .el-icon {
  color: #409eff;
  flex-shrink: 0;
  font-size: 15px;
}

.category-detail-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px 28px;
  margin-bottom: 24px;
  color: #fff;
}

.cat-banner-header {
  margin-bottom: 16px;
}

.cat-banner-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #fff;
}

.cat-banner-desc {
  font-size: 14px;
  margin: 0;
  opacity: 0.9;
  line-height: 1.6;
}

.cat-banner-meta {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.cat-banner-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cat-banner-label {
  padding: 2px 10px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  backdrop-filter: blur(4px);
}

.cat-banner-value {
  font-size: 14px;
  opacity: 0.95;
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
