package com.example.Daol_2025.domain;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
public class Welfare_services { // 지원금
    private String service_id;
    private String title;
    private String description;
    private String required_documents;
    private String region_id;
    private Date apply_start_date;
    private Date apply_end_date;
}

//int region_id FK
//date apply_start_date
//date apply_end_date
