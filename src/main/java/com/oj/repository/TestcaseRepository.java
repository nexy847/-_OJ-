package com.oj.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oj.entity.Testcase;

public interface TestcaseRepository extends JpaRepository<Testcase, Long> {
    List<Testcase> findByProblemId(Long problemId);
    Optional<Testcase> findByIdAndProblemId(Long id, Long problemId);
}
