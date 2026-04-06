package com.oj.dto;

public class UserLanguageDailyResponse {
    private final String date;
    private final String language;
    private final long total;
    private final long accepted;
    private final double acRate;

    public UserLanguageDailyResponse(String date, String language, long total, long accepted, double acRate) {
        this.date = date;
        this.language = language;
        this.total = total;
        this.accepted = accepted;
        this.acRate = acRate;
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
}
