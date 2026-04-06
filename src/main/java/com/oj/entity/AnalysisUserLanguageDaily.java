package com.oj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_user_language_daily")
@IdClass(AnalysisUserLanguageDailyId.class)
public class AnalysisUserLanguageDaily {
    @Id
    @Column(length = 10)
    private String dt;

    @Id
    @Column(nullable = false)
    private Long userId;

    @Id
    @Column(nullable = false, length = 16)
    private String language;

    @Column(nullable = false)
    private long total;

    @Column(nullable = false)
    private long accepted;

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
}
