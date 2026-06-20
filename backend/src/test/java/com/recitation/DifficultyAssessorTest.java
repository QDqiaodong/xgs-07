package com.recitation;

import com.recitation.dto.DifficultyAssessmentDTO;
import com.recitation.enums.DifficultyLevel;
import com.recitation.service.difficulty.DifficultyAssessor;
import com.recitation.service.difficulty.DifficultyRuleLibrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DifficultyAssessorTest {

    private DifficultyRuleLibrary ruleLibrary;
    private DifficultyAssessor assessor;

    @BeforeEach
    void setUp() {
        ruleLibrary = new DifficultyRuleLibrary();
        assessor = new DifficultyAssessor();
        ReflectionTestUtils.setField(assessor, "ruleLibrary", ruleLibrary);
    }

    @Test
    void levelForLength_boundaryMappings_areConsistent() {
        assertEquals(DifficultyLevel.BEGINNER, ruleLibrary.levelForLength(0));
        assertEquals(DifficultyLevel.BEGINNER, ruleLibrary.levelForLength(200));
        assertEquals(DifficultyLevel.PRIMARY, ruleLibrary.levelForLength(201));
        assertEquals(DifficultyLevel.PRIMARY, ruleLibrary.levelForLength(500));
        assertEquals(DifficultyLevel.INTERMEDIATE, ruleLibrary.levelForLength(501));
        assertEquals(DifficultyLevel.INTERMEDIATE, ruleLibrary.levelForLength(1000));
        assertEquals(DifficultyLevel.ADVANCED, ruleLibrary.levelForLength(1001));
    }

    @Test
    void levelForComplexity_boundaryMappings_areConsistent() {
        assertEquals(DifficultyLevel.BEGINNER, ruleLibrary.levelForComplexity(0));
        assertEquals(DifficultyLevel.BEGINNER, ruleLibrary.levelForComplexity(15));
        assertEquals(DifficultyLevel.PRIMARY, ruleLibrary.levelForComplexity(16));
        assertEquals(DifficultyLevel.PRIMARY, ruleLibrary.levelForComplexity(25));
        assertEquals(DifficultyLevel.INTERMEDIATE, ruleLibrary.levelForComplexity(26));
        assertEquals(DifficultyLevel.INTERMEDIATE, ruleLibrary.levelForComplexity(40));
        assertEquals(DifficultyLevel.ADVANCED, ruleLibrary.levelForComplexity(41));
    }

    @Test
    void levelForEmotion_boundaryMappings_areConsistent() {
        assertEquals(DifficultyLevel.BEGINNER, ruleLibrary.levelForEmotion(0));
        assertEquals(DifficultyLevel.BEGINNER, ruleLibrary.levelForEmotion(1));
        assertEquals(DifficultyLevel.PRIMARY, ruleLibrary.levelForEmotion(2));
        assertEquals(DifficultyLevel.INTERMEDIATE, ruleLibrary.levelForEmotion(3));
        assertEquals(DifficultyLevel.ADVANCED, ruleLibrary.levelForEmotion(4));
        assertEquals(DifficultyLevel.ADVANCED, ruleLibrary.levelForEmotion(6));
    }

    @Test
    void assess_shortSimplePoem_recommendsBeginner() {
        String content = "春眠不觉晓，处处闻啼鸟。夜来风雨声，花落知多少。";
        DifficultyAssessmentDTO result = assessor.assess(content);

        assertEquals(DifficultyLevel.BEGINNER.getLabel(), result.getRecommendedLevel());
        assertEquals(1, result.getRecommendedLevelNum());
        assertEquals(1.0, result.getOverallScore());
        assertDimensionBreakdown(result);
    }

    @Test
    void assess_longComplexEmotionalText_recommendsAdvanced() {
        String sentence = "在这漫长的黑夜里，他满怀着愤怒与悲伤，心中充满恐惧，却又怀着一丝惊讶与思念，仿佛看到了往昔的欢笑。";
        String content = String.join("", Collections.nCopies(23, sentence));

        DifficultyAssessmentDTO result = assessor.assess(content);

        assertEquals(DifficultyLevel.ADVANCED.getLabel(), result.getRecommendedLevel());
        assertEquals(4, result.getRecommendedLevelNum());
        assertTrue(result.getOverallScore() >= 3.5, "overall score should reach advanced threshold");
        assertDimensionBreakdown(result);
    }

    @Test
    void assess_emptyContent_defaultsToBeginner() {
        DifficultyAssessmentDTO result = assessor.assess("   ");
        assertEquals(DifficultyLevel.BEGINNER.getLabel(), result.getRecommendedLevel());
    }

    @Test
    void assess_dimensionWeights_sumToOne() {
        double sum = ruleLibrary.getWeight(DifficultyRuleLibrary.DIMENSION_LENGTH)
                + ruleLibrary.getWeight(DifficultyRuleLibrary.DIMENSION_COMPLEXITY)
                + ruleLibrary.getWeight(DifficultyRuleLibrary.DIMENSION_EMOTION);
        assertEquals(1.0, sum, 0.0001);
    }

    private void assertDimensionBreakdown(DifficultyAssessmentDTO result) {
        List<DifficultyAssessmentDTO.DimensionResult> dims = result.getDimensions();
        assertNotNull(dims);
        assertEquals(3, dims.size());
        assertTrue(dims.stream().allMatch(d -> d.getLevelNum() != null && d.getLevelNum() >= 1 && d.getLevelNum() <= 4));
    }
}
