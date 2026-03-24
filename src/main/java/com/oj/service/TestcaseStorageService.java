package com.oj.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.oj.config.OjProperties;

@Service
public class TestcaseStorageService {
    private final OjProperties properties;

    public TestcaseStorageService(OjProperties properties) {
        this.properties = properties;
    }

    public StoredTestcase store(Long problemId, String inputContent, String outputContent) throws IOException {
        Path baseDir = baseDir();
        Path problemDir = baseDir.resolve("problem-" + problemId);
        Files.createDirectories(problemDir);

        String caseId = UUID.randomUUID().toString().replace("-", "");//去掉连字符（可能被误认为命令行参数开头）
        Path inputPath = problemDir.resolve("case-" + caseId + ".in");
        Path outputPath = problemDir.resolve("case-" + caseId + ".out");

        Files.writeString(inputPath, inputContent, StandardCharsets.UTF_8);
        Files.writeString(outputPath, outputContent, StandardCharsets.UTF_8);

        //得出从baseDir到inputPath的相对路径，win用\ linux用/ 适应docker
        String relInput = baseDir.relativize(inputPath).toString().replace('\\', '/');
        String relOutput = baseDir.relativize(outputPath).toString().replace('\\', '/');

        return new StoredTestcase(relInput, relOutput);
    }

    public void overwrite(String inputRelPath, String outputRelPath, String inputContent, String outputContent) throws IOException {
        Path inputPath = resolveRelative(inputRelPath);
        Path outputPath = resolveRelative(outputRelPath);
        Files.createDirectories(inputPath.getParent());
        Files.createDirectories(outputPath.getParent());
        Files.writeString(inputPath, inputContent, StandardCharsets.UTF_8);
        Files.writeString(outputPath, outputContent, StandardCharsets.UTF_8);
    }

    public void delete(String inputRelPath, String outputRelPath) throws IOException {
        Path inputPath = resolveRelative(inputRelPath);
        Path outputPath = resolveRelative(outputRelPath);
        Files.deleteIfExists(inputPath);
        Files.deleteIfExists(outputPath);
    }

    private Path baseDir() {
        return Path.of(properties.getJudge().getTestcaseDir()).toAbsolutePath().normalize();
    }

    private Path resolveRelative(String relPath) {
        Path base = baseDir();
        Path resolved = base.resolve(relPath).normalize();
        if (!resolved.startsWith(base)) {
            throw new IllegalArgumentException("Invalid testcase path");
        }
        return resolved;
    }

    public record StoredTestcase(String inputPath, String outputPath) {}
}
