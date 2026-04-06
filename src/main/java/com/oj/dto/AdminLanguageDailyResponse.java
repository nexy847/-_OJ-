package com.oj.dto;

public class AdminLanguageDailyResponse {
    private final String date;
    private final String language;
    private final long total;
    private final long accepted;
    private final double acRate;
    private final double avgTimeMs;
    private final double avgMemoryKb;

    public AdminLanguageDailyResponse(String date, String language, long total, long accepted,
                                      double acRate, double avgTimeMs, double avgMemoryKb) {
        this.date = date;
        this.language = language;
        this.total = total;
        this.accepted = accepted;
        this.acRate = acRate;
        this.avgTimeMs = avgTimeMs;
        this.avgMemoryKb = avgMemoryKb;
    }

    public String getDate() {
        return date;
    }

    public String getLanguage() {
        return language;
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
