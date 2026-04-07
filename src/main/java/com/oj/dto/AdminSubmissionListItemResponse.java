package com.oj.dto;

import java.time.Instant;

public class AdminSubmissionListItemResponse {
    private final Long submissionId;
    private final String username;
    private final String problemTitle;
    private final String verdict;
    private final Instant createdAt;

    public AdminSubmissionListItemResponse(Long submissionId, String username, String problemTitle, String verdict, Instant createdAt) {
        this.submissionId = submissionId;
        this.username = username;
        this.problemTitle = problemTitle;
        this.verdict = verdict;
        this.createdAt = createdAt;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public String getUsername() {
        return username;
    }

    public String getProblemTitle() {
        return problemTitle;
    }

    public String getVerdict() {
        return verdict;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
