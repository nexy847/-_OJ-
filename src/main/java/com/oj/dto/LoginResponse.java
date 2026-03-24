package com.oj.dto;

import java.time.Instant;

public class LoginResponse {
    private String token;
    private Instant expiresAt;

    public LoginResponse(String token, Instant expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
