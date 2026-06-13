package com.recitation.service;

import com.recitation.dto.ManuscriptDTO;
import com.recitation.entity.Category;
import com.recitation.entity.Manuscript;
import com.recitation.repository.ManuscriptRepository;
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
        redisCacheService.evictHotManuscriptCache();
        return saved;
    }

    @Transactional
    public Manuscript updateManuscript(Long id, ManuscriptDTO dto) {
        Manuscript manuscript = manuscriptRepository.findById(id).orElse(null);
        if (manuscript == null) {
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
        redisCacheService.evictManuscriptCache(id);
        redisCacheService.evictHotManuscriptCache();
        return saved;
    }

    public Manuscript getManuscriptById(Long id) {
        Object cached = redisCacheService.getCachedManuscript(id);
        if (cached != null) {
            return (Manuscript) cached;
        }
        Manuscript manuscript = manuscriptRepository.findById(id).orElse(null);
        if (manuscript != null) {
            redisCacheService.cacheManuscript(id, manuscript);
        }
        return manuscript;
    }

    @Transactional
    public Manuscript getManuscriptDetail(Long id) {
        Manuscript manuscript = manuscriptRepository.findById(id).orElse(null);
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
        if (manuscriptRepository.existsById(id)) {
            manuscriptRepository.deleteById(id);
            redisCacheService.evictManuscriptCache(id);
            redisCacheService.evictHotManuscriptCache();
            return true;
        }
        return false;
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
