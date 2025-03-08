package com.example.Daol_2025.service;

import com.example.Daol_2025.domain.User;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import org.springframework.beans.factory.annotation.Value;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PolicyService {

    @Value("${policy.apiKey}")
    private String SERVICE_KEY;

    @Value("${gemini.apiKey}")
    private String GEMINI_SERVICE_KEY;

    private static final String API_URL = "https://api.odcloud.kr/api/gov24/v3/serviceList";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    private String getPolicyApiUrl() {

        return API_URL + "?page=1&perPage=80&returnType=json&serviceKey=" + SERVICE_KEY;
    }

    private String getGeminiApiUrl() {
        return GEMINI_API_URL + "?key=" + GEMINI_SERVICE_KEY;
    }

    // 정책 받아오기
    public String getPolicies() throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        String policy_api = this.getPolicyApiUrl();

        // System.out.println(policy_api);
        URI uri = new URI(policy_api);
        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        // ResponseEntity<String> response = restTemplate.getForEntity(policy_api, String.class);
        return response.getBody();
    }

    // gemini 로 추천 정책 - 느려서... 최적화를 하거나 배포 떄 비동기로 처리하거나 redis 로 해봐?
    public List<Map<String, Object>> getRecommendation(User user, String policiesJson) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 JSON 구성
        JsonObject messagePart = new JsonObject();
        messagePart.addProperty("text", "User Profile: " + user.toString() + "\n\n" +
                "Policies: " + policiesJson + "\n\n" +
                "I want to get 10 recommended policies in JSON array format. " +
                "Each object should include: {\"서비스명\", \"상세조회URL\", \"서비스목적요약\", \"지원내용\", \"지원대상\", \"신청기한\", \"신청방법\"}. " +
                "Do not include additional text before or after JSON.");

        JsonArray partsArray = new JsonArray();
        partsArray.add(messagePart);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.add("parts", partsArray);

        JsonArray contentsArray = new JsonArray();
        contentsArray.add(userMessage);

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contentsArray);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        String geminiApiUrl = GEMINI_API_URL + "?key=" + GEMINI_SERVICE_KEY;

        Map<String, Object> response = restTemplate.postForObject(geminiApiUrl, request, Map.class);

        // 응답 파싱
        try {

            return extractPolicies(response);
        } catch (Exception e){
            System.out.println(e);
            return null;
        }
    }

    private List<Map<String, Object>> extractPolicies(Map<String, Object> response) {
        if (response == null || !response.containsKey("candidates")) {
            return List.of();
        }

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");

        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<String, Object> candidate = candidates.get(0);

        Object contentObj = candidate.get("content");
        if (!(contentObj instanceof Map)) {
            return List.of();
        }

        Map<String, Object> contentMap = (Map<String, Object>) contentObj;
        Object partsObj = contentMap.get("parts");
        if (!(partsObj instanceof List)) {
            return List.of();
        }

        List<?> partsList = (List<?>) partsObj;
        if (partsList.isEmpty() || !(partsList.get(0) instanceof Map)) {
            return List.of();
        }

        Map<String, Object> firstPart = (Map<String, Object>) partsList.get(0);
        Object textObj = firstPart.get("text");
        if (!(textObj instanceof String)) {
            return List.of();
        }

        String jsonText = ((String) textObj).replaceAll("```json", "").replaceAll("```", "").trim();
        try {
            return new Gson().fromJson(jsonText, new TypeToken<List<Map<String, Object>>>() {}.getType());
        } catch (JsonSyntaxException e) {
            System.err.println("JSON 파싱 오류: " + e.getMessage());
            return List.of();
        }
    }
}
