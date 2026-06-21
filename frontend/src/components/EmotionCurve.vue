<template>
  <div class="emotion-curve-panel" v-if="curveData.length > 0">
    <div class="curve-header">
      <span class="curve-title">
        <el-icon><TrendCharts /></el-icon>
        情绪曲线
      </span>
      <span class="curve-subtitle">从铺垫到高潮再到收束的情绪走向</span>
    </div>
    <div class="chart-wrapper" ref="chartWrapper">
      <svg class="emotion-svg" :viewBox="`0 0 ${svgWidth} ${svgHeight}`" preserveAspectRatio="xMidYMid meet">
        <defs>
          <linearGradient id="curveGradient" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" :style="{ stopColor: 'rgba(245, 108, 108, 0.4)' }" />
            <stop offset="100%" :style="{ stopColor: 'rgba(245, 108, 108, 0.02)' }" />
          </linearGradient>
          <linearGradient id="lineGradient" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop v-for="(stop, idx) in gradientStops" :key="idx"
              :offset="stop.offset + '%'"
              :style="{ stopColor: stop.color }" />
          </linearGradient>
        </defs>
        <line v-for="i in 4" :key="'hline-' + i"
          :x1="padding.left"
          :y1="padding.top + ((chartHeight / 4) * (i - 1))"
          :x2="svgWidth - padding.right"
          :y2="padding.top + ((chartHeight / 4) * (i - 1))"
          stroke="#f0f0f0" stroke-dasharray="3,3" />
        <text :x="padding.left - 8" :y="padding.top + 4" class="axis-label" text-anchor="end">强</text>
        <text :x="padding.left - 8" :y="padding.top + chartHeight / 2 + 4" class="axis-label" text-anchor="end">中</text>
        <text :x="padding.left - 8" :y="padding.top + chartHeight" class="axis-label" text-anchor="end">弱</text>
        <path :d="areaPath" fill="url(#curveGradient)" />
        <path :d="linePath" fill="none" stroke="url(#lineGradient)" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
        <g v-for="(point, idx) in curvePoints" :key="'point-' + idx" class="data-point-group"
          @mouseenter="activePoint = idx"
          @mouseleave="activePoint = -1"
          @click="onPointClick(point)">
          <circle :cx="point.x" :cy="point.y" :r="activePoint === idx ? 8 : 5"
            :fill="point.color" stroke="#fff" stroke-width="2"
            class="data-point" />
          <g v-if="activePoint === idx">
            <rect :x="point.x - 60" :y="point.y - 50" width="120" height="42" rx="6"
              fill="rgba(0,0,0,0.82)" />
            <text :x="point.x" :y="point.y - 32" class="tooltip-label" text-anchor="middle"
              :fill="point.color">{{ point.emotionLabel }}</text>
            <text :x="point.x" :y="point.y - 16" class="tooltip-text" text-anchor="middle">
              第{{ point.paragraphIndex + 1 }}段 · 强度{{ point.intensity }}
            </text>
          </g>
          <text :x="point.x" :y="svgHeight - 10" class="paragraph-label" text-anchor="middle">
            {{ point.paragraphIndex + 1 }}
          </text>
        </g>
      </svg>
    </div>
    <div class="stage-markers">
      <div class="stage-item" v-for="(stage, idx) in stages" :key="'stage-' + idx">
        <span class="stage-line" :style="{ background: stage.color }"></span>
        <span class="stage-label">{{ stage.label }}</span>
        <span class="stage-range">{{ stage.range }}</span>
      </div>
    </div>
    <div class="legend-bar" v-if="curveData.length >= 3">
      <div class="legend-item" v-for="(trend, idx) in trends" :key="'trend-' + idx" :class="trend.direction">
        <span class="trend-icon">
          <el-icon v-if="trend.direction === 'rising'"><Top /></el-icon>
          <el-icon v-else-if="trend.direction === 'falling'"><Bottom /></el-icon>
          <el-icon v-else><Minus /></el-icon>
        </span>
        <span class="trend-text">{{ trend.text }}</span>
      </div>
    </div>
  </div>
  <div class="emotion-curve-empty" v-else>
    <el-icon class="empty-icon"><DataLine /></el-icon>
    <span>请先为段落设置情感标记，即可生成情绪曲线</span>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { TrendCharts, Top, Bottom, Minus, DataLine } from '@element-plus/icons-vue'

