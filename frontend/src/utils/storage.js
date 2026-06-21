const DRAFT_KEY = 'recitation_draft_'
const RHYTHM_KEY = 'recitation_rhythm_'
const PROGRESS_KEY = 'recitation_progress_'
const EMOTION_KEY = 'recitation_emotion_'
const DIFFICULTY_KEY = 'recitation_difficulty_'
const TRAINING_CURRENT_KEY = 'recitation_training_current_'

const hashContent = (content) => {
  if (!content) return ''
  let hash = 0
  const str = String(content)
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash
  }
  return String(hash)
}

export const getContentHash = hashContent

export const saveDraft = (key, data) => {
  try {
    localStorage.setItem(DRAFT_KEY + key, JSON.stringify({
      data,
      timestamp: Date.now()
    }))
  } catch (e) {
    console.error('保存草稿失败', e)
  }
}

export const getDraft = (key) => {
  try {
    const item = localStorage.getItem(DRAFT_KEY + key)
    if (item) {
      const parsed = JSON.parse(item)
      return parsed.data
    }
  } catch (e) {
    console.error('读取草稿失败', e)
  }
  return null
}

export const removeDraft = (key) => {
  try {
    localStorage.removeItem(DRAFT_KEY + key)
  } catch (e) {
    console.error('删除草稿失败', e)
  }
}

export const saveRhythm = (userId, manuscriptId, rhythmData) => {
  try {
    localStorage.setItem(RHYTHM_KEY + userId + '_' + manuscriptId, JSON.stringify({
      data: rhythmData,
      timestamp: Date.now()
    }))
  } catch (e) {
    console.error('保存节奏数据失败', e)
  }
}

export const getRhythm = (userId, manuscriptId) => {
  try {
    const item = localStorage.getItem(RHYTHM_KEY + userId + '_' + manuscriptId)
    if (item) {
      const parsed = JSON.parse(item)
      return parsed.data
    }
  } catch (e) {
    console.error('读取节奏数据失败', e)
  }
  return {}
}

export const removeRhythm = (userId, manuscriptId) => {
  try {
    localStorage.removeItem(RHYTHM_KEY + userId + '_' + manuscriptId)
  } catch (e) {
    console.error('删除节奏数据失败', e)
  }
}

export const saveProgress = (userId, manuscriptId, progressData) => {
  try {
    localStorage.setItem(PROGRESS_KEY + userId + '_' + manuscriptId, JSON.stringify({
      data: progressData,
      timestamp: Date.now()
    }))
  } catch (e) {
    console.error('保存段落进度失败', e)
  }
}

export const getProgress = (userId, manuscriptId) => {
  try {
    const item = localStorage.getItem(PROGRESS_KEY + userId + '_' + manuscriptId)
    if (item) {
      const parsed = JSON.parse(item)
      return parsed.data
    }
  } catch (e) {
    console.error('读取段落进度失败', e)
  }
  return {}
}

export const removeProgress = (userId, manuscriptId) => {
  try {
    localStorage.removeItem(PROGRESS_KEY + userId + '_' + manuscriptId)
  } catch (e) {
    console.error('删除段落进度失败', e)
  }
}

export const saveTrainingCurrent = (userId, manuscriptId, currentIndex) => {
  try {
    localStorage.setItem(TRAINING_CURRENT_KEY + userId + '_' + manuscriptId, JSON.stringify({
      data: currentIndex,
      timestamp: Date.now()
    }))
  } catch (e) {
    console.error('保存跟读训练进度失败', e)
  }
}

export const getTrainingCurrent = (userId, manuscriptId) => {
  try {
    const item = localStorage.getItem(TRAINING_CURRENT_KEY + userId + '_' + manuscriptId)
    if (item) {
      const parsed = JSON.parse(item)
      return parsed.data
    }
  } catch (e) {
    console.error('读取跟读训练进度失败', e)
  }
  return null
}

export const removeTrainingCurrent = (userId, manuscriptId) => {
  try {
    localStorage.removeItem(TRAINING_CURRENT_KEY + userId + '_' + manuscriptId)
  } catch (e) {
    console.error('删除跟读训练进度失败', e)
  }
}

export const saveEmotion = (userId, manuscriptId, emotionData) => {
  try {
    localStorage.setItem(EMOTION_KEY + userId + '_' + manuscriptId, JSON.stringify({
      data: emotionData,
      timestamp: Date.now()
    }))
  } catch (e) {
    console.error('保存情感色带数据失败', e)
  }
}

export const getEmotion = (userId, manuscriptId) => {
  try {
    const item = localStorage.getItem(EMOTION_KEY + userId + '_' + manuscriptId)
    if (item) {
      const parsed = JSON.parse(item)
      return parsed.data
    }
  } catch (e) {
    console.error('读取情感色带数据失败', e)
  }
  return {}
}

export const removeEmotion = (userId, manuscriptId) => {
  try {
    localStorage.removeItem(EMOTION_KEY + userId + '_' + manuscriptId)
  } catch (e) {
    console.error('删除情感色带数据失败', e)
  }
}

export const getCurrentUserId = () => {
  return 1
}

export const USER_ID_PREFIX = 'user_'

export const formatUserId = (userId) => {
  if (userId === null || userId === undefined || userId === '') return null
  const strUserId = String(userId)
  if (strUserId.startsWith(USER_ID_PREFIX)) return strUserId
  return USER_ID_PREFIX + strUserId
}

export const canAccessManuscript = (manuscript, userId) => {
  if (!manuscript) return false
  if (manuscript.isPublic) return true
  const formattedUserId = formatUserId(userId)
  const createUser = manuscript.createUser
  return createUser && formattedUserId && createUser === formattedUserId
}

export const saveDifficulty = (userId, manuscriptId, difficultyData, contentHash) => {
  try {
    localStorage.setItem(DIFFICULTY_KEY + userId + '_' + manuscriptId, JSON.stringify({
      data: difficultyData,
      contentHash: contentHash || '',
      timestamp: Date.now()
    }))
  } catch (e) {
    console.error('保存难点标注数据失败', e)
  }
}

export const getDifficulty = (userId, manuscriptId, contentHash) => {
  try {
    const item = localStorage.getItem(DIFFICULTY_KEY + userId + '_' + manuscriptId)
    if (item) {
      const parsed = JSON.parse(item)
      if (contentHash !== undefined && contentHash !== null) {
        if (parsed.contentHash !== contentHash) {
          return null
        }
      }
      return parsed.data
    }
  } catch (e) {
    console.error('读取难点标注数据失败', e)
  }
  return null
}

export const removeDifficulty = (userId, manuscriptId) => {
  try {
    localStorage.removeItem(DIFFICULTY_KEY + userId + '_' + manuscriptId)
  } catch (e) {
    console.error('删除难点标注数据失败', e)
  }
}
