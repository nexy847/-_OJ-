package com.oj.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;

import com.oj.dto.AdminHourlyActivityResponse;
import com.oj.dto.AdminLanguageDailyResponse;
import com.oj.dto.AdminProblemDifficultyHistoryResponse;
import com.oj.dto.AdminProblemDifficultyResponse;
import com.oj.dto.AdminProblemVerdictResponse;
import com.oj.dto.AdminSubmissionForecastResponse;
import com.oj.dto.AdminVerdictDailyResponse;
import com.oj.dto.UserLanguageDailyResponse;
import com.oj.dto.UserVerdictDailyResponse;
import com.oj.entity.AnalysisHourlyActivity;
import com.oj.entity.AnalysisLanguageDaily;
import com.oj.entity.AnalysisProblemDifficultyDaily;
import com.oj.entity.AnalysisProblemVerdictDaily;
import com.oj.entity.AnalysisSubmissionForecastDaily;
import com.oj.entity.AnalysisUserLanguageDaily;
import com.oj.entity.AnalysisUserVerdictDaily;
import com.oj.entity.AnalysisVerdictDaily;
import com.oj.repository.AnalysisHourlyActivityRepository;
import com.oj.repository.AnalysisLanguageDailyRepository;
import com.oj.repository.AnalysisProblemDifficultyDailyRepository;
import com.oj.repository.AnalysisProblemVerdictDailyRepository;
import com.oj.repository.AnalysisSubmissionForecastDailyRepository;
import com.oj.repository.AnalysisUserLanguageDailyRepository;
import com.oj.repository.AnalysisUserVerdictDailyRepository;
import com.oj.repository.AnalysisVerdictDailyRepository;
import com.oj.repository.ProblemRepository;

@Service
public class AnalyticsPlatformService {
    private final AnalysisLanguageDailyRepository languageDailyRepository;
    private final AnalysisVerdictDailyRepository verdictDailyRepository;
    private final AnalysisHourlyActivityRepository hourlyActivityRepository;
    private final AnalysisProblemDifficultyDailyRepository problemDifficultyDailyRepository;
    private final AnalysisProblemVerdictDailyRepository problemVerdictDailyRepository;
    private final AnalysisSubmissionForecastDailyRepository submissionForecastDailyRepository;
    private final AnalysisUserLanguageDailyRepository userLanguageDailyRepository;
    private final AnalysisUserVerdictDailyRepository userVerdictDailyRepository;
    private final ProblemRepository problemRepository;

    public AnalyticsPlatformService(AnalysisLanguageDailyRepository languageDailyRepository,
                                    AnalysisVerdictDailyRepository verdictDailyRepository,
                                    AnalysisHourlyActivityRepository hourlyActivityRepository,
                                    AnalysisProblemDifficultyDailyRepository problemDifficultyDailyRepository,
                                    AnalysisProblemVerdictDailyRepository problemVerdictDailyRepository,
                                    AnalysisSubmissionForecastDailyRepository submissionForecastDailyRepository,
                                    AnalysisUserLanguageDailyRepository userLanguageDailyRepository,
                                    AnalysisUserVerdictDailyRepository userVerdictDailyRepository,
                                    ProblemRepository problemRepository) {
        this.languageDailyRepository = languageDailyRepository;
        this.verdictDailyRepository = verdictDailyRepository;
        this.hourlyActivityRepository = hourlyActivityRepository;
        this.problemDifficultyDailyRepository = problemDifficultyDailyRepository;
        this.problemVerdictDailyRepository = problemVerdictDailyRepository;
        this.submissionForecastDailyRepository = submissionForecastDailyRepository;
        this.userLanguageDailyRepository = userLanguageDailyRepository;
        this.userVerdictDailyRepository = userVerdictDailyRepository;
        this.problemRepository = problemRepository;
    }

    public List<AdminLanguageDailyResponse> getAdminLanguageDaily(int days) {
        DateRange range = buildDateRange(days);
        return languageDailyRepository.findByDtBetweenOrderByDtAscLanguageAsc(range.start(), range.end()).stream()
                .map(row -> new AdminLanguageDailyResponse(row.getDt(), row.getLanguage(), row.getTotal(),
                        row.getAccepted(), rate(row.getAccepted(), row.getTotal()),
                        valueOrZero(row.getAvgTimeMs()), valueOrZero(row.getAvgMemoryKb())))
                .toList();
    }

    public List<AdminVerdictDailyResponse> getAdminVerdictDaily(int days) {
        DateRange range = buildDateRange(days);
        return verdictDailyRepository.findByDtBetweenOrderByDtAscVerdictAsc(range.start(), range.end()).stream()
                .map(row -> new AdminVerdictDailyResponse(row.getDt(), row.getVerdict(), row.getTotal()))
                .toList();
    }

