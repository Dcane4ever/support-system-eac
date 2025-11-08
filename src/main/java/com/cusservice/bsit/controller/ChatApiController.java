package com.cusservice.bsit.controller;

import com.cusservice.bsit.model.ChatMessage;
import com.cusservice.bsit.model.ChatSession;
import com.cusservice.bsit.model.User;
import com.cusservice.bsit.service.ChatService;
import com.cusservice.bsit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatApiController {
    
    private final ChatService chatService;
    private final UserService userService;
    
    /**
     * Get current session status for logged-in user
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getChatStatus(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        
        User user = userOpt.get();
        Map<String, Object> status = new HashMap<>();
        
        // Check if student
        if (user.getRole() == User.Role.STUDENT) {
            Optional<ChatSession> activeSession = chatService.getActiveSessionForStudent(user);
            if (activeSession.isPresent()) {
                status.put("hasActiveSession", true);
                status.put("sessionId", activeSession.get().getId());
                status.put("status", "active");
                if (activeSession.get().getAgent() != null) {
                    status.put("agentName", activeSession.get().getAgent().getFullName());
                }
                return ResponseEntity.ok(status);
            }
            
            Optional<ChatSession> waitingSession = chatService.getWaitingSessionForStudent(user);
            if (waitingSession.isPresent()) {
                status.put("hasActiveSession", true);
                status.put("sessionId", waitingSession.get().getId());
                status.put("status", "waiting");
                status.put("position", chatService.getQueuePosition(waitingSession.get().getId()));
                status.put("queueSize", chatService.getQueueSize());
                return ResponseEntity.ok(status);
            }
            
            status.put("hasActiveSession", false);
            status.put("queueSize", chatService.getQueueSize());
        }
        
        // Check if agent
        if (user.getRole() == User.Role.SUPPORT_AGENT) {
            List<ChatSession> activeSessions = chatService.getActiveSessionsForAgent(user);
            status.put("activeSessions", activeSessions.size());
            status.put("queueSize", chatService.getQueueSize());
            status.put("waitingStudents", chatService.getWaitingStudents());
        }
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Get messages for a specific session
     */
    @GetMapping("/session/{sessionId}/messages")
    public ResponseEntity<List<ChatMessage>> getSessionMessages(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        User user = userOpt.get();
        Optional<ChatSession> sessionOpt = chatService.getSessionById(sessionId);
        
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ChatSession session = sessionOpt.get();
        
        // Check if user is authorized to view messages
        boolean authorized = session.getCustomer().getId().equals(user.getId()) ||
                           (session.getAgent() != null && session.getAgent().getId().equals(user.getId())) ||
                           user.getRole() == User.Role.ADMIN;
        
        if (!authorized) {
            return ResponseEntity.status(403).build();
        }
        
        List<ChatMessage> messages = chatService.getSessionMessages(sessionId);
        return ResponseEntity.ok(messages);
    }
    
    /**
     * Get chat history for current user
     */
    @GetMapping("/history")
    public ResponseEntity<List<ChatSession>> getChatHistory(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        User user = userOpt.get();
        List<ChatSession> history;
        
        if (user.getRole() == User.Role.STUDENT) {
            history = chatService.getChatHistoryForStudent(user);
        } else if (user.getRole() == User.Role.SUPPORT_AGENT) {
            history = chatService.getChatHistoryForAgent(user);
        } else {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(history);
    }
    
    /**
     * Get waiting students queue (agents only)
     */
    @GetMapping("/queue")
    public ResponseEntity<Map<String, Object>> getQueue(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty() || userOpt.get().getRole() != User.Role.SUPPORT_AGENT) {
            return ResponseEntity.status(403).build();
        }
        
        Map<String, Object> queueData = new HashMap<>();
        queueData.put("queueSize", chatService.getQueueSize());
        queueData.put("waitingStudents", chatService.getWaitingStudents());
        
        return ResponseEntity.ok(queueData);
    }
    
    /**
     * Get active sessions for agent (agents only)
     */
    @GetMapping("/agent/sessions")
    public ResponseEntity<List<ChatSession>> getAgentSessions(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty() || userOpt.get().getRole() != User.Role.SUPPORT_AGENT) {
            return ResponseEntity.status(403).build();
        }
        
        User agent = userOpt.get();
        List<ChatSession> activeSessions = chatService.getActiveSessionsForAgent(agent);
        
        return ResponseEntity.ok(activeSessions);
    }
    
    /**
     * Get all chat history for agents with optional filters
     */
    @GetMapping("/history/all")
    public ResponseEntity<List<ChatSession>> getAllChatHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String agentName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty() || userOpt.get().getRole() != User.Role.SUPPORT_AGENT) {
            return ResponseEntity.status(403).build();
        }
        
        List<ChatSession> history = chatService.getAllChatHistory(studentName, agentName, status, startDate, endDate);
        return ResponseEntity.ok(history);
    }
}
