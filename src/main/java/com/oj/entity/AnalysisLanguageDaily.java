package com.oj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_language_daily")
@IdClass(AnalysisLanguageDailyId.class)
public class AnalysisLanguageDaily {
    @Id
    @Column(length = 10)
    private String dt;

    @Id
    @Column(nullable = false, length = 16)
    private String language;

    @Column(nullable = false)
    private long total;

    @Column(nullable = false)
    private long accepted;

    private Double avgTimeMs;

    private Double avgMemoryKb;

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getAccepted() {
        return accepted;
    }

    public void setAccepted(long accepted) {
        this.accepted = accepted;
    }

    public Double getAvgTimeMs() {
        return avgTimeMs;
    }

    public void setAvgTimeMs(Double avgTimeMs) {
        this.avgTimeMs = avgTimeMs;
    }

    public Double getAvgMemoryKb() {
        return avgMemoryKb;
    }

    public void setAvgMemoryKb(Double avgMemoryKb) {
        this.avgMemoryKb = avgMemoryKb;
    }
}
