const ALIAS_TO_CANONICAL = {
  '鲁迅': '周树人',
  'lao she': '老舍',
  'laoshe': '老舍',
  'ba jin': '巴金',
  'bajin': '巴金',
  'mao dun': '茅盾',
  'maodun': '茅盾',
  'cao yu': '曹禺',
  'caoyu': '曹禺',
  'guo moruo': '郭沫若',
  'guomoruo': '郭沫若',
  'zhu ziqing': '朱自清',
  'zhuziqing': '朱自清',
  'bing xin': '冰心',
  'bingxin': '冰心',
  'xie wanying': '冰心',
  'xiewanying': '冰心',
  'li bai': '李白',
  'libai': '李白',
  'du fu': '杜甫',
  'dufu': '杜甫',
  'bai juyi': '白居易',
  'baijuyi': '白居易',
  'su shi': '苏轼',
  'sushi': '苏轼',
  'su dongpo': '苏轼',
  'sudongpo': '苏轼',
  'xin qiji': '辛弃疾',
  'xinqiji': '辛弃疾',
  'lu you': '陆游',
  'luyou': '陆游',
  'wang wei': '王维',
  'wangwei': '王维',
  'tao yuanming': '陶渊明',
  'taoyuanming': '陶渊明',
  'qu yuan': '屈原',
  'quyuan': '屈原',
  'han yu': '韩愈',
  'hanyu': '韩愈',
  'liu zongyuan': '柳宗元',
  'liuzongyuan': '柳宗元',
  'ouyang xiu': '欧阳修',
  'ouyangxiu': '欧阳修',
  'wang anshi': '王安石',
  'wanganshi': '王安石',
  'zeng gong': '曾巩',
  'zenggong': '曾巩',
  'su zhe': '苏辙',
  'suzhe': '苏辙',
  'su xun': '苏洵',
  'suxun': '苏洵',
  'fan zhongyan': '范仲淹',
  'fanzhongyan': '范仲淹',
  'wen tianxiang': '文天祥',
  'wentianxiang': '文天祥',
  'yu guangzhong': '余光中',
  'yuguangzhong': '余光中',
  'haizi': '海子',
  'hai zi': '海子',
  'zha haisheng': '海子',
  'zahaisheng': '海子',
  'bei dao': '北岛',
  'beidao': '北岛',
  'zhao zhenkai': '北岛',
  'zhaozhenkai': '北岛',
  'shu ting': '舒婷',
  'shuting': '舒婷',
  'gu cheng': '顾城',
  'gucheng': '顾城',
  'gorky': '高尔基',
  'gorkiy': '高尔基',
  'pushkin': '普希金',
  'tolstoy': '托尔斯泰',
  'lev tolstoy': '托尔斯泰',
  'levtolstoy': '托尔斯泰',
  'dostoevsky': '陀思妥耶夫斯基',
  'chekhov': '契诃夫',
  'shakespeare': '莎士比亚',
  'dickens': '狄更斯',
  'hugo': '雨果',
  'balzac': '巴尔扎克',
  'maupassant': '莫泊桑',
  'flaubert': '福楼拜',
  'zola': '左拉',
  'goethe': '歌德',
  'schiller': '席勒',
  'kafka': '卡夫卡',
  'hemingway': '海明威',
  'twain': '马克·吐温',
  'mark twain': '马克·吐温',
  'marktwain': '马克·吐温',
  'whitman': '惠特曼',
  'dickinson': '狄金森',
  'frost': '弗罗斯特',
  'tagore': '泰戈尔',
  'dante': '但丁',
  'boccaccio': '薄伽丘',
  'cervantes': '塞万提斯',
  'ibsen': '易卜生',
  'andersen': '安徒生',
  'grimm': '格林兄弟'
}

export const normalizeAuthorName = (authorName) => {
  if (authorName == null) {
    return null
  }

  let normalized = String(authorName).trim()

  if (normalized === '') {
    return null
  }

  normalized = normalized.replace(/\s+/g, ' ')
  normalized = normalized.replace(/　+/g, ' ')
  normalized = normalized.trim()

  if (normalized === '') {
    return null
  }

  const lowerKey = normalized.toLowerCase()
  if (ALIAS_TO_CANONICAL[lowerKey]) {
    return ALIAS_TO_CANONICAL[lowerKey]
  }

  const noSpaceKey = normalized.replace(/\s+/g, '').toLowerCase()
  if (ALIAS_TO_CANONICAL[noSpaceKey]) {
    return ALIAS_TO_CANONICAL[noSpaceKey]
  }

  return normalized
}

export const isSameAuthor = (name1, name2) => {
  const n1 = normalizeAuthorName(name1)
  const n2 = normalizeAuthorName(name2)
  if (n1 == null && n2 == null) return true
  if (n1 == null || n2 == null) return false
  return n1 === n2
}
