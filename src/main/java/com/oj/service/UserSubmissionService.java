package com.oj.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import com.oj.dto.AdminSubmissionDetailResponse;
import com.oj.dto.AdminSubmissionListItemResponse;
import com.oj.entity.JudgeResult;
import com.oj.entity.Problem;
import com.oj.entity.Submission;
import com.oj.entity.User;
import com.oj.repository.JudgeResultRepository;
import com.oj.repository.ProblemRepository;
import com.oj.repository.SubmissionRepository;
import com.oj.repository.UserRepository;
import com.oj.util.SecurityUtils;

@Service
public class UserSubmissionService {
    private final SubmissionRepository submissionRepository;
    private final JudgeResultRepository judgeResultRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;

    public UserSubmissionService(SubmissionRepository submissionRepository,
                                 JudgeResultRepository judgeResultRepository,
                                 UserRepository userRepository,
                                 ProblemRepository problemRepository) {
        this.submissionRepository = submissionRepository;
        this.judgeResultRepository = judgeResultRepository;
        this.userRepository = userRepository;
        this.problemRepository = problemRepository;
    }

    public List<AdminSubmissionListItemResponse> listCurrentUser() {
        User currentUser = currentUserOrThrow();
        return submissionRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                .map(submission -> {
                    Problem problem = problemRepository.findById(submission.getProblemId()).orElse(null);
                    return new AdminSubmissionListItemResponse(
                            submission.getId(),
                            currentUser.getUsername(),
                            problem == null ? null : problem.getTitle(),
                            submission.getVerdict() == null ? null : submission.getVerdict().name(),
                            submission.getCreatedAt());
                })
                .toList();
    }

    public AdminSubmissionDetailResponse getCurrentUserDetail(Long submissionId) {
        User currentUser = currentUserOrThrow();
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        if (!currentUser.getId().equals(submission.getUserId())) {
            throw new AccessDeniedException("Forbidden");
        }
        Problem problem = problemRepository.findById(submission.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));
        JudgeResult result = judgeResultRepository.findBySubmissionId(submissionId).orElse(null);
        return new AdminSubmissionDetailResponse(
                submission.getId(),
                currentUser.getId(),
                currentUser.getUsername(),
                submission.getProblemId(),
                problem.getTitle(),
                submission.getLanguage() == null ? null : submission.getLanguage().name(),
                submission.getStatus() == null ? null : submission.getStatus().name(),
                submission.getVerdict() == null ? null : submission.getVerdict().name(),
                submission.getCreatedAt(),
                submission.getUpdatedAt(),
                submission.getCode(),
                result == null ? null : result.getTimeMs(),
                result == null ? null : result.getMemoryKb(),
                result == null ? null : result.getCompileError(),
                result == null ? null : result.getRuntimeError(),
                result == null ? null : result.getMessage());
    }

    private User currentUserOrThrow() {
        String username = SecurityUtils.currentUsername();
        if (username == null) {
            throw new AccessDeniedException("Forbidden");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Forbidden"));
    }
}
