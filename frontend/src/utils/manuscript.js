export const detectManuscriptType = (categoryName = '', content = '') => {
  const poetryKeywords = ['诗', '词', '曲', '赋', '古诗', '唐诗', '宋词', '元曲', '绝句', '律诗', '乐府']
  const isPoetryCategory = poetryKeywords.some(kw => categoryName.includes(kw))

  if (isPoetryCategory) return 'poetry'

  const lines = content.split(/\n+/).filter(l => l.trim())
  const shortLineCount = lines.filter(l => l.trim().length <= 12).length
  const isPoetryContent = lines.length >= 4 && shortLineCount / lines.length >= 0.6

  return isPoetryContent ? 'poetry' : 'prose'
}

export const splitContentSections = (content, manuscriptType = 'prose') => {
  if (!content) return []
  const lines = content.split(/\n+/).filter(line => line.trim())
  const sections = []

  const isHeading = (trimmed) => {
    return trimmed.length < 15 && (trimmed.endsWith('：') || trimmed.endsWith(':') || /^[第零一二三四五六七八九十\d]+/.test(trimmed))
  }

  if (manuscriptType === 'poetry') {
    const mergedParagraphs = []
    lines.forEach(line => {
      const trimmed = line.trim()
      if (isHeading(trimmed)) {
        if (mergedParagraphs.length > 0) {
          sections.push({ type: 'paragraph', content: mergedParagraphs.join('\n') })
          mergedParagraphs.length = 0
        }
        sections.push({ type: 'heading', content: trimmed })
      } else {
        mergedParagraphs.push(trimmed)
      }
    })
    if (mergedParagraphs.length > 0) {
      sections.push({ type: 'paragraph', content: mergedParagraphs.join('\n') })
    }
  } else {
    lines.forEach(line => {
      const trimmed = line.trim()
      if (isHeading(trimmed)) {
        sections.push({ type: 'heading', content: trimmed })
      } else {
        sections.push({ type: 'paragraph', content: trimmed })
      }
    })
  }

  return sections
}

export const getParagraphSections = (content, manuscriptType = 'prose') => {
  return splitContentSections(content, manuscriptType).filter(s => s.type === 'paragraph')
}

export const getParagraphCount = (content, manuscriptType = 'prose') => {
  return getParagraphSections(content, manuscriptType).length
}

export const getParagraphIndex = (sections, sectionIndex) => {
  let paraIndex = 0
  for (let i = 0; i < sectionIndex; i++) {
    if (sections[i]?.type === 'paragraph') {
      paraIndex++
    }
  }
  return paraIndex
}
