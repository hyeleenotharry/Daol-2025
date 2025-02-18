package com.example.Daol_2025.domain;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
public class Region {
    private String regionId;
    private String regionName;
    private String geocode;
    private String additionalInfo;
}
