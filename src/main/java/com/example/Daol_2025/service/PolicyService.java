package com.example.Daol_2025.service;

import com.example.Daol_2025.domain.User;
import org.springframework.beans.factory.annotation.Value;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

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

    public String getPolicies() throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        String policy_api = this.getPolicyApiUrl();

        // System.out.println(policy_api);
        URI uri = new URI(policy_api);
        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        // ResponseEntity<String> response = restTemplate.getForEntity(policy_api, String.class);
        return response.getBody();
    }

    public String getRecommendation(User user, String policiesJson){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.set("Content-Type", "application/json");
        // Gemini API 프롬프트 구성
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("prompt", "User Profile: " + user.toString() + "\n\n" +
                "Policies: " + policiesJson + "\n\n" +
                "I want to get 10 recommended policies considering User Profile. You should return result with json file without any additional text or description.");

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        String gemini_api = this.getGeminiApiUrl();
        ResponseEntity<String> response = restTemplate.postForEntity(gemini_api, request, String.class);

        return response.getBody();
    }
}
