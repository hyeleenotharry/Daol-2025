package com.example.Daol_2025.domain;

import lombok.*;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
public class Recommendation_logs {
    private String logId;
    private String userId;
    private String recommended_item;
    private Timestamp created_at;
}

//%% 추천 로그 테이블
//RECOMMENDATION_LOGS {
//    int log_id PK
//    int user_id FK
//    string recommended_items "JSON 또는 CSV 형태로 저장"
//    timestamp created_at
//}
