package com.oj.dto;

public class TestcaseContentResponse {
    private final Long id;
    private final String inputPath;
    private final String outputPath;
    private final int weight;
    private final String inputContent;
    private final String outputContent;

    public TestcaseContentResponse(Long id, String inputPath, String outputPath, int weight,
                                   String inputContent, String outputContent) {
        this.id = id;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.weight = weight;
        this.inputContent = inputContent;
        this.outputContent = outputContent;
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

    public String getInputContent() {
        return inputContent;
    }

    public String getOutputContent() {
        return outputContent;
    }
}
