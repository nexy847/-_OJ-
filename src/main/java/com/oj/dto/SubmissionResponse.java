package com.oj.dto;

import java.time.Instant;

import com.oj.enums.Language;
import com.oj.enums.SubmissionStatus;
import com.oj.enums.Verdict;

public class SubmissionResponse {
    private Long id;
    private Long userId;
    private Long problemId;
    private Language language;
    private SubmissionStatus status;
    private Verdict verdict;
    private Instant createdAt;
    private Instant updatedAt;

    public SubmissionResponse(Long id, Long userId, Long problemId, Language language, SubmissionStatus status, Verdict verdict, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.problemId = problemId;
        this.language = language;
        this.status = status;
        this.verdict = verdict;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getProblemId() {
        return problemId;
    }

    public Language getLanguage() {
        return language;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
