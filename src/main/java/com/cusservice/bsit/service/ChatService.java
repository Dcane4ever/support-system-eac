package com.cusservice.bsit.service;

import com.cusservice.bsit.dto.QueueStudentDTO;
import com.cusservice.bsit.model.ChatMessage;
import com.cusservice.bsit.model.ChatSession;
import com.cusservice.bsit.model.User;
import com.cusservice.bsit.repository.ChatMessageRepository;
import com.cusservice.bsit.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    
    // Queue for students waiting for an agent
    private final Queue<ChatSession> studentQueue = new ConcurrentLinkedQueue<>();
    
    /**
     * Create a new chat session for a student
     */
    public ChatSession createChatSession(User student) {
        System.out.println("Creating chat session for student: " + student.getUsername());
        
        ChatSession chatSession = new ChatSession();
        chatSession.setCustomer(student);
        chatSession.setStatus(ChatSession.SessionStatus.WAITING);
        chatSession.setStartedAt(LocalDateTime.now());
        
        ChatSession savedSession = chatSessionRepository.save(chatSession);
        System.out.println("Saved chat session with ID: " + savedSession.getId());
        
        // Add to waiting queue
        studentQueue.add(savedSession);
        System.out.println("Added to queue, current queue size: " + studentQueue.size());
        
        // Notify all agents about new student in queue
        Map<String, Object> queueUpdate = new HashMap<>();
        queueUpdate.put("type", "NEW_STUDENT");
        queueUpdate.put("sessionId", savedSession.getId());
        queueUpdate.put("studentName", student.getFullName());
        queueUpdate.put("studentId", student.getStudentId());
        queueUpdate.put("queueSize", studentQueue.size());
        messagingTemplate.convertAndSend("/topic/queue-updates", queueUpdate);
        
        return savedSession;
    }
    
    /**
     * Add a message to a chat session
     */
    @Transactional
    public ChatMessage addMessage(Long sessionId, User sender, String content) {
        return addMessage(sessionId, sender, content, ChatMessage.MessageType.TEXT);
    }
    
    /**
     * Add a message to a chat session with specific type
     */
    @Transactional
    public ChatMessage addMessage(Long sessionId, User sender, String content, ChatMessage.MessageType type) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Chat session not found"));
        
        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setSender(sender);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        message.setType(type);
        
        return chatMessageRepository.save(message);
    }
    
    /**
     * Get all messages for a chat session
     */
    public List<ChatMessage> getSessionMessages(Long sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Chat session not found"));
        return chatMessageRepository.findBySessionOrderBySentAtAsc(session);
    }
    
    /**
     * Assign an agent to a waiting chat session
     */
    @Transactional
    public ChatSession assignAgentToSession(Long sessionId, User agent) {
        System.out.println("Assigning agent " + agent.getUsername() + " to session " + sessionId);
        
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Chat session not found"));
        
        // Check if session is already assigned
        if (session.getStatus() == ChatSession.SessionStatus.ACTIVE && session.getAgent() != null) {
            if (session.getAgent().getId().equals(agent.getId())) {
                System.out.println("Session already assigned to this agent");
                return session;
            } else {
                throw new RuntimeException("Chat session is already assigned to another agent");
            }
        }
        
        if (session.getStatus() != ChatSession.SessionStatus.WAITING) {
            throw new RuntimeException("Chat session is not in waiting status. Current status: " + session.getStatus());
        }
        
        // Assign the agent
        session.setAgent(agent);
        session.setStatus(ChatSession.SessionStatus.ACTIVE);
        
        // Mark agent as unavailable
        userService.updateAgentAvailability(agent.getId(), false);
        
        // Remove from queue using ID comparison (since the session object might be a different instance)
        boolean removed = studentQueue.removeIf(s -> s.getId().equals(sessionId));
        System.out.println("Removed from queue: " + removed + ", new queue size: " + studentQueue.size());
        
            // Add system message about agent joining
            ChatMessage systemMessage = new ChatMessage();
            systemMessage.setSession(session);
            systemMessage.setSender(agent);
            systemMessage.setContent("Agent " + agent.getFullName() + " has joined the chat.");
            systemMessage.setType(ChatMessage.MessageType.SYSTEM);
            chatMessageRepository.save(systemMessage);        ChatSession savedSession = chatSessionRepository.save(session);
        
        // Notify student that agent joined
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "AGENT_JOINED");
        notification.put("sessionId", sessionId);
        notification.put("agentName", agent.getFullName());
        messagingTemplate.convertAndSendToUser(
            session.getCustomer().getUsername(),
            "/queue/notifications",
            notification
        );
        
        // Notify agents about queue update
        Map<String, Object> queueUpdate = new HashMap<>();
        queueUpdate.put("type", "STUDENT_ASSIGNED");
        queueUpdate.put("sessionId", sessionId);
        queueUpdate.put("queueSize", studentQueue.size());
        messagingTemplate.convertAndSend("/topic/queue-updates", queueUpdate);
        
        System.out.println("Session " + sessionId + " successfully assigned to agent " + agent.getUsername());
        return savedSession;
    }
    
    /**
     * End a chat session
     */
    @Transactional
    public ChatSession endChatSession(Long sessionId, User user) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Chat session not found"));
        
        // Remove from queue if still in waiting status (e.g., student cancels before agent accepts)
        if (session.getStatus() == ChatSession.SessionStatus.WAITING) {
            boolean removed = studentQueue.removeIf(s -> s.getId().equals(sessionId));
            System.out.println("Removed waiting session from queue: " + removed + ", new queue size: " + studentQueue.size());
            
            // Notify agents about queue update
            Map<String, Object> queueUpdate = new HashMap<>();
            queueUpdate.put("type", "QUEUE_UPDATE");
            queueUpdate.put("queueSize", studentQueue.size());
            messagingTemplate.convertAndSend("/topic/queue-updates", queueUpdate);
        }
        
        session.setStatus(ChatSession.SessionStatus.CLOSED);
        session.setEndedAt(LocalDateTime.now());
        
        // Add system message about chat ending
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setSession(session);
        systemMessage.setSender(user);
        systemMessage.setContent("Chat session has been closed.");
        systemMessage.setType(ChatMessage.MessageType.SYSTEM);
        chatMessageRepository.save(systemMessage);
        
        // Make agent available again if there was one
        if (session.getAgent() != null) {
            userService.updateAgentAvailability(session.getAgent().getId(), true);
            
            // Notify agent about queue status
            Map<String, Object> queueUpdate = new HashMap<>();
            queueUpdate.put("type", "QUEUE_UPDATE");
            queueUpdate.put("queueSize", studentQueue.size());
            messagingTemplate.convertAndSend("/topic/queue-updates", queueUpdate);
        }
        
        return chatSessionRepository.save(session);
    }
    
    /**
     * Get all students waiting in queue
     */
    public List<QueueStudentDTO> getWaitingStudents() {
        return studentQueue.stream()
                .map(QueueStudentDTO::fromChatSession)
                .collect(Collectors.toList());
    }
    
    /**
     * Get queue size
     */
    public int getQueueSize() {
        return studentQueue.size();
    }
    
    /**
     * Get position of a session in queue
     */
    public int getQueuePosition(Long sessionId) {
        int position = 0;
        for (ChatSession session : studentQueue) {
            position++;
            if (session.getId().equals(sessionId)) {
                return position;
            }
        }
        return -1; // Not in queue
    }
    
    /**
     * Get active sessions for an agent
     */
    public List<ChatSession> getActiveSessionsForAgent(User agent) {
        return chatSessionRepository.findByAgentAndStatus(agent, ChatSession.SessionStatus.ACTIVE);
    }
    
    /**
     * Get active session for a student
     */
    public Optional<ChatSession> getActiveSessionForStudent(User student) {
        List<ChatSession> sessions = chatSessionRepository.findByCustomerAndStatus(student, ChatSession.SessionStatus.ACTIVE);
        return sessions.isEmpty() ? Optional.empty() : Optional.of(sessions.get(0));
    }
    
    /**
     * Get waiting session for a student
     */
    public Optional<ChatSession> getWaitingSessionForStudent(User student) {
        List<ChatSession> sessions = chatSessionRepository.findByCustomerAndStatus(student, ChatSession.SessionStatus.WAITING);
        return sessions.isEmpty() ? Optional.empty() : Optional.of(sessions.get(0));
    }
    
    /**
     * Get session by ID
     */
    public Optional<ChatSession> getSessionById(Long sessionId) {
        return chatSessionRepository.findById(sessionId);
    }
    
    /**
     * Get chat history for a student
     */
    public List<ChatSession> getChatHistoryForStudent(User student) {
        return chatSessionRepository.findByCustomerOrderByStartedAtDesc(student);
    }
    
    /**
     * Get chat history for an agent
     */
    public List<ChatSession> getChatHistoryForAgent(User agent) {
        return chatSessionRepository.findByAgentOrderByStartedAtDesc(agent);
    }
    
    /**
     * Get all chat history with optional filters (for agents)
     */
    public List<ChatSession> getAllChatHistory(String studentName, String agentName, String status, String startDate, String endDate) {
        // Get all sessions ordered by most recent first
        List<ChatSession> allSessions = chatSessionRepository.findAllByOrderByStartedAtDesc();
        
        // Apply filters
        return allSessions.stream()
                .filter(session -> {
                    // Filter by student name
                    if (studentName != null && !studentName.trim().isEmpty()) {
                        String fullName = session.getCustomer().getFullName().toLowerCase();
                        if (!fullName.contains(studentName.toLowerCase())) {
                            return false;
                        }
                    }
                    
                    // Filter by agent name
                    if (agentName != null && !agentName.trim().isEmpty()) {
                        if (session.getAgent() == null) {
                            return false;
                        }
                        String fullName = session.getAgent().getFullName().toLowerCase();
                        if (!fullName.contains(agentName.toLowerCase())) {
                            return false;
                        }
                    }
                    
                    // Filter by status
                    if (status != null && !status.trim().isEmpty()) {
                        if (!session.getStatus().toString().equalsIgnoreCase(status)) {
                            return false;
                        }
                    }
                    
                    // Filter by date range
                    if (startDate != null && !startDate.trim().isEmpty()) {
                        try {
                            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
                            if (session.getStartedAt().isBefore(start)) {
                                return false;
                            }
                        } catch (Exception e) {
                            // Invalid date format, skip filter
                        }
                    }
                    
                    if (endDate != null && !endDate.trim().isEmpty()) {
                        try {
                            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
                            if (session.getStartedAt().isAfter(end)) {
                                return false;
                            }
                        } catch (Exception e) {
                            // Invalid date format, skip filter
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }
}
