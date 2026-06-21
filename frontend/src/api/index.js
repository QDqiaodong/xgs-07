import request from './request'

export const getCategories = () => request.get('/categories')

export const assessDifficulty = (content) => request.post('/difficulty/assess', { content })

export const getDifficultyLevels = () => request.get('/difficulty/levels')

export const getManuscripts = (params) => request.get('/manuscripts', { params })

export const getManuscriptDetail = (id, userId) => request.get(`/manuscripts/${id}/detail`, { params: { userId } })

export const getManuscriptById = (id, userId) => request.get(`/manuscripts/${id}`, { params: { userId } })

export const getHotManuscripts = () => request.get('/manuscripts/hot')

export const createManuscript = (data) => request.post('/manuscripts', data)

export const updateManuscript = (id, data) => request.put(`/manuscripts/${id}`, data)

export const deleteManuscript = (id, userId) => request.delete(`/manuscripts/${id}`, { params: { userId } })

export const getManuscriptParagraphs = (manuscriptId) => request.get('/manuscript-paragraphs', { params: { manuscriptId } })

export const saveManuscriptParagraph = (data) => request.post('/manuscript-paragraphs', data)

export const deleteManuscriptParagraph = (manuscriptId, paragraphIndex) => request.delete('/manuscript-paragraphs', { params: { manuscriptId, paragraphIndex } })

export const addFavorite = (userId, manuscriptId) => request.post('/favorites', null, { params: { userId, manuscriptId } })

export const removeFavorite = (userId, manuscriptId) => request.delete('/favorites', { params: { userId, manuscriptId } })

export const checkFavorite = (userId, manuscriptId) => request.get('/favorites/check', { params: { userId, manuscriptId } })

export const getFavorites = (params) => request.get('/favorites', { params })

export const saveNote = (data) => request.post('/notes', data)

export const getNote = (userId, manuscriptId) => request.get('/notes', { params: { userId, manuscriptId } })

export const getUserNotes = (params) => request.get('/notes/user', { params })

export const getManuscriptNotes = (manuscriptId) => request.get(`/notes/manuscript/${manuscriptId}`)

export const deleteNote = (id, userId) => request.delete(`/notes/${id}`, { params: { userId } })

export const getEmotionScoreTrend = (userId) => request.get('/notes/emotion-trend', { params: { userId } })

export const saveParagraphProgress = (data) => request.post('/paragraph-progress', data)

export const getParagraphProgress = (userId, manuscriptId) => request.get('/paragraph-progress', { params: { userId, manuscriptId } })

export const getParagraphProgressList = (userId, manuscriptId) => request.get('/paragraph-progress/list', { params: { userId, manuscriptId } })

export const getParagraphTrainingState = (userId, manuscriptId) => request.get('/paragraph-progress/training-state', { params: { userId, manuscriptId } })

export const getTrainingProgress = (userId, manuscriptId) => request.get('/paragraph-progress/stats', { params: { userId, manuscriptId } })

export const getUserTrainingProgressList = (userId) => request.get('/paragraph-progress/user-stats', { params: { userId } })

export const deleteParagraphProgress = (userId, manuscriptId, paragraphIndex) => request.delete('/paragraph-progress', { params: { userId, manuscriptId, paragraphIndex } })

export const saveEmotionBand = (data) => request.post('/emotion-bands', data)

export const getEmotionBands = (userId, manuscriptId) => request.get('/emotion-bands', { params: { userId, manuscriptId } })

export const getEmotionBandList = (userId, manuscriptId) => request.get('/emotion-bands/list', { params: { userId, manuscriptId } })

export const deleteEmotionBand = (userId, manuscriptId, paragraphIndex) => request.delete('/emotion-bands', { params: { userId, manuscriptId, paragraphIndex } })

export const getAuthorProfile = (name) => request.get(`/authors/${encodeURIComponent(name)}/profile`)

export const getPracticeCalendar = (userId, year, month) => request.get('/paragraph-progress/calendar', { params: { userId, year, month } })

export const savePronunciationDifficulty = (data) => request.post('/pronunciation-difficulty', data)

export const getPronunciationDifficultyMap = (manuscriptId, userId) => request.get('/pronunciation-difficulty', { params: { manuscriptId, userId } })

export const getPronunciationDifficultyList = (manuscriptId, userId) => request.get('/pronunciation-difficulty/list', { params: { manuscriptId, userId } })

export const getPronunciationDifficultyByParagraph = (manuscriptId, paragraphIndex, userId) => request.get('/pronunciation-difficulty/paragraph', { params: { manuscriptId, paragraphIndex, userId } })

export const deletePronunciationDifficulty = (manuscriptId, paragraphIndex, userId) => request.delete('/pronunciation-difficulty', { params: { manuscriptId, paragraphIndex, userId } })

export const startPracticeSession = (data) => request.post('/practice-sessions/start', data)

export const endPracticeSession = (id, data) => request.post(`/practice-sessions/${id}/end`, data)

export const savePracticeSession = (data) => request.post('/practice-sessions', data)

export const getPracticeSessions = (userId, manuscriptId) => request.get('/practice-sessions', { params: { userId, manuscriptId } })

export const getPracticeSessionsByDate = (userId, date) => request.get('/practice-sessions/date', { params: { userId, date } })

export const getPracticeSessionsByRange = (userId, startDate, endDate) => request.get('/practice-sessions/range', { params: { userId, startDate, endDate } })

export const getLatestPracticeSession = (userId, manuscriptId) => request.get('/practice-sessions/latest', { params: { userId, manuscriptId } })

export const getPracticeSessionStats = (userId, manuscriptId) => request.get('/practice-sessions/stats', { params: { userId, manuscriptId } })

export const deletePracticeSession = (id, userId) => request.delete(`/practice-sessions/${id}`, { params: { userId } })
