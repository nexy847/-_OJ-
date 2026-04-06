package com.oj.dto;

public class AdminProblemVerdictResponse {
    private final String date;
    private final long problemId;
    private final String verdict;
    private final long total;

    public AdminProblemVerdictResponse(String date, long problemId, String verdict, long total) {
        this.date = date;
        this.problemId = problemId;
        this.verdict = verdict;
        this.total = total;
    }

    public String getDate() {
        return date;
    }

    public long getProblemId() {
        return problemId;
    }

    public String getVerdict() {
        return verdict;
    }

    public long getTotal() {
        return total;
    }
}
