package com.oj.dto;

import java.time.Instant;

public class UserRecentSubmissionResponse {
    private Long submissionId;
    private Long problemId;
    private String problemTitle;
    private String language;
    private String verdict;
    private long timeMs;
    private long memoryKb;
    private Instant createdAt;

    public UserRecentSubmissionResponse(Long submissionId, Long problemId, String problemTitle,
                                        String language, String verdict, long timeMs, long memoryKb,
                                        Instant createdAt) {
        this.submissionId = submissionId;
        this.problemId = problemId;
        this.problemTitle = problemTitle;
        this.language = language;
        this.verdict = verdict;
        this.timeMs = timeMs;
        this.memoryKb = memoryKb;
        this.createdAt = createdAt;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public Long getProblemId() {
        return problemId;
    }

    public String getProblemTitle() {
        return problemTitle;
    }

    public String getLanguage() {
        return language;
    }

    public String getVerdict() {
        return verdict;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public long getMemoryKb() {
        return memoryKb;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
