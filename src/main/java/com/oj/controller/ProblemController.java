package com.oj.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.oj.dto.CreateProblemRequest;
import com.oj.dto.CreateTestcaseContentRequest;
import com.oj.dto.ProblemResponse;
import com.oj.dto.TestcaseResponse;
import com.oj.dto.UpdateProblemRequest;
import com.oj.entity.Problem;
import com.oj.entity.Testcase;
import com.oj.service.ProblemService;
import com.oj.util.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/problems")
public class ProblemController {
    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping
    public List<ProblemResponse> list() {
        return problemService.findAll().stream()
                .map(problem -> toResponse(problem, List.of()))
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ProblemResponse create(@Valid @RequestBody CreateProblemRequest request) {
        Problem problem = new Problem();
        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setTimeLimitMs(request.getTimeLimitMs());
        problem.setMemoryLimitMb(request.getMemoryLimitMb());
        Problem saved = problemService.createProblem(problem, request.getTestcases(), request.getTestcaseContents());
        List<Testcase> testcases = problemService.findTestcases(saved.getId());
        return toResponse(saved, testcases);
    }

    @GetMapping("/{id}")
    public ProblemResponse get(@PathVariable("id") Long id) {
        Problem problem = problemService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));
        List<Testcase> testcases = SecurityUtils.isAdmin()
                ? problemService.findTestcases(id)
                : List.of();
        return toResponse(problem, testcases);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProblemResponse update(@PathVariable("id") Long id, @Valid @RequestBody UpdateProblemRequest request) {
        Problem updated = problemService.updateProblem(id, request.getTitle(), request.getDescription(),
                request.getTimeLimitMs(), request.getMemoryLimitMb());
        List<Testcase> testcases = problemService.findTestcases(id);
        return toResponse(updated, testcases);
    }

    @PostMapping("/{id}/testcases")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TestcaseResponse addTestcase(@PathVariable("id") Long id,
                                        @Valid @RequestBody CreateTestcaseContentRequest request) {
        Testcase testcase = problemService.addTestcaseContent(id, request.getInputContent(),
                request.getOutputContent(), request.getWeight());
        return new TestcaseResponse(testcase.getId(), testcase.getInputPath(), testcase.getOutputPath(), testcase.getWeight());
    }

    @GetMapping("/{id}/testcases")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TestcaseResponse> listTestcases(@PathVariable("id") Long id) {
        List<Testcase> testcases = problemService.findTestcases(id);
        return testcases.stream()
                .map(tc -> new TestcaseResponse(tc.getId(), tc.getInputPath(), tc.getOutputPath(), tc.getWeight()))
                .collect(Collectors.toList());
    }

    @PutMapping("/{problemId}/testcases/{testcaseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public TestcaseResponse overwriteTestcase(@PathVariable("problemId") Long problemId,
                                              @PathVariable("testcaseId") Long testcaseId,
                                              @Valid @RequestBody CreateTestcaseContentRequest request) {
        Testcase testcase = problemService.overwriteTestcase(problemId, testcaseId,
                request.getInputContent(), request.getOutputContent(), request.getWeight());
        return new TestcaseResponse(testcase.getId(), testcase.getInputPath(), testcase.getOutputPath(), testcase.getWeight());
    }

    @DeleteMapping("/{problemId}/testcases/{testcaseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTestcase(@PathVariable("problemId") Long problemId,
                               @PathVariable("testcaseId") Long testcaseId) {
        problemService.deleteTestcase(problemId, testcaseId);
    }

    private ProblemResponse toResponse(Problem problem, List<Testcase> testcases) {
        List<TestcaseResponse> tcResponses = testcases.stream()
                .map(tc -> new TestcaseResponse(tc.getId(), tc.getInputPath(), tc.getOutputPath(), tc.getWeight()))
                .collect(Collectors.toList());
        return new ProblemResponse(problem.getId(), problem.getTitle(), problem.getDescription(),
                problem.getTimeLimitMs(), problem.getMemoryLimitMb(), problem.getCreatedAt(), tcResponses);
    }
}
