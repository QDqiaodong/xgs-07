package com.recitation.service;

import com.recitation.entity.Category;
import com.recitation.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

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
}
