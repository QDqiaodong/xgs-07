package com.recitation.service.difficulty;

import com.recitation.enums.DifficultyLevel;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 难度等级规则库：为入门、初级、中级、高级文稿建立的统一判定标准。
 * <p>
 * 结合三个维度进行判定，保证文稿难度划分更一致：
 * <ul>
 *     <li>篇幅（length）：以非空白字符数衡量朗读体量</li>
 *     <li>句式复杂度（complexity）：以平均句长衡量句式结构复杂程度</li>
 *     <li>情绪跨度（emotion）：以文本出现的情绪类别数量衡量情绪广度</li>
 * </ul>
 * 各维度的等级阈值集中维护在此处，作为难度判定的唯一事实来源。
 */
@Component
public class DifficultyRuleLibrary {

    public static final String DIMENSION_LENGTH = "length";
    public static final String DIMENSION_COMPLEXITY = "complexity";
    public static final String DIMENSION_EMOTION = "emotion";

    public static final String LABEL_LENGTH = "篇幅";
    public static final String LABEL_COMPLEXITY = "句式复杂度";
    public static final String LABEL_EMOTION = "情绪跨度";

    private static final double WEIGHT_LENGTH = 0.3;
    private static final double WEIGHT_COMPLEXITY = 0.4;
    private static final double WEIGHT_EMOTION = 0.3;

    private static final int LENGTH_LV1 = 200;
    private static final int LENGTH_LV2 = 500;
    private static final int LENGTH_LV3 = 1000;

    private static final double COMPLEXITY_LV1 = 15;
    private static final double COMPLEXITY_LV2 = 25;
    private static final double COMPLEXITY_LV3 = 40;

    private static final int EMOTION_LV1 = 1;
    private static final int EMOTION_LV2 = 2;
    private static final int EMOTION_LV3 = 3;

    /**
     * 情绪词典：情绪类别 -> 关键词集合。
     * 通过关键词在文本中的命中情况统计情绪类别数量，作为情绪跨度的度量。
     */
    private static final Map<String, List<String>> EMOTION_DICTIONARY;

    static {
        Map<String, List<String>> dict = new LinkedHashMap<>();
        dict.put("喜", Arrays.asList(
                "喜", "乐", "欢", "笑", "欣", "悦", "愉", "畅", "雀跃", "开心", "快乐",
                "欣喜", "欢乐", "欢笑", "喜悦", "欢欣", "欢腾", "愉悦"));
        dict.put("怒", Arrays.asList(
                "怒", "愤", "恨", "怨", "恼", "怒火", "愤怒", "愤恨", "怨恨", "恼怒",
                "激愤", "愠怒", "愤然"));
        dict.put("哀", Arrays.asList(
                "哀", "悲", "愁", "泣", "哭", "泪", "凄", "怅", "悲伤", "哀愁",
                "哀伤", "悲痛", "凄凉", "哀婉", "凄清", "愁苦"));
        dict.put("惧", Arrays.asList(
                "惧", "怕", "畏", "恐", "惶", "害怕", "畏惧", "恐惧", "惶恐",
                "惊恐", "惶惑", "畏缩", "惊慌"));
        dict.put("惊", Arrays.asList(
                "惊", "讶", "奇", "惊讶", "惊奇", "诧异", "震惊", "惊异",
                "惊叹", "骇然", "惊愕"));
        dict.put("思", Arrays.asList(
                "思", "念", "忆", "恋", "盼", "望", "怀念", "思念", "期望",
                "思慕", "眷恋", "怅惘", "缅怀", "憧憬"));
        EMOTION_DICTIONARY = Collections.unmodifiableMap(dict);
    }

    public Map<String, List<String>> getEmotionDictionary() {
        return EMOTION_DICTIONARY;
    }

