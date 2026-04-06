package com.oj.dto;

public class AdminProblemDifficultyResponse {
    private final String date;
    private final Long problemId;
    private final String problemTitle;
    private final long totalSubmissions;
    private final long acceptedSubmissions;
    private final double acRate;
    private final double avgTimeMs;
    private final double avgMemoryKb;
    private final double avgAttemptsPerUser;
    private final double waRate;
    private final double reRate;
    private final double tleRate;
    private final double difficultyScore;
    private final String difficultyLabel;
    private final String modelName;

    public AdminProblemDifficultyResponse(String date, Long problemId, String problemTitle, long totalSubmissions,
                                          long acceptedSubmissions, double acRate, double avgTimeMs, double avgMemoryKb,
                                          double avgAttemptsPerUser, double waRate, double reRate, double tleRate,
                                          double difficultyScore, String difficultyLabel, String modelName) {
        this.date = date;
        this.problemId = problemId;
        this.problemTitle = problemTitle;
        this.totalSubmissions = totalSubmissions;
        this.acceptedSubmissions = acceptedSubmissions;
        this.acRate = acRate;
        this.avgTimeMs = avgTimeMs;
        this.avgMemoryKb = avgMemoryKb;
        this.avgAttemptsPerUser = avgAttemptsPerUser;
        this.waRate = waRate;
        this.reRate = reRate;
        this.tleRate = tleRate;
        this.difficultyScore = difficultyScore;
        this.difficultyLabel = difficultyLabel;
        this.modelName = modelName;
    }

    public String getDate() {
        return date;
    }

    public Long getProblemId() {
        return problemId;
    }

    public String getProblemTitle() {
        return problemTitle;
    }

    public long getTotalSubmissions() {
        return totalSubmissions;
    }

    public long getAcceptedSubmissions() {
        return acceptedSubmissions;
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

    public double getAvgAttemptsPerUser() {
        return avgAttemptsPerUser;
    }

    public double getWaRate() {
        return waRate;
    }

    public double getReRate() {
        return reRate;
    }

    public double getTleRate() {
        return tleRate;
    }

    public double getDifficultyScore() {
        return difficultyScore;
    }

    public String getDifficultyLabel() {
        return difficultyLabel;
    }

    public String getModelName() {
        return modelName;
    }
}
