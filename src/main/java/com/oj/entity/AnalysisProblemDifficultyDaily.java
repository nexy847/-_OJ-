package com.oj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_problem_difficulty_daily")
@IdClass(AnalysisProblemDifficultyDailyId.class)
public class AnalysisProblemDifficultyDaily {
    @Id
    @Column(length = 10)
    private String dt;

    @Id
    @Column(nullable = false)
    private Long problemId;

    @Column(nullable = false)
    private long totalSubmissions;

    @Column(nullable = false)
    private long acceptedSubmissions;

    @Column(nullable = false)
    private double acRate;

    @Column(nullable = false)
    private double avgTimeMs;

    @Column(nullable = false)
    private double avgMemoryKb;

    @Column(nullable = false)
    private double avgAttemptsPerUser;

    @Column(nullable = false)
    private double waRate;

    @Column(nullable = false)
    private double reRate;

    @Column(nullable = false)
    private double tleRate;

    @Column(nullable = false)
    private double difficultyScore;

    @Column(nullable = false, length = 16)
    private String difficultyLabel;

    @Column(nullable = false, length = 64)
    private String modelName;

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public long getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(long totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public long getAcceptedSubmissions() {
        return acceptedSubmissions;
    }

    public void setAcceptedSubmissions(long acceptedSubmissions) {
        this.acceptedSubmissions = acceptedSubmissions;
    }

    public double getAcRate() {
        return acRate;
    }

    public void setAcRate(double acRate) {
        this.acRate = acRate;
    }

    public double getAvgTimeMs() {
        return avgTimeMs;
    }

    public void setAvgTimeMs(double avgTimeMs) {
        this.avgTimeMs = avgTimeMs;
    }

    public double getAvgMemoryKb() {
        return avgMemoryKb;
    }

    public void setAvgMemoryKb(double avgMemoryKb) {
        this.avgMemoryKb = avgMemoryKb;
    }

    public double getAvgAttemptsPerUser() {
        return avgAttemptsPerUser;
    }

    public void setAvgAttemptsPerUser(double avgAttemptsPerUser) {
        this.avgAttemptsPerUser = avgAttemptsPerUser;
    }

    public double getWaRate() {
        return waRate;
    }

    public void setWaRate(double waRate) {
        this.waRate = waRate;
    }

    public double getReRate() {
        return reRate;
    }

    public void setReRate(double reRate) {
        this.reRate = reRate;
    }

    public double getTleRate() {
        return tleRate;
    }

    public void setTleRate(double tleRate) {
        this.tleRate = tleRate;
    }

    public double getDifficultyScore() {
        return difficultyScore;
    }

    public void setDifficultyScore(double difficultyScore) {
        this.difficultyScore = difficultyScore;
    }

    public String getDifficultyLabel() {
        return difficultyLabel;
    }

    public void setDifficultyLabel(String difficultyLabel) {
        this.difficultyLabel = difficultyLabel;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
