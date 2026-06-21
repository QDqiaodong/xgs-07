package com.recitation.controller;

import com.recitation.common.Result;
import com.recitation.dto.AuthorProfileDTO;
import com.recitation.entity.EmotionBand;
import com.recitation.entity.Manuscript;
import com.recitation.repository.EmotionBandRepository;
import com.recitation.repository.ManuscriptRepository;
import com.recitation.utils.AuthorNameUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/authors")
public class AuthorProfileController {

    @Resource
    private ManuscriptRepository manuscriptRepository;

    @Resource
    private EmotionBandRepository emotionBandRepository;

    private static final Map<String, String> EMOTION_LABEL_MAP = Map.of(
            "calm", "平缓",
            "passionate", "激昂",
            "low", "低沉",
            "bright", "明亮",
            "gentle", "温柔",
            "sad", "悲伤",
            "joyful", "欢快",
            "solemn", "庄严"
    );

    private static final Map<String, Integer> DIFFICULTY_LEVEL_MAP = Map.of(
            "入门", 1,
            "初级", 2,
            "中级", 3,
            "高级", 4
    );

    private static final List<String> DIFFICULTY_ORDER = List.of("入门", "初级", "中级", "高级");

    @GetMapping("/{name}/profile")
    public Result<AuthorProfileDTO> getAuthorProfile(@PathVariable String name) {
        String normalizedName = AuthorNameUtils.normalizeForQuery(name);
        if (normalizedName == null) {
            return Result.error("作者名称无效");
        }

        List<Manuscript> exactMatch = manuscriptRepository
                .findByAuthorAndIsPublicTrueAndStatus(normalizedName, 1);

        List<Manuscript> manuscripts = new ArrayList<>(exactMatch);

        if (exactMatch.isEmpty()) {
            List<Manuscript> allPublic = manuscriptRepository.findByIsPublicTrueAndStatus(1);
            Set<String> seenIds = new HashSet<>();
            for (Manuscript m : allPublic) {
                if (AuthorNameUtils.isSameAuthor(m.getAuthor(), normalizedName) && seenIds.add(m.getId().toString())) {
                    manuscripts.add(m);
                }
            }
        } else {
            Set<String> seenIds = manuscripts.stream()
                    .map(m -> m.getId().toString())
                    .collect(Collectors.toSet());
            List<Manuscript> allPublic = manuscriptRepository.findByIsPublicTrueAndStatus(1);
            for (Manuscript m : allPublic) {
                if (AuthorNameUtils.isSameAuthor(m.getAuthor(), normalizedName) && seenIds.add(m.getId().toString())) {
                    manuscripts.add(m);
                }
            }
        }

        if (manuscripts.isEmpty()) {
            return Result.error("未找到该作者的文稿");
        }

        AuthorProfileDTO profile = new AuthorProfileDTO();
        profile.setAuthor(normalizedName);
        profile.setTotalManuscripts(manuscripts.size());
        profile.setTotalViews(manuscripts.stream().mapToInt(m -> m.getViewCount() != null ? m.getViewCount() : 0).sum());
        profile.setTotalFavorites(manuscripts.stream().mapToInt(m -> m.getFavoriteCount() != null ? m.getFavoriteCount() : 0).sum());

        profile.setGenreDistribution(buildGenreDistribution(manuscripts));
        profile.setDifficultyDistribution(buildDifficultyDistribution(manuscripts));
        profile.setExpressionDistribution(buildExpressionDistribution(manuscripts));
        profile.setManuscripts(buildManuscriptSummaries(manuscripts));

        return Result.success(profile);
    }

    private List<AuthorProfileDTO.GenreItem> buildGenreDistribution(List<Manuscript> manuscripts) {
        Map<String, Long> genreCount = manuscripts.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getCategoryName() != null ? m.getCategoryName() : "未分类",
                        Collectors.counting()
                ));

        int total = manuscripts.size();
        return genreCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new AuthorProfileDTO.GenreItem(
                        e.getKey(),
                        e.getValue().intValue(),
                        total > 0 ? (e.getValue() * 100.0 / total) : 0
                ))
                .collect(Collectors.toList());
    }

    private List<AuthorProfileDTO.DifficultyItem> buildDifficultyDistribution(List<Manuscript> manuscripts) {
        Map<String, Long> diffCount = manuscripts.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getDifficulty() != null ? m.getDifficulty() : "未设置",
                        Collectors.counting()
                ));

        int total = manuscripts.size();

        List<AuthorProfileDTO.DifficultyItem> result = new ArrayList<>();
        for (String diffName : DIFFICULTY_ORDER) {
            Long count = diffCount.get(diffName);
            if (count != null && count > 0) {
                int level = DIFFICULTY_LEVEL_MAP.getOrDefault(diffName, 0);
                result.add(new AuthorProfileDTO.DifficultyItem(
                        diffName, level, count.intValue(),
                        total > 0 ? (count * 100.0 / total) : 0
                ));
            }
        }

        Long unsetCount = diffCount.get("未设置");
        if (unsetCount != null && unsetCount > 0) {
            result.add(new AuthorProfileDTO.DifficultyItem("未设置", 0, unsetCount.intValue(),
                    total > 0 ? (unsetCount * 100.0 / total) : 0));
        }

        return result;
    }

    private List<AuthorProfileDTO.ExpressionItem> buildExpressionDistribution(List<Manuscript> manuscripts) {
        List<Long> manuscriptIds = manuscripts.stream()
                .map(Manuscript::getId)
                .collect(Collectors.toList());

        if (manuscriptIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<EmotionBand> emotionBands = emotionBandRepository.findByManuscriptIdIn(manuscriptIds);

        Set<String> uniqueKey = new HashSet<>();
        Map<String, Long> emotionCount = new HashMap<>();
        for (EmotionBand e : emotionBands) {
            if (e.getEmotionType() != null && !e.getEmotionType().isBlank()) {
                String key = e.getManuscriptId() + "_" + e.getParagraphIndex();
                if (uniqueKey.add(key)) {
                    emotionCount.merge(e.getEmotionType(), 1L, Long::sum);
                }
            }
        }

        if (emotionCount.isEmpty()) {
            return Collections.emptyList();
        }

        long total = emotionCount.values().stream().mapToLong(Long::longValue).sum();

        return emotionCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new AuthorProfileDTO.ExpressionItem(
                        e.getKey(),
                        EMOTION_LABEL_MAP.getOrDefault(e.getKey(), e.getKey()),
                        e.getValue().intValue(),
                        total > 0 ? (e.getValue() * 100.0 / total) : 0
                ))
                .collect(Collectors.toList());
    }

    private List<AuthorProfileDTO.ManuscriptSummary> buildManuscriptSummaries(List<Manuscript> manuscripts) {
        return manuscripts.stream()
                .sorted(Comparator.comparing(Manuscript::getViewCount, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(m -> {
                    AuthorProfileDTO.ManuscriptSummary s = new AuthorProfileDTO.ManuscriptSummary();
                    s.setId(m.getId());
                    s.setTitle(m.getTitle());
                    s.setCategoryName(m.getCategoryName());
                    s.setDifficulty(m.getDifficulty());
                    s.setViewCount(m.getViewCount());
                    s.setFavoriteCount(m.getFavoriteCount());
                    return s;
                })
                .collect(Collectors.toList());
    }
}
