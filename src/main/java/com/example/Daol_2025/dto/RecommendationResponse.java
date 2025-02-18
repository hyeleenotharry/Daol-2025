package com.example.Daol_2025.dto;

public class RecommendationResponse {
    private String userId;
    private String recommendedPolicy;

    public RecommendationResponse(String userId, String recommendedPolicy) {
        this.userId = userId;
        this.recommendedPolicy = recommendedPolicy;
    }

    // Getter & Setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRecommendedPolicy() { return recommendedPolicy; }
    public void setRecommendedPolicy(String recommendedPolicy) { this.recommendedPolicy = recommendedPolicy; }
}

