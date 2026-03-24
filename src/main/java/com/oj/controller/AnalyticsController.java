package com.oj.controller;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oj.dto.AdminDailyTrendResponse;
import com.oj.dto.AdminLanguageStatResponse;
import com.oj.dto.AdminOverviewResponse;
import com.oj.dto.AdminProblemStatResponse;
import com.oj.dto.AdminUserStatResponse;
import com.oj.dto.AdminVerdictStatResponse;
import com.oj.dto.ProblemDailyStatResponse;
import com.oj.dto.SummaryDailyStatResponse;
import com.oj.dto.UserDailyTrendResponse;
import com.oj.dto.UserLanguageStatResponse;
import com.oj.dto.UserOverviewResponse;
import com.oj.dto.UserProblemStatResponse;
import com.oj.dto.UserRecentSubmissionResponse;
import com.oj.dto.UserVerdictStatResponse;
import com.oj.dto.UserDailyStatResponse;
import com.oj.service.AnalyticsReadService;
import com.oj.service.AnalyticsDashboardService;
import com.oj.repository.UserRepository;
import com.oj.util.SecurityUtils;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {
    private final AnalyticsReadService analyticsReadService;
    private final AnalyticsDashboardService analyticsDashboardService;
    private final UserRepository userRepository;

    public AnalyticsController(AnalyticsReadService analyticsReadService,
                               AnalyticsDashboardService analyticsDashboardService,
                               UserRepository userRepository) {
        this.analyticsReadService = analyticsReadService;
        this.analyticsDashboardService = analyticsDashboardService;
        this.userRepository = userRepository;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public SummaryDailyStatResponse summary(@RequestParam("date") String date) {
        return analyticsReadService.getSummary(parseDate(date));
    }

    @GetMapping("/problems/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProblemDailyStatResponse> problemDaily(@RequestParam("date") String date) {
        return analyticsReadService.getProblemDaily(parseDate(date));
    }

    @GetMapping("/users/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDailyStatResponse> userDaily(@RequestParam("date") String date) {
        return analyticsReadService.getUserDaily(parseDate(date));
    }

    @GetMapping("/user/overview")
    public UserOverviewResponse userOverview(@RequestParam(value = "userId", required = false) Long userId) {
        Long resolved = resolveUserId(userId);
        return analyticsDashboardService.getUserOverview(resolved);
    }

    @GetMapping("/user/daily")
    public List<UserDailyTrendResponse> userDailyTrend(@RequestParam(value = "userId", required = false) Long userId,
                                                       @RequestParam(value = "days", required = false, defaultValue = "30") int days) {
        Long resolved = resolveUserId(userId);
        return analyticsDashboardService.getUserDailyTrend(resolved, clamp(days, 1, 365));
    }

    @GetMapping("/user/language")
    public List<UserLanguageStatResponse> userLanguage(@RequestParam(value = "userId", required = false) Long userId) {
        Long resolved = resolveUserId(userId);
        return analyticsDashboardService.getUserLanguageStats(resolved);
    }

    @GetMapping("/user/verdict")
    public List<UserVerdictStatResponse> userVerdict(@RequestParam(value = "userId", required = false) Long userId) {
        Long resolved = resolveUserId(userId);
        return analyticsDashboardService.getUserVerdictStats(resolved);
    }

    @GetMapping("/user/problems")
    public List<UserProblemStatResponse> userProblems(@RequestParam(value = "userId", required = false) Long userId,
                                                      @RequestParam(value = "limit", required = false, defaultValue = "50") int limit) {
        Long resolved = resolveUserId(userId);
        return analyticsDashboardService.getUserProblemStats(resolved, clamp(limit, 1, 200));
    }

    @GetMapping("/user/recent")
    public List<UserRecentSubmissionResponse> userRecent(@RequestParam(value = "userId", required = false) Long userId,
                                                         @RequestParam(value = "limit", required = false, defaultValue = "10") int limit) {
        Long resolved = resolveUserId(userId);
        return analyticsDashboardService.getUserRecentSubmissions(resolved, clamp(limit, 1, 50));
    }

    @GetMapping("/admin/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminOverviewResponse adminOverview(@RequestParam(value = "date", required = false) String date) {
        return analyticsDashboardService.getAdminOverview(parseDateOrToday(date));
    }

    @GetMapping("/admin/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminDailyTrendResponse> adminDaily(@RequestParam(value = "days", required = false, defaultValue = "30") int days) {
        return analyticsDashboardService.getAdminDailyTrend(clamp(days, 1, 365));
    }

    @GetMapping("/admin/language")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminLanguageStatResponse> adminLanguage(@RequestParam(value = "date", required = false) String date) {
        return analyticsDashboardService.getAdminLanguageStats(parseDateOrToday(date));
    }

    @GetMapping("/admin/verdict")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminVerdictStatResponse> adminVerdict(@RequestParam(value = "date", required = false) String date) {
        return analyticsDashboardService.getAdminVerdictStats(parseDateOrToday(date));
    }

    @GetMapping("/admin/problems/top")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminProblemStatResponse> adminTopProblems(@RequestParam(value = "date", required = false) String date,
                                                           @RequestParam(value = "limit", required = false, defaultValue = "20") int limit) {
        return analyticsDashboardService.getAdminTopProblems(parseDateOrToday(date), clamp(limit, 1, 200));
    }

    @GetMapping("/admin/users/top")
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
