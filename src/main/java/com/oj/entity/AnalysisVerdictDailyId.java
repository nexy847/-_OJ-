package com.oj.entity;

import java.io.Serializable;
import java.util.Objects;

public class AnalysisVerdictDailyId implements Serializable {
    private String dt;
    private String verdict;

    public AnalysisVerdictDailyId() {
    }

    public AnalysisVerdictDailyId(String dt, String verdict) {
        this.dt = dt;
        this.verdict = verdict;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnalysisVerdictDailyId that)) {
            return false;
        }
        return Objects.equals(dt, that.dt) && Objects.equals(verdict, that.verdict);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dt, verdict);
    }
}