    public double getWeight(String dimension) {
        switch (dimension) {
            case DIMENSION_LENGTH:
                return WEIGHT_LENGTH;
            case DIMENSION_COMPLEXITY:
                return WEIGHT_COMPLEXITY;
            case DIMENSION_EMOTION:
                return WEIGHT_EMOTION;
            default:
                return 0d;
        }
    }

    public String getDimensionLabel(String dimension) {
        switch (dimension) {
            case DIMENSION_LENGTH:
                return LABEL_LENGTH;
            case DIMENSION_COMPLEXITY:
                return LABEL_COMPLEXITY;
            case DIMENSION_EMOTION:
                return LABEL_EMOTION;
            default:
                return dimension;
        }
    }

    public DifficultyLevel levelForLength(int charCount) {
        if (charCount <= LENGTH_LV1) {
            return DifficultyLevel.BEGINNER;
        } else if (charCount <= LENGTH_LV2) {
            return DifficultyLevel.PRIMARY;
        } else if (charCount <= LENGTH_LV3) {
            return DifficultyLevel.INTERMEDIATE;
        }
        return DifficultyLevel.ADVANCED;
    }

    public DifficultyLevel levelForComplexity(double avgSentenceLength) {
        if (avgSentenceLength <= COMPLEXITY_LV1) {
            return DifficultyLevel.BEGINNER;
        } else if (avgSentenceLength <= COMPLEXITY_LV2) {
            return DifficultyLevel.PRIMARY;
        } else if (avgSentenceLength <= COMPLEXITY_LV3) {
            return DifficultyLevel.INTERMEDIATE;
        }
        return DifficultyLevel.ADVANCED;
    }

    public DifficultyLevel levelForEmotion(int emotionCategoryCount) {
        if (emotionCategoryCount <= EMOTION_LV1) {
            return DifficultyLevel.BEGINNER;
        } else if (emotionCategoryCount == EMOTION_LV2) {
            return DifficultyLevel.PRIMARY;
        } else if (emotionCategoryCount == EMOTION_LV3) {
            return DifficultyLevel.INTERMEDIATE;
        }
        return DifficultyLevel.ADVANCED;
    }

    public String describeLength(DifficultyLevel level) {
        switch (level) {
            case BEGINNER:
                return "篇幅较短（≤" + LENGTH_LV1 + " 字），朗读体量小";
            case PRIMARY:
                return "篇幅适中（" + (LENGTH_LV1 + 1) + "-" + LENGTH_LV2 + " 字）";
            case INTERMEDIATE:
                return "篇幅较长（" + (LENGTH_LV2 + 1) + "-" + LENGTH_LV3 + " 字）";
            case ADVANCED:
                return "篇幅长（>" + LENGTH_LV3 + " 字），朗读耐力要求高";
            default:
                return "";
        }
    }

    public String describeComplexity(DifficultyLevel level) {
        switch (level) {
            case BEGINNER:
                return "平均句长较短（≤" + (int) COMPLEXITY_LV1 + " 字），句式简单";
            case PRIMARY:
                return "平均句长适中（" + (int) COMPLEXITY_LV1 + "-" + (int) COMPLEXITY_LV2 + " 字）";
            case INTERMEDIATE:
                return "含较长句子（" + (int) (COMPLEXITY_LV1 + 1) + "-" + (int) COMPLEXITY_LV3 + " 字）";
            case ADVANCED:
                return "长句密集（>" + (int) COMPLEXITY_LV3 + " 字），句式复杂";
            default:
                return "";
        }
    }

    public String describeEmotion(DifficultyLevel level) {
        switch (level) {
            case BEGINNER:
                return "情绪单一（≤" + EMOTION_LV1 + " 类），基调平稳";
            case PRIMARY:
                return "情绪跨度较小（" + EMOTION_LV2 + " 类）";
            case INTERMEDIATE:
                return "情绪有一定起伏（" + EMOTION_LV3 + " 类）";
            case ADVANCED:
                return "情绪跨度大（≥4 类），转换频繁";
            default:
                return "";
        }
    }
}
