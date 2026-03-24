package com.oj.repository;

import java.util.List;

import java.time.Instant;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.oj.entity.JudgeTask;
import com.oj.enums.JudgeTaskStatus;

public interface JudgeTaskRepository extends JpaRepository<JudgeTask, Long> {
    @Query("select t from JudgeTask t where t.status = :status and (t.nextRunAt is null or t.nextRunAt <= :now) order by t.createdAt asc")
    List<JudgeTask> findReady(@Param("status") JudgeTaskStatus status, @Param("now") Instant now, Pageable pageable);

    @Modifying
    @Transactional
    @Query("update JudgeTask t set t.status = :running, t.updatedAt = :now, t.tries = t.tries + 1, t.nextRunAt = null " +
           "where t.id = :id and t.status = :pending and (t.nextRunAt is null or t.nextRunAt <= :now)")
    int claimTask(@Param("id") Long id,
                  @Param("pending") JudgeTaskStatus pending,
                  @Param("running") JudgeTaskStatus running,
                  @Param("now") Instant now);
}
