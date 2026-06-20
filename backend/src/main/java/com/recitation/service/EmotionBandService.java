package com.recitation.service;

import com.recitation.dto.EmotionBandDTO;
import com.recitation.entity.EmotionBand;
import com.recitation.repository.EmotionBandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class EmotionBandService {

    @Resource
    private EmotionBandRepository emotionBandRepository;

    @Transactional
    public EmotionBand saveOrUpdate(EmotionBandDTO dto) {
        Optional<EmotionBand> existing = emotionBandRepository
                .findByUserIdAndManuscriptIdAndParagraphIndex(dto.getUserId(), dto.getManuscriptId(), dto.getParagraphIndex());
        if (dto.getEmotionType() == null || dto.getEmotionType().isBlank()) {
            if (existing.isPresent()) {
                emotionBandRepository.delete(existing.get());
            }
            return null;
        }
        EmotionBand emotionBand;
        if (existing.isPresent()) {
            emotionBand = existing.get();
        } else {
            emotionBand = new EmotionBand();
            emotionBand.setUserId(dto.getUserId());
            emotionBand.setManuscriptId(dto.getManuscriptId());
            emotionBand.setParagraphIndex(dto.getParagraphIndex());
        }
        emotionBand.setEmotionType(dto.getEmotionType());
        emotionBand.setRemark(dto.getRemark());
        return emotionBandRepository.save(emotionBand);
    }

    public Map<Integer, Map<String, String>> getEmotionMap(Long userId, Long manuscriptId) {
        List<EmotionBand> list = emotionBandRepository.findByUserIdAndManuscriptId(userId, manuscriptId);
        Map<Integer, Map<String, String>> map = new HashMap<>();
        Set<Integer> seenParagraphs = new HashSet<>();
        for (EmotionBand e : list) {
            if (e.getEmotionType() != null && !e.getEmotionType().isBlank()) {
                if (!seenParagraphs.contains(e.getParagraphIndex())) {
                    Map<String, String> emotionData = new HashMap<>();
                    emotionData.put("emotionType", e.getEmotionType());
                    emotionData.put("remark", e.getRemark() != null ? e.getRemark() : "");
                    map.put(e.getParagraphIndex(), emotionData);
                    seenParagraphs.add(e.getParagraphIndex());
                }
            }
        }
        return map;
    }

    public List<EmotionBand> getEmotionList(Long userId, Long manuscriptId) {
        List<EmotionBand> list = emotionBandRepository.findByUserIdAndManuscriptId(userId, manuscriptId);
        Map<Integer, EmotionBand> uniqueMap = new HashMap<>();
        for (EmotionBand e : list) {
            if (e.getEmotionType() != null && !e.getEmotionType().isBlank()) {
                uniqueMap.putIfAbsent(e.getParagraphIndex(), e);
            }
        }
        return new ArrayList<>(uniqueMap.values());
    }

    @Transactional
    public boolean deleteEmotion(Long userId, Long manuscriptId, Integer paragraphIndex) {
        Optional<EmotionBand> existing = emotionBandRepository
                .findByUserIdAndManuscriptIdAndParagraphIndex(userId, manuscriptId, paragraphIndex);
        if (existing.isPresent()) {
            emotionBandRepository.delete(existing.get());
            return true;
        }
        return false;
    }
}
