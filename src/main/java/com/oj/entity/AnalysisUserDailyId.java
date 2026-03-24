package com.oj.entity;

import java.io.Serializable;
import java.util.Objects;

public class AnalysisUserDailyId implements Serializable {
    private String dt;
    private Long userId;

    public AnalysisUserDailyId() {
    }

    public AnalysisUserDailyId(String dt, Long userId) {
        this.dt = dt;
        this.userId = userId;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AnalysisUserDailyId that = (AnalysisUserDailyId) o;
        return Objects.equals(dt, that.dt) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dt, userId);
    }
}
