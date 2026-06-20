<template>
  <div class="detail-page" v-loading="loading">
    <div class="not-allowed-page" v-if="notAllowed">
      <el-result icon="warning" title="无法访问" sub-title="该文稿不存在或为未公开文稿，您暂无权限查看">
        <template #extra>
          <el-button type="primary" @click="$router.back()">返回</el-button>
          <el-button @click="$router.push('/manuscripts')">去文稿列表</el-button>
        </template>
      </el-result>
    </div>
    <div class="detail-header" v-if="manuscript">
      <el-page-header @back="$router.back()" content="返回列表">
        <template #extra>
          <el-button :type="emotionPanelVisible ? 'success' : 'primary'" @click="toggleEmotionPanel">
            <el-icon><MagicStick /></el-icon>
            情感色带
          </el-button>
          <el-button :type="rhythmPanelVisible ? 'success' : 'primary'" @click="toggleRhythmPanel">
            <el-icon><Tickets /></el-icon>
            节奏板
          </el-button>
          <el-button :type="difficultyPanelVisible ? 'warning' : 'primary'" @click="toggleDifficultyPanel">
            <el-icon><Warning /></el-icon>
            难点标注
          </el-button>
          <el-button :type="isFavorited ? 'success' : 'primary'" @click="toggleFavorite">
            <el-icon><Star :fill="isFavorited ? '#67c23a' : 'none'" /></el-icon>
            {{ isFavorited ? '已收藏' : '收藏' }}
          </el-button>
          <el-button type="primary" @click="openNoteDialog">
            <el-icon><EditPen /></el-icon>
            记录笔记
          </el-button>
          <el-button v-if="!practiceSessionActive" type="success" @click="startPractice" :loading="savingPractice">
            <el-icon><VideoPlay /></el-icon>
            开始练习
          </el-button>
          <el-button v-else type="danger" @click="endPractice" :loading="savingPractice">
            <el-icon><Close /></el-icon>
            结束练习
            <span class="timer-display">{{ formatElapsedTime(elapsedSeconds) }}</span>
          </el-button>
        </template>
      </el-page-header>
    </div>

    <div class="main-content" v-if="manuscript">
      <div class="rhythm-panel" :class="{ collapsed: !rhythmPanelVisible }">
        <div class="rhythm-header">
          <span class="rhythm-title">朗读节奏板</span>
          <el-button type="text" size="small" @click="rhythmPanelVisible = false" class="collapse-btn">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
        </div>
        <div class="rhythm-list">
          <div
            class="rhythm-item"
            v-for="(section, index) in paragraphSections"
            :key="index"
            :class="{ active: activeParagraphIndex === index }"
            @click="scrollToParagraph(index)"
          >
            <div class="rhythm-item-header">
              <span class="paragraph-num">第 {{ index + 1 }} 段</span>
              <span :class="['status-badge', 'status-' + getParagraphStatus(index)]">{{ getStatusLabel(getParagraphStatus(index)) }}</span>
              <el-button type="text" size="small" @click.stop="openRhythmEditor(index)">
                <el-icon><Edit /></el-icon>
              </el-button>
            </div>
            <div class="rhythm-item-content">
              <div v-if="rhythmData[index]?.pause" class="rhythm-tag pause">
                <span class="tag-label">停顿</span>
                <span class="tag-value">{{ rhythmData[index].pause }}</span>
              </div>
              <div v-if="rhythmData[index]?.stress" class="rhythm-tag stress">
                <span class="tag-label">重音</span>
                <span class="tag-value">{{ rhythmData[index].stress }}</span>
              </div>
              <div v-if="rhythmData[index]?.speed" class="rhythm-tag speed">
                <span class="tag-label">语速</span>
                <span class="tag-value">{{ rhythmData[index].speed }}</span>
              </div>
              <div v-if="!hasRhythmData(index)" class="no-rhythm-tip">
                点击编辑添加节奏提示
              </div>
            </div>
            <div class="rhythm-item-preview">{{ section.content.slice(0, 30) }}{{ section.content.length > 30 ? '...' : '' }}</div>
          </div>
        </div>
      </div>

      <div class="rhythm-expand-btn" v-if="!rhythmPanelVisible" @click="rhythmPanelVisible = true">
        <el-icon><Tickets /></el-icon>
        <span>节奏板</span>
      </div>

      <div class="content-column">
        <div class="manuscript-content" :class="'type-' + manuscriptType">
          <div class="article-header">
            <h1 class="article-title">{{ manuscript.title }}</h1>
            <div class="article-meta">
              <el-tag type="primary" size="small">{{ manuscript.categoryName }}</el-tag>
              <el-tag size="small" :type="manuscriptType === 'poetry' ? 'warning' : 'info'">
                {{ manuscriptType === 'poetry' ? '古诗词' : '散文' }}
              </el-tag>
              <DifficultyBadge v-if="manuscript.difficulty" :difficulty="manuscript.difficulty" />
              <span v-if="manuscript.author" class="meta-item author-link" @click="goAuthorProfile(manuscript.author)">作者：{{ manuscript.author }}</span>
              <span class="meta-item">浏览：{{ manuscript.viewCount }}</span>
              <span class="meta-item">收藏：{{ manuscript.favoriteCount }}</span>
            </div>
            <div class="progress-stats" v-if="paragraphSections.length > 0">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
              </div>
              <div class="progress-info">
                <span class="progress-item mastered">
                  <span class="progress-dot"></span>
                  已熟练 {{ progressStats.mastered }}
                </span>
                <span class="progress-item strengthen">
                  <span class="progress-dot"></span>
                  需加强 {{ progressStats.strengthen }}
                </span>
                <span class="progress-item skip">
                  <span class="progress-dot"></span>
                  暂不练 {{ progressStats.skip }}
                </span>
                <span class="progress-item total">
                  共 {{ paragraphSections.length }} 段
                </span>
              </div>
            </div>
            <div class="practice-stats" v-if="practiceStats && practiceStats.sessionCount > 0">
              <div class="practice-stats-title">
                <el-icon><Timer /></el-icon>
                <span>练习统计</span>
              </div>
              <div class="practice-stats-row">
                <div class="practice-stat-item">
                  <span class="practice-stat-label">累计练习</span>
                  <span class="practice-stat-value">{{ practiceStats.sessionCount || 0 }} 次</span>
                </div>
                <div class="practice-stat-item">
                  <span class="practice-stat-label">累计时长</span>
                  <span class="practice-stat-value">{{ formatDuration(practiceStats.totalDurationSeconds || 0) }}</span>
                </div>
                <div class="practice-stat-item">
                  <span class="practice-stat-label">平均时长</span>
                  <span class="practice-stat-value">{{ formatDuration(practiceStats.sessionCount > 0 ? Math.round(practiceStats.totalDurationSeconds / practiceStats.sessionCount) : 0) }}</span>
                </div>
                <div class="practice-stat-item" v-if="practiceStats.averageScore > 0">
                  <span class="practice-stat-label">平均评分</span>
                  <span class="practice-stat-value">{{ Number(practiceStats.averageScore).toFixed(1) }} 分</span>
                </div>
                <div class="practice-stat-item" v-if="practiceStats.lastPracticeTime">
                  <span class="practice-stat-label">最近练习</span>
                  <span class="practice-stat-value">{{ formatRelativeTime(practiceStats.lastPracticeTime) }}</span>
                </div>
              </div>
            </div>
            <div class="emotion-legend" v-if="emotionPanelVisible">
              <span class="legend-title">情感色带：</span>
              <span class="legend-item" v-for="opt in emotionOptions" :key="opt.value">
                <span class="legend-color" :style="{ background: opt.color }"></span>
                {{ opt.label }}
              </span>
            </div>
            <div class="difficulty-legend" v-if="difficultyPanelVisible">
              <span class="legend-title">难点标注：</span>
              <span class="legend-item">
                <span class="legend-color diff-legend-long"></span>
                长句 ({{ difficultyStats.long }})
              </span>
              <span class="legend-item">
                <span class="legend-color diff-legend-tongue"></span>
                绕口句 ({{ difficultyStats.tongue }})
              </span>
              <span class="legend-item">
                <span class="legend-color diff-legend-breath"></span>
                换气点 ({{ difficultyStats.breath }})
              </span>
              <span class="legend-item">
                <span class="legend-color diff-legend-misread"></span>
                易读错词 ({{ difficultyStats.misread }})
              </span>
            </div>
            <p v-if="manuscript.introduction" class="introduction">{{ manuscript.introduction }}</p>
          </div>

          <div class="article-body">
            <div
              class="content-section"
              v-for="(section, index) in contentSections"
              :key="index"
              :id="'paragraph-' + index"
              :class="{
                'paragraph-highlight': isParagraphActive(index),
                'paragraph-status-mastered': section.type === 'paragraph' && getParagraphStatus(getParagraphIndex(index)) === 'mastered',
                'paragraph-status-strengthen': section.type === 'paragraph' && getParagraphStatus(getParagraphIndex(index)) === 'strengthen',
                'paragraph-status-skip': section.type === 'paragraph' && getParagraphStatus(getParagraphIndex(index)) === 'skip'
              }"
              @mouseenter="onParagraphHover(index)"
              @mouseleave="onParagraphLeave"
            >
              <div class="paragraph-wrapper" v-if="section.type === 'paragraph'">
                <div class="emotion-band-sidebar" v-if="emotionPanelVisible">
                  <div
                    class="emotion-band-indicator"
                    :style="{ background: getEmotionColor(getParagraphIndex(index)) }"
                    :class="{ 'has-emotion': hasEmotion(getParagraphIndex(index)) }"
                    @click.stop="openEmotionEditor(getParagraphIndex(index))"
                  >
                    <el-tooltip v-if="hasEmotion(getParagraphIndex(index))" :content="getEmotionTooltip(getParagraphIndex(index))" placement="left">
                      <span class="emotion-dot"></span>
                    </el-tooltip>
                    <el-tooltip v-else content="点击添加情感标记" placement="left">
                      <span class="emotion-add-icon">+</span>
                    </el-tooltip>
                  </div>
                </div>
                <div class="paragraph-status-bar">
                  <span class="status-label">第 {{ getParagraphIndex(index) + 1 }} 段</span>
                  <div class="status-actions">
                    <el-button
                      :type="getParagraphStatus(getParagraphIndex(index)) === 'mastered' ? 'success' : 'default'"
                      size="small"
                      :loading="savingStatusMap[getParagraphIndex(index)]"
                      @click.stop="setParagraphStatus(getParagraphIndex(index), 'mastered')"
                    >
                      <el-icon><Check /></el-icon>
                      已熟练
                    </el-button>
                    <el-button
                      :type="getParagraphStatus(getParagraphIndex(index)) === 'strengthen' ? 'warning' : 'default'"
                      size="small"
                      :loading="savingStatusMap[getParagraphIndex(index)]"
                      @click.stop="setParagraphStatus(getParagraphIndex(index), 'strengthen')"
                    >
                      <el-icon><Warning /></el-icon>
                      需加强
                    </el-button>
                    <el-button
                      :type="getParagraphStatus(getParagraphIndex(index)) === 'skip' ? 'info' : 'default'"
                      size="small"
                      :loading="savingStatusMap[getParagraphIndex(index)]"
                      @click.stop="setParagraphStatus(getParagraphIndex(index), 'skip')"
                    >
                      <el-icon><Clock /></el-icon>
                      暂不练
                    </el-button>
                    <el-button
                      v-if="emotionPanelVisible"
                      type="primary"
                      size="small"
                      plain
                      @click.stop="openEmotionEditor(getParagraphIndex(index))"
                    >
                      <el-icon><MagicStick /></el-icon>
                      情感
                    </el-button>
                  </div>
                </div>
                <div
                  class="paragraph"
                  :class="{ 'paragraph-with-emotion': emotionPanelVisible }"
                  :style="{ 
                    marginBottom: getParagraphMargin(getParagraphIndex(index)),
                    borderLeftColor: getEmotionColor(getParagraphIndex(index))
                  }"
                >
                  <template v-if="difficultyPanelVisible && difficultyAnalysis[getParagraphIndex(index)]">
                    <span v-html="getAnnotatedContent(getParagraphIndex(index))"></span>
                  </template>
                  <template v-else>{{ section.content }}</template>
                </div>
              </div>
              <div v-else-if="section.type === 'heading'" class="section-heading">{{ section.content }}</div>
              <div v-if="section.type === 'paragraph' && rhythmData[getParagraphIndex(index)]?.note" class="rhythm-note-inline">
                💡 {{ rhythmData[getParagraphIndex(index)].note }}
              </div>
              <div v-if="section.type === 'paragraph' && emotionPanelVisible && emotionData[getParagraphIndex(index)]?.remark" class="emotion-remark-inline" :style="{ borderLeftColor: getEmotionColor(getParagraphIndex(index)) }">
                🎭 {{ emotionData[getParagraphIndex(index)].remark }}
              </div>
            </div>
          </div>
        </div>

        <div class="notes-section" v-if="manuscript && notes.length > 0">
          <h3 class="section-title">
            <el-icon><EditPen /></el-icon>
            大家的练习笔记
          </h3>
          <div v-for="note in notes" :key="note.id" class="note-card-wrapper">
            <div class="note-sections">
              <div class="section-item section-difficulty" v-if="note.difficultyPoints">
                <div class="section-header">
                  <el-icon class="section-icon"><Warning /></el-icon>
                  <span class="section-title">难点句</span>
                </div>
                <div class="section-content">{{ note.difficultyPoints }}</div>
              </div>
              
              <div class="section-item section-tone" v-if="note.toneControl">
                <div class="section-header">
                  <el-icon class="section-icon"><Microphone /></el-icon>
                  <span class="section-title">语气控制</span>
                </div>
                <div class="section-content">{{ note.toneControl }}</div>
              </div>
              
              <div class="section-item section-emotion" v-if="note.emotionExpression">
                <div class="section-header">
                  <el-icon class="section-icon"><MagicStick /></el-icon>
                  <span class="section-title">情感表达</span>
                </div>
                <div class="section-content">{{ note.emotionExpression }}</div>
              </div>
              
              <div class="section-item section-other" v-if="note.otherNotes">
                <div class="section-header">
                  <el-icon class="section-icon"><EditPen /></el-icon>
                  <span class="section-title">其他记录</span>
                </div>
                <div class="section-content">{{ note.otherNotes }}</div>
              </div>

              <div class="section-item section-emotion-score" v-if="note.emotionControlScore">
                <div class="section-header">
                  <el-icon class="section-icon"><Star /></el-icon>
                  <span class="section-title">情绪控制评分</span>
                  <span class="score-badge" :class="getScoreClass(note.emotionControlScore)">{{ note.emotionControlScore }}分</span>
                </div>
                <div class="section-content">
                  <div class="score-bar">
                    <div class="score-fill" :class="getScoreClass(note.emotionControlScore)" :style="{ width: (note.emotionControlScore * 10) + '%' }"></div>
                  </div>
                  <div class="score-note" v-if="note.emotionControlNote">{{ note.emotionControlNote }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="difficulty-panel" :class="{ collapsed: !difficultyPanelVisible }" v-if="manuscript">
        <div class="difficulty-panel-header">
          <span class="difficulty-panel-title">难点句标注</span>
          <el-button type="text" size="small" @click="difficultyPanelVisible = false" class="collapse-btn">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
        </div>
        <div class="difficulty-stats-bar">
          <div class="diff-stat-item diff-stat-long">
            <span class="diff-stat-num">{{ difficultyStats.long }}</span>
            <span class="diff-stat-label">长句</span>
          </div>
          <div class="diff-stat-item diff-stat-tongue">
            <span class="diff-stat-num">{{ difficultyStats.tongue }}</span>
            <span class="diff-stat-label">绕口句</span>
          </div>
          <div class="diff-stat-item diff-stat-breath">
            <span class="diff-stat-num">{{ difficultyStats.breath }}</span>
            <span class="diff-stat-label">换气点</span>
          </div>
          <div class="diff-stat-item diff-stat-misread">
            <span class="diff-stat-num">{{ difficultyStats.misread }}</span>
            <span class="diff-stat-label">易读错</span>
          </div>
        </div>
        <div class="difficulty-list">
          <template v-for="(analysis, pIndex) in difficultyAnalysis" :key="pIndex">
            <div class="difficulty-group" v-if="analysis && analysis.hasAny">
              <div class="difficulty-group-header" @click="scrollToParagraph(pIndex)">
                <span class="diff-group-num">第 {{ pIndex + 1 }} 段</span>
                <div class="diff-group-actions">
                  <el-button type="text" size="small" @click.stop="openPronunciationEditor(pIndex)">
                    <el-icon><Edit /></el-icon>
                    发音归档
                  </el-button>
                  <el-icon class="diff-group-arrow"><ArrowRight /></el-icon>
                </div>
              </div>
              <div class="difficulty-group-items">
                <div v-if="pronunciationDifficultyMap[pIndex]" class="pronunciation-summary">
                  <div v-if="pronunciationDifficultyMap[pIndex].polyphonicWords" class="pronunciation-item polyphonic">
                    <span class="pronunciation-badge">多音字</span>
                    <span class="pronunciation-text">{{ pronunciationDifficultyMap[pIndex].polyphonicWords }}</span>
                  </div>
                  <div v-if="pronunciationDifficultyMap[pIndex].linking" class="pronunciation-item linking">
                    <span class="pronunciation-badge">连读</span>
                    <span class="pronunciation-text">{{ pronunciationDifficultyMap[pIndex].linking }}</span>
                  </div>
                  <div v-if="pronunciationDifficultyMap[pIndex].stress" class="pronunciation-item stress">
                    <span class="pronunciation-badge">轻重音</span>
                    <span class="pronunciation-text">{{ pronunciationDifficultyMap[pIndex].stress }}</span>
                  </div>
                  <div v-if="pronunciationDifficultyMap[pIndex].breathPoints" class="pronunciation-item breath">
                    <span class="pronunciation-badge">换气点</span>
                    <span class="pronunciation-text">{{ pronunciationDifficultyMap[pIndex].breathPoints }}</span>
                  </div>
                </div>
                <div v-for="(s, sIdx) in analysis.longSentences" :key="'long-' + sIdx" class="diff-item diff-item-long">
                  <span class="diff-item-badge">长句</span>
                  <span class="diff-item-text">{{ s.text.length > 50 ? s.text.slice(0, 50) + '...' : s.text }}</span>
                </div>
                <div v-for="(s, sIdx) in analysis.tongueTwisters" :key="'tongue-' + sIdx" class="diff-item diff-item-tongue">
                  <span class="diff-item-badge">绕口</span>
                  <span class="diff-item-text">{{ s.text.length > 50 ? s.text.slice(0, 50) + '...' : s.text }}</span>
                </div>
                <div v-for="(m, mIdx) in analysis.misreadWords" :key="'misread-' + mIdx" class="diff-item diff-item-misread">
                  <span class="diff-item-badge">易读错</span>
                  <span class="diff-item-word">{{ m.word }}</span>
                  <span class="diff-item-note">{{ m.note }}</span>
                </div>
                <div v-if="analysis.breathingPoints.length > 0" class="diff-item diff-item-breath">
                  <span class="diff-item-badge">换气</span>
                  <span class="diff-item-text">本段含 {{ analysis.breathingPoints.length }} 个换气点</span>
                </div>
              </div>
            </div>
          </template>
          <div v-if="difficultyStats.long + difficultyStats.tongue + difficultyStats.breath + difficultyStats.misread === 0" class="no-difficulty-tip">
            <el-icon><CircleCheck /></el-icon>
            <span>本文未检测到明显朗读难点</span>
          </div>
        </div>
      </div>

      <div class="difficulty-expand-btn" v-if="!difficultyPanelVisible" @click="difficultyPanelVisible = true">
        <el-icon><Warning /></el-icon>
        <span>难点</span>
      </div>
    </div>

    <div class="reading-follow-bar" v-if="manuscript && paragraphSections.length > 0">
      <div class="follow-bar-progress">
        <div class="follow-bar-fill" :style="{ width: followProgressPercent + '%' }"></div>
      </div>
      <div class="follow-bar-content">
        <span class="follow-bar-segment">
          <span class="follow-bar-num">第 {{ currentParagraphIndex + 1 }} 段</span>
          <span class="follow-bar-total">/ 共 {{ paragraphSections.length }} 段</span>
        </span>
        <span :class="['follow-bar-status', 'status-' + getParagraphStatus(currentParagraphIndex)]">
          <span class="follow-bar-dot"></span>
          {{ getStatusLabel(getParagraphStatus(currentParagraphIndex)) }}
        </span>
        <span class="follow-bar-percent">{{ followProgressPercent }}%</span>
      </div>
    </div>

    <el-dialog v-model="showNoteDialog" title="记录练习笔记" width="600px">
      <el-form :model="noteForm" label-width="100px">
        <div class="form-section-title">
          <el-icon><Warning /></el-icon>
          <span>朗读难点记录</span>
        </div>
        <el-form-item label="难点句">
          <el-input v-model="noteForm.difficultyPoints" type="textarea" :rows="2" placeholder="记录练习中遇到的难读句子、发音难点等" />
        </el-form-item>
        
        <div class="form-section-title">
          <el-icon><Microphone /></el-icon>
          <span>语气控制要点</span>
        </div>
        <el-form-item label="语气控制">
          <el-input v-model="noteForm.toneControl" type="textarea" :rows="2" placeholder="记录语气、语调、节奏等控制要点" />
        </el-form-item>
        
        <div class="form-section-title">
          <el-icon><MagicStick /></el-icon>
          <span>情感表达要点</span>
        </div>
        <el-form-item label="情感表达">
          <el-input v-model="noteForm.emotionExpression" type="textarea" :rows="2" placeholder="记录情感表达、情绪把控要点" />
        </el-form-item>
        
        <div class="form-section-title">
          <el-icon><EditPen /></el-icon>
          <span>其他训练记录</span>
        </div>
        <el-form-item label="其他记录">
          <el-input v-model="noteForm.otherNotes" type="textarea" :rows="2" placeholder="其他练习心得、感悟等" />
        </el-form-item>

        <div class="form-section-title">
          <el-icon><Star /></el-icon>
          <span>情绪控制评分</span>
        </div>
        <el-form-item label="情绪评分">
          <div class="emotion-score-input">
            <el-rate
              v-model="noteForm.emotionControlScore"
              :max="10"
              :low-threshold="3"
              :high-threshold="7"
              :colors="['#f56c6c', '#e6a23c', '#67c23a']"
              :texts="['很差', '较差', '一般', '较好', '很好']"
              show-text
              allow-half
            />
            <span class="score-num" v-if="noteForm.emotionControlScore">{{ noteForm.emotionControlScore }} 分</span>
          </div>
        </el-form-item>
        <el-form-item label="评分说明">
          <el-input v-model="noteForm.emotionControlNote" type="textarea" :rows="2" placeholder="记录本次练习情绪控制的要点、感受或改进方向" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showNoteDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingNote" @click="saveNote">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showRhythmEditor" title="编辑段落节奏" width="500px">
      <el-form :model="rhythmForm" label-width="80px">
        <el-form-item label="停顿">
          <el-select v-model="rhythmForm.pause" placeholder="选择停顿方式" clearable style="width: 100%">
            <el-option v-for="item in pauseOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="重音">
          <el-select v-model="rhythmForm.stress" placeholder="选择重音方式" clearable style="width: 100%">
            <el-option v-for="item in stressOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="语速">
          <el-select v-model="rhythmForm.speed" placeholder="选择语速" clearable style="width: 100%">
            <el-option v-for="item in speedOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="rhythmForm.note" type="textarea" :rows="3" placeholder="输入节奏备注提示" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRhythmEditor = false">取消</el-button>
        <el-button type="primary" :loading="savingRhythm" @click="saveRhythmData">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEmotionEditor" title="编辑情感色带" width="520px">
      <el-form :model="emotionForm" label-width="80px">
        <el-form-item label="情感类型">
          <div class="emotion-picker">
            <div
              class="emotion-option"
              v-for="opt in emotionOptions"
              :key="opt.value"
              :class="{ active: emotionForm.emotionType === opt.value }"
              :style="{ '--emotion-color': opt.color }"
              @click="selectEmotion(opt.value)"
            >
              <span class="emotion-color-dot" :style="{ background: opt.color }"></span>
              <span class="emotion-label">{{ opt.label }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="情感备注">
          <el-input
            v-model="emotionForm.remark"
            type="textarea"
            :rows="3"
            placeholder="输入情感表达提示，如'声音轻柔、语速放缓、带淡淡忧伤'等"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="clearEmotion" type="danger" plain>清除情感</el-button>
        <el-button @click="showEmotionEditor = false">取消</el-button>
        <el-button type="primary" :loading="savingEmotion" @click="saveEmotionData">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showPronunciationEditor" title="发音难点归档" width="600px">
      <el-form :model="pronunciationForm" label-width="100px">
        <div class="pronunciation-section-header">
          <span class="pronunciation-section-num">第 {{ editingPronunciationParagraphIndex + 1 }} 段</span>
          <span class="pronunciation-section-tip">记录本段朗读中的发音难点，便于练习时参考</span>
        </div>
        <el-form-item label="多音字">
          <el-input
            v-model="pronunciationForm.polyphonicWords"
            type="textarea"
            :rows="2"
            placeholder="如：'行'此处读xíng，不读háng；'重'此处读zhòng，不读chóng"
          />
        </el-form-item>
        <el-form-item label="连读">
          <el-input
            v-model="pronunciationForm.linking"
            type="textarea"
            :rows="2"
            placeholder="如：'是啊'读作shì ya；'对啊'读作duì ya"
          />
        </el-form-item>
        <el-form-item label="轻重音">
          <el-input
            v-model="pronunciationForm.stress"
            type="textarea"
            :rows="2"
            placeholder="如：'长江'重音在'长'；'母亲'重音在'母'"
          />
        </el-form-item>
        <el-form-item label="换气点">
          <el-input
            v-model="pronunciationForm.breathPoints"
            type="textarea"
            :rows="2"
            placeholder="如：第5个字后换气；逗号处停顿3秒；句号处换气"
          />
        </el-form-item>
        <el-form-item label="共享">
          <el-switch v-model="pronunciationForm.isPublic" active-text="设为公开（其他用户可见）" inactive-text="仅自己可见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="clearPronunciation" type="danger" plain>清除归档</el-button>
        <el-button @click="showPronunciationEditor = false">取消</el-button>
        <el-button type="primary" :loading="savingPronunciation" @click="savePronunciationData">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showPracticeEndDialog" title="本次练习小结" width="500px">
      <el-form :model="practiceForm" label-width="100px">
        <div class="practice-summary-card">
          <div class="practice-summary-item">
            <span class="practice-summary-label">练习时长</span>
            <span class="practice-summary-value">{{ formatElapsedTime(elapsedSeconds) }}</span>
          </div>
        </div>
        <el-form-item label="完成段落">
          <el-select
            v-model="practiceForm.completedParagraphs"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="选择本次完成的段落"
            style="width: 100%"
          >
            <el-option
              v-for="(section, index) in paragraphSections"
              :key="index"
              :label="'第 ' + (index + 1) + ' 段'"
              :value="String(index)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="自评状态">
          <div class="self-assessment-picker">
            <div
              class="self-assessment-option"
              v-for="opt in selfAssessmentOptions"
              :key="opt.value"
              :class="{ active: practiceForm.selfAssessmentStatus === opt.value }"
              :style="{ '--assess-color': opt.color }"
              @click="selectSelfAssessment(opt.value)"
            >
              <span class="assess-dot" :style="{ background: opt.color }"></span>
              <span class="assess-label">{{ opt.label }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="自评分数">
          <div class="practice-score-input">
            <el-rate
              v-model="practiceForm.selfAssessmentScore"
              :max="10"
              :low-threshold="3"
              :high-threshold="7"
              :colors="['#f56c6c', '#e6a23c', '#67c23a']"
              :texts="['很差', '较差', '一般', '较好', '很好']"
              show-text
              allow-half
            />
            <span class="practice-score-num" v-if="practiceForm.selfAssessmentScore">{{ practiceForm.selfAssessmentScore }} 分</span>
          </div>
        </el-form-item>
        <el-form-item label="练习心得">
          <el-input
            v-model="practiceForm.selfAssessmentNote"
            type="textarea"
            :rows="3"
            placeholder="记录本次练习的心得、收获、待改进的地方..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPracticeEndDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingPractice" @click="savePracticeSession">保存记录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Tickets, ArrowLeft, ArrowRight, Edit, Check, Warning, Clock, MagicStick, EditPen, Microphone, CircleCheck, Star, Timer, VideoPlay, Close, VideoCamera } from '@element-plus/icons-vue'
import { getManuscriptDetail, addFavorite, removeFavorite, checkFavorite, getManuscriptNotes, saveNote as saveNoteApi, getNote, saveParagraphProgress, getParagraphProgress, deleteParagraphProgress, saveEmotionBand, getEmotionBands, deleteEmotionBand, savePronunciationDifficulty, getPronunciationDifficultyMap, getPronunciationDifficultyByParagraph, deletePronunciationDifficulty, startPracticeSession, endPracticeSession, savePracticeSession as savePracticeSessionApi, getPracticeSessionStats, getLatestPracticeSession } from '@/api'
import { getCurrentUserId, getRhythm, saveRhythm, getProgress, saveProgress, getEmotion, saveEmotion, getDifficulty, saveDifficulty, canAccessManuscript } from '@/utils/storage'
import { splitContentSections, getParagraphSections, getParagraphIndex as calcParagraphIndex, detectManuscriptType, analyzeDifficultContent, renderAnnotatedHtml } from '@/utils/manuscript'
import DifficultyBadge from '@/components/DifficultyBadge.vue'

const route = useRoute()
const router = useRouter()
const manuscript = ref(null)
const loading = ref(false)
const notAllowed = ref(false)
const isFavorited = ref(false)
const notes = ref([])
const showNoteDialog = ref(false)
const noteForm = ref({
  difficultyPoints: '',
  toneControl: '',
  emotionExpression: '',
  otherNotes: '',
  emotionControlScore: null,
  emotionControlNote: ''
})

const userId = getCurrentUserId()

const savingNote = ref(false)
const savingRhythm = ref(false)
const savingEmotion = ref(false)
const savingStatusMap = ref({})

const rhythmPanelVisible = ref(true)
const rhythmData = ref({})
const activeParagraphIndex = ref(-1)
const currentSectionIndex = ref(0)
let scrollRaf = null
const showRhythmEditor = ref(false)
const editingParagraphIndex = ref(-1)
const rhythmForm = ref({
  pause: '',
  stress: '',
  speed: '',
  note: ''
})

const paragraphProgress = ref({})

const emotionPanelVisible = ref(true)
const emotionData = ref({})
const showEmotionEditor = ref(false)
const editingEmotionParagraphIndex = ref(-1)
const emotionForm = ref({
  emotionType: '',
  remark: ''
})

const difficultyPanelVisible = ref(false)
const difficultyAnalysis = ref({})
const difficultyStats = ref({ long: 0, tongue: 0, breath: 0, misread: 0 })

const pronunciationDifficultyMap = ref({})
const showPronunciationEditor = ref(false)
const editingPronunciationParagraphIndex = ref(-1)
const pronunciationForm = ref({
  polyphonicWords: '',
  linking: '',
  stress: '',
  breathPoints: '',
  isPublic: false
})
const savingPronunciation = ref(false)

const practiceStats = ref(null)
const practiceSessionActive = ref(false)
const currentSessionId = ref(null)
const sessionStartTime = ref(null)
const sessionTimer = ref(null)
const elapsedSeconds = ref(0)
const showPracticeEndDialog = ref(false)
const practiceForm = ref({
  completedParagraphs: '',
  selfAssessmentStatus: '',
  selfAssessmentScore: null,
  selfAssessmentNote: ''
})
const savingPractice = ref(false)

const selfAssessmentOptions = [
  { value: 'excellent', label: '优秀', color: '#67c23a' },
  { value: 'good', label: '良好', color: '#409eff' },
  { value: 'fair', label: '一般', color: '#e6a23c' },
  { value: 'needs_work', label: '需改进', color: '#f56c6c' }
]

const emotionOptions = [
  { value: 'calm', label: '平缓', color: '#909399' },
  { value: 'passionate', label: '激昂', color: '#f56c6c' },
  { value: 'low', label: '低沉', color: '#606266' },
  { value: 'bright', label: '明亮', color: '#e6a23c' },
  { value: 'gentle', label: '温柔', color: '#f48fb1' },
  { value: 'sad', label: '悲伤', color: '#409eff' },
  { value: 'joyful', label: '欢快', color: '#67c23a' },
  { value: 'solemn', label: '庄严', color: '#9b59b6' }
]

const statusOptions = [
  { value: 'mastered', label: '已熟练', type: 'success' },
  { value: 'strengthen', label: '需加强', type: 'warning' },
  { value: 'skip', label: '暂不练', type: 'info' }
]

const pauseOptions = ['短停', '中停', '长停', '换气', '停顿强调']
const stressOptions = ['重音', '轻读', '拖音', '颤音', '气声']
const speedOptions = ['慢速', '稍慢', '正常', '稍快', '快速']

const manuscriptType = computed(() => {
  if (!manuscript.value) return 'prose'
  return detectManuscriptType(manuscript.value.categoryName || '', manuscript.value.content || '')
})

const pauseMarginMap = {
  '短停': '1em',
  '中停': '1.5em',
  '长停': '2.5em',
  '换气': '1.2em',
  '停顿强调': '2em'
}

const getParagraphMargin = (paraIndex) => {
  const data = rhythmData.value[paraIndex]
  if (data?.pause && pauseMarginMap[data.pause]) {
    return pauseMarginMap[data.pause]
  }
  return manuscriptType.value === 'poetry' ? '0.8em' : '1.5em'
}

const contentSections = computed(() => {
  return splitContentSections(manuscript.value?.content, manuscriptType.value)
})

const paragraphSections = computed(() => {
  return getParagraphSections(manuscript.value?.content, manuscriptType.value)
})

const getParagraphIndex = (sectionIndex) => {
  return calcParagraphIndex(contentSections.value, sectionIndex)
}

const hasRhythmData = (index) => {
  const data = rhythmData.value[index]
  return data && (data.pause || data.stress || data.speed || data.note)
}

const progressStats = computed(() => {
  const stats = { mastered: 0, strengthen: 0, skip: 0, total: paragraphSections.value.length }
  for (let i = 0; i < paragraphSections.value.length; i++) {
    const status = paragraphProgress.value[i]
    if (status === 'mastered') stats.mastered++
    else if (status === 'strengthen') stats.strengthen++
    else if (status === 'skip') stats.skip++
  }
  return stats
})

const progressPercent = computed(() => {
  const total = paragraphSections.value.length
  if (total === 0) return 0
  return Math.round((progressStats.value.mastered / total) * 100)
})

const currentParagraphIndex = computed(() => {
  const sections = contentSections.value
  if (sections.length === 0) return 0
  let idx = currentSectionIndex.value
  if (idx < 0) idx = 0
  if (idx >= sections.length) idx = sections.length - 1
  let paraIndex = getParagraphIndex(idx)
  const total = paragraphSections.value.length
  if (paraIndex >= total) paraIndex = Math.max(0, total - 1)
  return paraIndex
})

const followProgressPercent = computed(() => {
  const total = paragraphSections.value.length
  if (total === 0) return 0
  return Math.round(((currentParagraphIndex.value + 1) / total) * 100)
})

const getParagraphStatus = (index) => {
  return paragraphProgress.value[index] || ''
}

const getStatusLabel = (status) => {
  const map = {
    mastered: '已熟练',
    strengthen: '需加强',
    skip: '暂不练'
  }
  return map[status] || '未标记'
}

const hasEmotion = (index) => {
  const data = emotionData.value[index]
  return data && data.emotionType
}

const getEmotionColor = (index) => {
  const data = emotionData.value[index]
  if (!data || !data.emotionType) return 'transparent'
  const opt = emotionOptions.find(o => o.value === data.emotionType)
  return opt ? opt.color : 'transparent'
}

const getEmotionTooltip = (index) => {
  const data = emotionData.value[index]
  if (!data) return ''
  const opt = emotionOptions.find(o => o.value === data.emotionType)
  const label = opt ? opt.label : ''
  if (data.remark) {
    return `${label}：${data.remark}`
  }
  return label
}

const getEmotionLabel = (emotionType) => {
  const opt = emotionOptions.find(o => o.value === emotionType)
  return opt ? opt.label : ''
}

const toggleEmotionPanel = () => {
  emotionPanelVisible.value = !emotionPanelVisible.value
}

const toggleDifficultyPanel = () => {
  difficultyPanelVisible.value = !difficultyPanelVisible.value
  if (difficultyPanelVisible.value && manuscript.value) {
    loadDifficultyAnalysis()
  }
}

const loadDifficultyAnalysis = () => {
  if (!manuscript.value?.content) return
  const savedData = getDifficulty(userId, route.params.id)
  if (savedData) {
    difficultyAnalysis.value = savedData.analysis || {}
    difficultyStats.value = savedData.stats || { long: 0, tongue: 0, breath: 0, misread: 0 }
    return
  }
  const result = analyzeDifficultContent(manuscript.value.content, manuscriptType.value)
  const analysisMap = {}
  result.paragraphs.forEach((p, idx) => {
    analysisMap[idx] = p
  })
  difficultyAnalysis.value = analysisMap
  difficultyStats.value = result.stats
  saveDifficulty(userId, route.params.id, { analysis: analysisMap, stats: result.stats })
}

const getAnnotatedContent = (paraIndex) => {
  const analysis = difficultyAnalysis.value[paraIndex]
  const content = paragraphSections.value[paraIndex]?.content
  if (!analysis || !content) return content || ''
  return renderAnnotatedHtml(content, analysis)
}

const selectEmotion = (emotionType) => {
  emotionForm.value.emotionType = emotionForm.value.emotionType === emotionType ? '' : emotionType
}

const openEmotionEditor = (paraIndex) => {
  editingEmotionParagraphIndex.value = paraIndex
  const data = emotionData.value[paraIndex] || {}
  emotionForm.value = {
    emotionType: data.emotionType || '',
    remark: data.remark || ''
  }
  showEmotionEditor.value = true
}

const saveEmotionData = async () => {
  if (savingEmotion.value) return
  savingEmotion.value = true
  const paraIndex = editingEmotionParagraphIndex.value
  try {
    if (emotionForm.value.emotionType) {
      emotionData.value[paraIndex] = { ...emotionForm.value }
    } else {
      delete emotionData.value[paraIndex]
    }
    saveEmotion(userId, route.params.id, emotionData.value)
    showEmotionEditor.value = false
    ElMessage.success('情感标记已保存')
    if (!emotionForm.value.emotionType) {
      await deleteEmotionBand(userId, route.params.id, paraIndex)
    } else {
      await saveEmotionBand({
        userId,
        manuscriptId: route.params.id,
        paragraphIndex: paraIndex,
        emotionType: emotionForm.value.emotionType,
        remark: emotionForm.value.remark
      })
    }
  } catch (e) {
    console.error('保存情感色带失败', e)
  } finally {
    savingEmotion.value = false
  }
}

const clearEmotion = () => {
  emotionForm.value = { emotionType: '', remark: '' }
}

const loadEmotionData = async () => {
  const localData = getEmotion(userId, route.params.id)
  if (localData) {
    emotionData.value = localData
  }
  try {
    const serverData = await getEmotionBands(userId, route.params.id)
    if (serverData) {
      emotionData.value = serverData
      saveEmotion(userId, route.params.id, serverData)
    }
  } catch (e) {
    console.error('加载情感色带失败', e)
  }
}

const setParagraphStatus = async (paraIndex, status) => {
  if (savingStatusMap.value[paraIndex]) return
  savingStatusMap.value[paraIndex] = true
  const currentStatus = paragraphProgress.value[paraIndex]
  const unmarking = currentStatus === status
  if (unmarking) {
    delete paragraphProgress.value[paraIndex]
  } else {
    paragraphProgress.value[paraIndex] = status
  }
  saveProgress(userId, route.params.id, paragraphProgress.value)
  try {
    if (unmarking) {
      await deleteParagraphProgress(userId, route.params.id, paraIndex)
    } else {
      await saveParagraphProgress({
        userId,
        manuscriptId: route.params.id,
        paragraphIndex: paraIndex,
        status
      })
    }
    ElMessage.success(unmarking ? '已取消标记' : '状态已更新')
  } catch (e) {
    console.error('保存段落进度失败', e)
    if (unmarking) {
      paragraphProgress.value[paraIndex] = currentStatus
    } else {
      if (currentStatus) {
        paragraphProgress.value[paraIndex] = currentStatus
      } else {
        delete paragraphProgress.value[paraIndex]
      }
    }
    saveProgress(userId, route.params.id, paragraphProgress.value)
  } finally {
    savingStatusMap.value[paraIndex] = false
  }
}

const isParagraphActive = (sectionIndex) => {
  if (contentSections.value[sectionIndex]?.type !== 'paragraph') return false
  const paraIndex = getParagraphIndex(sectionIndex)
  return activeParagraphIndex.value === paraIndex
}

const toggleRhythmPanel = () => {
  rhythmPanelVisible.value = !rhythmPanelVisible.value
}

const scrollToParagraph = (paraIndex) => {
  let sectionIndex = 0
  let paraCount = 0
  for (let i = 0; i < contentSections.value.length; i++) {
    if (contentSections.value[i].type === 'paragraph') {
      if (paraCount === paraIndex) {
        sectionIndex = i
        break
      }
      paraCount++
    }
  }
  const el = document.getElementById('paragraph-' + sectionIndex)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
    activeParagraphIndex.value = paraIndex
    setTimeout(() => {
      activeParagraphIndex.value = -1
    }, 2000)
  }
}

const onParagraphHover = (sectionIndex) => {
  if (contentSections.value[sectionIndex]?.type === 'paragraph') {
    activeParagraphIndex.value = getParagraphIndex(sectionIndex)
  }
}

const onParagraphLeave = () => {
  activeParagraphIndex.value = -1
}

const updateCurrentSection = () => {
  const sections = document.querySelectorAll('.content-section')
  if (!sections.length) {
    currentSectionIndex.value = 0
    return
  }
  const triggerLine = window.innerHeight * 0.35
  let current = 0
  sections.forEach((el, idx) => {
    const rect = el.getBoundingClientRect()
    if (rect.top <= triggerLine) {
      current = idx
    }
  })
  currentSectionIndex.value = current
}

const onScroll = () => {
  if (scrollRaf) return
  scrollRaf = requestAnimationFrame(() => {
    updateCurrentSection()
    scrollRaf = null
  })
}

const openRhythmEditor = (paraIndex) => {
  editingParagraphIndex.value = paraIndex
  const data = rhythmData.value[paraIndex] || {}
  rhythmForm.value = {
    pause: data.pause || '',
    stress: data.stress || '',
    speed: data.speed || '',
    note: data.note || ''
  }
  showRhythmEditor.value = true
}

const saveRhythmData = () => {
  if (savingRhythm.value) return
  savingRhythm.value = true
  const paraIndex = editingParagraphIndex.value
  if (rhythmForm.value.pause || rhythmForm.value.stress || rhythmForm.value.speed || rhythmForm.value.note) {
    rhythmData.value[paraIndex] = { ...rhythmForm.value }
  } else {
    delete rhythmData.value[paraIndex]
  }
  saveRhythm(userId, route.params.id, rhythmData.value)
  showRhythmEditor.value = false
  ElMessage.success('节奏提示已保存')
  savingRhythm.value = false
}

const loadRhythmData = () => {
  const data = getRhythm(userId, route.params.id)
  if (data) {
    rhythmData.value = data
  }
}

const loadProgressData = async () => {
  const localData = getProgress(userId, route.params.id)
  if (localData) {
    paragraphProgress.value = localData
  }
  try {
    const serverData = await getParagraphProgress(userId, route.params.id)
    if (serverData) {
      paragraphProgress.value = serverData
      saveProgress(userId, route.params.id, serverData)
    }
  } catch (e) {
    console.error('加载段落进度失败', e)
  }
}

const loadDetail = async () => {
  loading.value = true
  notAllowed.value = false
  try {
    const id = route.params.id
    manuscript.value = await getManuscriptDetail(id, userId)
    if (!manuscript.value) {
      notAllowed.value = true
      return
    }
    if (!canAccessManuscript(manuscript.value, userId)) {
      notAllowed.value = true
      manuscript.value = null
      return
    }
    await Promise.all([
      checkFavoriteStatus(),
      loadNotes()
    ])
    loadRhythmData()
    loadProgressData()
    loadEmotionData()
    loadPronunciationDifficulty()
    loadPracticeStats()
    await nextTick()
    updateCurrentSection()
  } catch (e) {
    console.error(e)
    notAllowed.value = true
  } finally {
    loading.value = false
  }
}

const checkFavoriteStatus = async () => {
  try {
    isFavorited.value = await checkFavorite(userId, route.params.id)
  } catch (e) {
    console.error(e)
  }
}

const toggleFavorite = async () => {
  try {
    if (isFavorited.value) {
      await removeFavorite(userId, route.params.id)
      isFavorited.value = false
      manuscript.value.favoriteCount--
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(userId, route.params.id)
      isFavorited.value = true
      manuscript.value.favoriteCount++
      ElMessage.success('收藏成功')
    }
  } catch (e) {
    console.error(e)
  }
}

const loadNotes = async () => {
  try {
    notes.value = await getManuscriptNotes(route.params.id)
  } catch (e) {
    console.error(e)
  }
}

const saveNote = async () => {
  if (savingNote.value) return
  savingNote.value = true
  try {
    await saveNoteApi({
      userId,
      manuscriptId: route.params.id,
      ...noteForm.value
    })
    ElMessage.success('笔记保存成功')
    showNoteDialog.value = false
    loadNotes()
  } catch (e) {
    console.error(e)
  } finally {
    savingNote.value = false
  }
}

const openNoteDialog = async () => {
  try {
    const existingNote = await getNote(userId, route.params.id)
    if (existingNote) {
      noteForm.value = {
        difficultyPoints: existingNote.difficultyPoints || '',
        toneControl: existingNote.toneControl || '',
        emotionExpression: existingNote.emotionExpression || '',
        otherNotes: existingNote.otherNotes || '',
        emotionControlScore: existingNote.emotionControlScore || null,
        emotionControlNote: existingNote.emotionControlNote || ''
      }
    }
  } catch (e) {
    console.error('加载笔记失败', e)
  }
  showNoteDialog.value = true
}

const goAuthorProfile = (author) => {
  router.push(`/author/${encodeURIComponent(author)}`)
}

const getScoreClass = (score) => {
  if (!score) return ''
  if (score >= 8) return 'score-high'
  if (score >= 5) return 'score-medium'
  return 'score-low'
}

const loadPronunciationDifficulty = async () => {
  try {
    const data = await getPronunciationDifficultyMap(route.params.id, userId)
    if (data) {
      pronunciationDifficultyMap.value = data
    }
  } catch (e) {
    console.error('加载发音难点归档失败', e)
  }
}

const openPronunciationEditor = async (paraIndex) => {
  editingPronunciationParagraphIndex.value = paraIndex
  pronunciationForm.value = {
    polyphonicWords: '',
    linking: '',
    stress: '',
    breathPoints: '',
    isPublic: false
  }
  try {
    const existing = await getPronunciationDifficultyByParagraph(route.params.id, paraIndex, userId)
    if (existing) {
      pronunciationForm.value = {
        polyphonicWords: existing.polyphonicWords || '',
        linking: existing.linking || '',
        stress: existing.stress || '',
        breathPoints: existing.breathPoints || '',
        isPublic: existing.isPublic || false
      }
    }
  } catch (e) {
    console.error('加载段落发音难点失败', e)
  }
  showPronunciationEditor.value = true
}

const savePronunciationData = async () => {
  if (savingPronunciation.value) return
  savingPronunciation.value = true
  try {
    const hasData = pronunciationForm.value.polyphonicWords || pronunciationForm.value.linking ||
                   pronunciationForm.value.stress || pronunciationForm.value.breathPoints
    if (hasData) {
      const saved = await savePronunciationDifficulty({
        manuscriptId: route.params.id,
        paragraphIndex: editingPronunciationParagraphIndex.value,
        userId,
        ...pronunciationForm.value
      })
      if (saved) {
        pronunciationDifficultyMap.value[editingPronunciationParagraphIndex.value] = saved
      }
    } else {
      const existing = pronunciationDifficultyMap.value[editingPronunciationParagraphIndex.value]
      if (existing && existing.id) {
        try {
          await deletePronunciationDifficulty(route.params.id, editingPronunciationParagraphIndex.value, userId)
          delete pronunciationDifficultyMap.value[editingPronunciationParagraphIndex.value]
        } catch (e) {
          console.error('删除发音难点归档失败', e)
        }
      }
    }
    ElMessage.success('发音难点归档已保存')
    showPronunciationEditor.value = false
  } catch (e) {
    console.error('保存发音难点归档失败', e)
  } finally {
    savingPronunciation.value = false
  }
}

const clearPronunciation = () => {
  pronunciationForm.value = {
    polyphonicWords: '',
    linking: '',
    stress: '',
    breathPoints: '',
    isPublic: false
  }
}

const loadPracticeStats = async () => {
  try {
    const stats = await getPracticeSessionStats(userId, route.params.id)
    practiceStats.value = stats
  } catch (e) {
    console.error('加载练习统计失败', e)
  }
}

const formatElapsedTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

const formatDuration = (seconds) => {
  if (!seconds || seconds === 0) return '0分钟'
  const hours = Math.floor(seconds / 3600)
  const mins = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  if (hours > 0) {
    return `${hours}小时${mins}分钟`
  } else if (mins > 0) {
    return `${mins}分钟${secs > 0 ? secs + '秒' : ''}`
  }
  return `${secs}秒`
}

const formatRelativeTime = (time) => {
  if (!time) return '暂无'
  const now = new Date()
  const target = new Date(time)
  const diff = now - target
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  return time.replace('T', ' ').substring(0, 10)
}

const startPractice = async () => {
  if (savingPractice.value) return
  savingPractice.value = true
  try {
    const startTime = new Date()
    const session = await startPracticeSession({
      userId,
      manuscriptId: route.params.id,
      startTime: startTime.toISOString()
    })
    if (session) {
      currentSessionId.value = session.id
      sessionStartTime.value = startTime
      practiceSessionActive.value = true
      elapsedSeconds.value = 0
      sessionTimer.value = setInterval(() => {
        elapsedSeconds.value++
      }, 1000)
      ElMessage.success('练习已开始，专注朗读吧！')
    }
  } catch (e) {
    console.error('开始练习失败', e)
  } finally {
    savingPractice.value = false
  }
}

const endPractice = () => {
  if (sessionTimer.value) {
    clearInterval(sessionTimer.value)
    sessionTimer.value = null
  }
  practiceForm.value = {
    completedParagraphs: '',
    selfAssessmentStatus: '',
    selfAssessmentScore: null,
    selfAssessmentNote: ''
  }
  showPracticeEndDialog.value = true
}

const selectSelfAssessment = (value) => {
  practiceForm.value.selfAssessmentStatus = practiceForm.value.selfAssessmentStatus === value ? '' : value
}

const savePracticeSession = async () => {
  if (savingPractice.value || !currentSessionId.value) return
  savingPractice.value = true
  try {
    const endTime = new Date()
    const completedParagraphs = Array.isArray(practiceForm.value.completedParagraphs)
      ? practiceForm.value.completedParagraphs.join(',')
      : practiceForm.value.completedParagraphs
    const saved = await endPracticeSession(currentSessionId.value, {
      endTime: endTime.toISOString(),
      durationSeconds: elapsedSeconds.value,
      completedParagraphs,
      selfAssessmentStatus: practiceForm.value.selfAssessmentStatus,
      selfAssessmentScore: practiceForm.value.selfAssessmentScore,
      selfAssessmentNote: practiceForm.value.selfAssessmentNote
    })
    if (saved) {
      ElMessage.success('练习记录已保存！')
      showPracticeEndDialog.value = false
      practiceSessionActive.value = false
      currentSessionId.value = null
      sessionStartTime.value = null
      elapsedSeconds.value = 0
      loadPracticeStats()
    }
  } catch (e) {
    console.error('保存练习记录失败', e)
  } finally {
    savingPractice.value = false
  }
}

onMounted(() => {
  loadDetail()
  window.addEventListener('scroll', onScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  if (scrollRaf) {
    cancelAnimationFrame(scrollRaf)
    scrollRaf = null
  }
})
</script>

<style scoped>
.manuscript-content {
  --prose-font-size: 17px;
  --prose-line-height: 2.2;
  --prose-letter-spacing: 0.05em;
  --prose-max-width: 720px;
  --prose-padding-x: 60px;
  
  --poetry-font-size: 18px;
  --poetry-line-height: 1.8;
  --poetry-letter-spacing: 0.1em;
  --poetry-max-width: 600px;
  --poetry-padding-x: 80px;
}

.article-header {
  text-align: center;
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid #ebeef5;
}

.article-title {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 20px;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 20px;
  align-items: center;
}

.meta-item {
  font-size: 14px;
  color: #909399;
}

.author-link {
  color: #409eff;
  cursor: pointer;
  transition: color 0.2s;
}

.author-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.introduction {
  font-size: 15px;
  color: #606266;
  line-height: 1.8;
  padding: 16px 24px;
  background: #f5f7fa;
  border-radius: 8px;
  font-style: italic;
  max-width: var(--prose-max-width);
  margin: 0 auto;
}

.progress-stats {
  margin: 20px auto 0;
  max-width: var(--prose-max-width);
}

.progress-bar {
  height: 8px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 12px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-info {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.progress-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.progress-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.progress-item.mastered .progress-dot {
  background: #67c23a;
}

.progress-item.strengthen .progress-dot {
  background: #e6a23c;
}

.progress-item.skip .progress-dot {
  background: #909399;
}

.progress-item.total {
  color: #909399;
}

.type-prose .article-body {
  font-size: var(--prose-font-size);
  line-height: var(--prose-line-height);
  letter-spacing: var(--prose-letter-spacing);
  color: #303133;
  max-width: var(--prose-max-width);
  margin: 0 auto;
  padding: 0 var(--prose-padding-x);
}

.type-poetry .article-body {
  font-size: var(--poetry-font-size);
  line-height: var(--poetry-line-height);
  letter-spacing: var(--poetry-letter-spacing);
  color: #303133;
  max-width: var(--poetry-max-width);
  margin: 0 auto;
  padding: 0 var(--poetry-padding-x);
  text-align: center;
}

.type-prose .paragraph {
  text-indent: 2em;
  text-align: justify;
}

.type-poetry .paragraph {
  text-indent: 0;
  text-align: center;
}

.section-heading {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
  margin: 32px 0 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #ecf5ff;
  text-align: left;
  text-indent: 0;
  letter-spacing: 0;
}

.notes-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 20px;
  margin-bottom: 16px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.note-card-wrapper {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.note-sections {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.section-item {
  border-radius: 10px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.section-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.section-header {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 13px;
}

.section-icon {
  font-size: 16px;
}

.section-item .section-title {
  font-size: 13px;
  font-weight: 600;
  margin: 0;
}

.section-content {
  padding: 14px 16px 16px;
  font-size: 14px;
  color: #303133;
  line-height: 1.8;
}

.section-difficulty .section-header {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  color: #f56c6c;
}

.section-difficulty .section-content {
  background: linear-gradient(135deg, #ffffff 0%, #fef5f5 100%);
  border-left: 3px solid #f56c6c;
}

.section-tone .section-header {
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
  color: #409eff;
}

.section-tone .section-content {
  background: linear-gradient(135deg, #ffffff 0%, #f5f9ff 100%);
  border-left: 3px solid #409eff;
}

.section-emotion .section-header {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
  color: #67c23a;
}

.section-emotion .section-content {
  background: linear-gradient(135deg, #ffffff 0%, #f8fcf5 100%);
  border-left: 3px solid #67c23a;
}

.section-other .section-header {
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
  color: #e6a23c;
}

.section-other .section-content {
  background: linear-gradient(135deg, #ffffff 0%, #fefaf5 100%);
  border-left: 3px solid #e6a23c;
}

.form-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 20px 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.form-section-title:first-child {
  margin-top: 0;
}

.main-content {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.content-column {
  flex: 1;
  min-width: 0;
}

.rhythm-panel {
  width: 280px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  position: sticky;
  top: 20px;
  max-height: calc(100vh - 40px);
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.rhythm-panel.collapsed {
  width: 0;
  opacity: 0;
  overflow: hidden;
}

.rhythm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.rhythm-title {
  font-size: 16px;
  font-weight: 600;
}

.collapse-btn {
  color: #fff !important;
  padding: 4px;
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.rhythm-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.rhythm-item {
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
  background: #f5f7fa;
}

.rhythm-item:hover {
  background: #ecf5ff;
  border-color: #b3d8ff;
}

.rhythm-item.active {
  background: #ecf5ff;
  border-color: #409eff;
}

.rhythm-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.paragraph-num {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.status-badge {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.status-badge.status-mastered {
  background: #f0f9eb;
  color: #67c23a;
}

.status-badge.status-strengthen {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-badge.status-skip {
  background: #f4f4f5;
  color: #909399;
}

.status-badge.status- {
  display: none;
}

.rhythm-item-content {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.rhythm-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.rhythm-tag.pause {
  background: #fdf6ec;
  color: #e6a23c;
}

.rhythm-tag.stress {
  background: #fef0f0;
  color: #f56c6c;
}

.rhythm-tag.speed {
  background: #f0f9eb;
  color: #67c23a;
}

.tag-label {
  opacity: 0.8;
}

.tag-value {
  font-weight: 600;
}

.no-rhythm-tip {
  font-size: 12px;
  color: #c0c4cc;
  font-style: italic;
}

.rhythm-item-preview {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.manuscript-content {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  margin-bottom: 32px;
}

.content-section {
  transition: all 0.3s ease;
  border-radius: 8px;
  padding: 4px 8px;
  margin: 0 -8px;
}

.paragraph-highlight {
  background: #ecf5ff;
  box-shadow: 0 0 0 2px #409eff;
}

.paragraph-wrapper {
  position: relative;
}

.paragraph-status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding: 4px 0;
  opacity: 0.7;
  transform: translateY(0);
  transition: all 0.2s ease;
}

.content-section:hover .paragraph-status-bar {
  opacity: 1;
}

@media (hover: none) and (pointer: coarse) {
  .paragraph-status-bar {
    opacity: 1;
  }
}

.status-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.status-actions {
  display: flex;
  gap: 6px;
}

.paragraph-status-mastered {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.08) 0%, rgba(133, 206, 97, 0.05) 100%);
  border-left: 3px solid #67c23a;
  padding-left: 12px !important;
  margin-left: -12px !important;
  border-radius: 4px;
}

.paragraph-status-strengthen {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.08) 0%, rgba(245, 205, 135, 0.05) 100%);
  border-left: 3px solid #e6a23c;
  padding-left: 12px !important;
  margin-left: -12px !important;
  border-radius: 4px;
}

.paragraph-status-skip {
  background: linear-gradient(135deg, rgba(144, 147, 153, 0.06) 0%, rgba(192, 196, 204, 0.04) 100%);
  border-left: 3px solid #909399;
  padding-left: 12px !important;
  margin-left: -12px !important;
  border-radius: 4px;
  opacity: 0.6;
}

.rhythm-note-inline {
  margin: 8px 0 20px;
  padding: 12px 16px;
  background: #fdf6ec;
  border-left: 4px solid #e6a23c;
  border-radius: 4px;
  font-size: 14px;
  color: #b88230;
  line-height: 1.6;
}

.rhythm-expand-btn {
  position: fixed;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 16px 8px;
  border-radius: 0 8px 8px 0;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  writing-mode: vertical-rl;
  z-index: 100;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  transition: all 0.2s ease;
}

.rhythm-expand-btn:hover {
  padding-right: 12px;
}

.detail-page {
  max-width: 1280px;
  margin: 0 auto;
}

.detail-header {
  margin-bottom: 24px;
}

.emotion-legend {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin: 16px auto 0;
  padding: 12px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border-radius: 8px;
  max-width: var(--prose-max-width);
}

.legend-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.legend-color {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
}

.emotion-band-sidebar {
  position: absolute;
  left: -36px;
  top: 0;
  bottom: 0;
  display: flex;
  align-items: stretch;
}

.emotion-band-indicator {
  width: 6px;
  min-height: 30px;
  border-radius: 3px;
  background: #e4e7ed;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  position: relative;
}

.emotion-band-indicator.has-emotion {
  width: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.emotion-band-indicator:hover {
  transform: scaleX(1.5);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

.emotion-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
}

.emotion-add-icon {
  font-size: 10px;
  color: #909399;
  font-weight: bold;
  line-height: 1;
}

.paragraph-with-emotion {
  border-left: 4px solid transparent;
  padding-left: 12px;
  margin-left: -16px;
  border-radius: 0 4px 4px 0;
  transition: border-color 0.3s ease;
}

.emotion-remark-inline {
  margin: 8px 0 20px;
  padding: 12px 16px;
  background: #f0f9ff;
  border-left: 4px solid #409eff;
  border-radius: 4px;
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}

.emotion-picker {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.emotion-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  border-radius: 8px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #f5f7fa;
}

.emotion-option:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.emotion-option.active {
  border-color: var(--emotion-color, #409eff);
  background: #ecf5ff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.emotion-color-dot {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: transform 0.2s ease;
}

.emotion-option:hover .emotion-color-dot {
  transform: scale(1.1);
}

.emotion-label {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
}

.emotion-option.active .emotion-label {
  color: var(--emotion-color, #409eff);
}

.type-poetry .emotion-band-sidebar {
  left: -28px;
}

.type-poetry .paragraph-with-emotion {
  padding-left: 8px;
  margin-left: -12px;
}

.difficulty-legend {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin: 16px auto 0;
  padding: 12px 20px;
  background: linear-gradient(135deg, #fef9f0 0%, #fdf0e6 100%);
  border-radius: 8px;
  max-width: var(--prose-max-width);
}

.diff-legend-long {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%) !important;
}

.diff-legend-tongue {
  background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%) !important;
}

.diff-legend-breath {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%) !important;
}

.diff-legend-misread {
  background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%) !important;
}

.difficulty-panel {
  width: 280px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  position: sticky;
  top: 20px;
  max-height: calc(100vh - 40px);
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.difficulty-panel.collapsed {
  width: 0;
  opacity: 0;
  overflow: hidden;
}

.difficulty-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #e6a23c 0%, #f56c6c 100%);
  color: #fff;
}

.difficulty-panel-title {
  font-size: 16px;
  font-weight: 600;
}

.difficulty-stats-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding: 12px;
  gap: 8px;
  border-bottom: 1px solid #f0f2f5;
}

.diff-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 8px 4px;
  border-radius: 8px;
  background: #f5f7fa;
}

.diff-stat-num {
  font-size: 20px;
  font-weight: 700;
  line-height: 1;
}

.diff-stat-label {
  font-size: 11px;
  color: #909399;
}

.diff-stat-item.diff-stat-long .diff-stat-num {
  color: #409eff;
}

.diff-stat-item.diff-stat-tongue .diff-stat-num {
  color: #e6a23c;
}

.diff-stat-item.diff-stat-breath .diff-stat-num {
  color: #67c23a;
}

.diff-stat-item.diff-stat-misread .diff-stat-num {
  color: #f56c6c;
}

.difficulty-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.difficulty-group {
  margin-bottom: 12px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #f0f2f5;
  transition: all 0.2s ease;
}

.difficulty-group:hover {
  border-color: #e6a23c;
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.15);
}

.difficulty-group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: linear-gradient(135deg, #fef9f0 0%, #fdf0e6 100%);
  cursor: pointer;
  transition: background 0.2s ease;
}

.difficulty-group-header:hover {
  background: linear-gradient(135deg, #fdf0e6 0%, #fce4d0 100%);
}

.diff-group-num {
  font-size: 13px;
  font-weight: 600;
  color: #e6a23c;
}

.diff-group-arrow {
  font-size: 14px;
  color: #e6a23c;
}

.difficulty-group-items {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.diff-item {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 6px 8px;
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.5;
}

.diff-item-badge {
  flex-shrink: 0;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: 600;
  color: #fff;
}

.diff-item-long {
  background: #ecf5ff;
}

.diff-item-long .diff-item-badge {
  background: #409eff;
}

.diff-item-tongue {
  background: #fdf6ec;
}

.diff-item-tongue .diff-item-badge {
  background: #e6a23c;
}

.diff-item-breath {
  background: #f0f9eb;
}

.diff-item-breath .diff-item-badge {
  background: #67c23a;
}

.diff-item-misread {
  background: #fef0f0;
  flex-wrap: wrap;
}

.diff-item-misread .diff-item-badge {
  background: #f56c6c;
}

.diff-item-word {
  font-weight: 700;
  color: #f56c6c;
}

.diff-item-note {
  color: #909399;
  font-size: 11px;
}

.diff-item-text {
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.no-difficulty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 32px 16px;
  color: #c0c4cc;
  font-size: 14px;
}

.no-difficulty-tip .el-icon {
  font-size: 40px;
  color: #67c23a;
}

.difficulty-expand-btn {
  position: fixed;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  background: linear-gradient(135deg, #e6a23c 0%, #f56c6c 100%);
  color: #fff;
  padding: 16px 8px;
  border-radius: 8px 0 0 8px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  writing-mode: vertical-rl;
  z-index: 100;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.15);
  transition: all 0.2s ease;
}

.difficulty-expand-btn:hover {
  padding-left: 12px;
}

.paragraph :deep(.diff-long) {
  background: rgba(64, 158, 255, 0.12);
  border-bottom: 2px solid #409eff;
  padding: 1px 2px;
  border-radius: 2px;
}

.paragraph :deep(.diff-tongue) {
  background: rgba(230, 162, 60, 0.12);
  border-bottom: 2px solid #e6a23c;
  padding: 1px 2px;
  border-radius: 2px;
}

.paragraph :deep(.diff-misread) {
  border-bottom: 2px wavy #f56c6c;
  cursor: help;
  padding: 1px 2px;
  position: relative;
}

.paragraph :deep(.diff-misread:hover) {
  background: rgba(245, 108, 108, 0.08);
}

.paragraph :deep(.diff-breath-mark) {
  display: inline-block;
  font-size: 10px;
  color: #67c23a;
  margin: 0 1px;
  vertical-align: middle;
  opacity: 0.8;
  font-style: normal;
}

.section-emotion-score .section-header {
  background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
  color: #fa8c16;
}

.section-emotion-score .section-content {
  background: linear-gradient(135deg, #ffffff 0%, #fffbf0 100%);
  border-left: 3px solid #fa8c16;
}

.score-badge {
  margin-left: auto;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.score-badge.score-high {
  background: #f0f9eb;
  color: #67c23a;
}

.score-badge.score-medium {
  background: #fdf6ec;
  color: #e6a23c;
}

.score-badge.score-low {
  background: #fef0f0;
  color: #f56c6c;
}

.score-bar {
  height: 8px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 10px;
}

.score-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.score-fill.score-high {
  background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%);
}

.score-fill.score-medium {
  background: linear-gradient(90deg, #e6a23c 0%, #f0c78a 100%);
}

.score-fill.score-low {
  background: linear-gradient(90deg, #f56c6c 0%, #f89898 100%);
}

.score-note {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.emotion-score-input {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.score-num {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.not-allowed-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.timer-display {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: 4px;
  margin-left: 8px;
}

.practice-stats {
  margin: 16px auto 0;
  max-width: var(--prose-max-width);
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f9eb 100%);
  border-radius: 12px;
  padding: 16px 20px;
}

.practice-stats-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 12px;
}

.practice-stats-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.practice-stat-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 80px;
}

.practice-stat-label {
  font-size: 12px;
  color: #909399;
}

.practice-stat-value {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.diff-group-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.diff-group-actions .el-button {
  padding: 4px 8px;
  font-size: 12px;
}

.pronunciation-summary {
  background: #fdf6ec;
  border-radius: 8px;
  padding: 8px;
  margin-bottom: 8px;
  border-left: 3px solid #e6a23c;
}

.pronunciation-item {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 4px 0;
  font-size: 12px;
  line-height: 1.5;
}

.pronunciation-badge {
  flex-shrink: 0;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: 600;
  color: #fff;
}

.pronunciation-item.polyphonic {
  background: #fef0f0;
}

.pronunciation-item.polyphonic .pronunciation-badge {
  background: #f56c6c;
}

.pronunciation-item.linking {
  background: #ecf5ff;
}

.pronunciation-item.linking .pronunciation-badge {
  background: #409eff;
}

.pronunciation-item.stress {
  background: #fdf6ec;
}

.pronunciation-item.stress .pronunciation-badge {
  background: #e6a23c;
}

.pronunciation-item.breath {
  background: #f0f9eb;
}

.pronunciation-item.breath .pronunciation-badge {
  background: #67c23a;
}

.pronunciation-text {
  color: #606266;
  flex: 1;
}

.pronunciation-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #fdf6ec 0%, #fef0f0 100%);
  border-radius: 8px;
  margin-bottom: 16px;
}

.pronunciation-section-num {
  font-size: 14px;
  font-weight: 600;
  color: #e6a23c;
}

.pronunciation-section-tip {
  font-size: 12px;
  color: #909399;
}

.practice-summary-card {
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f9eb 100%);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  text-align: center;
}

.practice-summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.practice-summary-label {
  font-size: 12px;
  color: #909399;
}

.practice-summary-value {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
  font-family: 'Courier New', monospace;
}

.self-assessment-picker {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.self-assessment-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  border-radius: 8px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #f5f7fa;
}

.self-assessment-option:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.self-assessment-option.active {
  border-color: var(--assess-color, #409eff);
  background: #ecf5ff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.assess-dot {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: transform 0.2s ease;
}

.self-assessment-option:hover .assess-dot {
  transform: scale(1.1);
}

.assess-label {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
}

.self-assessment-option.active .assess-label {
  color: var(--assess-color, #409eff);
}

.practice-score-input {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.practice-score-num {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.reading-follow-bar {
  display: none;
}

@media (max-width: 768px) {
  .reading-follow-bar {
    display: block;
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 200;
    background: rgba(255, 255, 255, 0.96);
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
    box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
    border-top: 1px solid #ebeef5;
    padding: 8px 16px calc(8px + env(safe-area-inset-bottom, 0px));
  }

  .follow-bar-progress {
    height: 3px;
    background: #f0f2f5;
    border-radius: 2px;
    overflow: hidden;
    margin-bottom: 8px;
  }

  .follow-bar-fill {
    height: 100%;
    background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%);
    border-radius: 2px;
    transition: width 0.2s ease;
  }

  .follow-bar-content {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 13px;
    color: #606266;
  }

  .follow-bar-segment {
    display: flex;
    align-items: baseline;
    gap: 4px;
  }

  .follow-bar-num {
    font-size: 15px;
    font-weight: 700;
    color: #303133;
  }

  .follow-bar-total {
    font-size: 12px;
    color: #909399;
  }

  .follow-bar-status {
    display: flex;
    align-items: center;
    gap: 5px;
    margin-left: auto;
    font-size: 12px;
    color: #909399;
    padding: 2px 8px;
    border-radius: 10px;
    background: #f4f4f5;
  }

  .follow-bar-dot {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: #c0c4cc;
    flex-shrink: 0;
  }

  .follow-bar-status.status-mastered {
    color: #67c23a;
    background: #f0f9eb;
  }

  .follow-bar-status.status-mastered .follow-bar-dot {
    background: #67c23a;
  }

  .follow-bar-status.status-strengthen {
    color: #e6a23c;
    background: #fdf6ec;
  }

  .follow-bar-status.status-strengthen .follow-bar-dot {
    background: #e6a23c;
  }

  .follow-bar-status.status-skip {
    color: #909399;
    background: #f4f4f5;
  }

  .follow-bar-status.status-skip .follow-bar-dot {
    background: #909399;
  }

  .follow-bar-percent {
    font-size: 12px;
    font-weight: 600;
    color: #409eff;
    font-family: 'Courier New', monospace;
    min-width: 36px;
    text-align: right;
  }

  .detail-page {
    padding-bottom: 72px;
  }
}
</style>
