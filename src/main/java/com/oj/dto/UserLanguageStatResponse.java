package com.oj.dto;

public class UserLanguageStatResponse {
    private String language;
    private long total;
    private long accepted;
    private double acRate;

    public UserLanguageStatResponse(String language, long total, long accepted, double acRate) {
        this.language = language;
        this.total = total;
        this.accepted = accepted;
        this.acRate = acRate;
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
