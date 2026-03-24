package com.oj.dto;

public class UserOverviewResponse {
    private Long userId;
    private long total;
    private long accepted;
    private double acRate;
    private double avgTimeMs;
    private double avgMemoryKb;

    public UserOverviewResponse(Long userId, long total, long accepted, double acRate, double avgTimeMs, double avgMemoryKb) {
        this.userId = userId;
        this.total = total;
        this.accepted = accepted;
        this.acRate = acRate;
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

    public double getAcRate() {
        return acRate;
    }

    public double getAvgTimeMs() {
        return avgTimeMs;
    }

    public double getAvgMemoryKb() {
        return avgMemoryKb;
    }
}
