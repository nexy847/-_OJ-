package com.oj.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oj.entity.JudgeResult;

public interface JudgeResultRepository extends JpaRepository<JudgeResult, Long> {
    Optional<JudgeResult> findBySubmissionId(Long submissionId);
}
