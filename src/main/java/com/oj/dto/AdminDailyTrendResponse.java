package com.oj.dto;

public class AdminDailyTrendResponse {
    private String date;
    private long total;
    private long accepted;
    private double acRate;
    private double avgTimeMs;
    private double avgMemoryKb;

    public AdminDailyTrendResponse(String date, long total, long accepted, double acRate,
                                   double avgTimeMs, double avgMemoryKb) {
        this.date = date;
        this.total = total;
        this.accepted = accepted;
        this.acRate = acRate;
        this.avgTimeMs = avgTimeMs;
        this.avgMemoryKb = avgMemoryKb;
    }

    public String getDate() {
        return date;
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
