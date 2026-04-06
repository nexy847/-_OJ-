package com.oj.entity;

import java.io.Serializable;
import java.util.Objects;

public class AnalysisProblemVerdictDailyId implements Serializable {
    private String dt;
    private Long problemId;
    private String verdict;

    public AnalysisProblemVerdictDailyId() {
    }

    public AnalysisProblemVerdictDailyId(String dt, Long problemId, String verdict) {
        this.dt = dt;
        this.problemId = problemId;
        this.verdict = verdict;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnalysisProblemVerdictDailyId that)) {
            return false;
        }
        return Objects.equals(dt, that.dt)
                && Objects.equals(problemId, that.problemId)
                && Objects.equals(verdict, that.verdict);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dt, problemId, verdict);
    }
}
