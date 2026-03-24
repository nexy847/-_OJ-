package com.oj.dto;

public class TestcaseResponse {
    private Long id;
    private String inputPath;
    private String outputPath;
    private int weight;

    public TestcaseResponse(Long id, String inputPath, String outputPath, int weight) {
        this.id = id;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.weight = weight;
    }

    public Long getId() {
        return id;
    }

    public String getInputPath() {
        return inputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public int getWeight() {
        return weight;
    }
}
