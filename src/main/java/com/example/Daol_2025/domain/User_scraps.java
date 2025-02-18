package com.example.Daol_2025.domain;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User_scraps {
    private String scrap_id;
    private String user_id;
    private String product_id;
    private String product_category;
    private Date saved_at;
}
