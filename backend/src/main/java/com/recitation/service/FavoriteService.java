package com.recitation.service;

import com.recitation.common.BusinessException;
import com.recitation.entity.Favorite;
import com.recitation.entity.Manuscript;
import com.recitation.repository.FavoriteRepository;
import com.recitation.utils.ManuscriptUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

@Service
public class FavoriteService {

    @Resource
    private FavoriteRepository favoriteRepository;

    @Resource
    private ManuscriptService manuscriptService;

    @Transactional
    public Favorite addFavorite(Long userId, Long manuscriptId) {
        if (favoriteRepository.existsByUserIdAndManuscriptId(userId, manuscriptId)) {
            return null;
        }
        Manuscript manuscript = manuscriptService.getManuscriptById(manuscriptId, ManuscriptUtils.formatUserId(userId));
        if (manuscript == null) {
            throw new BusinessException("文稿不存在或无权限访问");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setManuscriptId(manuscriptId);
        Favorite saved = favoriteRepository.save(favorite);
        manuscriptService.incrementFavoriteCount(manuscriptId);
        return saved;
    }

    @Transactional
    public boolean removeFavorite(Long userId, Long manuscriptId) {
        if (favoriteRepository.existsByUserIdAndManuscriptId(userId, manuscriptId)) {
            favoriteRepository.deleteByUserIdAndManuscriptId(userId, manuscriptId);
            manuscriptService.decrementFavoriteCount(manuscriptId);
            return true;
        }
        return false;
    }

    public boolean isFavorite(Long userId, Long manuscriptId) {
        return favoriteRepository.existsByUserIdAndManuscriptId(userId, manuscriptId);
    }

    public Page<Favorite> getUserFavorites(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return favoriteRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
    }
}
