package com.oj.dto;

public class AdminProblemDifficultyHistoryResponse {
    private final String date;
    private final double difficultyScore;
    private final String difficultyLabel;
    private final double acRate;
    private final double avgTimeMs;
    private final double avgAttemptsPerUser;

    public AdminProblemDifficultyHistoryResponse(String date, double difficultyScore, String difficultyLabel,
                                                 double acRate, double avgTimeMs, double avgAttemptsPerUser) {
        this.date = date;
        this.difficultyScore = difficultyScore;
        this.difficultyLabel = difficultyLabel;
        this.acRate = acRate;
        this.avgTimeMs = avgTimeMs;
        this.avgAttemptsPerUser = avgAttemptsPerUser;
    }

    public String getDate() {
        return date;
    }

    public double getDifficultyScore() {
        return difficultyScore;
    }

    public String getDifficultyLabel() {
        return difficultyLabel;
    }

    public double getAcRate() {
        return acRate;
    }

    public double getAvgTimeMs() {
        return avgTimeMs;
    }

    public double getAvgAttemptsPerUser() {
        return avgAttemptsPerUser;
    }
}
