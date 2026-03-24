package com.oj.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateProblemRequest {
    @NotBlank
    @Size(max = 128)
    private String title;

    @NotBlank
    @Size(max = 4000)
    private String description;

    @NotNull
    private Integer timeLimitMs;

    @NotNull
    private Integer memoryLimitMb;

    @Valid
    private List<TestcaseRequest> testcases;

    @Valid
    private List<CreateTestcaseContentRequest> testcaseContents;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTimeLimitMs() {
        return timeLimitMs;
    }

    public void setTimeLimitMs(Integer timeLimitMs) {
        this.timeLimitMs = timeLimitMs;
    }

    public Integer getMemoryLimitMb() {
        return memoryLimitMb;
    }

    public void setMemoryLimitMb(Integer memoryLimitMb) {
        this.memoryLimitMb = memoryLimitMb;
    }

    public List<TestcaseRequest> getTestcases() {
        return testcases;
    }

    public void setTestcases(List<TestcaseRequest> testcases) {
        this.testcases = testcases;
    }

    public List<CreateTestcaseContentRequest> getTestcaseContents() {
        return testcaseContents;
    }

    public void setTestcaseContents(List<CreateTestcaseContentRequest> testcaseContents) {
        this.testcaseContents = testcaseContents;
    }
}
