package com.oj.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oj.entity.AnalysisUserDaily;
import com.oj.entity.AnalysisUserDailyId;

public interface AnalysisUserDailyRepository extends JpaRepository<AnalysisUserDaily, AnalysisUserDailyId> {
    List<AnalysisUserDaily> findByDtOrderByUserId(String dt);
    List<AnalysisUserDaily> findTop50ByDtOrderByTotalDesc(String dt);
    long countByDt(String dt);
}
