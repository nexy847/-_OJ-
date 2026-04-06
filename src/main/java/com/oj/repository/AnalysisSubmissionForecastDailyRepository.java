package com.oj.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.oj.entity.AnalysisSubmissionForecastDaily;

public interface AnalysisSubmissionForecastDailyRepository extends JpaRepository<AnalysisSubmissionForecastDaily, Long> {
    List<AnalysisSubmissionForecastDaily> findByForecastDateOrderByTargetDateAsc(String forecastDate);

    @Query("select max(a.forecastDate) from AnalysisSubmissionForecastDaily a")
    Optional<String> findLatestForecastDate();
}
