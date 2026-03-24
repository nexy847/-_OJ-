package com.oj.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oj.entity.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
}
