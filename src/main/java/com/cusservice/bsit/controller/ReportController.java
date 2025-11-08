package com.cusservice.bsit.controller;

import com.cusservice.bsit.model.User;
import com.cusservice.bsit.service.ReportService;
import com.cusservice.bsit.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/support/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {
    
    private final ReportService reportService;
    private final UserService userService;
    
    public ReportController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }
    
    @GetMapping
    public String getReportsPage(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        model.addAttribute("user", user);
        return "support-reports";
    }
    
    @GetMapping("/api/analytics")
    @ResponseBody
    public Map<String, Object> getAnalytics() {
        return reportService.getAnalyticsOverview();
    }
    
    @GetMapping("/api/character-map")
    @ResponseBody
    public Map<String, Object> getCharacterMap() {
        return reportService.getCharacterMapData();
    }
    
    @GetMapping("/api/trend-data")
    @ResponseBody
    public List<Map<String, Object>> getTrendData() {
        return reportService.getTrendData();
    }
    
    @GetMapping("/api/session-details")
    @ResponseBody
    public List<Map<String, Object>> getSessionDetails() {
        return reportService.getSessionDetails();
    }
    
    @GetMapping("/api/agent-performance")
    @ResponseBody
    public List<Map<String, Object>> getAgentPerformance() {
        return reportService.getAgentPerformance();
    }
    
    @GetMapping("/api/status-distribution")
    @ResponseBody
    public Map<String, Object> getStatusDistribution() {
        return reportService.getStatusDistribution();
    }
    
    @GetMapping("/api/hourly-activity")
    @ResponseBody
    public List<Map<String, Object>> getHourlyActivity() {
        return reportService.getHourlyActivity();
    }
    
    @GetMapping("/api/rating-distribution")
    @ResponseBody
    public Map<String, Object> getRatingDistribution() {
        return reportService.getRatingDistribution();
    }
    
    @GetMapping("/api/response-time")
    @ResponseBody
    public Map<String, Object> getResponseTimeStats() {
        return reportService.getResponseTimeStats();
    }
    
    @GetMapping("/api/satisfaction-trend")
    @ResponseBody
    public List<Map<String, Object>> getSatisfactionTrend() {
        return reportService.getSatisfactionTrend();
    }
    
    @GetMapping("/api/resolution-metrics")
    @ResponseBody
    public Map<String, Object> getResolutionMetrics() {
        return reportService.getResolutionMetrics();
    }
    
    @GetMapping("/api/topic-distribution")
    @ResponseBody
    public List<Map<String, Object>> getTopicDistribution() {
        return reportService.getTopicDistribution();
    }
    
    @GetMapping("/api/peak-hours")
    @ResponseBody
    public List<Map<String, Object>> getPeakHours() {
        return reportService.getPeakHours();
    }
    
    @GetMapping("/api/top-customers")
    @ResponseBody
    public List<Map<String, Object>> getTopCustomers() {
        return reportService.getTopCustomers();
    }
}
