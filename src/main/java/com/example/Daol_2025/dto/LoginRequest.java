package com.example.Daol_2025.dto;

public class LoginRequest {
    private String userID;
    private String password;

    // Getters and Setters
    public String getUserId() {
        return userID;
    }

    public void setUserId(String userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
