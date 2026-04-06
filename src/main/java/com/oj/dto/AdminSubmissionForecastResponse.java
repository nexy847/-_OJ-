package com.oj.dto;

public class AdminSubmissionForecastResponse {
    private final String forecastDate;
    private final String targetDate;
    private final int predictedSubmissions;
    private final String modelName;

    public AdminSubmissionForecastResponse(String forecastDate, String targetDate, int predictedSubmissions, String modelName) {
        this.forecastDate = forecastDate;
        this.targetDate = targetDate;
        this.predictedSubmissions = predictedSubmissions;
        this.modelName = modelName;
    }

    public String getForecastDate() {
        return forecastDate;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public int getPredictedSubmissions() {
        return predictedSubmissions;
    }

    public String getModelName() {
        return modelName;
    }
}
