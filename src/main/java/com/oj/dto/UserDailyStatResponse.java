package com.oj.dto;

public class UserDailyStatResponse {
    private Long userId;
    private long total;
    private long accepted;
    private double avgTimeMs;
    private double avgMemoryKb;

    public UserDailyStatResponse(Long userId, long total, long accepted, double avgTimeMs, double avgMemoryKb) {
        this.userId = userId;
        this.total = total;
        this.accepted = accepted;
        this.avgTimeMs = avgTimeMs;
        this.avgMemoryKb = avgMemoryKb;
    }

    public Long getUserId() {
        return userId;
    }

    public long getTotal() {
        return total;
    }

    public long getAccepted() {
        return accepted;
    }

    public double getAvgTimeMs() {
        return avgTimeMs;
    }

    public double getAvgMemoryKb() {
        return avgMemoryKb;
    }
}
