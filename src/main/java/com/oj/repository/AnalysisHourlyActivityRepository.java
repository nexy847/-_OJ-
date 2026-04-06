package com.oj.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oj.entity.AnalysisHourlyActivity;
import com.oj.entity.AnalysisHourlyActivityId;

public interface AnalysisHourlyActivityRepository extends JpaRepository<AnalysisHourlyActivity, AnalysisHourlyActivityId> {
    List<AnalysisHourlyActivity> findByDtOrderByHourOfDayAsc(String dt);
}
