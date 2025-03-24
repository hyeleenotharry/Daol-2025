package com.example.Daol_2025.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeminiWorker {

    private final StringRedisTemplate redisTemplate;
    private final GeminiService geminiService;
    private final GcsPdfService gcsService;

    private final Set<String> processingDocs = ConcurrentHashMap.newKeySet();

    public GeminiWorker(StringRedisTemplate redisTemplate, GeminiService geminiService, GcsPdfService gcsService) {
        this.redisTemplate = redisTemplate;
        this.geminiService = geminiService;
        this.gcsService = gcsService;
    }

    @Scheduled(fixedDelay = 10000)
    public void processJobs() {
        Set<String> keys = redisTemplate.keys("gemini:jobs:*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            String docId = key.replace("gemini:jobs:", "");

            if (!processingDocs.add(docId)) continue;

            // ❗ 순차 처리 (Thread 제거)
            processOneDocument(docId);
            break; // ✅ 한 번에 하나의 문서만 처리
        }
    }

    public void processOneDocument(String docId) {
        try {
            String jobKey = "gemini:jobs:" + docId;
            String chunk = redisTemplate.opsForList().leftPop(jobKey);

            while (chunk != null) {
                boolean success = false;
                int retryCount = 0;

                // ✅ 최대 3회 재시도
                while (!success && retryCount < 2) {
                    try {
                        geminiService.summarizeAndAccumulate(docId, chunk);
                        success = true;
                        Thread.sleep(1000); // rate 제한을 피하기 위해 대기
                    } catch (Exception e) {
                        retryCount++;
                        System.err.println("❌ 요약 실패(" + retryCount + "/2): " + e.getMessage());
                        Thread.sleep(1000); // 실패 후 잠시 대기
                    }
                }

                if (!success) {
                    // 3회 실패하면 마지막으로 다시 push
                    redisTemplate.opsForList().rightPush(jobKey, chunk);
                    System.err.println("🚨 반복 실패로 해당 chunk 재시도 대기: " + docId);
                    return;
                }

                chunk = redisTemplate.opsForList().leftPop(jobKey);
            }

            // ✅ 모든 chunk 처리 완료 → 결과 병합 및 저장
            String merged = geminiService.getAccumulatedSummary(docId);
            gcsService.uploadSummary(docId, merged);
            geminiService.resetAccumulatedSummary(docId);
            redisTemplate.delete("gemini:jobs:" + docId);
            System.out.println("🎉 " + docId + " 처리 완료 및 업로드 완료!");

        } catch (Exception e) {
            System.err.println("❗ 문서 처리 중 예외 발생: " + e.getMessage());
        } finally {
            processingDocs.remove(docId);
        }
    }
}

