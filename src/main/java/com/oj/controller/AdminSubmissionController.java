package com.oj.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oj.dto.AdminSubmissionDetailResponse;
import com.oj.dto.AdminSubmissionListItemResponse;
import com.oj.service.AdminSubmissionService;

@RestController
@RequestMapping("/admin/submissions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubmissionController {
    private final AdminSubmissionService adminSubmissionService;

    public AdminSubmissionController(AdminSubmissionService adminSubmissionService) {
        this.adminSubmissionService = adminSubmissionService;
    }

    @GetMapping
    public List<AdminSubmissionListItemResponse> list() {
        return adminSubmissionService.listAll();
    }

    @GetMapping("/{id}")
    public AdminSubmissionDetailResponse detail(@PathVariable("id") Long id) {
        return adminSubmissionService.getDetail(id);
    }
}
