package com.oj.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oj.entity.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findAllByOrderByCreatedAtAsc();
}
