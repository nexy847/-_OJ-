package com.oj.entity;

import java.io.Serializable;
import java.util.Objects;

public class AnalysisUserVerdictDailyId implements Serializable {
    private String dt;
    private Long userId;
    private String verdict;

    public AnalysisUserVerdictDailyId() {
    }

    public AnalysisUserVerdictDailyId(String dt, Long userId, String verdict) {
        this.dt = dt;
        this.userId = userId;
        this.verdict = verdict;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnalysisUserVerdictDailyId that)) {
            return false;
        }
        return Objects.equals(dt, that.dt)
                && Objects.equals(userId, that.userId)
                && Objects.equals(verdict, that.verdict);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dt, userId, verdict);
    }
}
