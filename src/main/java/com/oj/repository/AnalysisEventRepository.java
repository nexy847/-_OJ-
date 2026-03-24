package com.oj.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.oj.entity.AnalysisEvent;

public interface AnalysisEventRepository extends JpaRepository<AnalysisEvent, Long> {
    @Query("select e from AnalysisEvent e where e.exported = false order by e.createdAt asc")
    List<AnalysisEvent> findPending(Pageable pageable);

    @Query(value = "select count(*) as total, " +
            "sum(case when verdict='AC' then 1 else 0 end) as accepted, " +
            "avg(time_ms) as avg_time_ms, " +
            "avg(memory_kb) as avg_memory_kb " +
            "from analysis_events where user_id = ?1", nativeQuery = true)
    List<Object[]> aggregateUserOverview(Long userId);

    @Query(value = "select date(created_at) as dt, " +
            "count(*) as total, " +
            "sum(case when verdict='AC' then 1 else 0 end) as accepted, " +
            "avg(time_ms) as avg_time_ms, " +
            "avg(memory_kb) as avg_memory_kb " +
            "from analysis_events " +
            "where user_id = ?1 and created_at >= ?2 and created_at < ?3 " +
            "group by date(created_at) order by dt", nativeQuery = true)
    List<Object[]> aggregateUserDaily(Long userId, Instant start, Instant end);

    @Query(value = "select language, count(*) as total, " +
            "sum(case when verdict='AC' then 1 else 0 end) as accepted " +
            "from analysis_events where user_id = ?1 group by language", nativeQuery = true)
    List<Object[]> aggregateUserLanguage(Long userId);

    @Query(value = "select verdict, count(*) as total " +
            "from analysis_events where user_id = ?1 group by verdict", nativeQuery = true)
    List<Object[]> aggregateUserVerdict(Long userId);

    @Query(value = "select problem_id, count(*) as total, " +
            "sum(case when verdict='AC' then 1 else 0 end) as accepted, " +
            "avg(time_ms) as avg_time_ms, " +
            "avg(memory_kb) as avg_memory_kb " +
            "from analysis_events where user_id = ?1 group by problem_id", nativeQuery = true)
    List<Object[]> aggregateUserProblems(Long userId);

    @Query(value = "select submission_id, problem_id, language, verdict, time_ms, memory_kb, created_at " +
            "from analysis_events where user_id = ?1 order by created_at desc limit ?2", nativeQuery = true)
    List<Object[]> findRecentByUser(Long userId, int limit);

    @Query(value = "select language, count(*) as total, " +
            "sum(case when verdict='AC' then 1 else 0 end) as accepted " +
            "from analysis_events where created_at >= ?1 and created_at < ?2 group by language", nativeQuery = true)
    List<Object[]> aggregateLanguageByDate(Instant start, Instant end);

    @Query(value = "select verdict, count(*) as total " +
            "from analysis_events where created_at >= ?1 and created_at < ?2 group by verdict", nativeQuery = true)
    List<Object[]> aggregateVerdictByDate(Instant start, Instant end);
}
