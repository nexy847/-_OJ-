package com.oj.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oj.entity.AnalysisProblemDifficultyDaily;
import com.oj.entity.AnalysisProblemDifficultyDailyId;

public interface AnalysisProblemDifficultyDailyRepository extends JpaRepository<AnalysisProblemDifficultyDaily, AnalysisProblemDifficultyDailyId> {
    List<AnalysisProblemDifficultyDaily> findByDtOrderByDifficultyScoreDesc(String dt);

    List<AnalysisProblemDifficultyDaily> findByProblemIdAndDtBetweenOrderByDtAsc(Long problemId, String start, String end);
}
