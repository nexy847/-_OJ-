package com.oj.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProcessRunner {
    public static CommandResult run(List<String> command, Duration timeout) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(false);//不合并进程 stdout和stderr分开走
        Process process = builder.start();//启动外部命令 docker run。。。

        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        //两个抽水泵
        Thread outThread = new Thread(() -> readStream(process.getInputStream(), stdout));
        Thread errThread = new Thread(() -> readStream(process.getErrorStream(), stderr));//stderr不仅有错误信息，也含有程序的状态信息
        outThread.start();
        errThread.start();

        boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
        }

        outThread.join();
        errThread.join();//结束后 返回类对象

        int exitCode = finished ? process.exitValue() : -1;
        return new CommandResult(exitCode, finished, stdout.toString(), stderr.toString());
    }

    private static void readStream(java.io.InputStream stream, StringBuilder builder) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } catch (IOException ignored) {
        }
    }

    public record CommandResult(int exitCode, boolean finished, String stdout, String stderr) {
        public boolean isSuccess() {
            return finished && exitCode == 0;
        }
    }
}
