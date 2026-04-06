package com.oj.dto;

public class UserVerdictDailyResponse {
    private final String date;
    private final String verdict;
    private final long total;

    public UserVerdictDailyResponse(String date, String verdict, long total) {
        this.date = date;
        this.verdict = verdict;
        this.total = total;
    }

    public String getDate() {
        return date;
    }

    public String getVerdict() {
        return verdict;
    }

    public long getTotal() {
        return total;
    }
}
