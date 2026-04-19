package com.oj.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.oj.dto.TestcaseContentResponse;
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
        List<Problem> problems = problemService.findAll();
        Set<Long> problemIds = problems.stream().map(Problem::getId).collect(Collectors.toSet());
        Map<Long, String> difficultyLabels = problemService.findLatestDifficultyLabels(problemIds);
        return problems.stream()
                .map(problem -> toResponse(problem, List.of(), difficultyLabels.get(problem.getId())))
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)//返回状态码
    @PreAuthorize("hasRole('ADMIN')")
    public ProblemResponse create(@Valid @RequestBody CreateProblemRequest request) {
        Problem problem = new Problem();
        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setTimeLimitMs(request.getTimeLimitMs());
        problem.setMemoryLimitMb(request.getMemoryLimitMb());
        Problem saved = problemService.createProblem(problem, request.getTestcases(), request.getTestcaseContents());
        List<Testcase> testcases = problemService.findTestcases(saved.getId());
        String difficultyLabel = problemService.findLatestDifficultyLabels(Set.of(saved.getId())).get(saved.getId());
        return toResponse(saved, testcases, difficultyLabel);
    }

    @GetMapping("/{id}")
    public ProblemResponse get(@PathVariable("id") Long id) {
        Problem problem = problemService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));
        List<Testcase> testcases = SecurityUtils.isAdmin()
                ? problemService.findTestcases(id)
                : List.of();
        String difficultyLabel = problemService.findLatestDifficultyLabels(Set.of(id)).get(id);
        return toResponse(problem, testcases, difficultyLabel);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProblemResponse update(@PathVariable("id") Long id, @Valid @RequestBody UpdateProblemRequest request) {
        Problem updated = problemService.updateProblem(id, request.getTitle(), request.getDescription(),
                request.getTimeLimitMs(), request.getMemoryLimitMb());
        List<Testcase> testcases = problemService.findTestcases(id);
        String difficultyLabel = problemService.findLatestDifficultyLabels(Set.of(id)).get(id);
        return toResponse(updated, testcases, difficultyLabel);
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

    @GetMapping("/{problemId}/testcases/{testcaseId}/content")
    @PreAuthorize("hasRole('ADMIN')")
    public TestcaseContentResponse getTestcaseContent(@PathVariable("problemId") Long problemId,
                                                      @PathVariable("testcaseId") Long testcaseId) {
        ProblemService.TestcaseContent content = problemService.getTestcaseContent(problemId, testcaseId);
        Testcase testcase = content.testcase();
        return new TestcaseContentResponse(
                testcase.getId(),
                testcase.getInputPath(),
                testcase.getOutputPath(),
                testcase.getWeight(),
                content.inputContent(),
                content.outputContent());
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

    private ProblemResponse toResponse(Problem problem, List<Testcase> testcases, String difficultyLabel) {
        List<TestcaseResponse> tcResponses = testcases.stream()
                .map(tc -> new TestcaseResponse(tc.getId(), tc.getInputPath(), tc.getOutputPath(), tc.getWeight()))
                .collect(Collectors.toList());
        return new ProblemResponse(problem.getId(), problem.getTitle(), problem.getDescription(),
                problem.getTimeLimitMs(), problem.getMemoryLimitMb(), problem.getCreatedAt(),
                difficultyLabel == null || difficultyLabel.isBlank() ? "暂无分类" : difficultyLabel, tcResponses);
    }
}
