package com.recitation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recitation.dto.ParagraphProgressDTO;
import com.recitation.entity.ParagraphProgress;
import com.recitation.repository.ParagraphProgressRepository;
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

import java.util.List;
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
class ParagraphProgressConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParagraphProgressRepository paragraphProgressRepository;

    @BeforeEach
    void cleanUp() {
        paragraphProgressRepository.deleteAll();
    }

    @Test
    void sequentialDuplicatePost_shouldReturnSuccess_andKeepSingleRow() throws Exception {
        ParagraphProgressDTO dto = new ParagraphProgressDTO();
        dto.setUserId(1L);
        dto.setManuscriptId(100L);
        dto.setParagraphIndex(0);
        dto.setStatus("mastered");

        String json = objectMapper.writeValueAsString(dto);

        MvcResult first = mockMvc.perform(post("/api/paragraph-progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult second = mockMvc.perform(post("/api/paragraph-progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String firstBody = first.getResponse().getContentAsString();
        String secondBody = second.getResponse().getContentAsString();
        assertTrue(firstBody.contains("\"code\":200"));
        assertTrue(secondBody.contains("\"code\":200"));

        List<ParagraphProgress> rows = paragraphProgressRepository.findByUserIdAndManuscriptId(1L, 100L);
        long matchingRows = rows.stream().filter(p -> p.getParagraphIndex() == 0).count();
        assertEquals(1, matchingRows, "同一业务键应只有一行记录");
    }

    @Test
    void sequentialDuplicatePost_shouldUpdateStatus() throws Exception {
        ParagraphProgressDTO dto1 = new ParagraphProgressDTO();
        dto1.setUserId(1L);
        dto1.setManuscriptId(100L);
        dto1.setParagraphIndex(0);
        dto1.setStatus("mastered");

        mockMvc.perform(post("/api/paragraph-progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isOk());

        ParagraphProgressDTO dto2 = new ParagraphProgressDTO();
        dto2.setUserId(1L);
        dto2.setManuscriptId(100L);
        dto2.setParagraphIndex(0);
        dto2.setStatus("strengthen");

        mockMvc.perform(post("/api/paragraph-progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isOk());

        List<ParagraphProgress> rows = paragraphProgressRepository.findByUserIdAndManuscriptId(1L, 100L);
        ParagraphProgress progress = rows.stream().filter(p -> p.getParagraphIndex() == 0).findFirst().orElse(null);
        assertNotNull(progress);
        assertEquals("strengthen", progress.getStatus());
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

        for (int i = 0; i < concurrency; i++) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    ParagraphProgressDTO dto = new ParagraphProgressDTO();
                    dto.setUserId(1L);
                    dto.setManuscriptId(200L);
                    dto.setParagraphIndex(0);
                    dto.setStatus("mastered");

                    String json = objectMapper.writeValueAsString(dto);
                    MvcResult result = mockMvc.perform(post("/api/paragraph-progress")
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

        List<ParagraphProgress> rows = paragraphProgressRepository.findByUserIdAndManuscriptId(1L, 200L);
        long matchingRows = rows.stream().filter(p -> p.getParagraphIndex() == 0).count();
        assertEquals(1, matchingRows, "同一业务键并发保存后应只有一行记录");
    }

    @Test
    void deleteThenSave_shouldWork() throws Exception {
        ParagraphProgressDTO dto = new ParagraphProgressDTO();
        dto.setUserId(1L);
        dto.setManuscriptId(300L);
        dto.setParagraphIndex(0);
        dto.setStatus("mastered");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/paragraph-progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/paragraph-progress")
                        .param("userId", "1")
                        .param("manuscriptId", "300")
                        .param("paragraphIndex", "0"))
                .andExpect(status().isOk());

        List<ParagraphProgress> afterDelete = paragraphProgressRepository.findByUserIdAndManuscriptId(1L, 300L);
        long matchingRows = afterDelete.stream().filter(p -> p.getParagraphIndex() == 0).count();
        assertEquals(0, matchingRows, "删除后不应有记录");

        mockMvc.perform(post("/api/paragraph-progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        List<ParagraphProgress> afterResave = paragraphProgressRepository.findByUserIdAndManuscriptId(1L, 300L);
        long resaveRows = afterResave.stream().filter(p -> p.getParagraphIndex() == 0).count();
        assertEquals(1, resaveRows, "重新保存后应有一条记录");
    }

    @Test
    void differentParagraphIndex_shouldCreateMultipleRows() throws Exception {
        for (int i = 0; i < 5; i++) {
            ParagraphProgressDTO dto = new ParagraphProgressDTO();
            dto.setUserId(1L);
            dto.setManuscriptId(400L);
            dto.setParagraphIndex(i);
            dto.setStatus("mastered");

            mockMvc.perform(post("/api/paragraph-progress")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }

        List<ParagraphProgress> rows = paragraphProgressRepository.findByUserIdAndManuscriptId(1L, 400L);
        assertEquals(5, rows.size(), "不同段落索引应有5行记录");
    }
}
