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

const LONG_SENTENCE_THRESHOLD = 40

const CONFUSION_SETS = [
  ['师', '施', '实', '是', '事', '世', '市', '势', '式', '示', '视', '氏'],
  ['四', '十', '时', '似', '死', '思', '司', '斯', '寺', '肆'],
  ['子', '自', '字', '紫', '资', '智', '知', '之', '只', '纸', '旨', '志'],
  ['出', '初', '处', '除', '楚', '储', '触'],
  ['在', '再', '栽', '宰', '载', '栽'],
  ['做', '作', '坐', '座', '昨', '佐'],
  ['长', '常', '场', '厂', '唱', '畅', '尝', '偿'],
  ['那', '内', '南', '年', '农', '能', '念', '宁'],
  ['拉', '来', '蓝', '连', '龙', '路', '落', '老', '力', '量', '立', '论'],
  ['人', '认', '任', '仁', '忍', '韧'],
  ['上', '商', '伤', '赏', '尚', '裳'],
  ['中', '重', '众', '种', '终', '钟', '忠'],
  ['发', '法', '翻', '凡', '反', '方', '分', '风', '非', '飞', '复', '放'],
  ['地', '得', '的', '底', '第', '递'],
  ['和', '合', '河', '何', '贺', '核', '荷'],
  ['一', '以', '已', '意', '义', '因', '引', '音'],
]

