package com.oj.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.oj.dto.ProblemDailyStatResponse;
import com.oj.dto.SummaryDailyStatResponse;
import com.oj.dto.UserDailyStatResponse;
import com.oj.entity.AnalysisProblemDaily;
import com.oj.entity.AnalysisSummaryDaily;
import com.oj.entity.AnalysisUserDaily;
import com.oj.repository.AnalysisProblemDailyRepository;
import com.oj.repository.AnalysisSummaryDailyRepository;
import com.oj.repository.AnalysisUserDailyRepository;

@Service
public class AnalyticsReadService {
    private final AnalysisSummaryDailyRepository summaryRepository;
    private final AnalysisProblemDailyRepository problemRepository;
    private final AnalysisUserDailyRepository userRepository;

    public AnalyticsReadService(AnalysisSummaryDailyRepository summaryRepository,
                                AnalysisProblemDailyRepository problemRepository,
                                AnalysisUserDailyRepository userRepository) {
        this.summaryRepository = summaryRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
    }

    public SummaryDailyStatResponse getSummary(String date) {
        return summaryRepository.findById(date)
                .map(row -> new SummaryDailyStatResponse(date, row.getTotal(), row.getAccepted(),
                        valueOrZero(row.getAvgTimeMs()), valueOrZero(row.getAvgMemoryKb())))
                .orElseGet(() -> new SummaryDailyStatResponse(date, 0, 0, 0, 0));
    }

    public List<ProblemDailyStatResponse> getProblemDaily(String date) {
        List<AnalysisProblemDaily> rows = problemRepository.findByDtOrderByProblemId(date);
        return rows.stream()
                .map(row -> new ProblemDailyStatResponse(row.getProblemId(), row.getTotal(), row.getAccepted(),
                        valueOrZero(row.getAvgTimeMs()), valueOrZero(row.getAvgMemoryKb())))
                .toList();
    }

    public List<UserDailyStatResponse> getUserDaily(String date) {
        List<AnalysisUserDaily> rows = userRepository.findByDtOrderByUserId(date);
        return rows.stream()
                .map(row -> new UserDailyStatResponse(row.getUserId(), row.getTotal(), row.getAccepted(),
                        valueOrZero(row.getAvgTimeMs()), valueOrZero(row.getAvgMemoryKb())))
                .toList();
    }

    private double valueOrZero(Double value) {
        return value == null ? 0 : value;
    }
}
