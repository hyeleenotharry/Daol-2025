package com.example.Daol_2025.domain;
import lombok.*;

import java.security.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
public class Search_history {
    private String searchId;
    private String userId;
    private String search_query;
    private Timestamp search_date;
}

//%% 사용자 검색 기록 테이블
//%% 추천 시스템에 주입하기 위함
//SEARCH_HISTORY {
//    int search_id PK
//    int user_id FK
//    string search_query
//    timestamp search_time
//}
