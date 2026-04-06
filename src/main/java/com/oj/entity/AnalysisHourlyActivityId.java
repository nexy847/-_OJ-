package com.oj.entity;

import java.io.Serializable;
import java.util.Objects;

public class AnalysisHourlyActivityId implements Serializable {
    private String dt;
    private Integer hourOfDay;

    public AnalysisHourlyActivityId() {
    }

    public AnalysisHourlyActivityId(String dt, Integer hourOfDay) {
        this.dt = dt;
        this.hourOfDay = hourOfDay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnalysisHourlyActivityId that)) {
            return false;
        }
        return Objects.equals(dt, that.dt) && Objects.equals(hourOfDay, that.hourOfDay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dt, hourOfDay);
    }
}
