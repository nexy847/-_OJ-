package com.oj.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oj.entity.AnalysisUserLanguageDaily;
import com.oj.entity.AnalysisUserLanguageDailyId;

public interface AnalysisUserLanguageDailyRepository extends JpaRepository<AnalysisUserLanguageDaily, AnalysisUserLanguageDailyId> {
    List<AnalysisUserLanguageDaily> findByUserIdAndDtBetweenOrderByDtAscLanguageAsc(Long userId, String start, String end);
}
