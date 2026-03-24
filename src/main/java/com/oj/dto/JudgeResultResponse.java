package com.oj.dto;

import java.time.Instant;

import com.oj.enums.Verdict;

public class JudgeResultResponse {
    private Long submissionId;
    private Verdict verdict;
    private long timeMs;
    private long memoryKb;
    private String compileError;
    private String runtimeError;
    private String message;
    private Instant createdAt;

    public JudgeResultResponse(Long submissionId, Verdict verdict, long timeMs, long memoryKb, String compileError, String runtimeError, String message, Instant createdAt) {
        this.submissionId = submissionId;
        this.verdict = verdict;
        this.timeMs = timeMs;
        this.memoryKb = memoryKb;
        this.compileError = compileError;
        this.runtimeError = runtimeError;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public long getMemoryKb() {
        return memoryKb;
    }

    public String getCompileError() {
        return compileError;
    }

    public String getRuntimeError() {
        return runtimeError;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
