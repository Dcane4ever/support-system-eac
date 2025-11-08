package com.cusservice.bsit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_sessions")
public class CallSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "caller_id", nullable = false)
    private User caller;
    
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;
    
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallStatus status;
    
    @Column(name = "call_type")
    private String callType; // "VOICE" or "VIDEO" (for future)
    
    @ManyToOne
    @JoinColumn(name = "chat_session_id")
    private ChatSession chatSession; // Link to associated chat session if any
    
    // Constructors
    public CallSession() {
        this.startedAt = LocalDateTime.now();
        this.status = CallStatus.INITIATED;
        this.callType = "VOICE";
    }
    
    public CallSession(User caller) {
        this();
        this.caller = caller;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getCaller() {
        return caller;
    }
    
    public void setCaller(User caller) {
        this.caller = caller;
    }
    
    public User getReceiver() {
        return receiver;
    }
    
    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getEndedAt() {
        return endedAt;
    }
    
    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }
    
    public Integer getDurationSeconds() {
        return durationSeconds;
    }
    
    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
    
    public CallStatus getStatus() {
        return status;
    }
    
    public void setStatus(CallStatus status) {
        this.status = status;
    }
    
    public String getCallType() {
        return callType;
    }
    
    public void setCallType(String callType) {
        this.callType = callType;
    }
    
    public ChatSession getChatSession() {
        return chatSession;
    }
    
    public void setChatSession(ChatSession chatSession) {
        this.chatSession = chatSession;
    }
    
    // Utility methods
    public void endCall() {
        this.endedAt = LocalDateTime.now();
        this.status = CallStatus.COMPLETED;
        
        if (this.startedAt != null && this.endedAt != null) {
            long seconds = java.time.Duration.between(this.startedAt, this.endedAt).getSeconds();
            this.durationSeconds = (int) seconds;
        }
    }
    
    public enum CallStatus {
        INITIATED,    // Call request sent
        RINGING,      // Receiver is being notified
        ACCEPTED,     // Receiver accepted, establishing connection
        CONNECTED,    // WebRTC connection established
        COMPLETED,    // Call ended normally
        REJECTED,     // Receiver rejected the call
        MISSED,       // Receiver didn't respond
        FAILED        // Technical failure
    }
}
