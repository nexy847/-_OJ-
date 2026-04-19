package com.oj.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oj.dto.TestcaseRequest;
import com.oj.dto.CreateTestcaseContentRequest;
import com.oj.entity.AnalysisProblemDifficultyDaily;
import com.oj.entity.Problem;
import com.oj.entity.Testcase;
import com.oj.repository.AnalysisProblemDifficultyDailyRepository;
import com.oj.repository.ProblemRepository;
import com.oj.repository.TestcaseRepository;

@Service
public class ProblemService {
    private final ProblemRepository problemRepository;
    private final TestcaseRepository testcaseRepository;
    private final TestcaseStorageService testcaseStorageService;
    private final AnalysisProblemDifficultyDailyRepository analysisProblemDifficultyDailyRepository;

    public ProblemService(ProblemRepository problemRepository,
                          TestcaseRepository testcaseRepository,
                          TestcaseStorageService testcaseStorageService,
                          AnalysisProblemDifficultyDailyRepository analysisProblemDifficultyDailyRepository) {
        this.problemRepository = problemRepository;
        this.testcaseRepository = testcaseRepository;
        this.testcaseStorageService = testcaseStorageService;
        this.analysisProblemDifficultyDailyRepository = analysisProblemDifficultyDailyRepository;
    }

    @Transactional
    public Problem createProblem(Problem problem, List<TestcaseRequest> testcases) {
        return createProblem(problem, testcases, null);
    }

    //可以自己创建好测试文件后，在数据库录入元数据；也可直接录入文件内容
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

    public Map<Long, String> findLatestDifficultyLabels(Collection<Long> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return Map.of();//一个空的映射对象
        }
        return analysisProblemDifficultyDailyRepository.findLatestByProblemIds(new ArrayList<>(problemIds)).stream()
                .collect(Collectors.toMap(AnalysisProblemDifficultyDaily::getProblemId,
                        AnalysisProblemDifficultyDaily::getDifficultyLabel, (a, b) -> a));
    }

    public List<Testcase> findTestcases(Long problemId) {
        return testcaseRepository.findByProblemId(problemId);
    }

    public TestcaseContent getTestcaseContent(Long problemId, Long testcaseId) {
        Testcase testcase = testcaseRepository.findByIdAndProblemId(testcaseId, problemId)
                .orElseThrow(() -> new IllegalArgumentException("Testcase not found"));
        try {
            TestcaseStorageService.StoredTestcaseContent stored =
                    testcaseStorageService.read(testcase.getInputPath(), testcase.getOutputPath());
            return new TestcaseContent(testcase, stored.inputContent(), stored.outputContent());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read testcase files: " + ex.getMessage(), ex);
        }
    }

    //添加testcase内容 无须指定文件名
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
            return testcaseRepository.save(testcase);//仅针对testcase的权重设置 方法核心还是写入文件
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
            testcaseStorageService.delete(testcase.getInputPath(), testcase.getOutputPath());//删除文件
            testcaseRepository.delete(testcase);//删除数据库的元数据
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to delete testcase files: " + ex.getMessage(), ex);
        }
    }

    public record TestcaseContent(Testcase testcase, String inputContent, String outputContent) {}
}
