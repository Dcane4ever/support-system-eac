package com.cusservice.bsit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;
    
    @ManyToOne
    @JoinColumn(name = "agent_id")
    private User agent;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status = SessionStatus.WAITING;
    
    @Column(nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();
    
    private LocalDateTime endedAt;
    
    private String topic;
    
    private Integer rating;
    
    private String feedback;
    
    @Column(nullable = false)
    private boolean anonymous = false;
    
    public enum SessionStatus {
        WAITING,
        ACTIVE,
        CLOSED
    }
    
    // Alias for compatibility
    public enum ChatStatus {
        WAITING,
        ACTIVE,
        CLOSED
    }
}
