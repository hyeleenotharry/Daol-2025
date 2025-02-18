package com.example.Daol_2025.domain;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
public class Financial_products {
    private String productId;
    private String title;
    private String description;
    private String category;
    private String government_support_detail;
    private String website_url;

    public Finanacial_category getCategory(){
        return Finanacial_category.valueOf(category);
    }

}


