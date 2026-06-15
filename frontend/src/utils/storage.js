const DRAFT_KEY = 'recitation_draft_'
const RHYTHM_KEY = 'recitation_rhythm_'

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

export const saveRhythm = (manuscriptId, rhythmData) => {
  try {
    localStorage.setItem(RHYTHM_KEY + manuscriptId, JSON.stringify({
      data: rhythmData,
      timestamp: Date.now()
    }))
  } catch (e) {
    console.error('保存节奏数据失败', e)
  }
}

export const getRhythm = (manuscriptId) => {
  try {
    const item = localStorage.getItem(RHYTHM_KEY + manuscriptId)
    if (item) {
      const parsed = JSON.parse(item)
      return parsed.data
    }
  } catch (e) {
    console.error('读取节奏数据失败', e)
  }
  return {}
}

export const removeRhythm = (manuscriptId) => {
  try {
    localStorage.removeItem(RHYTHM_KEY + manuscriptId)
  } catch (e) {
    console.error('删除节奏数据失败', e)
  }
}

export const getCurrentUserId = () => {
  return 1
}