const props = defineProps({
  curveData: {
    type: Array,
    default: () => []
  },
  emotionOptions: {
    type: Array,
    default: () => [
      { value: 'calm', label: '平缓', color: '#909399' },
      { value: 'passionate', label: '激昂', color: '#f56c6c' },
      { value: 'low', label: '低沉', color: '#606266' },
      { value: 'bright', label: '明亮', color: '#e6a23c' },
      { value: 'gentle', label: '温柔', color: '#f48fb1' },
      { value: 'sad', label: '悲伤', color: '#409eff' },
      { value: 'joyful', label: '欢快', color: '#67c23a' },
      { value: 'solemn', label: '庄严', color: '#9b59b6' }
    ]
  }
})

const emit = defineEmits(['paragraph-click'])

const activePoint = ref(-1)
const svgWidth = 640
const svgHeight = 260
const padding = { top: 30, right: 20, bottom: 36, left: 42 }
const chartWidth = svgWidth - padding.left - padding.right
const chartHeight = svgHeight - padding.top - padding.bottom

const getEmotionInfo = (emotionType) => {
  const opt = props.emotionOptions.find(o => o.value === emotionType)
  return opt || { label: '未标记', color: '#c0c4cc' }
}

const curvePoints = computed(() => {
  const data = props.curveData
  if (!data || data.length === 0) return []
  const count = data.length
  return data.map((item, idx) => {
    const info = getEmotionInfo(item.emotionType)
    const intensity = item.intensity != null ? item.intensity : 50
    const x = count === 1 ? padding.left + chartWidth / 2 : padding.left + (chartWidth * idx) / (count - 1)
    const y = padding.top + chartHeight - (chartHeight * intensity) / 100
    return {
      x,
      y,
      paragraphIndex: item.paragraphIndex,
      emotionType: item.emotionType,
      emotionLabel: info.label,
      color: info.color,
      intensity,
      remark: item.remark
    }
  })
})

const linePath = computed(() => {
  const points = curvePoints.value
  if (points.length === 0) return ''
  if (points.length === 1) {
    return `M ${points[0].x} ${points[0].y}`
  }
  if (points.length === 2) {
    return `M ${points[0].x} ${points[0].y} L ${points[1].x} ${points[1].y}`
  }
  let d = `M ${points[0].x} ${points[0].y}`
  for (let i = 0; i < points.length - 1; i++) {
    const p0 = points[i - 1] || points[i]
    const p1 = points[i]
    const p2 = points[i + 1]
    const p3 = points[i + 2] || p2
    const cp1x = p1.x + (p2.x - p0.x) / 6
    const cp1y = p1.y + (p2.y - p0.y) / 6
    const cp2x = p2.x - (p3.x - p1.x) / 6
    const cp2y = p2.y - (p3.y - p1.y) / 6
    d += ` C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${p2.x} ${p2.y}`
  }
  return d
})

const areaPath = computed(() => {
  const points = curvePoints.value
  if (points.length < 2) return ''
  const baseY = padding.top + chartHeight
  let d = `M ${points[0].x} ${baseY} L ${points[0].x} ${points[0].y}`
  if (points.length === 2) {
    d += ` L ${points[1].x} ${points[1].y}`
  } else {
    for (let i = 0; i < points.length - 1; i++) {
      const p0 = points[i - 1] || points[i]
      const p1 = points[i]
      const p2 = points[i + 1]
      const p3 = points[i + 2] || p2
      const cp1x = p1.x + (p2.x - p0.x) / 6
      const cp1y = p1.y + (p2.y - p0.y) / 6
      const cp2x = p2.x - (p3.x - p1.x) / 6
      const cp2y = p2.y - (p3.y - p1.y) / 6
      d += ` C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${p2.x} ${p2.y}`
    }
  }
  const last = points[points.length - 1]
  d += ` L ${last.x} ${baseY} Z`
  return d
})

const gradientStops = computed(() => {
  const points = curvePoints.value
  if (points.length === 0) return []
  if (points.length === 1) {
    return [{ offset: 0, color: points[0].color }, { offset: 100, color: points[0].color }]
  }
  return points.map((p, idx) => ({
    offset: Math.round((idx / (points.length - 1)) * 100),
    color: p.color
  }))
})

