package com.oj.dto;

public class SummaryDailyStatResponse {
    private String date;
    private long total;
    private long accepted;
    private double avgTimeMs;
    private double avgMemoryKb;

    public SummaryDailyStatResponse(String date, long total, long accepted, double avgTimeMs, double avgMemoryKb) {
        this.date = date;
        this.total = total;
        this.accepted = accepted;
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

    public double getAvgTimeMs() {
        return avgTimeMs;
    }

    public double getAvgMemoryKb() {
        return avgMemoryKb;
    }
}
