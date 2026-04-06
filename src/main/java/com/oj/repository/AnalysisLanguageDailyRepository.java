package com.oj.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oj.entity.AnalysisLanguageDaily;
import com.oj.entity.AnalysisLanguageDailyId;

public interface AnalysisLanguageDailyRepository extends JpaRepository<AnalysisLanguageDaily, AnalysisLanguageDailyId> {
    List<AnalysisLanguageDaily> findByDtBetweenOrderByDtAscLanguageAsc(String start, String end);

    List<AnalysisLanguageDaily> findByDtOrderByLanguageAsc(String dt);
}
