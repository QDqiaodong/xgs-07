import request from './request'

export const getCategories = () => request.get('/categories')

export const getManuscripts = (params) => request.get('/manuscripts', { params })

export const getManuscriptDetail = (id) => request.get(`/manuscripts/${id}/detail`)

export const getManuscriptById = (id) => request.get(`/manuscripts/${id}`)

export const getHotManuscripts = () => request.get('/manuscripts/hot')

export const createManuscript = (data) => request.post('/manuscripts', data)

export const updateManuscript = (id, data) => request.put(`/manuscripts/${id}`, data)

export const deleteManuscript = (id) => request.delete(`/manuscripts/${id}`)

export const addFavorite = (userId, manuscriptId) => request.post('/favorites', null, { params: { userId, manuscriptId } })

export const removeFavorite = (userId, manuscriptId) => request.delete('/favorites', { params: { userId, manuscriptId } })

export const checkFavorite = (userId, manuscriptId) => request.get('/favorites/check', { params: { userId, manuscriptId } })

export const getFavorites = (params) => request.get('/favorites', { params })

export const saveNote = (data) => request.post('/notes', data)

export const getNote = (userId, manuscriptId) => request.get('/notes', { params: { userId, manuscriptId } })

export const getUserNotes = (params) => request.get('/notes/user', { params })

export const getManuscriptNotes = (manuscriptId) => request.get(`/notes/manuscript/${manuscriptId}`)

export const deleteNote = (id, userId) => request.delete(`/notes/${id}`, { params: { userId } })

export const saveParagraphProgress = (data) => request.post('/paragraph-progress', data)

export const getParagraphProgress = (userId, manuscriptId) => request.get('/paragraph-progress', { params: { userId, manuscriptId } })

export const getParagraphProgressList = (userId, manuscriptId) => request.get('/paragraph-progress/list', { params: { userId, manuscriptId } })

export const deleteParagraphProgress = (userId, manuscriptId, paragraphIndex) => request.delete('/paragraph-progress', { params: { userId, manuscriptId, paragraphIndex } })

export const saveEmotionBand = (data) => request.post('/emotion-bands', data)

export const getEmotionBands = (userId, manuscriptId) => request.get('/emotion-bands', { params: { userId, manuscriptId } })

export const getEmotionBandList = (userId, manuscriptId) => request.get('/emotion-bands/list', { params: { userId, manuscriptId } })

export const deleteEmotionBand = (userId, manuscriptId, paragraphIndex) => request.delete('/emotion-bands', { params: { userId, manuscriptId, paragraphIndex } })
