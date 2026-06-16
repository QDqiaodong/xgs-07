const DRAFT_KEY = 'recitation_draft_'
const RHYTHM_KEY = 'recitation_rhythm_'
const PROGRESS_KEY = 'recitation_progress_'

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

export const getCurrentUserId = () => {
  return 1
}
