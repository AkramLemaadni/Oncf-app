package com.oncf.oncf.payload.response;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String role;

    public JwtResponse(String accessToken, String role) {
        this.token = accessToken;
        this.role = role;
    }
} 