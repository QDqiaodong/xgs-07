package com.recitation.service;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheService {

    private static final String CATEGORY_KEY = "recitation:category";
    private static final String HOT_MANUSCRIPT_KEY = "recitation:manuscript:hot";
    private static final String MANUSCRIPT_KEY_PREFIX = "recitation:manuscript:";
    private static final long DEFAULT_EXPIRE = 3600;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public void cacheCategories(List<?> categories) {
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        hashOps.put(CATEGORY_KEY, "list", categories);
        redisTemplate.expire(CATEGORY_KEY, DEFAULT_EXPIRE, TimeUnit.SECONDS);
    }

    public List<?> getCachedCategories() {
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        Object result = hashOps.get(CATEGORY_KEY, "list");
        return result != null ? (List<?>) result : null;
    }

    public void cacheHotManuscripts(List<?> manuscripts) {
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        hashOps.put(HOT_MANUSCRIPT_KEY, "list", manuscripts);
        redisTemplate.expire(HOT_MANUSCRIPT_KEY, 1800, TimeUnit.SECONDS);
    }

    public List<?> getCachedHotManuscripts() {
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        Object result = hashOps.get(HOT_MANUSCRIPT_KEY, "list");
        return result != null ? (List<?>) result : null;
    }

    public void cacheManuscript(Long id, Object manuscript) {
        String key = MANUSCRIPT_KEY_PREFIX + id;
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        hashOps.put(key, "data", manuscript);
        redisTemplate.expire(key, 1800, TimeUnit.SECONDS);
    }

    public Object getCachedManuscript(Long id) {
        String key = MANUSCRIPT_KEY_PREFIX + id;
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        return hashOps.get(key, "data");
    }

    public void evictManuscriptCache(Long id) {
        String key = MANUSCRIPT_KEY_PREFIX + id;
        redisTemplate.delete(key);
    }

    public void evictCategoryCache() {
        redisTemplate.delete(CATEGORY_KEY);
    }

    public void evictHotManuscriptCache() {
        redisTemplate.delete(HOT_MANUSCRIPT_KEY);
    }
}
