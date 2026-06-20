package com.recitation.service;

import com.recitation.dto.ManuscriptParagraphDTO;
import com.recitation.entity.ManuscriptParagraph;
import com.recitation.repository.ManuscriptParagraphRepository;
import com.recitation.utils.ManuscriptUtils;
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
public class ManuscriptParagraphService {

    @Resource
    private ManuscriptParagraphRepository manuscriptParagraphRepository;

    @Transactional
    public void syncParagraphs(Long manuscriptId, String content, String categoryName) {
        List<String> newContents = ManuscriptUtils.splitParagraphContents(content, categoryName);
        List<ManuscriptParagraph> existing = manuscriptParagraphRepository.findByManuscriptIdOrderByParagraphIndexAsc(manuscriptId);

        Map<String, ManuscriptParagraph> byContent = new HashMap<>();
        Map<Integer, ManuscriptParagraph> byIndex = new HashMap<>();
        for (ManuscriptParagraph e : existing) {
            byIndex.put(e.getParagraphIndex(), e);
            if (e.getContent() != null) {
                byContent.putIfAbsent(e.getContent(), e);
            }
        }

        Set<Long> usedIds = new HashSet<>();
        List<ManuscriptParagraph> toSave = new ArrayList<>();
        boolean[] matched = new boolean[newContents.size()];

        for (int i = 0; i < newContents.size(); i++) {
            String body = newContents.get(i);
            ManuscriptParagraph mp = byContent.get(body);
            if (mp != null && !usedIds.contains(mp.getId())) {
                usedIds.add(mp.getId());
                toSave.add(buildParagraph(manuscriptId, i, body, mp.getReadingTip(), mp.getPracticeFocus()));
                matched[i] = true;
            }
        }

        for (int i = 0; i < newContents.size(); i++) {
            if (matched[i]) {
                continue;
            }
            String body = newContents.get(i);
            ManuscriptParagraph idxMp = byIndex.get(i);
            String readingTip = "";
            String practiceFocus = "";
            if (idxMp != null && !usedIds.contains(idxMp.getId())) {
                usedIds.add(idxMp.getId());
                readingTip = idxMp.getReadingTip();
                practiceFocus = idxMp.getPracticeFocus();
            }
            toSave.add(buildParagraph(manuscriptId, i, body, readingTip, practiceFocus));
        }

        manuscriptParagraphRepository.deleteByManuscriptId(manuscriptId);
        manuscriptParagraphRepository.flush();
        if (!toSave.isEmpty()) {
            manuscriptParagraphRepository.saveAll(toSave);
        }
    }

    private ManuscriptParagraph buildParagraph(Long manuscriptId, Integer paragraphIndex, String content,
                                                String readingTip, String practiceFocus) {
        ManuscriptParagraph mp = new ManuscriptParagraph();
        mp.setManuscriptId(manuscriptId);
        mp.setParagraphIndex(paragraphIndex);
        mp.setContent(content);
        mp.setReadingTip(readingTip != null ? readingTip : "");
        mp.setPracticeFocus(practiceFocus != null ? practiceFocus : "");
        return mp;
    }

    @Transactional
    public ManuscriptParagraph saveOrUpdate(ManuscriptParagraphDTO dto) {
        Optional<ManuscriptParagraph> existing = manuscriptParagraphRepository
                .findByManuscriptIdAndParagraphIndex(dto.getManuscriptId(), dto.getParagraphIndex());
        ManuscriptParagraph mp;
        if (existing.isPresent()) {
            mp = existing.get();
        } else {
            mp = new ManuscriptParagraph();
            mp.setManuscriptId(dto.getManuscriptId());
            mp.setParagraphIndex(dto.getParagraphIndex());
        }
        if (dto.getContent() != null) {
            mp.setContent(dto.getContent());
        } else if (mp.getContent() == null) {
            mp.setContent("");
        }
        mp.setReadingTip(dto.getReadingTip() != null ? dto.getReadingTip() : "");
        mp.setPracticeFocus(dto.getPracticeFocus() != null ? dto.getPracticeFocus() : "");
        return manuscriptParagraphRepository.save(mp);
    }

    public List<ManuscriptParagraph> getParagraphList(Long manuscriptId) {
        return manuscriptParagraphRepository.findByManuscriptIdOrderByParagraphIndexAsc(manuscriptId);
    }

    @Transactional
    public boolean deleteParagraph(Long manuscriptId, Integer paragraphIndex) {
        Optional<ManuscriptParagraph> existing = manuscriptParagraphRepository
                .findByManuscriptIdAndParagraphIndex(manuscriptId, paragraphIndex);
        if (existing.isPresent()) {
            manuscriptParagraphRepository.delete(existing.get());
            return true;
        }
        return false;
    }

    @Transactional
    public void deleteByManuscript(Long manuscriptId) {
        manuscriptParagraphRepository.deleteByManuscriptId(manuscriptId);
    }
}
