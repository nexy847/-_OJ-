package com.oj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_hourly_activity")
@IdClass(AnalysisHourlyActivityId.class)
public class AnalysisHourlyActivity {
    @Id
    @Column(length = 10)
    private String dt;

    @Id
    @Column(nullable = false)
    private Integer hourOfDay;

    @Column(nullable = false)
    private long total;

    @Column(nullable = false)
    private long accepted;

    @Column(nullable = false)
    private long activeUsers;

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public Integer getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(Integer hourOfDay) {
        this.hourOfDay = hourOfDay;
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

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }
}
