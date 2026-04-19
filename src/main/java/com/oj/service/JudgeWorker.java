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
        this.semaphore = new Semaphore(Math.max(1, properties.getJudge().getMaxConcurrency()));//初始化并发数量
    }

    //semaphore对并发数量作控制 并放行交给线程池执行
    @Scheduled(fixedDelayString = "${oj.judge.poll-interval:1000}")//每隔1000毫秒自动执行
    public void poll() {
        int available = semaphore.availablePermits();//查看还有多少个可并发的数量 为零就退出
        if (available <= 0) {
            return;
        }
        int batchSize = Math.min(properties.getJudge().getBatchSize(), available);//取出最小的那个 会让取出的任务数小于等于semaphore的可并发数
        List<JudgeTask> tasks = judgeTaskRepository.findReady(JudgeTaskStatus.PENDING, Instant.now(), PageRequest.of(0, batchSize));//创建第0页 每页batchSize行数据
        for (JudgeTask task : tasks) {
            if (tryClaim(task.getId())) {//将judge_task表中pending的数据设为running 被影响的数据大于零即进入判题
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
            failTask(task);//没有对应的submission直接fail就行了 只有条件齐全的判题流程出错才有重试机会
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
        if (task.getTries() < maxRetries) {//若失败 可重试 task和submission都可
            task.setStatus(JudgeTaskStatus.PENDING);
            task.setNextRunAt(Instant.now().plus(delaySeconds, ChronoUnit.SECONDS));//在当前时间戳上增加一个指定的delaysecond(此变量是一个间隔时间 用以下次执行)
            submission.setStatus(SubmissionStatus.PENDING);
            submission.setVerdict(Verdict.PENDING);
            submissionService.save(submission);
        } else {//超过重试次数 则设为failed和error
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
