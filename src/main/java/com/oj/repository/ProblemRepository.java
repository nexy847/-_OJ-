package com.oj.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oj.entity.Problem;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
}
