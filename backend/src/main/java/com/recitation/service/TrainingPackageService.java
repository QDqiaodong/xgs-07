package com.recitation.service;

import com.recitation.common.BusinessException;
import com.recitation.dto.TrainingPackageDTO;
import com.recitation.dto.TrainingPackageItemDTO;
import com.recitation.dto.TrainingPackageProgressDTO;
import com.recitation.entity.Manuscript;
import com.recitation.entity.ParagraphProgress;
import com.recitation.entity.TrainingPackage;
import com.recitation.entity.TrainingPackageItem;
import com.recitation.entity.TrainingPackageProgress;
import com.recitation.repository.ManuscriptRepository;
import com.recitation.repository.ParagraphProgressRepository;
import com.recitation.repository.TrainingPackageItemRepository;
import com.recitation.repository.TrainingPackageProgressRepository;
import com.recitation.repository.TrainingPackageRepository;
import com.recitation.utils.ManuscriptUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrainingPackageService {

    @Resource
    private TrainingPackageRepository trainingPackageRepository;

    @Resource
    private TrainingPackageItemRepository trainingPackageItemRepository;

    @Resource
    private TrainingPackageProgressRepository trainingPackageProgressRepository;

    @Resource
    private ManuscriptRepository manuscriptRepository;

    @Resource
    private ParagraphProgressRepository paragraphProgressRepository;

    public List<TrainingPackageDTO> listPublicPackages(String categoryId, String difficulty, Long userId) {
        List<TrainingPackage> packages;
        if (categoryId != null && !categoryId.isBlank()) {
            packages = trainingPackageRepository.findByCategoryIdAndStatusTrueAndIsPublicTrueOrderBySortOrderAsc(Long.parseLong(categoryId));
        } else if (difficulty != null && !difficulty.isBlank()) {
            packages = trainingPackageRepository.findByDifficultyAndStatusTrueAndIsPublicTrueOrderBySortOrderAsc(difficulty);
        } else {
            packages = trainingPackageRepository.findByStatusTrueAndIsPublicTrueOrderBySortOrderAscCreateTimeDesc();
        }
        return packages.stream()
                .map(p -> convertToDTO(p, userId))
                .collect(Collectors.toList());
    }

    public TrainingPackageDTO getPackageDetail(Long id, Long userId) {
        TrainingPackage trainingPackage = trainingPackageRepository.findById(id)
                .orElseThrow(() -> new BusinessException("训练包不存在"));
        if (!trainingPackage.getStatus()) {
            throw new BusinessException("训练包已被禁用");
        }
        String userIdStr = ManuscriptUtils.formatUserId(userId);
        if (!trainingPackage.getIsPublic() && !userIdStr.equals(trainingPackage.getCreateUser())) {
            throw new BusinessException("无权限访问该训练包");
        }
        TrainingPackageDTO dto = convertToDTO(trainingPackage, userId);
        List<TrainingPackageItem> items = trainingPackageItemRepository.findByPackageIdOrderBySortOrderAsc(id);
        List<TrainingPackageItemDTO> itemDTOs = new ArrayList<>();
        for (TrainingPackageItem item : items) {
            TrainingPackageItemDTO itemDTO = new TrainingPackageItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setPackageId(item.getPackageId());
            itemDTO.setManuscriptId(item.getManuscriptId());
            itemDTO.setSortOrder(item.getSortOrder());
            itemDTO.setStage(item.getStage());
            itemDTO.setStageNote(item.getStageNote());
            manuscriptRepository.findById(item.getManuscriptId()).ifPresent(m -> {
                itemDTO.setManuscriptTitle(m.getTitle());
                itemDTO.setManuscriptAuthor(m.getAuthor());
                itemDTO.setManuscriptDifficulty(m.getDifficulty());
            });
            itemDTOs.add(itemDTO);
        }
        dto.setItems(itemDTOs);
        return dto;
    }

    @Transactional
    public TrainingPackage createPackage(TrainingPackageDTO dto) {
        TrainingPackage trainingPackage = new TrainingPackage();
        copyDTOToEntity(dto, trainingPackage);
        trainingPackage = trainingPackageRepository.save(trainingPackage);
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            List<TrainingPackageItem> items = new ArrayList<>();
            int sortOrder = 0;
            for (TrainingPackageItemDTO itemDTO : dto.getItems()) {
                TrainingPackageItem item = new TrainingPackageItem();
                item.setPackageId(trainingPackage.getId());
                item.setManuscriptId(itemDTO.getManuscriptId());
                item.setSortOrder(sortOrder++);
                item.setStage(itemDTO.getStage());
                item.setStageNote(itemDTO.getStageNote());
                items.add(item);
            }
            trainingPackageItemRepository.saveAll(items);
        }
        return trainingPackage;
    }

    @Transactional
    public TrainingPackage updatePackage(Long id, TrainingPackageDTO dto) {
        TrainingPackage trainingPackage = trainingPackageRepository.findById(id)
                .orElseThrow(() -> new BusinessException("训练包不存在"));
        copyDTOToEntity(dto, trainingPackage);
        trainingPackage = trainingPackageRepository.save(trainingPackage);
        if (dto.getItems() != null) {
            trainingPackageItemRepository.deleteByPackageId(id);
            List<TrainingPackageItem> items = new ArrayList<>();
            int sortOrder = 0;
            for (TrainingPackageItemDTO itemDTO : dto.getItems()) {
                TrainingPackageItem item = new TrainingPackageItem();
                item.setPackageId(trainingPackage.getId());
                item.setManuscriptId(itemDTO.getManuscriptId());
                item.setSortOrder(sortOrder++);
                item.setStage(itemDTO.getStage());
                item.setStageNote(itemDTO.getStageNote());
                items.add(item);
            }
            trainingPackageItemRepository.saveAll(items);
        }
        return trainingPackage;
    }

    @Transactional
    public boolean deletePackage(Long id, Long userId) {
        TrainingPackage trainingPackage = trainingPackageRepository.findById(id)
                .orElseThrow(() -> new BusinessException("训练包不存在"));
        String userIdStr = ManuscriptUtils.formatUserId(userId);
        if (!userIdStr.equals(trainingPackage.getCreateUser())) {
            throw new BusinessException("无权限删除该训练包");
        }
        trainingPackageItemRepository.deleteByPackageId(id);
        trainingPackageRepository.delete(trainingPackage);
        return true;
    }

    public TrainingPackageProgressDTO startOrGetProgress(Long userId, Long packageId) {
        Optional<TrainingPackageProgress> existing = trainingPackageProgressRepository
                .findByUserIdAndPackageId(userId, packageId);
        TrainingPackageProgress progress;
        if (existing.isPresent()) {
            progress = existing.get();
        } else {
            progress = new TrainingPackageProgress();
            progress.setUserId(userId);
            progress.setPackageId(packageId);
            progress.setStatus("in_progress");
            progress.setStartDate(LocalDate.now());
            progress = trainingPackageProgressRepository.save(progress);
        }
        return calculateProgressDetail(progress);
    }

    public List<TrainingPackageProgressDTO> getUserProgressList(Long userId) {
        List<TrainingPackageProgress> progressList = trainingPackageProgressRepository.findByUserId(userId);
        return progressList.stream()
                .map(this::calculateProgressDetail)
                .collect(Collectors.toList());
    }

    public TrainingPackageProgressDTO updateProgress(TrainingPackageProgressDTO dto) {
        TrainingPackageProgress progress = trainingPackageProgressRepository
                .findByUserIdAndPackageId(dto.getUserId(), dto.getPackageId())
                .orElseThrow(() -> new BusinessException("进度不存在"));
        if (dto.getStatus() != null) {
            progress.setStatus(dto.getStatus());
            if ("completed".equals(dto.getStatus())) {
                progress.setCompleteDate(LocalDate.now());
            }
        }
        if (dto.getCurrentManuscriptIndex() != null) {
            progress.setCurrentManuscriptIndex(dto.getCurrentManuscriptIndex());
        }
        if (dto.getTotalPracticeMinutes() != null) {
            progress.setTotalPracticeMinutes(dto.getTotalPracticeMinutes());
        }
        if (dto.getTotalPracticeCount() != null) {
            progress.setTotalPracticeCount(dto.getTotalPracticeCount());
        }
        progress = trainingPackageProgressRepository.save(progress);
        return calculateProgressDetail(progress);
    }

    @Transactional
    public TrainingPackageProgressDTO recordPractice(Long userId, Long packageId, int practiceMinutes) {
        TrainingPackageProgress progress = trainingPackageProgressRepository
                .findByUserIdAndPackageId(userId, packageId)
                .orElseThrow(() -> new BusinessException("请先开始训练"));
        progress.setTotalPracticeMinutes(progress.getTotalPracticeMinutes() + practiceMinutes);
        progress.setTotalPracticeCount(progress.getTotalPracticeCount() + 1);
        progress = trainingPackageProgressRepository.save(progress);
        return calculateProgressDetail(progress);
    }

    private TrainingPackageProgressDTO calculateProgressDetail(TrainingPackageProgress progress) {
        TrainingPackageProgressDTO dto = new TrainingPackageProgressDTO();
        dto.setId(progress.getId());
        dto.setUserId(progress.getUserId());
        dto.setPackageId(progress.getPackageId());
        dto.setStatus(progress.getStatus());
        dto.setStartDate(progress.getStartDate());
        dto.setCompleteDate(progress.getCompleteDate());
        dto.setCurrentManuscriptIndex(progress.getCurrentManuscriptIndex());
        dto.setTotalPracticeMinutes(progress.getTotalPracticeMinutes());
        dto.setTotalPracticeCount(progress.getTotalPracticeCount());

        List<TrainingPackageItem> items = trainingPackageItemRepository
                .findByPackageIdOrderBySortOrderAsc(progress.getPackageId());
        int totalManuscripts = items.size();
        dto.setTotalManuscripts(totalManuscripts);

        int completedCount = 0;
        for (TrainingPackageItem item : items) {
            List<ParagraphProgress> paragraphProgresses = paragraphProgressRepository
                    .findByUserIdAndManuscriptId(progress.getUserId(), item.getManuscriptId());
            long masteredCount = paragraphProgresses.stream()
                    .filter(p -> "mastered".equals(p.getStatus()))
                    .count();
            Optional<Manuscript> manuscriptOpt = manuscriptRepository.findById(item.getManuscriptId());
            int paragraphCount = manuscriptOpt
                    .map(m -> ManuscriptUtils.countParagraphs(m.getContent()))
                    .orElse(0);
            if (paragraphCount > 0 && masteredCount >= paragraphCount) {
                completedCount++;
            }
        }
        dto.setCompletedManuscripts(completedCount);
        dto.setCompletionPercent(totalManuscripts > 0 ? (double) completedCount / totalManuscripts * 100 : 0.0);

        if (totalManuscripts > 0 && completedCount >= totalManuscripts &&
                !"completed".equals(progress.getStatus())) {
            progress.setStatus("completed");
            progress.setCompleteDate(LocalDate.now());
            trainingPackageProgressRepository.save(progress);
            dto.setStatus("completed");
            dto.setCompleteDate(progress.getCompleteDate());
        }

        return dto;
    }

    public Map<String, Object> getPackageWithProgress(Long packageId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("package", getPackageDetail(packageId, userId));
        try {
            result.put("progress", startOrGetProgress(userId, packageId));
        } catch (Exception e) {
            result.put("progress", null);
        }
        return result;
    }

    private TrainingPackageDTO convertToDTO(TrainingPackage entity, Long userId) {
        TrainingPackageDTO dto = new TrainingPackageDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCoverImage(entity.getCoverImage());
        dto.setCategoryId(entity.getCategoryId());
        dto.setCategoryName(entity.getCategoryName());
        dto.setDifficulty(entity.getDifficulty());
        dto.setTargetDuration(entity.getTargetDuration());
        dto.setTargetDays(entity.getTargetDays());
        dto.setTrainingGoals(entity.getTrainingGoals());
        dto.setTips(entity.getTips());
        dto.setSortOrder(entity.getSortOrder());
        dto.setStatus(entity.getStatus());
        dto.setIsPublic(entity.getIsPublic());
        dto.setCreateUser(entity.getCreateUser());
        return dto;
    }

    private void copyDTOToEntity(TrainingPackageDTO dto, TrainingPackage entity) {
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getCoverImage() != null) entity.setCoverImage(dto.getCoverImage());
        if (dto.getCategoryId() != null) entity.setCategoryId(dto.getCategoryId());
        if (dto.getCategoryName() != null) entity.setCategoryName(dto.getCategoryName());
        if (dto.getDifficulty() != null) entity.setDifficulty(dto.getDifficulty());
        if (dto.getTargetDuration() != null) entity.setTargetDuration(dto.getTargetDuration());
        if (dto.getTargetDays() != null) entity.setTargetDays(dto.getTargetDays());
        if (dto.getTrainingGoals() != null) entity.setTrainingGoals(dto.getTrainingGoals());
        if (dto.getTips() != null) entity.setTips(dto.getTips());
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getIsPublic() != null) entity.setIsPublic(dto.getIsPublic());
        if (dto.getCreateUser() != null) entity.setCreateUser(dto.getCreateUser());
    }
}
