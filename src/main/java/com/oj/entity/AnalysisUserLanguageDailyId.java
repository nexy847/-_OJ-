package com.oj.entity;

import java.io.Serializable;
import java.util.Objects;

public class AnalysisUserLanguageDailyId implements Serializable {
    private String dt;
    private Long userId;
    private String language;

    public AnalysisUserLanguageDailyId() {
    }

    public AnalysisUserLanguageDailyId(String dt, Long userId, String language) {
        this.dt = dt;
        this.userId = userId;
        this.language = language;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnalysisUserLanguageDailyId that)) {
            return false;
        }
        return Objects.equals(dt, that.dt)
                && Objects.equals(userId, that.userId)
                && Objects.equals(language, that.language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dt, userId, language);
    }
}
