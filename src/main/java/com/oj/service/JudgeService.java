package com.oj.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oj.entity.JudgeResult;
import com.oj.entity.Problem;
import com.oj.entity.Submission;
import com.oj.entity.Testcase;
import com.oj.enums.SubmissionStatus;
import com.oj.repository.JudgeResultRepository;

@Service
public class JudgeService {
    private final DockerJudgeRunner dockerJudgeRunner;
    private final ProblemService problemService;
    private final JudgeResultRepository judgeResultRepository;
    private final AnalysisEventService analysisEventService;
    private final SubmissionService submissionService;

    public JudgeService(DockerJudgeRunner dockerJudgeRunner,
                        ProblemService problemService,
                        JudgeResultRepository judgeResultRepository,
                        AnalysisEventService analysisEventService,
                        SubmissionService submissionService) {
        this.dockerJudgeRunner = dockerJudgeRunner;
        this.problemService = problemService;
        this.judgeResultRepository = judgeResultRepository;
        this.analysisEventService = analysisEventService;
        this.submissionService = submissionService;
    }

    @Transactional
    public JudgeResult judge(Submission submission) {
        Problem problem = problemService.findById(submission.getProblemId())
                .orElseThrow(() -> new IllegalStateException("Problem not found: " + submission.getProblemId()));
        List<Testcase> testcases = problemService.findTestcases(problem.getId());

        JudgeRunResult runResult;
        try {
            runResult = dockerJudgeRunner.run(submission, problem, testcases);
        } finally {
            dockerJudgeRunner.cleanupWorkDir(submission.getId());
        }

        JudgeResult result = new JudgeResult();
        result.setSubmissionId(submission.getId());
        result.setVerdict(runResult.getVerdict());
        result.setTimeMs(runResult.getTimeMs());
        result.setMemoryKb(runResult.getMemoryKb());
        result.setCompileError(runResult.getCompileError());
        result.setRuntimeError(runResult.getRuntimeError());
        result.setMessage(runResult.getMessage());
        result.setCreatedAt(Instant.now());
        judgeResultRepository.save(result);

        submission.setStatus(SubmissionStatus.DONE);
        submission.setVerdict(runResult.getVerdict());
        submissionService.save(submission);

        analysisEventService.recordFrom(submission, runResult);
        return result;
    }
}
