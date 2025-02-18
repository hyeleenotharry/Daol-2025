package com.example.Daol_2025.domain;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
public class Insurance_products {
    private String productId;
    private String title;
    private String description;
    private String category;
    private String coverage;
    private String website_url;

    public Insurance_category getCategory() {
        return Insurance_category.valueOf(category);
    }

}
