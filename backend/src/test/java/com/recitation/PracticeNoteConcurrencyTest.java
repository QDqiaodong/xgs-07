package com.recitation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recitation.dto.PracticeNoteDTO;
import com.recitation.entity.Category;
import com.recitation.entity.Manuscript;
import com.recitation.repository.CategoryRepository;
import com.recitation.repository.ManuscriptRepository;
import com.recitation.repository.PracticeNoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class PracticeNoteConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PracticeNoteRepository practiceNoteRepository;

    @Autowired
    private ManuscriptRepository manuscriptRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long testManuscriptId;

    @BeforeEach
    void cleanUp() {
        practiceNoteRepository.deleteAll();
        manuscriptRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test");
        category.setSortOrder(0);
        category.setStatus(true);
        category = categoryRepository.save(category);

        Manuscript manuscript = new Manuscript();
        manuscript.setTitle("Test Manuscript");
        manuscript.setContent("Test content for practice note");
        manuscript.setCategoryId(category.getId());
        manuscript.setCategoryName(category.getName());
        manuscript.setIsPublic(true);
        manuscript.setStatus(1);
        manuscript.setViewCount(0);
        manuscript.setFavoriteCount(0);
        manuscript = manuscriptRepository.save(manuscript);

        testManuscriptId = manuscript.getId();
    }

    @Test
    void sequentialDuplicatePost_shouldReturnSuccess_andKeepSingleRow() throws Exception {
        PracticeNoteDTO dto = new PracticeNoteDTO();
        dto.setUserId(1L);
        dto.setManuscriptId(testManuscriptId);
        dto.setDifficultyPoints("hard");
        dto.setToneControl("low");
        dto.setEmotionExpression("sad");
        dto.setOtherNotes("test note");

        String json = objectMapper.writeValueAsString(dto);

        MvcResult first = mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult second = mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String firstBody = first.getResponse().getContentAsString();
        String secondBody = second.getResponse().getContentAsString();
        assertTrue(firstBody.contains("\"code\":200"));
        assertTrue(secondBody.contains("\"code\":200"));

        long rowCount = practiceNoteRepository.findByUserIdAndManuscriptId(1L, testManuscriptId)
                .map(n -> 1L)
                .orElse(0L);
        assertEquals(1, rowCount, "同一业务键应只有一行记录");
    }

    @Test
    void sequentialDuplicatePost_shouldUpdateContent() throws Exception {
        PracticeNoteDTO dto1 = new PracticeNoteDTO();
        dto1.setUserId(1L);
        dto1.setManuscriptId(testManuscriptId);
        dto1.setDifficultyPoints("initial difficulty");
        dto1.setToneControl("initial tone");
        dto1.setEmotionExpression("initial emotion");
        dto1.setOtherNotes("initial notes");

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isOk());

        PracticeNoteDTO dto2 = new PracticeNoteDTO();
        dto2.setUserId(1L);
        dto2.setManuscriptId(testManuscriptId);
        dto2.setDifficultyPoints("updated difficulty");
        dto2.setToneControl("updated tone");
        dto2.setEmotionExpression("updated emotion");
        dto2.setOtherNotes("updated notes");

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isOk());

        var note = practiceNoteRepository.findByUserIdAndManuscriptId(1L, testManuscriptId).orElse(null);
        assertNotNull(note);
        assertEquals("updated difficulty", note.getDifficultyPoints());
        assertEquals("updated tone", note.getToneControl());
        assertEquals("updated emotion", note.getEmotionExpression());
        assertEquals("updated notes", note.getOtherNotes());
    }

    @Test
    void concurrentPost_sameBusinessKey_allShouldReturnSuccess() throws Exception {
        int concurrency = 10;
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        CountDownLatch readyLatch = new CountDownLatch(concurrency);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(concurrency);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        Long manuscriptId = testManuscriptId;

        for (int i = 0; i < concurrency; i++) {
            final int idx = i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    PracticeNoteDTO dto = new PracticeNoteDTO();
                    dto.setUserId(1L);
                    dto.setManuscriptId(manuscriptId);
                    dto.setDifficultyPoints("difficulty " + idx);
                    dto.setToneControl("tone " + idx);
                    dto.setEmotionExpression("emotion " + idx);
                    dto.setOtherNotes("notes " + idx);

                    String json = objectMapper.writeValueAsString(dto);
                    MvcResult result = mockMvc.perform(post("/api/notes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                            .andReturn();

                    int status = result.getResponse().getStatus();
                    String body = result.getResponse().getContentAsString();
                    if (status == 200 && body.contains("\"code\":200")) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertEquals(concurrency, successCount.get(), "所有并发请求都应返回成功");
        assertEquals(0, failCount.get(), "不应有失败的请求");

        var note = practiceNoteRepository.findByUserIdAndManuscriptId(1L, manuscriptId);
        assertTrue(note.isPresent(), "应存在一条记录");
    }

    @Test
    void differentUsers_shouldCreateMultipleRows() throws Exception {
        for (long userId = 1; userId <= 3; userId++) {
            PracticeNoteDTO dto = new PracticeNoteDTO();
            dto.setUserId(userId);
            dto.setManuscriptId(testManuscriptId);
            dto.setDifficultyPoints("difficulty for user " + userId);
            dto.setToneControl("tone");
            dto.setEmotionExpression("emotion");
            dto.setOtherNotes("notes");

            mockMvc.perform(post("/api/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }

        var allNotes = practiceNoteRepository.findByManuscriptIdOrderByCreateTimeDesc(testManuscriptId);
        assertEquals(3, allNotes.size(), "不同用户应有3行记录");
    }
}
