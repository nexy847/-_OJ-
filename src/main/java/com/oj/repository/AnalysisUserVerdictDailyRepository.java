package com.oj.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oj.entity.AnalysisUserVerdictDaily;
import com.oj.entity.AnalysisUserVerdictDailyId;

public interface AnalysisUserVerdictDailyRepository extends JpaRepository<AnalysisUserVerdictDaily, AnalysisUserVerdictDailyId> {
    List<AnalysisUserVerdictDaily> findByUserIdAndDtBetweenOrderByDtAscVerdictAsc(Long userId, String start, String end);
}
