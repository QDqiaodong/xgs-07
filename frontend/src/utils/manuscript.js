export const splitContentSections = (content) => {
  if (!content) return []
  const lines = content.split(/\n+/).filter(line => line.trim())
  const sections = []
  lines.forEach(line => {
    const trimmed = line.trim()
    if (trimmed.length < 15 && (trimmed.endsWith('：') || trimmed.endsWith(':') || /^[第零一二三四五六七八九十\d]+/.test(trimmed))) {
      sections.push({ type: 'heading', content: trimmed })
    } else {
      sections.push({ type: 'paragraph', content: trimmed })
    }
  })
  return sections
}

export const getParagraphSections = (content) => {
  return splitContentSections(content).filter(s => s.type === 'paragraph')
}

export const getParagraphCount = (content) => {
  return getParagraphSections(content).length
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
