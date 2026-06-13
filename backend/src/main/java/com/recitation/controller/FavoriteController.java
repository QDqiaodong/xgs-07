package com.recitation.controller;

import com.recitation.common.Result;
import com.recitation.entity.Favorite;
import com.recitation.service.FavoriteService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Resource
    private FavoriteService favoriteService;

    @PostMapping
    public Result<Favorite> add(@RequestParam Long userId, @RequestParam Long manuscriptId) {
        Favorite favorite = favoriteService.addFavorite(userId, manuscriptId);
        if (favorite == null) {
            return Result.error("已收藏");
        }
        return Result.success(favorite);
    }

    @DeleteMapping
    public Result<Void> remove(@RequestParam Long userId, @RequestParam Long manuscriptId) {
        boolean removed = favoriteService.removeFavorite(userId, manuscriptId);
        if (!removed) {
            return Result.error("未收藏");
        }
        return Result.success();
    }

    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam Long userId, @RequestParam Long manuscriptId) {
        return Result.success(favoriteService.isFavorite(userId, manuscriptId));
    }

    @GetMapping
    public Result<Page<Favorite>> getUserFavorites(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(favoriteService.getUserFavorites(userId, page, size));
    }
}
