package com.oj.dto;

public class AdminUserStatResponse {
    private Long userId;
    private String username;
    private long total;
    private long accepted;
    private double acRate;

    public AdminUserStatResponse(Long userId, String username, long total, long accepted, double acRate) {
        this.userId = userId;
        this.username = username;
        this.total = total;
        this.accepted = accepted;
        this.acRate = acRate;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public long getTotal() {
        return total;
    }

    public long getAccepted() {
        return accepted;
    }

    public double getAcRate() {
        return acRate;
    }
}