const MISREAD_ENTRIES = [
  { word: '模样', note: 'mú yàng（不读 mó）' },
  { word: '给予', note: 'jǐ yǔ（不读 gěi）' },
  { word: '似的', note: 'shì de（不读 sì）' },
  { word: '着想', note: 'zhuó xiǎng（不读 zháo）' },
  { word: '着落', note: 'zhuó luò（不读 zháo）' },
  { word: '着急', note: 'zháo jí（不读 zhuó）' },
  { word: '着火', note: 'zháo huǒ（不读 zhuó）' },
  { word: '着迷', note: 'zháo mí（不读 zhuó）' },
  { word: '处理', note: 'chǔ lǐ（不读 chù）' },
  { word: '处暑', note: 'chǔ shǔ（不读 chù）' },
  { word: '到处', note: 'dào chù（不读 chǔ）' },
  { word: '处所', note: 'chù suǒ（不读 chǔ）' },
  { word: '朝代', note: 'cháo dài（不读 zhāo）' },
  { word: '朝阳', note: 'zhāo yáng（不读 cháo）' },
  { word: '朝气', note: 'zhāo qì（不读 cháo）' },
  { word: '剥削', note: 'bō xuē（不读 xiāo）' },
  { word: '削减', note: 'xuē jiǎn（不读 xiāo）' },
  { word: '削皮', note: 'xiāo pí（不读 xuē）' },
  { word: '流血', note: 'liú xiě（口语）' },
  { word: '血液', note: 'xuè yè（书面）' },
  { word: '血压', note: 'xuè yā（书面）' },
  { word: '蛋壳', note: 'dàn ké（口语）' },
  { word: '地壳', note: 'dì qiào（书面）' },
  { word: '金蝉脱壳', note: 'qiào（不读 ké）' },
  { word: '曾经', note: 'céng jīng（不读 zēng）' },
  { word: '曾孙', note: 'zēng sūn（不读 céng）' },
  { word: '重新', note: 'chóng xīn（不读 zhòng）' },
  { word: '重量', note: 'zhòng liàng（不读 chóng）' },
  { word: '行走', note: 'xíng zǒu（不读 háng）' },
  { word: '银行', note: 'yín háng（不读 xíng）' },
  { word: '长短', note: 'cháng duǎn（不读 zhǎng）' },
  { word: '成长', note: 'chéng zhǎng（不读 cháng）' },
  { word: '教书', note: 'jiāo shū（不读 jiào）' },
  { word: '教师', note: 'jiào shī（不读 jiāo）' },
  { word: '弹琴', note: 'tán qín（不读 dàn）' },
  { word: '子弹', note: 'zǐ dàn（不读 tán）' },
  { word: '露水', note: 'lù shuǐ（不读 lòu）' },
  { word: '露面', note: 'lòu miàn（不读 lù）' },
  { word: '喝彩', note: 'hè cǎi（不读 hē）' },
  { word: '喝水', note: 'hē shuǐ（不读 hè）' },
  { word: '蔓延', note: 'màn yán（不读 wàn）' },
  { word: '瓜蔓', note: 'guā wàn（不读 màn）' },
  { word: '参加', note: 'cān jiā（不读 shēn）' },
  { word: '人参', note: 'rén shēn（不读 cān）' },
  { word: '参差', note: 'cēn cī（不读 cān）' },
  { word: '薄弱', note: 'bó ruò（不读 báo）' },
  { word: '薄饼', note: 'báo bǐng（不读 bó）' },
  { word: '薄荷', note: 'bò he（不读 bó）' },
  { word: '落枕', note: 'lào zhěn（不读 luò）' },
  { word: '丢三落四', note: 'là（不读 luò）' },
  { word: '落叶', note: 'luò yè（不读 lào）' },
  { word: '挨打', note: 'ái dǎ（不读 āi）' },
  { word: '挨近', note: 'āi jìn（不读 ái）' },
  { word: '柏树', note: 'bǎi shù（不读 bó）' },
  { word: '柏林', note: 'bó lín（不读 bǎi）' },
  { word: '开辟', note: 'pì（不读 pǐ）' },
  { word: '偏僻', note: 'pì（不读 bì）' },
  { word: '休息', note: 'xiū xi（不读 xǔ）' },
  { word: '哺乳', note: 'bǔ rǔ（不读 pǔ）' },
  { word: '逮捕', note: 'dài bǔ（不读 bǔ）' },
  { word: '立即', note: 'lì jí（不读 lì jì）' },
  { word: '淋漓尽致', note: 'lín lí（不读 lǐn lí）' },
  { word: '称心', note: 'chèn xīn（不读 chēng）' },
  { word: '称呼', note: 'chēng hu（不读 chèn）' },
  { word: '模糊', note: 'mó hu（不读 mú）' },
  { word: '模型', note: 'mó xíng（不读 mó）' },
  { word: '木模', note: 'mù mú（不读 mó）' },
  { word: '勉强', note: 'miǎn qiǎng（不读 mián）' },
  { word: '强求', note: 'qiǎng qiú（不读 qiáng）' },
  { word: '强大', note: 'qiáng dà（不读 qiǎng）' },
  { word: '倔强', note: 'jué jiàng（不读 qiáng）' },
  { word: '分泌', note: 'mì（不读 bì）' },
  { word: '秘密', note: 'mì mì（不读 bì）' },
  { word: '提供', note: 'tí gōng（不读 dī）' },
  { word: '提防', note: 'dī fáng（不读 tí）' },
  { word: '卡片', note: 'kǎ piàn（不读 kā）' },
  { word: '关卡', note: 'guǎn qiǎ（不读 kǎ）' },
  { word: '子弹', note: 'zǐ dàn（不读 tán）' },
  { word: '勾当', note: 'gòu dàng（不读 gōu）' },
  { word: '恰当', note: 'qià dàng（不读 qiā）' },
  { word: '悄声', note: 'qiǎo shēng（不读 qiāo）' },
  { word: '悄悄', note: 'qiāo qiāo（不读 qiǎo）' },
  { word: '穿着', note: 'zhuó（不读 zháo）' },
  { word: '执着', note: 'zhuó（不读 zháo）' },
  { word: '沉着', note: 'zhuó（不读 zháo）' },
  { word: '栖息', note: 'qī xī（不读 xī）' },
  { word: '悄悄话', note: 'qiāo（不读 qiǎo）' },
  { word: '载人', note: 'zài rén（不读 zǎi）' },
  { word: '记载', note: 'jì zǎi（不读 zài）' },
  { word: '千载难逢', note: 'zǎi（不读 zài）' },
  { word: '冤枉', note: 'wang（不读 wàng）' },
  { word: '玫瑰', note: 'guī（不读 gui）' },
  { word: '虽然', note: 'suī rán（不读 suí）' },
  { word: '尽管', note: 'jǐn guǎn（不读 jìn）' },
  { word: '尽然', note: 'jìn rán（不读 jǐn）' },
  { word: '尽量', note: 'jǐn liàng（不读 jìn）' },
  { word: '尽情', note: 'jìn qíng（不读 jǐn）' },
  { word: '血淋淋', note: 'xiě lín lín（不读 xuè）' },
  { word: '呕心沥血', note: 'xuè（不读 xiě）' },
  { word: '角色', note: 'jué sè（不读 jiǎo）' },
  { word: '口角', note: 'kǒu jué（不读 jiǎo）' },
  { word: '角斗', note: 'jué dòu（不读 jiǎo）' },
  { word: '主角', note: 'zhǔ jué（不读 jiǎo）' },
  { word: '配角', note: 'pèi jué（不读 jiǎo）' },
  { word: ' 厦门', note: 'xià mén（不读 shà）' },
  { word: '大厦', note: 'dà shà（不读 xià）' },
  { word: '高中', note: 'gāo zhōng（不读 zhòng）' },
  { word: '中暑', note: 'zhòng shǔ（不读 zhōng）' },
  { word: '中奖', note: 'zhòng jiǎng（不读 zhōng）' },
  { word: '中间', note: 'zhōng jiān（不读 zhòng）' },
  { word: '嫌疑', note: 'xián yí（不读 xuán）' },
  { word: '弦乐', note: 'xián yuè（不读 xuán）' },
  { word: '肖像', note: 'xiào xiàng（不读 xiāo）' },
  { word: '气喘吁吁', note: 'xū xū（不读 yū）' },
  { word: '呼吁', note: 'hū yù（不读 xū）' },
  { word: '创伤', note: 'chuāng shāng（不读 chuàng）' },
  { word: '创造', note: 'chuàng zào（不读 chuāng）' },
  { word: '开创', note: 'kāi chuàng（不读 chuāng）' },
  { word: '重创', note: 'zhòng chuāng（不读 chuàng）' },
  { word: '横行', note: 'héng xíng（不读 hèng）' },
  { word: '蛮横', note: 'màn hèng（不读 héng）' },
  { word: '横祸', note: 'hèng huò（不读 héng）' },
  { word: '奇数', note: 'jī shù（不读 qí）' },
  { word: '奇怪', note: 'qí guài（不读 jī）' },
  { word: '奇观', note: 'qí guān（不读 jī）' },
  { word: '不禁', note: 'bù jīn（不读 jìn）' },
  { word: '禁止', note: 'jìn zhǐ（不读 jīn）' },
  { word: '拘禁', note: 'jū jìn（不读 jīn）' },
  { word: '自怨自艾', note: 'zì yì（不读 ài）' },
  { word: '方兴未艾', note: 'fāng xīng wèi ài（不读 yì）' },
]

