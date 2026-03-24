package com.oj.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oj.entity.AnalysisSummaryDaily;

public interface AnalysisSummaryDailyRepository extends JpaRepository<AnalysisSummaryDaily, String> {
    List<AnalysisSummaryDaily> findByDtBetweenOrderByDt(String start, String end);
}
