package com.example.Daol_2025.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultMerger {
    private final StringRedisTemplate redisTemplate;

    public ResultMerger(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String merge(String docId) {
        List<String> parts = redisTemplate.opsForList().range("gemini:results:" + docId, 0, -1);
        if (parts == null || parts.isEmpty()) return null;

        String joined = String.join("\n---\n", parts);
        redisTemplate.delete("gemini:jobs:" + docId);
        redisTemplate.delete("gemini:results:" + docId);

        return joined;
    }
}
