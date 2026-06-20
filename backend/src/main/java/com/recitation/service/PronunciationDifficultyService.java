package com.recitation.service;

import com.recitation.common.BusinessException;
import com.recitation.dto.PronunciationDifficultyDTO;
import com.recitation.entity.Manuscript;
import com.recitation.entity.PronunciationDifficulty;
import com.recitation.repository.PronunciationDifficultyRepository;
import com.recitation.utils.ManuscriptUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PronunciationDifficultyService {

    @Resource
    private PronunciationDifficultyRepository pronunciationDifficultyRepository;

    @Resource
    private ManuscriptService manuscriptService;

    private void validateManuscriptAccess(Long manuscriptId, Long userId) {
        Manuscript manuscript = manuscriptService.getManuscriptById(manuscriptId);
        if (manuscript == null) {
            throw new BusinessException("文稿不存在，无法保存发音难点");
        }
        if (manuscript.getStatus() != 1) {
            throw new BusinessException("文稿状态异常，无法保存发音难点");
        }
        String userIdStr = ManuscriptUtils.formatUserId(userId);
        if (!ManuscriptUtils.canAccessManuscript(manuscript, userIdStr)) {
            throw new BusinessException("无权限访问该文稿，无法保存发音难点");
        }
    }

    @Transactional
    public PronunciationDifficulty saveOrUpdate(PronunciationDifficultyDTO dto) {
        validateManuscriptAccess(dto.getManuscriptId(), dto.getUserId());
        Boolean isPublic = dto.getIsPublic() != null ? dto.getIsPublic() : false;
        pronunciationDifficultyRepository.upsertByUniqueKey(
                dto.getManuscriptId(),
                dto.getParagraphIndex(),
                dto.getUserId(),
                isPublic,
                dto.getPolyphonicWords(),
                dto.getLinking(),
                dto.getStress(),
                dto.getBreathPoints()
        );
        return pronunciationDifficultyRepository
                .findByManuscriptIdAndParagraphIndexAndUserId(
                        dto.getManuscriptId(), dto.getParagraphIndex(), dto.getUserId())
                .orElse(null);
    }

    public List<PronunciationDifficulty> getByManuscriptId(Long manuscriptId) {
        return pronunciationDifficultyRepository.findByManuscriptId(manuscriptId);
    }

    public List<PronunciationDifficulty> getAccessibleByManuscriptAndUser(Long manuscriptId, Long userId) {
        return pronunciationDifficultyRepository.findByManuscriptIdAndUserIdOrIsPublicTrue(manuscriptId, userId);
    }

    public Map<Integer, PronunciationDifficulty> getDifficultyMap(Long manuscriptId, Long userId) {
        List<PronunciationDifficulty> list = getAccessibleByManuscriptAndUser(manuscriptId, userId);
        Map<Integer, PronunciationDifficulty> map = new LinkedHashMap<>();
        for (PronunciationDifficulty p : list) {
            if (p.getUserId() == null || p.getUserId().equals(userId) || p.getIsPublic()) {
                if (!map.containsKey(p.getParagraphIndex()) || (p.getUserId() != null && p.getUserId().equals(userId))) {
                    map.put(p.getParagraphIndex(), p);
                }
            }
        }
        return map;
    }

    public PronunciationDifficulty getByManuscriptAndParagraphAndUser(Long manuscriptId, Integer paragraphIndex, Long userId) {
        return pronunciationDifficultyRepository
                .findByManuscriptIdAndParagraphIndexAndUserId(manuscriptId, paragraphIndex, userId)
                .orElse(null);
    }

    @Transactional
    public boolean delete(Long manuscriptId, Integer paragraphIndex, Long userId) {
        pronunciationDifficultyRepository.deleteByManuscriptIdAndParagraphIndexAndUserId(manuscriptId, paragraphIndex, userId);
        return true;
    }

    public boolean hasDifficultyData(Long manuscriptId, Integer paragraphIndex) {
        List<PronunciationDifficulty> list = pronunciationDifficultyRepository.findByManuscriptId(manuscriptId);
        for (PronunciationDifficulty p : list) {
            if (p.getParagraphIndex().equals(paragraphIndex)) {
                if ((p.getPolyphonicWords() != null && !p.getPolyphonicWords().isBlank()) ||
                    (p.getLinking() != null && !p.getLinking().isBlank()) ||
                    (p.getStress() != null && !p.getStress().isBlank()) ||
                    (p.getBreathPoints() != null && !p.getBreathPoints().isBlank())) {
                    return true;
                }
            }
        }
        return false;
    }
}
