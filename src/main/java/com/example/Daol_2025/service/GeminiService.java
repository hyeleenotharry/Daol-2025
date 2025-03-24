package com.example.Daol_2025.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GeminiService {
    private final String API_ENDPOINT = "https://us-central1-aiplatform.googleapis.com/v1/projects/ancient-ceiling-454506-i1/locations/us-central1/publishers/google/models/gemini-2.0-flash-001:streamGenerateContent";

    @Value("${gemini.credentials}")
    private Resource credentialsFile;

    private final Map<String, StringBuilder> docBuffers = new ConcurrentHashMap<>();

    private final StringBuilder accumulatedText = new StringBuilder(); // 누적 저장용

    public void summarizeAndAccumulate(String docId, String content) throws IOException {
        String prompt = "다음은 보험 약관의 일부입니다.  \n" +
                "이 문서에서 일반 소비자가 반드시 주의 깊게 알아야 할 내용만 선별해 정리해주세요.  \n" +
                "단순한 내용 요약이 아니라, 소비자에게 **불이익이 갈 수 있는 조항**이나, **보장되는 범위**, **예외 사항** 등 실질적으로 중요한 핵심만 골라주세요.\n" +
                "\n" +
                "- 모든 내용을 다 요약하지 마세요. key information 만 요약해주세요. \n" +
                "- 소비자가 반드시 알아야 할 조항만 **항목별로 요약**해주세요. 초보자가 볼 것이기 때문에, 전문 용어는 쉽게 풀어쓰거나 다른 단어로 대체해주세요.  \n" +
                "- 불리한 조항이나 놓치기 쉬운 제한 사항은 꼭 강조해주세요.  \n" +
                "- 끝에 불필요한 맺음말은 쓰지 마세요.\n" +
                "- 텍스트 안에 근거가 명확하지 않거나 반복되는 표현은 생략하세요.\n" +
                "\n" +
                "다음은 문서의 일부입니다:\n(한국어):\n\n" + content;

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(credentialsFile.getInputStream())
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
        credentials.refreshIfExpired();
        String accessToken = credentials.getAccessToken().getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> request = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", prompt))
                ))
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        RestTemplate restTemplate = new RestTemplate();
        System.out.println("Gemini 한테 request 보내는 중...");

        ResponseEntity<String> response = restTemplate.postForEntity(API_ENDPOINT, entity, String.class);
        String responseBody = response.getBody();

        // JSON에서 "text"만 추출
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseBody);
        StringBuilder extractedText = new StringBuilder();

        if (rootNode.isArray()) {
            for (JsonNode item : rootNode) {
                JsonNode candidates = item.path("candidates");
                for (JsonNode candidate : candidates) {
                    JsonNode parts = candidate.path("content").path("parts");
                    for (JsonNode part : parts) {
                        String text = part.path("text").asText();
                        extractedText.append(text);
                    }
                }
            }
        }

        System.out.println("추출된 요약:\n" + extractedText.toString());
        // ✅ docId 별로 누적 저장
        docBuffers.computeIfAbsent(docId, id -> new StringBuilder())
                .append("\n\n")
                .append(extractedText);
    }

    public String getAccumulatedSummary(String docId) {
        return docBuffers.getOrDefault(docId, new StringBuilder()).toString();
    }

    public void resetAccumulatedSummary(String docId) {
        docBuffers.remove(docId);
    }
}
