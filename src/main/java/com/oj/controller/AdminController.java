package com.oj.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.oj.service.AnalysisExportService;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AnalysisExportService analysisExportService;

    public AdminController(AnalysisExportService analysisExportService) {
        this.analysisExportService = analysisExportService;
    }

    @PostMapping("/export-hdfs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String exportToHdfs() {
        int exported = analysisExportService.exportPendingToHdfs();
        return "exported=" + exported;
    }
}
