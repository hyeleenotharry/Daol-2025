package com.example.Daol_2025.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private String userId;
    private String name;
    private String email;
    private String password;
    private Integer age;
    private String income;
    private String healthStatus;
    private String occupation;
    private String address;
    private String regionId;


    public User(String userId, String name, String income, String password, String email, Integer age, String occupation, String address, String regionId) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.income = income;
        this.age = age;
        this.occupation = occupation;
        this.address = address;
        this.regionId = regionId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setIncome(String income) {
        this.income = income;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }
}
