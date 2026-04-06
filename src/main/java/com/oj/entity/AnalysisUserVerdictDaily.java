package com.oj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_user_verdict_daily")
@IdClass(AnalysisUserVerdictDailyId.class)
public class AnalysisUserVerdictDaily {
    @Id
    @Column(length = 10)
    private String dt;

    @Id
    @Column(nullable = false)
    private Long userId;

    @Id
    @Column(nullable = false, length = 16)
    private String verdict;

    @Column(nullable = false)
    private long total;

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
