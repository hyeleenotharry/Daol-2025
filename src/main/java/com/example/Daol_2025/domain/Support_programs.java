package com.example.Daol_2025.domain;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
public class Support_programs {
    private String programId;
    private String title;
    private String description;
    private String region_id;
    private Date apply_start_date;
    private Date apply_end_date;

}

//SUPPORT_PROGRAMS {
//    int program_id PK
//    string title
//    string description
//    string eligibility_criteria
//    int region_id FK
//    date apply_start_date
//    date apply_end_date
//}