function splitSentences(text) {
  const result = []
  let current = ''
  let start = 0
  for (let i = 0; i < text.length; i++) {
    current += text[i]
    if ('。！？；'.includes(text[i])) {
      result.push({ text: current, start, end: i + 1 })
      current = ''
      start = i + 1
    }
  }
  if (current.trim()) {
    result.push({ text: current, start, end: text.length })
  }
  return result
}

function isTongueTwister(text) {
  for (const group of CONFUSION_SETS) {
    let count = 0
    for (const char of text) {
      if (group.includes(char)) count++
    }
    if (count >= 3) return true
  }
  const chars = text.replace(/[^\u4e00-\u9fa5]/g, '')
  if (chars.length < 4) return false
  const charMap = {}
  for (const c of chars) {
    charMap[c] = (charMap[c] || 0) + 1
  }
  const maxRepeat = Math.max(...Object.values(charMap))
  if (maxRepeat >= 3) return true
  return false
}

function findBreathingPoints(text) {
  const points = []
  let charCount = 0
  for (let i = 0; i < text.length; i++) {
    if (text[i] !== ' ' && text[i] !== '\n') charCount++
    if ('，、'.includes(text[i]) && charCount >= 10) {
      points.push({ position: i, char: text[i] })
      charCount = 0
    }
    if ('。！？'.includes(text[i])) {
      charCount = 0
    }
  }
  return points
}

function findMisreadWords(text) {
  const results = []
  for (const entry of MISREAD_ENTRIES) {
    const word = entry.word.trim()
    if (!word) continue
    let searchFrom = 0
    while (true) {
      const idx = text.indexOf(word, searchFrom)
      if (idx === -1) break
      results.push({ word, start: idx, end: idx + word.length, note: entry.note })
      searchFrom = idx + word.length
    }
  }
  results.sort((a, b) => a.start - b.start)
  return results
}

