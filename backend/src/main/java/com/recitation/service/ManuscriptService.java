package com.recitation.service;

import com.recitation.dto.ManuscriptDTO;
import com.recitation.entity.Category;
import com.recitation.entity.Manuscript;
import com.recitation.repository.FavoriteRepository;
import com.recitation.repository.ManuscriptRepository;
import com.recitation.repository.ParagraphProgressRepository;
import com.recitation.repository.PracticeNoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class ManuscriptService {

    @Resource
    private ManuscriptRepository manuscriptRepository;

    @Resource
    private CategoryService categoryService;

    @Resource
    private RedisCacheService redisCacheService;

    @Resource
    private PracticeNoteRepository practiceNoteRepository;

    @Resource
    private ParagraphProgressRepository paragraphProgressRepository;

    @Resource
    private FavoriteRepository favoriteRepository;

    @Resource
    private ManuscriptParagraphService manuscriptParagraphService;

    @Transactional
    public Manuscript createManuscript(ManuscriptDTO dto) {
        Manuscript manuscript = new Manuscript();
        manuscript.setTitle(dto.getTitle());
        manuscript.setContent(dto.getContent());
        manuscript.setIntroduction(dto.getIntroduction());
        manuscript.setCategoryId(dto.getCategoryId());
        manuscript.setAuthor(dto.getAuthor());
        manuscript.setDifficulty(dto.getDifficulty());
        manuscript.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : false);
        manuscript.setCreateUser(dto.getCreateUser());

        Category category = categoryService.getCategoryById(dto.getCategoryId());
        if (category != null) {
            manuscript.setCategoryName(category.getName());
        }

        Manuscript saved = manuscriptRepository.save(manuscript);
        manuscriptParagraphService.syncParagraphs(saved.getId(), saved.getContent(), saved.getCategoryName());
        redisCacheService.evictHotManuscriptCache();
        return saved;
    }

    @Transactional
    public Manuscript updateManuscript(Long id, ManuscriptDTO dto) {
        Manuscript manuscript = manuscriptRepository.findById(id).orElse(null);
        if (manuscript == null) {
            return null;
        }
        String createUser = manuscript.getCreateUser();
        String currentUserId = dto.getCreateUser();
        if (createUser == null || !createUser.equals(currentUserId)) {
            return null;
        }
        manuscript.setTitle(dto.getTitle());
        manuscript.setContent(dto.getContent());
        manuscript.setIntroduction(dto.getIntroduction());
        manuscript.setCategoryId(dto.getCategoryId());
        manuscript.setAuthor(dto.getAuthor());
        manuscript.setDifficulty(dto.getDifficulty());
        if (dto.getIsPublic() != null) {
            manuscript.setIsPublic(dto.getIsPublic());
        }

        Category category = categoryService.getCategoryById(dto.getCategoryId());
        if (category != null) {
            manuscript.setCategoryName(category.getName());
        }

        Manuscript saved = manuscriptRepository.save(manuscript);
        manuscriptParagraphService.syncParagraphs(saved.getId(), saved.getContent(), saved.getCategoryName());
        redisCacheService.evictManuscriptCache(id);
        redisCacheService.evictHotManuscriptCache();
        return saved;
    }

    public Manuscript getManuscriptById(Long id) {
        return getManuscriptById(id, null);
    }

    public Manuscript getManuscriptById(Long id, String userId) {
        Object cached = redisCacheService.getCachedManuscript(id);
        Manuscript manuscript;
        if (cached != null) {
            manuscript = (Manuscript) cached;
        } else {
            manuscript = manuscriptRepository.findById(id).orElse(null);
            if (manuscript != null) {
                redisCacheService.cacheManuscript(id, manuscript);
            }
        }
        if (manuscript != null && !manuscript.getIsPublic()) {
            String createUser = manuscript.getCreateUser();
            String currentUserId = userId != null ? "user_" + userId : null;
            if (createUser == null || !createUser.equals(currentUserId)) {
                return null;
            }
        }
        return manuscript;
    }

    @Transactional
    public Manuscript getManuscriptDetail(Long id) {
        return getManuscriptDetail(id, null);
    }

    @Transactional
    public Manuscript getManuscriptDetail(Long id, String userId) {
        Manuscript manuscript = manuscriptRepository.findById(id).orElse(null);
        if (manuscript != null && !manuscript.getIsPublic()) {
            String createUser = manuscript.getCreateUser();
            String currentUserId = userId != null ? "user_" + userId : null;
            if (createUser == null || !createUser.equals(currentUserId)) {
                return null;
            }
        }
        if (manuscript != null) {
            manuscriptRepository.incrementViewCount(id);
            manuscript.setViewCount(manuscript.getViewCount() + 1);
            redisCacheService.evictManuscriptCache(id);
        }
        return manuscript;
    }

    public Page<Manuscript> getPublicManuscripts(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (categoryId != null && categoryId > 0) {
            return manuscriptRepository.findByIsPublicTrueAndStatusAndCategoryIdOrderByCreateTimeDesc(1, categoryId, pageable);
        }
        return manuscriptRepository.findByIsPublicTrueAndStatusOrderByCreateTimeDesc(1, pageable);
    }

    public List<Manuscript> getHotManuscripts() {
        List<?> cached = redisCacheService.getCachedHotManuscripts();
        if (cached != null && !cached.isEmpty()) {
            return (List<Manuscript>) cached;
        }
        List<Manuscript> manuscripts = manuscriptRepository.findTop10ByIsPublicTrueAndStatusOrderByFavoriteCountDesc(1);
        if (!manuscripts.isEmpty()) {
            redisCacheService.cacheHotManuscripts(manuscripts);
        }
        return manuscripts;
    }

    @Transactional
    public boolean deleteManuscript(Long id) {
        return deleteManuscript(id, null);
    }

    @Transactional
    public boolean deleteManuscript(Long id, String userId) {
        Manuscript manuscript = manuscriptRepository.findById(id).orElse(null);
        if (manuscript == null) {
            return false;
        }
        String createUser = manuscript.getCreateUser();
        String currentUserId = userId != null ? "user_" + userId : null;
        if (createUser == null || !createUser.equals(currentUserId)) {
            return false;
        }
        practiceNoteRepository.deleteByManuscriptId(id);
        paragraphProgressRepository.deleteByManuscriptId(id);
        favoriteRepository.deleteByManuscriptId(id);
        manuscriptParagraphService.deleteByManuscript(id);
        manuscriptRepository.deleteById(id);
        redisCacheService.evictManuscriptCache(id);
        redisCacheService.evictHotManuscriptCache();
        return true;
    }

    @Transactional
    public void incrementFavoriteCount(Long id) {
        manuscriptRepository.incrementFavoriteCount(id);
        redisCacheService.evictManuscriptCache(id);
        redisCacheService.evictHotManuscriptCache();
    }

    @Transactional
    public void decrementFavoriteCount(Long id) {
        manuscriptRepository.decrementFavoriteCount(id);
        redisCacheService.evictManuscriptCache(id);
        redisCacheService.evictHotManuscriptCache();
    }
}
