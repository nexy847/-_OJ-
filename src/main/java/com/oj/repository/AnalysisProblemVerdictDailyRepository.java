package com.oj.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oj.entity.AnalysisProblemVerdictDaily;
import com.oj.entity.AnalysisProblemVerdictDailyId;

public interface AnalysisProblemVerdictDailyRepository extends JpaRepository<AnalysisProblemVerdictDaily, AnalysisProblemVerdictDailyId> {
    List<AnalysisProblemVerdictDaily> findByDtAndProblemIdOrderByVerdictAsc(String dt, Long problemId);
}
