package com.oj.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oj.dto.TestcaseRequest;
import com.oj.dto.CreateTestcaseContentRequest;
import com.oj.entity.Problem;
import com.oj.entity.Testcase;
import com.oj.repository.ProblemRepository;
import com.oj.repository.TestcaseRepository;

@Service
public class ProblemService {
    private final ProblemRepository problemRepository;
    private final TestcaseRepository testcaseRepository;
    private final TestcaseStorageService testcaseStorageService;

    public ProblemService(ProblemRepository problemRepository,
                          TestcaseRepository testcaseRepository,
                          TestcaseStorageService testcaseStorageService) {
        this.problemRepository = problemRepository;
        this.testcaseRepository = testcaseRepository;
        this.testcaseStorageService = testcaseStorageService;
    }

    @Transactional
    public Problem createProblem(Problem problem, List<TestcaseRequest> testcases) {
        return createProblem(problem, testcases, null);
    }

    @Transactional
    public Problem createProblem(Problem problem,
                                 List<TestcaseRequest> testcases,
                                 List<CreateTestcaseContentRequest> testcaseContents) {
        Problem saved = problemRepository.save(problem);
        if (testcases != null) {
            List<Testcase> entities = new ArrayList<>();
            for (TestcaseRequest request : testcases) {
                Testcase tc = new Testcase();
                tc.setProblemId(saved.getId());
                tc.setInputPath(request.getInputPath());
                tc.setOutputPath(request.getOutputPath());
                tc.setWeight(request.getWeight() == null ? 1 : request.getWeight());
                entities.add(tc);
            }
            testcaseRepository.saveAll(entities);
        }
        if (testcaseContents != null) {
            for (CreateTestcaseContentRequest request : testcaseContents) {
                addTestcaseContent(saved.getId(), request.getInputContent(), request.getOutputContent(), request.getWeight());
            }
        }
        return saved;
    }

    public Optional<Problem> findById(Long id) {
        return problemRepository.findById(id);
    }

    @Transactional
    public Problem updateProblem(Long id, String title, String description, Integer timeLimitMs, Integer memoryLimitMb) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));
        problem.setTitle(title);
        problem.setDescription(description);
        problem.setTimeLimitMs(timeLimitMs);
        problem.setMemoryLimitMb(memoryLimitMb);
        return problemRepository.save(problem);
    }

    public List<Problem> findAll() {
        return problemRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public List<Testcase> findTestcases(Long problemId) {
        return testcaseRepository.findByProblemId(problemId);
    }

    @Transactional
    public Testcase addTestcaseContent(Long problemId, String inputContent, String outputContent, Integer weight) {
        if (!problemRepository.existsById(problemId)) {
            throw new IllegalArgumentException("Problem not found");
        }
        try {
            TestcaseStorageService.StoredTestcase stored = testcaseStorageService.store(problemId, inputContent, outputContent);
            Testcase testcase = new Testcase();
            testcase.setProblemId(problemId);
            testcase.setInputPath(stored.inputPath());
            testcase.setOutputPath(stored.outputPath());
            testcase.setWeight(weight == null ? 1 : weight);
            return testcaseRepository.save(testcase);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to store testcase files: " + ex.getMessage(), ex);
        }
    }

    @Transactional
    public Testcase overwriteTestcase(Long problemId, Long testcaseId, String inputContent, String outputContent, Integer weight) {
        Testcase testcase = testcaseRepository.findById(testcaseId)
                .orElseThrow(() -> new IllegalArgumentException("Testcase not found"));
        if (!testcase.getProblemId().equals(problemId)) {
            throw new IllegalArgumentException("Testcase not in problem");
        }
        try {
            testcaseStorageService.overwrite(testcase.getInputPath(), testcase.getOutputPath(), inputContent, outputContent);
            if (weight != null) {
                testcase.setWeight(weight);
            }
            return testcaseRepository.save(testcase);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to overwrite testcase files: " + ex.getMessage(), ex);
        }
    }

    @Transactional
    public void deleteTestcase(Long problemId, Long testcaseId) {
        Testcase testcase = testcaseRepository.findById(testcaseId)
                .orElseThrow(() -> new IllegalArgumentException("Testcase not found"));
        if (!testcase.getProblemId().equals(problemId)) {
            throw new IllegalArgumentException("Testcase not in problem");
        }
        try {
            testcaseStorageService.delete(testcase.getInputPath(), testcase.getOutputPath());
            testcaseRepository.delete(testcase);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to delete testcase files: " + ex.getMessage(), ex);
        }
    }
}
