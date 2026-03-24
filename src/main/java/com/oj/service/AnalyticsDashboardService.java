package com.oj.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.oj.dto.AdminDailyTrendResponse;
import com.oj.dto.AdminLanguageStatResponse;
import com.oj.dto.AdminOverviewResponse;
import com.oj.dto.AdminProblemStatResponse;
import com.oj.dto.AdminUserStatResponse;
import com.oj.dto.AdminVerdictStatResponse;
import com.oj.dto.UserDailyTrendResponse;
import com.oj.dto.UserLanguageStatResponse;
import com.oj.dto.UserOverviewResponse;
import com.oj.dto.UserProblemStatResponse;
import com.oj.dto.UserRecentSubmissionResponse;
import com.oj.dto.UserVerdictStatResponse;
import com.oj.entity.AnalysisProblemDaily;
import com.oj.entity.AnalysisSummaryDaily;
import com.oj.entity.AnalysisUserDaily;
import com.oj.entity.Problem;
import com.oj.entity.User;
import com.oj.repository.AnalysisEventRepository;
import com.oj.repository.AnalysisProblemDailyRepository;
import com.oj.repository.AnalysisSummaryDailyRepository;
import com.oj.repository.AnalysisUserDailyRepository;
import com.oj.repository.ProblemRepository;
import com.oj.repository.UserRepository;

@Service
public class AnalyticsDashboardService {
    private final AnalysisEventRepository analysisEventRepository;
    private final AnalysisSummaryDailyRepository summaryRepository;
    private final AnalysisProblemDailyRepository problemDailyRepository;
    private final AnalysisUserDailyRepository userDailyRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public AnalyticsDashboardService(AnalysisEventRepository analysisEventRepository,
                                     AnalysisSummaryDailyRepository summaryRepository,
                                     AnalysisProblemDailyRepository problemDailyRepository,
                                     AnalysisUserDailyRepository userDailyRepository,
                                     ProblemRepository problemRepository,
                                     UserRepository userRepository) {
        this.analysisEventRepository = analysisEventRepository;
        this.summaryRepository = summaryRepository;
        this.problemDailyRepository = problemDailyRepository;
        this.userDailyRepository = userDailyRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
    }

    public UserOverviewResponse getUserOverview(Long userId) {
        List<Object[]> rows = analysisEventRepository.aggregateUserOverview(userId);
        Object[] row = rows == null || rows.isEmpty() ? null : rows.get(0);
        long total = row == null ? 0 : toLong(row[0]);
        long accepted = row == null ? 0 : toLong(row[1]);
        double avgTime = row == null ? 0 : toDouble(row[2]);
        double avgMemory = row == null ? 0 : toDouble(row[3]);
        double acRate = rate(accepted, total);
        return new UserOverviewResponse(userId, total, accepted, acRate, avgTime, avgMemory);
    }

    public List<UserDailyTrendResponse> getUserDailyTrend(Long userId, int days) {
        LocalDate endDate = LocalDate.now(ZoneOffset.UTC);
        LocalDate startDate = endDate.minusDays(Math.max(1, days) - 1L);
        Instant start = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Object[]> rows = analysisEventRepository.aggregateUserDaily(userId, start, end);
        Map<String, UserDailyTrendResponse> map = new HashMap<>();
        for (Object[] row : rows) {
            String date = row[0].toString();
            long total = toLong(row[1]);
            long accepted = toLong(row[2]);
            double avgTime = toDouble(row[3]);
            double avgMemory = toDouble(row[4]);
            map.put(date, new UserDailyTrendResponse(date, total, accepted, rate(accepted, total), avgTime, avgMemory));
        }
        List<UserDailyTrendResponse> result = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {//将没有产出的日子全部设为零
            String date = cursor.toString();
            result.add(map.getOrDefault(date, new UserDailyTrendResponse(date, 0, 0, 0, 0, 0)));
            cursor = cursor.plusDays(1);
        }
        return result;
    }