const stages = computed(() => {
  const data = props.curveData
  if (!data || data.length === 0) return []
  const n = data.length
  if (n < 4) {
    return [{ label: '全文', range: `第1-${n}段`, color: '#e6a23c' }]
  }
  const part1 = Math.max(1, Math.ceil(n * 0.25))
  const part2 = Math.ceil(n * 0.5)
  const part3 = Math.ceil(n * 0.8)
  return [
    { label: '铺垫', range: `第1-${part1}段`, color: '#909399' },
    { label: '发展', range: `第${part1 + 1}-${part2}段`, color: '#409eff' },
    { label: '高潮', range: `第${part2 + 1}-${part3}段`, color: '#f56c6c' },
    { label: '收束', range: `第${part3 + 1}-${n}段`, color: '#67c23a' }
  ]
})

const trends = computed(() => {
  const points = curvePoints.value
  if (points.length < 3) return []
  const result = []
  const n = points.length
  const startAvg = points.slice(0, Math.max(1, Math.ceil(n / 3)))
    .reduce((s, p) => s + p.intensity, 0) / Math.max(1, Math.ceil(n / 3))
  const midAvg = points.slice(Math.ceil(n / 3), Math.ceil(n * 2 / 3))
    .reduce((s, p) => s + p.intensity, 0) / Math.max(1, Math.ceil(n / 3))
  const endAvg = points.slice(Math.ceil(n * 2 / 3))
    .reduce((s, p) => s + p.intensity, 0) / Math.max(1, n - Math.ceil(n * 2 / 3))
  const diff1 = midAvg - startAvg
  const diff2 = endAvg - midAvg
  const trend1 = diff1 > 8 ? 'rising' : diff1 < -8 ? 'falling' : 'flat'
  const trend2 = diff2 > 8 ? 'rising' : diff2 < -8 ? 'falling' : 'flat'
  const trendTexts = {
    rising: { text: '情绪上扬，走向高潮', direction: 'rising' },
    falling: { text: '情绪回落，逐渐收束', direction: 'falling' },
    flat: { text: '情绪平稳，保持一致', direction: 'flat' }
  }
  result.push({ section: '前段→中段', ...trendTexts[trend1] })
  result.push({ section: '中段→后段', ...trendTexts[trend2] })
  return result
})

const onPointClick = (point) => {
  emit('paragraph-click', point.paragraphIndex)
}

watch(() => props.curveData, () => {
  activePoint.value = -1
}, { deep: true })
</script>

<style scoped>
.emotion-curve-panel {
  background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
  border-radius: 12px;
  padding: 18px 20px 16px;
  border: 1px solid #ebeef5;
  margin-bottom: 18px;
}
.curve-header {
  display: flex;
  align-items: baseline;
  margin-bottom: 12px;
  gap: 12px;
}
.curve-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
}
.curve-title .el-icon {
  color: #f56c6c;
}
.curve-subtitle {
  font-size: 12px;
  color: #909399;
}
.chart-wrapper {
  width: 100%;
  overflow-x: auto;
}
.emotion-svg {
  width: 100%;
  min-width: 520px;
  height: auto;
  max-height: 260px;
}
.axis-label {
  font-size: 10px;
  fill: #909399;
}
.paragraph-label {
  font-size: 10px;
  fill: #a8abb2;
}
.tooltip-label {
  font-size: 12px;
  font-weight: 600;
}
.tooltip-text {
  font-size: 11px;
  fill: #fff;
}
.data-point {
  cursor: pointer;
  transition: all 0.2s ease;
}
.data-point-group:hover .data-point {
  filter: brightness(1.1);
}
.stage-markers {
  display: flex;
  gap: 16px;
  margin-top: 8px;
  padding-left: 42px;
  flex-wrap: wrap;
}
.stage-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #606266;
}
.stage-line {
  width: 16px;
  height: 3px;
  border-radius: 2px;
}
.stage-label {
  font-weight: 500;
  color: #303133;
}
.stage-range {
  color: #909399;
}
.legend-bar {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #e4e7ed;
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  padding-left: 42px;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;
}
.legend-item.rising .trend-icon { color: #f56c6c; }
.legend-item.falling .trend-icon { color: #67c23a; }
.legend-item.flat .trend-icon { color: #909399; }
.trend-icon {
  display: flex;
  font-size: 14px;
}
.emotion-curve-empty {
  padding: 36px 20px;
  text-align: center;
  background: #fafafa;
  border-radius: 12px;
  border: 1px dashed #e4e7ed;
  color: #909399;
  font-size: 13px;
  margin-bottom: 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.empty-icon {
  font-size: 32px;
  color: #c0c4cc;
}
</style>
