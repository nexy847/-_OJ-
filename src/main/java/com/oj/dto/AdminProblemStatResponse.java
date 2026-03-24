package com.oj.dto;

public class AdminProblemStatResponse {
    private Long problemId;
    private String problemTitle;
    private long total;
    private long accepted;
    private double acRate;
    private double avgTimeMs;
    private double avgMemoryKb;

    public AdminProblemStatResponse(Long problemId, String problemTitle, long total, long accepted,
                                    double acRate, double avgTimeMs, double avgMemoryKb) {
        this.problemId = problemId;
        this.problemTitle = problemTitle;
        this.total = total;
        this.accepted = accepted;
        this.acRate = acRate;
        this.avgTimeMs = avgTimeMs;
        this.avgMemoryKb = avgMemoryKb;
    }

    public Long getProblemId() {
        return problemId;
    }

    public String getProblemTitle() {
        return problemTitle;
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
