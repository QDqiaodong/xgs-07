<template>
  <div class="manuscript-list-page">
    <div class="filter-bar">
      <el-radio-group v-model="activeCategory" @change="loadList">
        <el-radio-button :label="0">全部分类</el-radio-button>
        <el-radio-button v-for="cat in categories" :key="cat.id" :label="cat.id">
          {{ cat.name }}
        </el-radio-button>
      </el-radio-group>
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
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getManuscripts, getCategories } from '@/api'
import ManuscriptCard from '@/components/ManuscriptCard.vue'

const route = useRoute()
const loading = ref(false)
const list = ref([])
const categories = ref([])
const activeCategory = ref(0)
const page = ref(1)
const size = ref(12)
const total = ref(0)

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
    const res = await getManuscripts(params)
    list.value = res.content
    total.value = res.totalElements
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
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

.list-content {
  min-height: 300px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
