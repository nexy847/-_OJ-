package com.oj.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oj.entity.JudgeTask;
import com.oj.entity.Submission;
import com.oj.enums.JudgeTaskStatus;
import com.oj.enums.SubmissionStatus;
import com.oj.enums.Verdict;
import com.oj.repository.JudgeTaskRepository;
import com.oj.repository.SubmissionRepository;

@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final JudgeTaskRepository judgeTaskRepository;

    public SubmissionService(SubmissionRepository submissionRepository, JudgeTaskRepository judgeTaskRepository) {
        this.submissionRepository = submissionRepository;
        this.judgeTaskRepository = judgeTaskRepository;
    }

    @Transactional
    public Submission createSubmission(Submission submission) {
        submission.setStatus(SubmissionStatus.PENDING);
        submission.setVerdict(Verdict.PENDING);
        submission.setCreatedAt(Instant.now());
        submission.setUpdatedAt(Instant.now());
        Submission saved = submissionRepository.save(submission);

        JudgeTask task = new JudgeTask();
        task.setSubmissionId(saved.getId());
        task.setStatus(JudgeTaskStatus.PENDING);
        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        judgeTaskRepository.save(task);

        return saved;
    }

    public Optional<Submission> findById(Long id) {
        return submissionRepository.findById(id);
    }

    public Submission save(Submission submission) {
        submission.setUpdatedAt(Instant.now());
        return submissionRepository.save(submission);
    }
}
