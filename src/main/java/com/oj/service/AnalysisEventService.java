package com.oj.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.oj.entity.AnalysisEvent;
import com.oj.entity.Submission;
import com.oj.repository.AnalysisEventRepository;

@Service
public class AnalysisEventService {
    private final AnalysisEventRepository analysisEventRepository;

    public AnalysisEventService(AnalysisEventRepository analysisEventRepository) {
        this.analysisEventRepository = analysisEventRepository;
    }

    public AnalysisEvent recordFrom(Submission submission, JudgeRunResult result) {
        AnalysisEvent event = new AnalysisEvent();
        event.setSubmissionId(submission.getId());
        event.setUserId(submission.getUserId());
        event.setProblemId(submission.getProblemId());
        event.setLanguage(submission.getLanguage());
        event.setVerdict(result.getVerdict());
        event.setTimeMs(result.getTimeMs());
        event.setMemoryKb(result.getMemoryKb());
        event.setCreatedAt(Instant.now());
        event.setExported(false);
        return analysisEventRepository.save(event);
    }
}
