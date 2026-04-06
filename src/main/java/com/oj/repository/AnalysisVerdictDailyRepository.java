package com.oj.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oj.entity.AnalysisVerdictDaily;
import com.oj.entity.AnalysisVerdictDailyId;

public interface AnalysisVerdictDailyRepository extends JpaRepository<AnalysisVerdictDaily, AnalysisVerdictDailyId> {
    List<AnalysisVerdictDaily> findByDtBetweenOrderByDtAscVerdictAsc(String start, String end);

    List<AnalysisVerdictDaily> findByDtOrderByVerdictAsc(String dt);
}