function escapeHtml(text) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

export const analyzeDifficultContent = (content) => {
  if (!content) return { paragraphs: [], stats: { long: 0, tongue: 0, breath: 0, misread: 0 } }

  const paragraphs = []
  const stats = { long: 0, tongue: 0, breath: 0, misread: 0 }

  const lines = content.split(/\n+/).filter(l => l.trim())
  lines.forEach((line) => {
    const sentences = splitSentences(line)
    const analysis = {
      content: line,
      longSentences: [],
      tongueTwisters: [],
      breathingPoints: [],
      misreadWords: [],
      hasAny: false
    }

    sentences.forEach(sentence => {
      const cleanText = sentence.text.replace(/[^\u4e00-\u9fa5a-zA-Z0-9，。、；：！？""''《》（）\s]/g, '')
      if (cleanText.length > LONG_SENTENCE_THRESHOLD) {
        analysis.longSentences.push(sentence)
        stats.long++
        const breaths = findBreathingPoints(sentence.text)
        analysis.breathingPoints.push(...breaths.map(b => ({
          ...b,
          globalPos: sentence.start + b.position
        })))
        stats.breath += breaths.length
      }
      if (isTongueTwister(sentence.text)) {
        analysis.tongueTwisters.push(sentence)
        stats.tongue++
      }
      const misread = findMisreadWords(sentence.text)
      analysis.misreadWords.push(...misread)
      stats.misread += misread.length
    })

    analysis.hasAny = analysis.longSentences.length > 0 ||
      analysis.tongueTwisters.length > 0 ||
      analysis.misreadWords.length > 0

    paragraphs.push(analysis)
  })

  return { paragraphs, stats }
}

export const renderAnnotatedHtml = (content, analysis) => {
  if (!content || !analysis) return escapeHtml(content || '')

  const allAnnotations = []

  analysis.longSentences.forEach(s => {
    allAnnotations.push({ start: s.start, end: s.end, type: 'long', text: s.text })
  })

  analysis.tongueTwisters.forEach(s => {
    allAnnotations.push({ start: s.start, end: s.end, type: 'tongue', text: s.text })
  })

  analysis.misreadWords.forEach(m => {
    allAnnotations.push({ start: m.start, end: m.end, type: 'misread', note: m.note })
  })

  analysis.breathingPoints.forEach(b => {
    allAnnotations.push({ start: b.globalPos, end: b.globalPos + 1, type: 'breath', char: b.char })
  })

  allAnnotations.sort((a, b) => {
    if (a.start !== b.start) return a.start - b.start
    const typeOrder = { breath: 0, misread: 1, tongue: 2, long: 3 }
    return (typeOrder[a.type] || 0) - (typeOrder[b.type] || 0)
  })

  const merged = []
  for (const ann of allAnnotations) {
    if (merged.length === 0) {
      merged.push({ ...ann })
      continue
    }
    const last = merged[merged.length - 1]
    if (ann.start <= last.end && ann.type === last.type) {
      last.end = Math.max(last.end, ann.end)
    } else {
      merged.push({ ...ann })
    }
  }

  let html = ''
  let lastEnd = 0
  for (const ann of merged) {
    if (ann.start > lastEnd) {
      html += escapeHtml(content.slice(lastEnd, ann.start))
    }
    if (ann.start < lastEnd) continue

    const text = escapeHtml(content.slice(ann.start, Math.min(ann.end, content.length)))
    if (ann.type === 'breath') {
      html += text + '<span class="diff-breath-mark" title="换气点">⏸</span>'
    } else if (ann.type === 'misread') {
      html += `<span class="diff-misread" title="${escapeHtml(ann.note || '')}">${text}</span>`
    } else if (ann.type === 'tongue') {
      html += `<span class="diff-tongue">${text}</span>`
    } else if (ann.type === 'long') {
      html += `<span class="diff-long">${text}</span>`
    } else {
      html += text
    }
    lastEnd = ann.end
  }
  if (lastEnd < content.length) {
    html += escapeHtml(content.slice(lastEnd))
  }

  return html
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
