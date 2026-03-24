package com.oj.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.oj.config.OjProperties;
import com.oj.entity.Problem;
import com.oj.entity.Submission;
import com.oj.entity.Testcase;
import com.oj.enums.Language;
import com.oj.enums.Verdict;
import com.oj.util.OutputComparator;
import com.oj.util.ProcessRunner;

@Service
public class DockerJudgeRunner {
    private static final String WORKSPACE = "/workspace";//挂载的虚拟目录
    private static final String TESTCASES = "/testcases";
    private static final Pattern TIME_PATTERN = Pattern.compile("TIME:([0-9.]+)\\s+MEM:(\\d+)");
    //正则：用来提取运行时间和内存占用

    private final OjProperties properties;

    public DockerJudgeRunner(OjProperties properties) {
        this.properties = properties;
    }

    public JudgeRunResult run(Submission submission, Problem problem, List<Testcase> testcases) {
        if (testcases == null || testcases.isEmpty()) {
            return new JudgeRunResult(Verdict.ERROR, 0, 0, null, null, "No testcases configured");
        }

        Path workDir;
        try {
            workDir = prepareWorkDir(submission);
        } catch (IOException e) {
            return new JudgeRunResult(Verdict.ERROR, 0, 0, null, null, "Failed to prepare workdir: " + e.getMessage());
        }

        Language language = submission.getLanguage();
        String image = imageFor(language);
        if (image == null) {
            return new JudgeRunResult(Verdict.ERROR, 0, 0, null, null, "No docker image configured for language: " + language);
        }

        CompileAndRunSpec spec = CompileAndRunSpec.forLanguage(language);
        try {//把用户提交的代码字符串存成磁盘上的 Main.cpp 或 Main.java
            writeSource(workDir, spec.sourceFileName(), submission.getCode());
        } catch (IOException e) {
            return new JudgeRunResult(Verdict.ERROR, 0, 0, null, null, "Failed to write source: " + e.getMessage());
        }

        if (spec.compileCommand() != null) {//在docker内编译
            ProcessRunner.CommandResult compileResult = runDockerCommand(image, spec.compileCommand(), workDir, null, Duration.ofSeconds(30), false);
            //编译后 会在/work/sub-{id}里留存一个*.out二进制文件（运行周期结束时会被删掉）
            if (!compileResult.isSuccess()) {
                String error = compileResult.stderr().isBlank() ? compileResult.stdout() : compileResult.stderr();
                return new JudgeRunResult(Verdict.CE, 0, 0, error, null, "Compilation failed");
            }
        }

        Path testcaseBase = Path.of(properties.getJudge().getTestcaseDir()).toAbsolutePath();
        long totalTime = 0;
        long maxMemoryKb = 0;
        for (Testcase testcase : testcases) {//循环跑关于题目的每个测试文件
            Path input = testcaseBase.resolve(testcase.getInputPath());
            Path expected = testcaseBase.resolve(testcase.getOutputPath());
            //这个expected变量在testcases表里是.out文件，比如1.out 存在于data/testcases/sum/1.out 同一文件夹下还有.in文件
            if (!Files.exists(input) || !Files.exists(expected)) {
                return new JudgeRunResult(Verdict.ERROR, totalTime, 0, null, null,
                        "Missing testcase files: " + testcase.getInputPath() + " / " + testcase.getOutputPath());
            }

            Path output = workDir.resolve("out.txt");
            try {
                Files.deleteIfExists(output);
            } catch (IOException ignored) {
            }

            String runCommand = spec.runCommand()
                    .replace("{input}", TESTCASES + "/" + normalizePath(testcase.getInputPath()))
                    .replace("{output}", WORKSPACE + "/out.txt");

            //在运行docker跑测试用例时 加入时间限制 题目要求时间 + 1秒冗余
            Duration timeout = Duration.ofMillis(problem.getTimeLimitMs() + 1000L);
            Instant start = Instant.now();
            ProcessRunner.CommandResult runResult = runDockerCommand(image, runCommand, workDir, testcaseBase, timeout, true);
            long elapsed = Duration.between(start, Instant.now()).toMillis();
            totalTime += elapsed;//elapsed为模糊时间 为系统开销加上用户代码运行时间

            MetricResult metrics = parseMetrics(runResult.stderr());
            if (metrics.timeMs > 0) {//此精准时间为/usr/bin/time
                totalTime = totalTime - elapsed + metrics.timeMs;//此时减去模糊时间 补上用户代码运行的精准时间
            }
            if (metrics.memoryKb > maxMemoryKb) {
                maxMemoryKb = metrics.memoryKb;
            }

            if (!runResult.finished()) {
                return new JudgeRunResult(Verdict.TLE, totalTime, 0, null, null, "Time limit exceeded");
            }

            if (runResult.exitCode() != 0) {
                String error = runResult.stderr().isBlank() ? runResult.stdout() : stripTimeMarker(runResult.stderr());
                return new JudgeRunResult(Verdict.RE, totalTime, maxMemoryKb, null, error, "Runtime error");
            }

            try {
                if (!Files.exists(output)) {
                    return new JudgeRunResult(Verdict.RE, totalTime, maxMemoryKb, null, "No output produced", "Runtime error");
                }
                long outputSize = Files.size(output);
                if (outputSize > properties.getJudge().getMaxOutputKb() * 1024L) {
                    return new JudgeRunResult(Verdict.RE, totalTime, maxMemoryKb, null, null, "Output limit exceeded");
                }
                //将洗干净的用户输出和标准答案作对比 不一致就wa
                if (!OutputComparator.equalsNormalized(output, expected)) {
                    String actualPreview = preview(output);
                    String expectedPreview = preview(expected);
                    return new JudgeRunResult(Verdict.WA, totalTime, maxMemoryKb, null, null,
                            "Wrong answer. Expected: " + expectedPreview + " Actual: " + actualPreview);
                }
            } catch (IOException e) {
                return new JudgeRunResult(Verdict.ERROR, totalTime, maxMemoryKb, null, null, "Failed to compare output: " + e.getMessage());
            }
        }

        return new JudgeRunResult(Verdict.AC, totalTime, maxMemoryKb, null, null, "Accepted");
    }

