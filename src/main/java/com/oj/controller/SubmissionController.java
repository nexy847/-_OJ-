package com.oj.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

import com.oj.config.OjProperties;
import com.oj.dto.CreateSubmissionRequest;
import com.oj.dto.JudgeResultResponse;
import com.oj.dto.SubmissionResponse;
import com.oj.entity.JudgeResult;
import com.oj.entity.Submission;
import com.oj.entity.User;
import com.oj.repository.JudgeResultRepository;
import com.oj.repository.UserRepository;
import com.oj.service.RateLimitService;
import com.oj.service.SubmissionService;
import com.oj.util.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/submissions")
public class SubmissionController {
    private final SubmissionService submissionService;
    private final JudgeResultRepository judgeResultRepository;
    private final UserRepository userRepository;
    private final RateLimitService rateLimitService;
    private final OjProperties properties;

    public SubmissionController(SubmissionService submissionService,
                                JudgeResultRepository judgeResultRepository,
                                UserRepository userRepository,
                                RateLimitService rateLimitService,
                                OjProperties properties) {
        this.submissionService = submissionService;
        this.judgeResultRepository = judgeResultRepository;
        this.userRepository = userRepository;
        this.rateLimitService = rateLimitService;
        this.properties = properties;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse create(@Valid @RequestBody CreateSubmissionRequest request,
                                     jakarta.servlet.http.HttpServletRequest httpRequest) {
        User currentUser = currentUserOrThrow();
        rateLimitService.checkSubmit(currentUser.getId() + ":" + httpRequest.getRemoteAddr());
        int maxCodeSizeKb = properties.getJudge().getMaxCodeSizeKb();
        if (request.getCode() != null && request.getCode().getBytes(StandardCharsets.UTF_8).length > maxCodeSizeKb * 1024L) {
            throw new IllegalArgumentException("Code size exceeds limit");
        }
        Submission submission = new Submission();
        submission.setUserId(currentUser.getId());
        submission.setProblemId(request.getProblemId());
        submission.setLanguage(request.getLanguage());
        submission.setCode(request.getCode());
        Submission saved = submissionService.createSubmission(submission);
        return toResponse(saved);
    }

    @GetMapping("/{id}")
    public SubmissionResponse get(@PathVariable("id") Long id) {
        Submission submission = submissionService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        assertCanAccess(submission);
        return toResponse(submission);
    }

    @GetMapping("/{id}/result")
    public JudgeResultResponse getResult(@PathVariable("id") Long id) {
        JudgeResult result = judgeResultRepository.findBySubmissionId(id)
                .orElseThrow(() -> new IllegalArgumentException("Result not found"));
        Submission submission = submissionService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        assertCanAccess(submission);
        return new JudgeResultResponse(result.getSubmissionId(), result.getVerdict(), result.getTimeMs(),
                result.getMemoryKb(), result.getCompileError(), result.getRuntimeError(), result.getMessage(), result.getCreatedAt());
    }

    private SubmissionResponse toResponse(Submission submission) {
        return new SubmissionResponse(submission.getId(), submission.getUserId(), submission.getProblemId(),
                submission.getLanguage(), submission.getStatus(), submission.getVerdict(),
                submission.getCreatedAt(), submission.getUpdatedAt());
    }

    private void assertCanAccess(Submission submission) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        User current = currentUserOrThrow();
        if (!current.getId().equals(submission.getUserId())) {
            throw new AccessDeniedException("Forbidden");
        }
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
