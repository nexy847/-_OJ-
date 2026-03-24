package com.oj.entity;

import java.io.Serializable;
import java.util.Objects;

public class AnalysisProblemDailyId implements Serializable {
    private String dt;
    private Long problemId;

    public AnalysisProblemDailyId() {
    }

    public AnalysisProblemDailyId(String dt, Long problemId) {
        this.dt = dt;
        this.problemId = problemId;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AnalysisProblemDailyId that = (AnalysisProblemDailyId) o;
        return Objects.equals(dt, that.dt) && Objects.equals(problemId, that.problemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dt, problemId);
    }
}
