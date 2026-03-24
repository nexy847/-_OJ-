package com.oj.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.oj.config.OjProperties;
import com.oj.entity.JudgeTask;
import com.oj.entity.Submission;
import com.oj.enums.JudgeTaskStatus;
import com.oj.enums.SubmissionStatus;
import com.oj.enums.Verdict;
import com.oj.repository.JudgeTaskRepository;

@Component
public class JudgeWorker {
    private final JudgeTaskRepository judgeTaskRepository;
    private final SubmissionService submissionService;
    private final JudgeService judgeService;
    private final OjProperties properties;
    private final ThreadPoolTaskExecutor executor;
    private final Semaphore semaphore;

    public JudgeWorker(JudgeTaskRepository judgeTaskRepository,
                       SubmissionService submissionService,
                       JudgeService judgeService,
                       OjProperties properties,
                       ThreadPoolTaskExecutor judgeTaskExecutor) {
        this.judgeTaskRepository = judgeTaskRepository;
        this.submissionService = submissionService;
        this.judgeService = judgeService;
        this.properties = properties;
        this.executor = judgeTaskExecutor;
        this.semaphore = new Semaphore(Math.max(1, properties.getJudge().getMaxConcurrency()));
    }

    @Scheduled(fixedDelayString = "${oj.judge.poll-interval:1000}")//每隔1000毫秒自动执行
    public void poll() {
        int available = semaphore.availablePermits();
        if (available <= 0) {
            return;
        }
        int batchSize = Math.min(properties.getJudge().getBatchSize(), available);
        List<JudgeTask> tasks = judgeTaskRepository.findReady(JudgeTaskStatus.PENDING, Instant.now(), PageRequest.of(0, batchSize));
        for (JudgeTask task : tasks) {
            if (tryClaim(task.getId())) {
                semaphore.acquireUninterruptibly();//获取许可
                executor.execute(() -> {
                    try {
                        process(task.getId());
                    } finally {
                        semaphore.release();
                    }
                });
            }
        }
    }

    protected boolean tryClaim(Long taskId) {
        int updated = judgeTaskRepository.claimTask(taskId, JudgeTaskStatus.PENDING, JudgeTaskStatus.RUNNING, Instant.now());
        return updated > 0;
    }

    private void process(Long taskId) {
        JudgeTask task = judgeTaskRepository.findById(taskId).orElse(null);
        if (task == null || task.getStatus() != JudgeTaskStatus.RUNNING) {
            return;
        }

        Submission submission = submissionService.findById(task.getSubmissionId()).orElse(null);
        if (submission == null) {
            failTask(task);
            return;
        }

        try {
            submission.setStatus(SubmissionStatus.RUNNING);
            submissionService.save(submission);
            judgeService.judge(submission);
            task.setStatus(JudgeTaskStatus.DONE);
            task.setNextRunAt(null);
        } catch (Exception ex) {
            handleFailure(task, submission);
        } finally {
            task.setUpdatedAt(Instant.now());
            judgeTaskRepository.save(task);
        }
    }

    private void handleFailure(JudgeTask task, Submission submission) {
        int maxRetries = properties.getJudge().getMaxRetries();
        int delaySeconds = properties.getJudge().getRetryDelaySeconds();
        if (task.getTries() < maxRetries) {
            task.setStatus(JudgeTaskStatus.PENDING);
            task.setNextRunAt(Instant.now().plus(delaySeconds, ChronoUnit.SECONDS));
            submission.setStatus(SubmissionStatus.PENDING);
            submission.setVerdict(Verdict.PENDING);
            submissionService.save(submission);
        } else {
            submission.setStatus(SubmissionStatus.FAILED);
            submission.setVerdict(Verdict.ERROR);
            submissionService.save(submission);
            task.setStatus(JudgeTaskStatus.FAILED);
        }
    }

    private void failTask(JudgeTask task) {
        task.setStatus(JudgeTaskStatus.FAILED);
        task.setUpdatedAt(Instant.now());
        judgeTaskRepository.save(task);
    }
}
