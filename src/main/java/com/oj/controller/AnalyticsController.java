package com.oj.controller;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oj.dto.AdminDailyTrendResponse;
import com.oj.dto.AdminLanguageStatResponse;
import com.oj.dto.AdminLanguageDailyResponse;
import com.oj.dto.AdminOverviewResponse;
import com.oj.dto.AdminProblemDifficultyHistoryResponse;
import com.oj.dto.AdminProblemDifficultyResponse;
import com.oj.dto.AdminProblemStatResponse;
import com.oj.dto.AdminProblemVerdictResponse;
import com.oj.dto.AdminUserStatResponse;
import com.oj.dto.AdminHourlyActivityResponse;
import com.oj.dto.AdminVerdictStatResponse;
import com.oj.dto.AdminVerdictDailyResponse;
import com.oj.dto.AdminSubmissionForecastResponse;
import com.oj.dto.ProblemDailyStatResponse;
import com.oj.dto.SummaryDailyStatResponse;
import com.oj.dto.UserDailyTrendResponse;
import com.oj.dto.UserLanguageStatResponse;
import com.oj.dto.UserLanguageDailyResponse;
import com.oj.dto.UserOverviewResponse;
import com.oj.dto.UserProblemStatResponse;
import com.oj.dto.UserRecentSubmissionResponse;
import com.oj.dto.UserVerdictStatResponse;
import com.oj.dto.UserVerdictDailyResponse;
import com.oj.dto.UserDailyStatResponse;
import com.oj.service.AnalyticsReadService;
import com.oj.service.AnalyticsDashboardService;
import com.oj.service.AnalyticsPlatformService;
import com.oj.repository.UserRepository;
import com.oj.util.SecurityUtils;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {
    private final AnalyticsReadService analyticsReadService;
    private final AnalyticsDashboardService analyticsDashboardService;
    private final AnalyticsPlatformService analyticsPlatformService;
    private final UserRepository userRepository;

    public AnalyticsController(AnalyticsReadService analyticsReadService,
                               AnalyticsDashboardService analyticsDashboardService,
                               AnalyticsPlatformService analyticsPlatformService,
                               UserRepository userRepository) {
        this.analyticsReadService = analyticsReadService;
        this.analyticsDashboardService = analyticsDashboardService;
        this.analyticsPlatformService = analyticsPlatformService;
        this.userRepository = userRepository;
    }

    @GetMapping("/summary")//根据日期查询总提交数，总ac数，平均耗时，平均内存开销
    @PreAuthorize("hasRole('ADMIN')")
    public SummaryDailyStatResponse summary(@RequestParam("date") String date) {
        return analyticsReadService.getSummary(parseDate(date));
    }

    @GetMapping("/problems/daily")//根据日期 查询每个问题的提交总数，ac数，平均耗时，平均内存开销
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProblemDailyStatResponse> problemDaily(@RequestParam("date") String date) {
        return analyticsReadService.getProblemDaily(parseDate(date));
    }

    @GetMapping("/users/daily")//根据日期 查询每个用户的提交总数，ac数，平均耗时，平均内存开销
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDailyStatResponse> userDaily(@RequestParam("date") String date) {
        return analyticsReadService.getUserDaily(parseDate(date));
    }

    @GetMapping("/user/overview")//根据用户id 查询指定用户的提交总数，ac数，ac率，平均耗时，平均内存开销
    public UserOverviewResponse userOverview(@RequestParam(value = "userId", required = false) Long userId) {
        Long resolved = resolveUserId(userId);
        return analyticsDashboardService.getUserOverview(resolved);
    }

    @GetMapping("/user/daily")//根据用户id 查询过去日子的用户的每天的提交总数，ac数，ac率，平均耗时，平均内存开销(具体几天看参数days)
    public List<UserDailyTrendResponse> userDailyTrend(@RequestParam(value = "userId", required = false) Long userId,
                                                       @RequestParam(value = "days", required = false, defaultValue = "30") int days) {
        Long resolved = resolveUserId(userId);
        return analyticsDashboardService.getUserDailyTrend(resolved, clamp(days, 1, 365));
    }

    @GetMapping("/user/language")//根据用户id 查询这个用户针对每门语言的提交总数，ac数，ac率
    public List<UserLanguageStatResponse> userLanguage(@RequestParam(value = "userId", required = false) Long userId) {
        Long resolved = resolveUserId(userId);
        return analyticsDashboardService.getUserLanguageStats(resolved);
    }

    @GetMapping("/user/verdict")//查询指定用户的verdict分布，例如 AC、WA、RE、CE 各有多少次
    public List<UserVerdictStatResponse> userVerdict(@RequestParam(value = "userId", required = false) Long userId) {
        Long resolved = resolveUserId(userId);
        return analyticsDashboardService.getUserVerdictStats(resolved);
    }

    @GetMapping("/user/language/daily")//查询指定用户最近 N 天内按“日期 + 语言”拆分的提交统计，用于语言趋势图
    public List<UserLanguageDailyResponse> userLanguageDaily(@RequestParam(value = "userId", required = false) Long userId,
                                                             @RequestParam(value = "days", required = false, defaultValue = "30") int days) {
        Long resolved = resolveUserId(userId);
        return analyticsPlatformService.getUserLanguageDaily(resolved, clamp(days, 1, 365));
    }

    @GetMapping("/user/verdict/daily")//查询指定用户最近 N 天内按“日期 + verdict”拆分的统计，用于 verdict 变化趋势图
    public List<UserVerdictDailyResponse> userVerdictDaily(@RequestParam(value = "userId", required = false) Long userId,
                                                           @RequestParam(value = "days", required = false, defaultValue = "30") int days) {
        Long resolved = resolveUserId(userId);
        return analyticsPlatformService.getUserVerdictDaily(resolved, clamp(days, 1, 365));
    }

    @GetMapping("/user/problems")//查询指定用户在各题目上的提交统计，用于“做题情况 / 题目掌握度”展示
    public List<UserProblemStatResponse> userProblems(@RequestParam(value = "userId", required = false) Long userId,
                                                      @RequestParam(value = "limit", required = false, defaultValue = "50") int limit) {
        Long resolved = resolveUserId(userId);
        return analyticsDashboardService.getUserProblemStats(resolved, clamp(limit, 1, 200));
    }

    @GetMapping("/user/recent")//查询指定用户最近若干次提交的明细记录
    public List<UserRecentSubmissionResponse> userRecent(@RequestParam(value = "userId", required = false) Long userId,
                                                         @RequestParam(value = "limit", required = false, defaultValue = "10") int limit) {
        Long resolved = resolveUserId(userId);
        return analyticsDashboardService.getUserRecentSubmissions(resolved, clamp(limit, 1, 50));
    }

    @GetMapping("/admin/overview")//查询某一天的全站总览，包括提交数、AC 数、AC 率、平均耗时、平均内存和活跃用户数
    @PreAuthorize("hasRole('ADMIN')")
    public AdminOverviewResponse adminOverview(@RequestParam(value = "date", required = false) String date) {
        return analyticsDashboardService.getAdminOverview(parseDateOrToday(date));
    }

    @GetMapping("/admin/daily")//查询最近 N 天的全站日趋势，包括每日提交数、AC 数、AC 率、平均耗时、平均内存
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminDailyTrendResponse> adminDaily(@RequestParam(value = "days", required = false, defaultValue = "30") int days) {
        return analyticsDashboardService.getAdminDailyTrend(clamp(days, 1, 365));
    }

    @GetMapping("/admin/language")//查询某一天的语言分布，统计每种语言的提交数、AC 数、AC 率
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminLanguageStatResponse> adminLanguage(@RequestParam(value = "date", required = false) String date) {
        return analyticsDashboardService.getAdminLanguageStats(parseDateOrToday(date));
    }

    @GetMapping("/admin/verdict")//查询某一天的 verdict 分布，返回verdict（AC之类）和对应的总数
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminVerdictStatResponse> adminVerdict(@RequestParam(value = "date", required = false) String date) {
        return analyticsDashboardService.getAdminVerdictStats(parseDateOrToday(date));
    }

    @GetMapping("/admin/language/daily")//查询最近 N 天的“日期 + 语言”统计，用于平台语言趋势图
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminLanguageDailyResponse> adminLanguageDaily(@RequestParam(value = "days", required = false, defaultValue = "30") int days) {
        return analyticsPlatformService.getAdminLanguageDaily(clamp(days, 1, 365));
    }

    @GetMapping("/admin/verdict/daily")//查询最近 N 天的“日期 + verdict”统计，用于 verdict 趋势图
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminVerdictDailyResponse> adminVerdictDaily(@RequestParam(value = "days", required = false, defaultValue = "30") int days) {
        return analyticsPlatformService.getAdminVerdictDaily(clamp(days, 1, 365));
    }

    @GetMapping("/admin/hourly")//查询某一天 24 小时内每小时的提交数、AC 数、AC 率、活跃用户数
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminHourlyActivityResponse> adminHourly(@RequestParam(value = "date", required = false) String date) {
        return analyticsPlatformService.getAdminHourlyActivity(parseDateOrToday(date));
    }

    @GetMapping("/admin/problems/{problemId}/verdict")//查询某一天某道题的 verdict 分布
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminProblemVerdictResponse> adminProblemVerdict(@PathVariable("problemId") Long problemId,
                                                                 @RequestParam(value = "date", required = false) String date) {
        return analyticsPlatformService.getAdminProblemVerdict(parseDateOrToday(date), problemId);
    }

    @GetMapping("/admin/problems/difficulty")//查询某一天各题目的难度评估结果，包括 AC 率、平均耗时、平均尝试次数、难度分数和难度标签
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminProblemDifficultyResponse> adminProblemDifficulty(
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "limit", required = false, defaultValue = "50") int limit,
            @RequestParam(value = "label", required = false) String label) {
        return analyticsPlatformService.getAdminProblemDifficulty(parseDateOrToday(date), clamp(limit, 1, 500), label);
    }

    @GetMapping("/admin/problems/{problemId}/difficulty/history")//查询某道题最近 N 天的难度分数变化历史，用于难度趋势图
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminProblemDifficultyHistoryResponse> adminProblemDifficultyHistory(
            @PathVariable("problemId") Long problemId,
            @RequestParam(value = "days", required = false, defaultValue = "30") int days) {
        return analyticsPlatformService.getAdminProblemDifficultyHistory(problemId, clamp(days, 1, 365));
    }

    @GetMapping("/admin/forecast/submissions")//查询 Spark 生成的未来每日提交量预测结果；不传 forecastDate 时返回最新一批预测
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminSubmissionForecastResponse> adminSubmissionForecast(
            @RequestParam(value = "forecastDate", required = false) String forecastDate) {
        return analyticsPlatformService.getAdminSubmissionForecast(parseNullableDate(forecastDate));
    }

    @GetMapping("/admin/problems/top")//查询某一天提交量最高的题目排行，并返回通过率、平均耗时、平均内存等指标
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminProblemStatResponse> adminTopProblems(@RequestParam(value = "date", required = false) String date,
                                                           @RequestParam(value = "limit", required = false, defaultValue = "20") int limit) {
        return analyticsDashboardService.getAdminTopProblems(parseDateOrToday(date), clamp(limit, 1, 200));
    }

    @GetMapping("/admin/users/top")//查询某一天提交量最高的用户排行，并返回通过率。 请求方式：GET 入参格式
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminUserStatResponse> adminTopUsers(@RequestParam(value = "date", required = false) String date,
                                                     @RequestParam(value = "limit", required = false, defaultValue = "20") int limit) {
        return analyticsDashboardService.getAdminTopUsers(parseDateOrToday(date), clamp(limit, 1, 200));
    }

    private String parseDate(String date) {
        try {
            LocalDate parsed = LocalDate.parse(date);
            return parsed.toString();
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format, expected YYYY-MM-DD");
        }
    }

    private LocalDate parseDateOrToday(String date) {
        if (date == null || date.isBlank()) {
            return LocalDate.now(ZoneOffset.UTC);
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format, expected YYYY-MM-DD");
        }
    }

    private LocalDate parseNullableDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format, expected YYYY-MM-DD");
        }
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private Long resolveUserId(Long userId) {
        if (userId != null) {
            return userId;
        }
        String username = SecurityUtils.currentUsername();
        if (username == null) {
            throw new IllegalArgumentException("User not authenticated");
        }
        return userRepository.findByUsername(username)
                .map(u -> u.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
