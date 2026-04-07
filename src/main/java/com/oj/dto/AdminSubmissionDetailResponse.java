package com.oj.dto;

import java.time.Instant;

public class AdminSubmissionDetailResponse {
    private final Long submissionId;
    private final Long userId;
    private final String username;
    private final Long problemId;
    private final String problemTitle;
    private final String language;
    private final String status;
    private final String verdict;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final String code;
    private final Long timeMs;
    private final Long memoryKb;
    private final String compileError;
    private final String runtimeError;
    private final String message;

    public AdminSubmissionDetailResponse(Long submissionId, Long userId, String username, Long problemId,
                                         String problemTitle, String language, String status, String verdict,
                                         Instant createdAt, Instant updatedAt, String code, Long timeMs,
                                         Long memoryKb, String compileError, String runtimeError, String message) {
        this.submissionId = submissionId;
        this.userId = userId;
        this.username = username;
        this.problemId = problemId;
        this.problemTitle = problemTitle;
        this.language = language;
        this.status = status;
        this.verdict = verdict;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.code = code;
        this.timeMs = timeMs;
        this.memoryKb = memoryKb;
        this.compileError = compileError;
        this.runtimeError = runtimeError;
        this.message = message;
    }

    public Long getSubmissionId() { return submissionId; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public Long getProblemId() { return problemId; }
    public String getProblemTitle() { return problemTitle; }
    public String getLanguage() { return language; }
    public String getStatus() { return status; }
    public String getVerdict() { return verdict; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getCode() { return code; }
    public Long getTimeMs() { return timeMs; }
    public Long getMemoryKb() { return memoryKb; }
    public String getCompileError() { return compileError; }
    public String getRuntimeError() { return runtimeError; }
    public String getMessage() { return message; }
}
