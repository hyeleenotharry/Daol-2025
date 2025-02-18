package com.example.Daol_2025.domain;

import lombok.*;

@Data
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 생성자

@Getter
@Setter
public class Terms_documents {
    private String id;
    private String productId; // 상품 id FK
    private String raw_txt;
    private String summary;
    private String keypoints;

}
