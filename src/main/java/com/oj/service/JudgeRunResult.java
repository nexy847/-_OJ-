package com.oj.service;

import com.oj.enums.Verdict;

public class JudgeRunResult {
    private final Verdict verdict;
    private final long timeMs;
    private final long memoryKb;
    private final String compileError;
    private final String runtimeError;
    private final String message;

    public JudgeRunResult(Verdict verdict, long timeMs, long memoryKb, String compileError, String runtimeError, String message) {
        this.verdict = verdict;
        this.timeMs = timeMs;
        this.memoryKb = memoryKb;
        this.compileError = compileError;
        this.runtimeError = runtimeError;
        this.message = message;
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
}
