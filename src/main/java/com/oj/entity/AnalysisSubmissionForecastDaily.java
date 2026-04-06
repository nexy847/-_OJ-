package com.oj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_submission_forecast_daily")
public class AnalysisSubmissionForecastDaily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String forecastDate;

    @Column(nullable = false, length = 10)
    private String targetDate;

    @Column(nullable = false)
    private int predictedSubmissions;

    @Column(nullable = false, length = 64)
    private String modelName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getForecastDate() {
        return forecastDate;
    }

    public void setForecastDate(String forecastDate) {
        this.forecastDate = forecastDate;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public int getPredictedSubmissions() {
        return predictedSubmissions;
    }

    public void setPredictedSubmissions(int predictedSubmissions) {
        this.predictedSubmissions = predictedSubmissions;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
