package com.oj.entity;

import java.io.Serializable;
import java.util.Objects;

public class AnalysisProblemDifficultyDailyId implements Serializable {
    private String dt;
    private Long problemId;

    public AnalysisProblemDifficultyDailyId() {
    }

    public AnalysisProblemDifficultyDailyId(String dt, Long problemId) {
        this.dt = dt;
        this.problemId = problemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnalysisProblemDifficultyDailyId that)) {
            return false;
        }
        return Objects.equals(dt, that.dt) && Objects.equals(problemId, that.problemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dt, problemId);
    }
}
