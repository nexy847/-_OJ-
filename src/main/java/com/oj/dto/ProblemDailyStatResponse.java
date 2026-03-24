package com.oj.dto;

public class ProblemDailyStatResponse {
    private Long problemId;
    private long total;
    private long accepted;
    private double avgTimeMs;
    private double avgMemoryKb;

    public ProblemDailyStatResponse(Long problemId, long total, long accepted, double avgTimeMs, double avgMemoryKb) {
        this.problemId = problemId;
        this.total = total;
        this.accepted = accepted;
        this.avgTimeMs = avgTimeMs;
        this.avgMemoryKb = avgMemoryKb;
    }

    public Long getProblemId() {
        return problemId;
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
