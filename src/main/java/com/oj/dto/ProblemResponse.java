package com.oj.dto;

import java.time.Instant;
import java.util.List;

public class ProblemResponse {
    private Long id;
    private String title;
    private String description;
    private int timeLimitMs;
    private int memoryLimitMb;
    private Instant createdAt;
    private List<TestcaseResponse> testcases;

    public ProblemResponse(Long id, String title, String description, int timeLimitMs, int memoryLimitMb, Instant createdAt, List<TestcaseResponse> testcases) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.timeLimitMs = timeLimitMs;
        this.memoryLimitMb = memoryLimitMb;
        this.createdAt = createdAt;
        this.testcases = testcases;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getTimeLimitMs() {
        return timeLimitMs;
    }

    public int getMemoryLimitMb() {
        return memoryLimitMb;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<TestcaseResponse> getTestcases() {
        return testcases;
    }
}
