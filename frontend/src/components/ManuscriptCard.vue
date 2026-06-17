<template>
  <el-card class="manuscript-card" shadow="hover">
    <div class="card-content" @click="goDetail">
      <div class="card-header">
        <el-tag :type="tagType" size="small">{{ manuscript.categoryName }}</el-tag>
        <DifficultyBadge v-if="manuscript.difficulty" :difficulty="manuscript.difficulty" size="small" />
      </div>
      <h3 class="title text-ellipsis">{{ manuscript.title }}</h3>
      <p v-if="manuscript.introduction" class="intro text-ellipsis-2">{{ manuscript.introduction }}</p>
      <p v-else class="intro text-ellipsis-3">{{ manuscript.content }}</p>
      <div class="card-footer">
        <span class="author" v-if="manuscript.author" @click.stop="goAuthorProfile(manuscript.author)">
          <el-icon><User /></el-icon>
          {{ manuscript.author }}
        </span>
        <div class="stats">
          <span class="stat">
            <el-icon><View /></el-icon>
            {{ manuscript.viewCount }}
          </span>
          <span class="stat">
            <el-icon><Star /></el-icon>
            {{ manuscript.favoriteCount }}
          </span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import DifficultyBadge from '@/components/DifficultyBadge.vue'

const props = defineProps({
  manuscript: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const tagType = computed(() => {
  const types = ['', 'primary', 'success', 'warning', 'danger', 'info']
  const index = props.manuscript.categoryId % types.length
  return types[index] || 'primary'
})

const goDetail = () => {
  router.push(`/manuscript/${props.manuscript.id}`)
}

const goAuthorProfile = (author) => {
  router.push(`/author/${encodeURIComponent(author)}`)
}
</script>

<style scoped>
.manuscript-card {
  height: 100%;
  cursor: pointer;
  transition: transform 0.2s;
}

.manuscript-card:hover {
  transform: translateY(-4px);
}

.card-content {
  padding: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  line-height: 1.4;
}

.intro {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
  min-height: 44px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.author {
  font-size: 13px;
  color: #409eff;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: color 0.2s;
}

.author:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.stats {
  display: flex;
  gap: 16px;
}

.stat {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
