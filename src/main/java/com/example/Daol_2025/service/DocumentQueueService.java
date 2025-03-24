package com.example.Daol_2025.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class DocumentQueueService {
    private final StringRedisTemplate redisTemplate;
    private final int CHUNK_SIZE = 5000; // 문자 수 기준

    public DocumentQueueService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void enqueue(String docId, String content) {
        int chunkCount = 0;
        for (int i = 0; i < content.length(); i += CHUNK_SIZE) {
            String chunk = content.substring(i, Math.min(content.length(), i + CHUNK_SIZE));
            redisTemplate.opsForList().rightPush("gemini:jobs:" + docId, chunk);
            chunkCount++;
        }
        System.out.println(docId + " → 분할 완료 (" + chunkCount + "조각)");

    }
}