    public List<AdminHourlyActivityResponse> getAdminHourlyActivity(LocalDate date) {
        return hourlyActivityRepository.findByDtOrderByHourOfDayAsc(date.toString()).stream()
                .map(row -> new AdminHourlyActivityResponse(row.getDt(), row.getHourOfDay(), row.getTotal(),
                        row.getAccepted(), rate(row.getAccepted(), row.getTotal()), row.getActiveUsers()))
                .toList();
    }

    public List<AdminProblemVerdictResponse> getAdminProblemVerdict(LocalDate date, Long problemId) {
        return problemVerdictDailyRepository.findByDtAndProblemIdOrderByVerdictAsc(date.toString(), problemId).stream()
                .map(row -> new AdminProblemVerdictResponse(row.getDt(), row.getProblemId(), row.getVerdict(), row.getTotal()))
                .toList();
    }

    public List<AdminProblemDifficultyResponse> getAdminProblemDifficulty(LocalDate date, int limit, String label) {
        List<AnalysisProblemDifficultyDaily> rows = problemDifficultyDailyRepository.findByDtOrderByDifficultyScoreDesc(date.toString());
        var titles = problemRepository.findAllById(rows.stream().map(AnalysisProblemDifficultyDaily::getProblemId).toList()).stream()
                .collect(java.util.stream.Collectors.toMap(com.oj.entity.Problem::getId, com.oj.entity.Problem::getTitle, (a, b) -> a));
        return rows.stream()
                .filter(row -> label == null || label.isBlank() || row.getDifficultyLabel().equalsIgnoreCase(label))
                .limit(limit)
                .map(row -> new AdminProblemDifficultyResponse(
                        row.getDt(),
                        row.getProblemId(),
                        titles.get(row.getProblemId()),
                        row.getTotalSubmissions(),
                        row.getAcceptedSubmissions(),
                        row.getAcRate(),
                        row.getAvgTimeMs(),
                        row.getAvgMemoryKb(),
                        row.getAvgAttemptsPerUser(),
                        row.getWaRate(),
                        row.getReRate(),
                        row.getTleRate(),
                        row.getDifficultyScore(),
                        row.getDifficultyLabel(),
                        row.getModelName()))
                .toList();
    }

    public List<AdminProblemDifficultyHistoryResponse> getAdminProblemDifficultyHistory(Long problemId, int days) {
        DateRange range = buildDateRange(days);
        return problemDifficultyDailyRepository.findByProblemIdAndDtBetweenOrderByDtAsc(problemId, range.start(), range.end()).stream()
                .map(row -> new AdminProblemDifficultyHistoryResponse(
                        row.getDt(),
                        row.getDifficultyScore(),
                        row.getDifficultyLabel(),
                        row.getAcRate(),
                        row.getAvgTimeMs(),
                        row.getAvgAttemptsPerUser()))
                .toList();
    }

    public List<AdminSubmissionForecastResponse> getAdminSubmissionForecast(LocalDate forecastDate) {
        String resolvedDate = forecastDate == null
                ? submissionForecastDailyRepository.findLatestForecastDate().orElse(LocalDate.now(ZoneOffset.UTC).toString())
                : forecastDate.toString();
        return submissionForecastDailyRepository.findByForecastDateOrderByTargetDateAsc(resolvedDate).stream()
                .map(row -> new AdminSubmissionForecastResponse(row.getForecastDate(), row.getTargetDate(),
                        row.getPredictedSubmissions(), row.getModelName()))
                .toList();
    }

    public List<UserLanguageDailyResponse> getUserLanguageDaily(Long userId, int days) {
        DateRange range = buildDateRange(days);
        return userLanguageDailyRepository.findByUserIdAndDtBetweenOrderByDtAscLanguageAsc(userId, range.start(), range.end()).stream()
                .map(row -> new UserLanguageDailyResponse(row.getDt(), row.getLanguage(), row.getTotal(),
                        row.getAccepted(), rate(row.getAccepted(), row.getTotal())))
                .toList();
    }

    public List<UserVerdictDailyResponse> getUserVerdictDaily(Long userId, int days) {
        DateRange range = buildDateRange(days);
        return userVerdictDailyRepository.findByUserIdAndDtBetweenOrderByDtAscVerdictAsc(userId, range.start(), range.end()).stream()
                .map(row -> new UserVerdictDailyResponse(row.getDt(), row.getVerdict(), row.getTotal()))
                .toList();
    }

    private DateRange buildDateRange(int days) {
        LocalDate endDate = LocalDate.now(ZoneOffset.UTC);
        LocalDate startDate = endDate.minusDays(Math.max(1, days) - 1L);
        return new DateRange(startDate.toString(), endDate.toString());
    }

    private double rate(long accepted, long total) {
        if (total <= 0) {
            return 0;
        }
        return accepted * 1.0 / total;
    }

    private double valueOrZero(Double value) {
        return value == null ? 0 : value;
    }

    private record DateRange(String start, String end) {
    }
}
