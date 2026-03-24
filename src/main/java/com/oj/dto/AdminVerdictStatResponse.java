package com.oj.dto;

public class AdminVerdictStatResponse {
    private String verdict;
    private long total;

    public AdminVerdictStatResponse(String verdict, long total) {
        this.verdict = verdict;
        this.total = total;
    }

    public String getVerdict() {
        return verdict;
    }

    public long getTotal() {
        return total;
    }
}
