package com.oncf.oncf.payload.response;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String role;

    public JwtResponse(String accessToken, String role) {
        this.token = accessToken;
        this.role = role;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
} 