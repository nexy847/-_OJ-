package com.oj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_summary_daily")
public class AnalysisSummaryDaily {
    @Id
    @Column(length = 10)
    private String dt;

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
