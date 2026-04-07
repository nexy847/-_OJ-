package com.oj.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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

@Service
public class AdminSubmissionService {
    private final SubmissionRepository submissionRepository;
    private final JudgeResultRepository judgeResultRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;

    public AdminSubmissionService(SubmissionRepository submissionRepository,
                                  JudgeResultRepository judgeResultRepository,
                                  UserRepository userRepository,
                                  ProblemRepository problemRepository) {
        this.submissionRepository = submissionRepository;
        this.judgeResultRepository = judgeResultRepository;
        this.userRepository = userRepository;
        this.problemRepository = problemRepository;
    }

    public List<AdminSubmissionListItemResponse> listAll() {
        List<Submission> submissions = submissionRepository.findAllByOrderByCreatedAtAsc();
        Map<Long, String> usernames = loadUsers(submissions.stream().map(Submission::getUserId).toList());
        Map<Long, String> problemTitles = loadProblems(submissions.stream().map(Submission::getProblemId).toList());
        return submissions.stream()
                .map(submission -> new AdminSubmissionListItemResponse(
                        submission.getId(),
                        usernames.get(submission.getUserId()),
                        problemTitles.get(submission.getProblemId()),
                        submission.getVerdict() == null ? null : submission.getVerdict().name(),
                        submission.getCreatedAt()))
                .toList();
    }

    public AdminSubmissionDetailResponse getDetail(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        User user = userRepository.findById(submission.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Problem problem = problemRepository.findById(submission.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));
        JudgeResult result = judgeResultRepository.findBySubmissionId(submissionId).orElse(null);
        return new AdminSubmissionDetailResponse(
                submission.getId(),
                submission.getUserId(),
                user.getUsername(),
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

    private Map<Long, String> loadUsers(Collection<Long> ids) {
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));
    }

    private Map<Long, String> loadProblems(Collection<Long> ids) {
        return problemRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Problem::getId, Problem::getTitle, (a, b) -> a));
    }
}
