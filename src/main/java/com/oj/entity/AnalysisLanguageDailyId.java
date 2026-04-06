package com.oj.entity;

import java.io.Serializable;
import java.util.Objects;

public class AnalysisLanguageDailyId implements Serializable {
    private String dt;
    private String language;

    public AnalysisLanguageDailyId() {
    }

    public AnalysisLanguageDailyId(String dt, String language) {
        this.dt = dt;
        this.language = language;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnalysisLanguageDailyId that)) {
            return false;
        }
        return Objects.equals(dt, that.dt) && Objects.equals(language, that.language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dt, language);
    }
}