    public List<UserLanguageStatResponse> getUserLanguageStats(Long userId) {
        List<Object[]> rows = analysisEventRepository.aggregateUserLanguage(userId);
        List<UserLanguageStatResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            String language = String.valueOf(row[0]);
            long total = toLong(row[1]);
            long accepted = toLong(row[2]);
            result.add(new UserLanguageStatResponse(language, total, accepted, rate(accepted, total)));
        }
        return result;
    }

    public List<UserVerdictStatResponse> getUserVerdictStats(Long userId) {
        List<Object[]> rows = analysisEventRepository.aggregateUserVerdict(userId);
        List<UserVerdictStatResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            String verdict = String.valueOf(row[0]);
            long total = toLong(row[1]);
            result.add(new UserVerdictStatResponse(verdict, total));
        }
        return result;
    }

    public List<UserProblemStatResponse> getUserProblemStats(Long userId, int limit) {
        List<Object[]> rows = analysisEventRepository.aggregateUserProblems(userId);
        List<UserProblemStatResponse> result = new ArrayList<>();
        Set<Long> problemIds = rows.stream()
                .map(row -> toLong(row[0]))
                .map(Long::valueOf)
                .collect(Collectors.toSet());
        Map<Long, String> titles = loadProblemTitles(problemIds);
        for (Object[] row : rows) {
            Long problemId = toLong(row[0]);
            long total = toLong(row[1]);
            long accepted = toLong(row[2]);
            double avgTime = toDouble(row[3]);
            double avgMemory = toDouble(row[4]);
            result.add(new UserProblemStatResponse(problemId, titles.get(problemId), total, accepted,
                    rate(accepted, total), avgTime, avgMemory));
        }
        result.sort(Comparator.comparingLong(UserProblemStatResponse::getTotal).reversed());
        if (limit > 0 && result.size() > limit) {
            return result.subList(0, limit);
        }
        return result;
    }

    public List<UserRecentSubmissionResponse> getUserRecentSubmissions(Long userId, int limit) {
        List<Object[]> rows = analysisEventRepository.findRecentByUser(userId, limit);
        Set<Long> problemIds = rows.stream()
                .map(row -> toLong(row[1]))
                .map(Long::valueOf)
                .collect(Collectors.toSet());
        Map<Long, String> titles = loadProblemTitles(problemIds);
        List<UserRecentSubmissionResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long submissionId = toLong(row[0]);
            Long problemId = toLong(row[1]);
            String language = String.valueOf(row[2]);
            String verdict = String.valueOf(row[3]);
            long timeMs = toLong(row[4]);
            long memoryKb = toLong(row[5]);
            Instant createdAt = toInstant(row[6]);
            result.add(new UserRecentSubmissionResponse(submissionId, problemId, titles.get(problemId), language,
                    verdict, timeMs, memoryKb, createdAt));
        }
        return result;
    }

    public AdminOverviewResponse getAdminOverview(LocalDate date) {
        String dt = date.toString();
        AnalysisSummaryDaily summary = summaryRepository.findById(dt).orElse(null);
        long total = summary == null ? 0 : summary.getTotal();
        long accepted = summary == null ? 0 : summary.getAccepted();
        double avgTime = summary == null ? 0 : valueOrZero(summary.getAvgTimeMs());
        double avgMemory = summary == null ? 0 : valueOrZero(summary.getAvgMemoryKb());
        long activeUsers = userDailyRepository.countByDt(dt);
        return new AdminOverviewResponse(dt, total, accepted, rate(accepted, total), avgTime, avgMemory, activeUsers);
    }

    public List<AdminDailyTrendResponse> getAdminDailyTrend(int days) {
        LocalDate endDate = LocalDate.now(ZoneOffset.UTC);
        LocalDate startDate = endDate.minusDays(Math.max(1, days) - 1L);
        String start = startDate.toString();
        String end = endDate.toString();
        List<AnalysisSummaryDaily> rows = summaryRepository.findByDtBetweenOrderByDt(start, end);
        Map<String, AnalysisSummaryDaily> map = rows.stream()
                .collect(Collectors.toMap(AnalysisSummaryDaily::getDt, row -> row));
        List<AdminDailyTrendResponse> result = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            String dt = cursor.toString();
            AnalysisSummaryDaily summary = map.get(dt);
            long total = summary == null ? 0 : summary.getTotal();
            long accepted = summary == null ? 0 : summary.getAccepted();
            double avgTime = summary == null ? 0 : valueOrZero(summary.getAvgTimeMs());
            double avgMemory = summary == null ? 0 : valueOrZero(summary.getAvgMemoryKb());
            result.add(new AdminDailyTrendResponse(dt, total, accepted, rate(accepted, total), avgTime, avgMemory));
            cursor = cursor.plusDays(1);
        }
        return result;
    }

    public List<AdminLanguageStatResponse> getAdminLanguageStats(LocalDate date) {
        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Object[]> rows = analysisEventRepository.aggregateLanguageByDate(start, end);
        List<AdminLanguageStatResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            String language = String.valueOf(row[0]);
            long total = toLong(row[1]);
            long accepted = toLong(row[2]);
            result.add(new AdminLanguageStatResponse(language, total, accepted, rate(accepted, total)));
        }
        return result;
    }

    public List<AdminVerdictStatResponse> getAdminVerdictStats(LocalDate date) {
        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Object[]> rows = analysisEventRepository.aggregateVerdictByDate(start, end);
        List<AdminVerdictStatResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            String verdict = String.valueOf(row[0]);
            long total = toLong(row[1]);
            result.add(new AdminVerdictStatResponse(verdict, total));
        }
        return result;
    }

    public List<AdminProblemStatResponse> getAdminTopProblems(LocalDate date, int limit) {
        String dt = date.toString();
        List<AnalysisProblemDaily> rows = limit <= 50
                ? problemDailyRepository.findTop50ByDtOrderByTotalDesc(dt)//jpa自动生成
                : problemDailyRepository.findByDtOrderByProblemId(dt);
        Set<Long> problemIds = rows.stream().map(AnalysisProblemDaily::getProblemId).collect(Collectors.toSet());
        Map<Long, String> titles = loadProblemTitles(problemIds);
        List<AdminProblemStatResponse> result = rows.stream()
                .map(row -> new AdminProblemStatResponse(row.getProblemId(), titles.get(row.getProblemId()),
                        row.getTotal(), row.getAccepted(), rate(row.getAccepted(), row.getTotal()),
                        valueOrZero(row.getAvgTimeMs()), valueOrZero(row.getAvgMemoryKb())))
                .sorted(Comparator.comparingLong(AdminProblemStatResponse::getTotal).reversed())
                .collect(Collectors.toList());
        if (limit > 0 && result.size() > limit) {
            return result.subList(0, limit);
        }
        return result;
    }

    public List<AdminUserStatResponse> getAdminTopUsers(LocalDate date, int limit) {
        String dt = date.toString();
        List<AnalysisUserDaily> rows = limit <= 50
                ? userDailyRepository.findTop50ByDtOrderByTotalDesc(dt)
                : userDailyRepository.findByDtOrderByUserId(dt);
        Set<Long> userIds = rows.stream().map(AnalysisUserDaily::getUserId).collect(Collectors.toSet());
        Map<Long, String> usernames = loadUsernames(userIds);
        List<AdminUserStatResponse> result = rows.stream()
                .map(row -> new AdminUserStatResponse(row.getUserId(), usernames.get(row.getUserId()),
                        row.getTotal(), row.getAccepted(), rate(row.getAccepted(), row.getTotal())))
                .sorted(Comparator.comparingLong(AdminUserStatResponse::getTotal).reversed())
                .collect(Collectors.toList());
        if (limit > 0 && result.size() > limit) {
            return result.subList(0, limit);
        }
        return result;
    }

    private Map<Long, String> loadProblemTitles(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return problemRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Problem::getId, Problem::getTitle, (a, b) -> a));
    }

    private Map<Long, String> loadUsernames(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private double toDouble(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(value.toString());
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

    private Instant toInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toInstant();
        }
        if (value instanceof java.util.Date date) {
            return date.toInstant();
        }
        return Instant.parse(value.toString());
    }
}