    private Path prepareWorkDir(Submission submission) throws IOException {
        Path base = Path.of(properties.getJudge().getWorkDir()).toAbsolutePath();
        Files.createDirectories(base);
        Path workDir = base.resolve("sub-" + submission.getId());
        Files.createDirectories(workDir);
        return workDir;
    }

    private void writeSource(Path workDir, String fileName, String code) throws IOException {
        Files.writeString(workDir.resolve(fileName), code, StandardCharsets.UTF_8);
    }

    private ProcessRunner.CommandResult runDockerCommand(String image, String command, Path workDir, Path testcaseDir, Duration timeout, boolean measure) {
        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("--rm");
        cmd.add("--network");
        cmd.add("none");//断网
        cmd.add("--security-opt");
        cmd.add("no-new-privileges");//禁止用户通过漏洞提升root权限
        cmd.add("--cap-drop");
        cmd.add("ALL");//剥夺所有系统特权（如修改时间、挂载磁盘）
        cmd.add("--read-only");//容器根目录只读
        cmd.add("--tmpfs");
        cmd.add("/tmp:rw,nosuid,nodev,noexec,size=64m");//使用内存文件系统挂载 /tmp
        cmd.add("--tmpfs");
        cmd.add("/var/tmp:rw,nosuid,nodev,noexec,size=64m");
        cmd.add("--cpus");//限制cpu 内存 进程数
        cmd.add(Double.toString(properties.getJudge().getCpus()));
        cmd.add("--memory");
        cmd.add(properties.getJudge().getMemoryMb() + "m");
        cmd.add("--pids-limit");
        cmd.add(Integer.toString(properties.getJudge().getPidsLimit()));
        cmd.add("-v");//挂载 将data/work映射到docker的WORKSPACE
        cmd.add(workDir.toAbsolutePath().toString() + ":" + WORKSPACE);
        if (testcaseDir != null) {
            cmd.add("-v");//挂载 将data/testcases映射到docker的TESTCASES
            cmd.add(testcaseDir.toAbsolutePath().toString() + ":" + TESTCASES + ":ro");
        }
        cmd.add("-w");
        cmd.add(WORKSPACE);//设置初始工作目录
        cmd.add(image);//设置镜像名
        if (measure) {//时间性能测量
            cmd.add("/usr/bin/time");
            cmd.add("-f");
            cmd.add("TIME:%e MEM:%M");//格式化输出
        }
        cmd.add("/bin/sh");
        cmd.add("-c");
        cmd.add(command);

        try {
            return ProcessRunner.run(cmd, timeout);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ProcessRunner.CommandResult(-1, true, "", e.getMessage());
        }
    }

    private String imageFor(Language language) {
        return properties.getJudge().getImages().get(language.name());
    }

    public void cleanupWorkDir(Long submissionId) {//清楚掉目录that有用户代码源代码和二进制文件和输出结果文件.out
        Path base = Path.of(properties.getJudge().getWorkDir()).toAbsolutePath().normalize();
        Path workDir = base.resolve("sub-" + submissionId).normalize();
        if (!workDir.startsWith(base)) {
            return;
        }
        if (!Files.exists(workDir)) {
            return;
        }
        try {
            Files.walk(workDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    private static String normalizePath(String path) {
        return path.replace('\\', '/');
    }

    private static String preview(Path path) throws IOException {
        String content = Files.readString(path, StandardCharsets.UTF_8).trim();
        if (content.length() > 200) {
            return content.substring(0, 200) + "...";
        }
        return content.replace("\n", "\\n");
    }

    private static MetricResult parseMetrics(String stderr) {
        if (stderr == null) {
            return new MetricResult(0, 0);
        }
        Matcher matcher = TIME_PATTERN.matcher(stderr);
        if (matcher.find()) {
            double seconds = Double.parseDouble(matcher.group(1));
            long timeMs = (long) (seconds * 1000);
            long memKb = Long.parseLong(matcher.group(2));
            return new MetricResult(timeMs, memKb);
        }
        return new MetricResult(0, 0);
    }

    private static String stripTimeMarker(String value) {
        if (value == null) {
            return "";
        }
        //正则 删掉含有 TIME: 的那一行，剩下的就是报错信息
        return value.replaceAll("(?m)^TIME:.*$", "").trim();
    }

    private record MetricResult(long timeMs, long memoryKb) {}

    private record CompileAndRunSpec(String sourceFileName, String compileCommand, String runCommand) {
        static CompileAndRunSpec forLanguage(Language language) {
            return switch (language) {
                case C -> new CompileAndRunSpec("Main.c", "gcc /workspace/Main.c -O2 -o /workspace/a.out", "/workspace/a.out < {input} > {output}");
                case CPP -> new CompileAndRunSpec("Main.cpp", "g++ /workspace/Main.cpp -O2 -std=c++17 -o /workspace/a.out", "/workspace/a.out < {input} > {output}");
                case JAVA -> new CompileAndRunSpec("Main.java", "javac /workspace/Main.java", "java -cp /workspace Main < {input} > {output}");
                case PYTHON -> new CompileAndRunSpec("main.py", null, "python /workspace/main.py < {input} > {output}");
            };
        }
    }
}
