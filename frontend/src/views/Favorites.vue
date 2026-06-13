<template>
  <div class="favorites-page">
    <h2 class="page-title">我的收藏</h2>
    
    <div class="list-content" v-loading="loading">
      <el-empty v-if="!loading && list.length === 0" description="暂无收藏的文稿" />
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in manuscriptList" :key="item.id">
          <div class="favorite-item">
            <ManuscriptCard :manuscript="item" />
            <el-button 
              type="danger" 
              size="small" 
              class="remove-btn"
              @click="removeFavorite(item.manuscriptId)"
            >
              <el-icon><Delete /></el-icon>
              取消收藏
            </el-button>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadList"
        @current-change="loadList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFavorites, removeFavorite as apiRemoveFavorite, getManuscriptById } from '@/api'
import { getCurrentUserId } from '@/utils/storage'
import ManuscriptCard from '@/components/ManuscriptCard.vue'

const loading = ref(false)
const list = ref([])
const page = ref(1)
const size = ref(12)
const total = ref(0)

const userId = getCurrentUserId()

const manuscriptList = computed(() => {
  return list.value.map(item => ({
    ...item.manuscript,
    manuscriptId: item.manuscriptId
  })).filter(item => item.id)
})

const loadList = async () => {
  loading.value = true
  try {
    const res = await getFavorites({
      userId,
      page: page.value - 1,
      size: size.value
    })
    list.value = res.content
    total.value = res.totalElements
    
    for (let i = 0; i < list.value.length; i++) {
      if (!list.value[i].manuscript) {
        try {
          const manuscript = await getManuscriptById(list.value[i].manuscriptId)
          list.value[i].manuscript = manuscript
        } catch (e) {
          console.error(e)
        }
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const removeFavorite = async (manuscriptId) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏这篇文稿吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await apiRemoveFavorite(userId, manuscriptId)
    ElMessage.success('已取消收藏')
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
.favorites-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  margin-bottom: 24px;
  color: #303133;
}

.favorite-item {
  position: relative;
  margin-bottom: 20px;
}

.remove-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 10;
  opacity: 0;
  transition: opacity 0.2s;
}

.favorite-item:hover .remove-btn {
  opacity: 1;
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
