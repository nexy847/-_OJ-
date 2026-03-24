package com.oj.dto;

import java.time.Instant;

public class UserResponse {
    private Long id;
    private String username;
    private Instant createdAt;

    public UserResponse(Long id, String username, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
