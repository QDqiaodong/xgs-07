<template>
  <el-tooltip :content="tooltipContent" placement="top" :disabled="!showTooltip">
    <span :class="['difficulty-badge', 'diff-' + level, size]">
      <span class="diff-label">{{ displayLabel }}</span>
      <span class="diff-level">Lv.{{ levelNum }}</span>
    </span>
  </el-tooltip>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  difficulty: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'default'
  },
  showTooltip: {
    type: Boolean,
    default: true
  }
})

const difficultyConfig = {
  '入门': {
    level: 1,
    label: '入门',
    color: '#67c23a',
    bgColor: '#f0f9eb',
    borderColor: '#c2e7b0',
    description: '短句多、用词简单，适合朗读初学者'
  },
  '初级': {
    level: 2,
    label: '初级',
    color: '#409eff',
    bgColor: '#ecf5ff',
    borderColor: '#b3d8ff',
    description: '句式常规、无生僻字，有一定基础即可'
  },
  '中级': {
    level: 3,
    label: '中级',
    color: '#e6a23c',
    bgColor: '#fdf6ec',
    borderColor: '#f5dab1',
    description: '包含长句、绕口词，需要一定朗读技巧'
  },
  '高级': {
    level: 4,
    label: '高级',
    color: '#f56c6c',
    bgColor: '#fef0f0',
    borderColor: '#fbc4c4',
    description: '多音字密集、情感复杂，专业级朗读挑战'
  }
}

const config = computed(() => {
  return difficultyConfig[props.difficulty] || null
})

const level = computed(() => {
  return config.value ? config.value.level : 0
})

const levelNum = computed(() => {
  return config.value ? config.value.level : '?'
})

const displayLabel = computed(() => {
  return config.value ? config.value.label : props.difficulty || '未设置'
})

const tooltipContent = computed(() => {
  if (!config.value) return ''
  return `难度 ${displayLabel.value} · ${config.value.description}`
})
</script>

<style scoped>
.difficulty-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
  border: 1px solid;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.difficulty-badge.small {
  padding: 1px 6px;
  font-size: 11px;
  gap: 3px;
}

.difficulty-badge.default {
  padding: 2px 10px;
  font-size: 12px;
}

.diff-label {
  font-weight: 600;
}

.diff-level {
  font-size: 0.85em;
  opacity: 0.8;
  font-weight: 500;
}

.diff-1 {
  color: #67c23a;
  background: #f0f9eb;
  border-color: #c2e7b0;
}

.diff-2 {
  color: #409eff;
  background: #ecf5ff;
  border-color: #b3d8ff;
}

.diff-3 {
  color: #e6a23c;
  background: #fdf6ec;
  border-color: #f5dab1;
}

.diff-4 {
  color: #f56c6c;
  background: #fef0f0;
  border-color: #fbc4c4;
}

.diff-0 {
  color: #909399;
  background: #f4f4f5;
  border-color: #dcdfe6;
}
</style>
