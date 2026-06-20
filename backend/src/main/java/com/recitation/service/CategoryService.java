package com.recitation.service;

import com.recitation.entity.Category;
import com.recitation.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Resource
    private CategoryRepository categoryRepository;

    @Resource
    private RedisCacheService redisCacheService;

    public List<Category> getAllCategories() {
        List<?> cached = redisCacheService.getCachedCategories();
        if (cached != null && !cached.isEmpty()) {
            return (List<Category>) cached;
        }
        List<Category> categories = categoryRepository.findByStatusTrueOrderBySortOrderAsc();
        if (!categories.isEmpty()) {
            redisCacheService.cacheCategories(categories);
        }
        return categories;
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public Category getActiveCategoryById(Long id) {
        if (id == null) {
            return null;
        }
        Optional<Category> opt = categoryRepository.findByIdAndStatusTrue(id);
        return opt.orElse(null);
    }

    public boolean isCategoryValid(Long id) {
        if (id == null) {
            return false;
        }
        return categoryRepository.findByIdAndStatusTrue(id).isPresent();
    }
}
