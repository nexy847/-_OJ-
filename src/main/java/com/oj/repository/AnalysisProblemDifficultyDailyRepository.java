package com.oj.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.oj.entity.AnalysisProblemDifficultyDaily;
import com.oj.entity.AnalysisProblemDifficultyDailyId;

public interface AnalysisProblemDifficultyDailyRepository extends JpaRepository<AnalysisProblemDifficultyDaily, AnalysisProblemDifficultyDailyId> {
    List<AnalysisProblemDifficultyDaily> findByDtOrderByDifficultyScoreDesc(String dt);

    List<AnalysisProblemDifficultyDaily> findByProblemIdAndDtBetweenOrderByDtAsc(Long problemId, String start, String end);

    @Query(value = """
            select apdd.*
            from analysis_problem_difficulty_daily apdd
            inner join (
                select problem_id, max(dt) as max_dt
                from analysis_problem_difficulty_daily
                where problem_id in (:problemIds)
                group by problem_id
            ) latest on latest.problem_id = apdd.problem_id and latest.max_dt = apdd.dt
            """, nativeQuery = true)
    List<AnalysisProblemDifficultyDaily> findLatestByProblemIds(List<Long> problemIds);
}
