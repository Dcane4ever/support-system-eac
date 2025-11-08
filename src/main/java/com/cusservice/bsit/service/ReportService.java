package com.cusservice.bsit.service;

import com.cusservice.bsit.model.ChatSession;
import com.cusservice.bsit.model.User;
import com.cusservice.bsit.repository.ChatSessionRepository;
import com.cusservice.bsit.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {
    
    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;
    
    public ReportService(ChatSessionRepository chatSessionRepository, UserRepository userRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.userRepository = userRepository;
    }
    
    // Analytics Overview
    public Map<String, Object> getAnalyticsOverview() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Total chat sessions
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        analytics.put("totalChats", allSessions.size());
        
        // Active chats
        long activeChats = allSessions.stream()
            .filter(s -> s.getStatus() == ChatSession.SessionStatus.ACTIVE)
            .count();
        analytics.put("activeChats", activeChats);
        
        // Closed chats
        long closedChats = allSessions.stream()
            .filter(s -> s.getStatus() == ChatSession.SessionStatus.CLOSED)
            .count();
        analytics.put("closedChats", closedChats);
        
        // Total students involved in chats
        Set<Long> studentIds = allSessions.stream()
            .map(s -> s.getCustomer().getId())
            .collect(Collectors.toSet());
        analytics.put("totalStudents", studentIds.size());
        
        // Total teachers/support agents involved
        long totalTeachers = userRepository.findAll().stream()
            .filter(u -> u.getRole() == User.Role.TEACHER || u.getRole() == User.Role.SUPPORT_AGENT)
            .count();
        analytics.put("totalTeachers", totalTeachers);
        
        // Average chat duration
        double avgDuration = allSessions.stream()
            .filter(s -> s.getStartedAt() != null && s.getEndedAt() != null)
            .mapToLong(s -> {
                long duration = java.time.temporal.ChronoUnit.MINUTES.between(s.getStartedAt(), s.getEndedAt());
                return duration;
            })
            .average()
            .orElse(0.0);
        analytics.put("avgDuration", String.format("%.2f min", avgDuration));
        
        return analytics;
    }
    
    // Character Map Data (Student vs Teacher)
    public Map<String, Object> getCharacterMapData() {
        Map<String, Object> characterMap = new HashMap<>();
        
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        
        // Count by student role
        long studentCount = allSessions.stream()
            .filter(s -> s.getCustomer() != null && s.getCustomer().getRole() == User.Role.STUDENT)
            .count();
        
        // Count by teacher/support agent role
        long teacherCount = allSessions.stream()
            .filter(s -> s.getAgent() != null && 
                    (s.getAgent().getRole() == User.Role.TEACHER || s.getAgent().getRole() == User.Role.SUPPORT_AGENT))
            .count();
        
        characterMap.put("students", studentCount);
        characterMap.put("teachers", teacherCount);
        
        return characterMap;
    }
    
    // Trend Data (Last 7 days)
    public List<Map<String, Object>> getTrendData() {
        List<Map<String, Object>> trendData = new ArrayList<>();
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        
        // Group by date for last 7 days
        LocalDateTime now = LocalDateTime.now();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.withHour(23).withMinute(59).withSecond(59);
            
            long studentChats = allSessions.stream()
                .filter(s -> s.getStartedAt().isAfter(dayStart) && 
                           s.getStartedAt().isBefore(dayEnd) &&
                           s.getCustomer() != null && s.getCustomer().getRole() == User.Role.STUDENT)
                .count();
            
            long teacherChats = allSessions.stream()
                .filter(s -> s.getStartedAt().isAfter(dayStart) && 
                           s.getStartedAt().isBefore(dayEnd) &&
                           s.getAgent() != null &&
                           (s.getAgent().getRole() == User.Role.TEACHER || s.getAgent().getRole() == User.Role.SUPPORT_AGENT))
                .count();
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dayStart.toLocalDate().toString());
            dayData.put("students", studentChats);
            dayData.put("teachers", teacherChats);
            
            trendData.add(dayData);
        }
        
        return trendData;
    }
    
    // Session Details for Table View
    public List<Map<String, Object>> getSessionDetails() {
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        return allSessions.stream()
            .sorted(Comparator.comparing(ChatSession::getStartedAt).reversed())
            .map(session -> {
                Map<String, Object> sessionMap = new HashMap<>();
                sessionMap.put("id", session.getId());
                sessionMap.put("customer", session.getCustomer() != null ? session.getCustomer().getUsername() : "N/A");
                sessionMap.put("agent", session.getAgent() != null ? session.getAgent().getUsername() : "N/A");
                sessionMap.put("status", session.getStatus().toString());
                sessionMap.put("startedAt", session.getStartedAt().toString());
                sessionMap.put("endedAt", session.getEndedAt() != null ? session.getEndedAt().toString() : "Ongoing");
                
                long duration = 0;
                if (session.getStartedAt() != null && session.getEndedAt() != null) {
                    duration = java.time.temporal.ChronoUnit.MINUTES.between(session.getStartedAt(), session.getEndedAt());
                }
                sessionMap.put("duration", duration + " min");
                sessionMap.put("topic", session.getTopic() != null ? session.getTopic() : "General");
                sessionMap.put("rating", session.getRating() != null ? session.getRating() : "N/A");
                
                return sessionMap;
            })
            .collect(Collectors.toList());
    }

    // Agent/Teacher Performance Metrics
    public List<Map<String, Object>> getAgentPerformance() {
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        Map<Long, Map<String, Object>> agentMetrics = new HashMap<>();
        
        allSessions.forEach(session -> {
            if (session.getAgent() != null) {
                Long agentId = session.getAgent().getId();
                agentMetrics.putIfAbsent(agentId, new HashMap<>());
                
                Map<String, Object> metrics = agentMetrics.get(agentId);
                metrics.put("agentName", session.getAgent().getUsername());
                metrics.put("totalSessions", (Integer) metrics.getOrDefault("totalSessions", 0) + 1);
                
                if (session.getStatus() == ChatSession.SessionStatus.CLOSED) {
                    metrics.put("closedSessions", (Integer) metrics.getOrDefault("closedSessions", 0) + 1);
                }
                
                if (session.getRating() != null) {
                    int currentRatings = (Integer) metrics.getOrDefault("ratingCount", 0);
                    int currentSum = (Integer) metrics.getOrDefault("ratingSum", 0);
                    metrics.put("ratingSum", currentSum + session.getRating());
                    metrics.put("ratingCount", currentRatings + 1);
                    metrics.put("avgRating", String.format("%.2f", (double) (currentSum + session.getRating()) / (currentRatings + 1)));
                }
            }
        });
        
        return new ArrayList<>(agentMetrics.values());
    }

    // Status Distribution Analytics
    public Map<String, Object> getStatusDistribution() {
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        Map<String, Object> distribution = new HashMap<>();
        
        long waiting = allSessions.stream().filter(s -> s.getStatus() == ChatSession.SessionStatus.WAITING).count();
        long active = allSessions.stream().filter(s -> s.getStatus() == ChatSession.SessionStatus.ACTIVE).count();
        long closed = allSessions.stream().filter(s -> s.getStatus() == ChatSession.SessionStatus.CLOSED).count();
        
        distribution.put("waiting", waiting);
        distribution.put("active", active);
        distribution.put("closed", closed);
        distribution.put("waitingPercentage", allSessions.isEmpty() ? 0 : String.format("%.1f", (waiting * 100.0 / allSessions.size())));
        distribution.put("activePercentage", allSessions.isEmpty() ? 0 : String.format("%.1f", (active * 100.0 / allSessions.size())));
        distribution.put("closedPercentage", allSessions.isEmpty() ? 0 : String.format("%.1f", (closed * 100.0 / allSessions.size())));
        
        return distribution;
    }

    // Hourly Activity Distribution
    public List<Map<String, Object>> getHourlyActivity() {
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        Map<Integer, Long> hourlyMap = new HashMap<>();
        
        for (int i = 0; i < 24; i++) {
            hourlyMap.put(i, 0L);
        }
        
        allSessions.forEach(session -> {
            if (session.getStartedAt() != null) {
                int hour = session.getStartedAt().getHour();
                hourlyMap.put(hour, hourlyMap.get(hour) + 1);
            }
        });
        
        List<Map<String, Object>> hourlyData = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            Map<String, Object> hourData = new HashMap<>();
            hourData.put("hour", String.format("%02d:00", i));
            hourData.put("count", hourlyMap.get(i));
            hourlyData.add(hourData);
        }
        
        return hourlyData;
    }

    // Rating Distribution
    public Map<String, Object> getRatingDistribution() {
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        List<ChatSession> ratedSessions = allSessions.stream()
            .filter(s -> s.getRating() != null)
            .collect(Collectors.toList());
        
        Map<String, Object> ratingDist = new HashMap<>();
        
        if (ratedSessions.isEmpty()) {
            ratingDist.put("rating1", 0);
            ratingDist.put("rating2", 0);
            ratingDist.put("rating3", 0);
            ratingDist.put("rating4", 0);
            ratingDist.put("rating5", 0);
            ratingDist.put("avgRating", 0.0);
        } else {
            ratingDist.put("rating1", ratedSessions.stream().filter(s -> s.getRating() == 1).count());
            ratingDist.put("rating2", ratedSessions.stream().filter(s -> s.getRating() == 2).count());
            ratingDist.put("rating3", ratedSessions.stream().filter(s -> s.getRating() == 3).count());
            ratingDist.put("rating4", ratedSessions.stream().filter(s -> s.getRating() == 4).count());
            ratingDist.put("rating5", ratedSessions.stream().filter(s -> s.getRating() == 5).count());
            
            double avgRating = ratedSessions.stream()
                .mapToInt(ChatSession::getRating)
                .average()
                .orElse(0.0);
            ratingDist.put("avgRating", String.format("%.2f", avgRating));
        }
        
        return ratingDist;
    }
    
    // Helper method to filter sessions by date range
    private List<ChatSession> getSessionsByDateRange(int days) {
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        if (days == 0) return allSessions; // 0 means all time
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return allSessions.stream()
            .filter(s -> s.getStartedAt() != null && s.getStartedAt().isAfter(cutoffDate))
            .collect(Collectors.toList());
    }
    
    // Get response time statistics
    public Map<String, Object> getResponseTimeStats() {
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        List<Long> responseTimes = new ArrayList<>();
        
        allSessions.forEach(session -> {
            if (session.getStartedAt() != null && session.getEndedAt() != null) {
                long minutes = java.time.temporal.ChronoUnit.MINUTES.between(session.getStartedAt(), session.getEndedAt());
                responseTimes.add(minutes);
            }
        });
        
        Map<String, Object> stats = new HashMap<>();
        if (responseTimes.isEmpty()) {
            stats.put("avgResponseTime", 0);
            stats.put("minResponseTime", 0);
            stats.put("maxResponseTime", 0);
            stats.put("medianResponseTime", 0);
        } else {
            double average = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
            Long min = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
            Long max = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
            
            Collections.sort(responseTimes);
            Long median = responseTimes.get(responseTimes.size() / 2);
            
            stats.put("avgResponseTime", String.format("%.2f", average));
            stats.put("minResponseTime", min);
            stats.put("maxResponseTime", max);
            stats.put("medianResponseTime", median);
        }
        
        return stats;
    }
    
    // Get customer satisfaction trend
    public List<Map<String, Object>> getSatisfactionTrend() {
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        List<Map<String, Object>> satisfactionData = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.withHour(23).withMinute(59).withSecond(59);
            
            List<ChatSession> daySessions = allSessions.stream()
                .filter(s -> s.getStartedAt() != null && 
                           s.getStartedAt().isAfter(dayStart) && 
                           s.getStartedAt().isBefore(dayEnd) &&
                           s.getRating() != null)
                .collect(Collectors.toList());
            
            double avgRating = daySessions.isEmpty() ? 0 : 
                daySessions.stream().mapToInt(ChatSession::getRating).average().orElse(0);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dayStart.toLocalDate().toString());
            dayData.put("avgRating", String.format("%.2f", avgRating));
            dayData.put("ratedCount", daySessions.size());
            
            satisfactionData.add(dayData);
        }
        
        return satisfactionData;
    }
    
    // Get resolution rate metrics
    public Map<String, Object> getResolutionMetrics() {
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        
        long totalSessions = allSessions.size();
        long resolvedSessions = allSessions.stream()
            .filter(s -> s.getStatus() == ChatSession.SessionStatus.CLOSED)
            .count();
        
        long pendingSessions = allSessions.stream()
            .filter(s -> s.getStatus() == ChatSession.SessionStatus.WAITING)
            .count();
        
        double resolutionRate = totalSessions == 0 ? 0 : (resolvedSessions * 100.0 / totalSessions);
        double pendingRate = totalSessions == 0 ? 0 : (pendingSessions * 100.0 / totalSessions);
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalSessions", totalSessions);
        metrics.put("resolvedSessions", resolvedSessions);
        metrics.put("pendingSessions", pendingSessions);
        metrics.put("resolutionRate", String.format("%.2f", resolutionRate));
        metrics.put("pendingRate", String.format("%.2f", pendingRate));
        
        return metrics;
    }
    
    // Get topic-wise distribution
    public List<Map<String, Object>> getTopicDistribution() {
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        Map<String, Long> topicMap = new HashMap<>();
        
        allSessions.forEach(session -> {
            String topic = session.getTopic() != null ? session.getTopic() : "General";
            topicMap.put(topic, topicMap.getOrDefault(topic, 0L) + 1);
        });
        
        return topicMap.entrySet().stream()
            .map(entry -> {
                Map<String, Object> topicData = new HashMap<>();
                topicData.put("topic", entry.getKey());
                topicData.put("count", entry.getValue());
                topicData.put("percentage", String.format("%.1f", (entry.getValue() * 100.0 / allSessions.size())));
                return topicData;
            })
            .sorted((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")))
            .collect(Collectors.toList());
    }
    
    // Get peak hours analysis
    public List<Map<String, Object>> getPeakHours() {
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        Map<Integer, Long> hourlyMap = new HashMap<>();
        
        for (int i = 0; i < 24; i++) {
            hourlyMap.put(i, 0L);
        }
        
        allSessions.forEach(session -> {
            if (session.getStartedAt() != null) {
                int hour = session.getStartedAt().getHour();
                hourlyMap.put(hour, hourlyMap.get(hour) + 1);
            }
        });
        
        return hourlyMap.entrySet().stream()
            .map(entry -> {
                Map<String, Object> hourData = new HashMap<>();
                hourData.put("hour", String.format("%02d:00", entry.getKey()));
                hourData.put("sessions", entry.getValue());
                hourData.put("isPeakHour", entry.getValue() > (allSessions.size() / 24));
                return hourData;
            })
            .collect(Collectors.toList());
    }
    
    // Get top customers by engagement
    public List<Map<String, Object>> getTopCustomers() {
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        Map<Long, Map<String, Object>> customerMap = new HashMap<>();
        
        allSessions.forEach(session -> {
            if (session.getCustomer() != null) {
                Long customerId = session.getCustomer().getId();
                customerMap.putIfAbsent(customerId, new HashMap<>());
                
                Map<String, Object> customerData = customerMap.get(customerId);
                customerData.put("customerName", session.getCustomer().getUsername());
                customerData.put("sessionCount", (Integer) customerData.getOrDefault("sessionCount", 0) + 1);
                
                if (session.getRating() != null) {
                    int currentCount = (Integer) customerData.getOrDefault("ratingCount", 0);
                    int currentSum = (Integer) customerData.getOrDefault("ratingSum", 0);
                    customerData.put("ratingSum", currentSum + session.getRating());
                    customerData.put("ratingCount", currentCount + 1);
                    customerData.put("avgRating", String.format("%.2f", (double) (currentSum + session.getRating()) / (currentCount + 1)));
                }
            }
        });
        
        return customerMap.values().stream()
            .sorted((a, b) -> Integer.compare((Integer) b.get("sessionCount"), (Integer) a.get("sessionCount")))
            .limit(10)
            .collect(Collectors.toList());
    }
}
