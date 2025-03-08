package com.example.Daol_2025.controller;


import com.example.Daol_2025.domain.Policy;
import com.example.Daol_2025.domain.User;
import com.example.Daol_2025.dto.RecommendationResponse;
import com.example.Daol_2025.service.PolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/policy/")
public class PolicyController {

    @Autowired
    private PolicyService policyService;

    @GetMapping("/")
    public ResponseEntity<Object> getPolicy() throws URISyntaxException {
        String policiesJson = policyService.getPolicies();

        return ResponseEntity.ok(policiesJson);
    }

    // 느림 - bigquery 나 vertex ai 사용 요망
    @GetMapping("/recommend")
    public List<Map<String, Object>> getRecommendedPolicy(@RequestHeader("Authorization") String token) {
        try {
            // 사용자 정보 디코딩 (토큰 기반)
            User user = decodeToken(token);

            // 정책 정보 가져오기
            String policiesJson = policyService.getPolicies();

            // 사용자 맞춤 추천 결과 가져오기
            List<Map<String, Object>> recommendations = policyService.getRecommendation(user, policiesJson);

            return recommendations;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private User decodeToken(String token) {
        try {
            // 1. 토큰을 '.' 기준으로 분할
            String[] chunks = token.split("\\.");
            if (chunks.length < 2) {
                throw new IllegalArgumentException("잘못된 토큰 형식입니다.");
            }

            // 2. Base64 URL Decoder로 payload 디코딩
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payloadJson = new String(decoder.decode(chunks[1]));

            // 3. JSON을 User 객체로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(payloadJson, User.class);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("토큰 디코딩 중 오류 발생: " + e.getMessage());
        }
    }

}
