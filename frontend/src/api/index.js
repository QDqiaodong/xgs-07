import request from './request'

export const getCategories = () => request.get('/categories')

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

export const getTrainingProgress = (userId, manuscriptId) => request.get('/paragraph-progress/stats', { params: { userId, manuscriptId } })

export const getUserTrainingProgressList = (userId) => request.get('/paragraph-progress/user-stats', { params: { userId } })

export const deleteParagraphProgress = (userId, manuscriptId, paragraphIndex) => request.delete('/paragraph-progress', { params: { userId, manuscriptId, paragraphIndex } })

export const saveEmotionBand = (data) => request.post('/emotion-bands', data)

export const getEmotionBands = (userId, manuscriptId) => request.get('/emotion-bands', { params: { userId, manuscriptId } })

export const getEmotionBandList = (userId, manuscriptId) => request.get('/emotion-bands/list', { params: { userId, manuscriptId } })

export const deleteEmotionBand = (userId, manuscriptId, paragraphIndex) => request.delete('/emotion-bands', { params: { userId, manuscriptId, paragraphIndex } })

export const getAuthorProfile = (name) => request.get(`/authors/${encodeURIComponent(name)}/profile`)

export const getPracticeCalendar = (userId, year, month) => request.get('/paragraph-progress/calendar', { params: { userId, year, month } })
