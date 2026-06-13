const DRAFT_KEY = 'recitation_draft_'

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

export const getCurrentUserId = () => {
  return 1
}
