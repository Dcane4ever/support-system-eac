package com.cusservice.bsit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;
    
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;
    
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;
    
    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();
    
    // Alias getters/setters for compatibility
    public ChatSession getChatSession() {
        return session;
    }
    
    public void setChatSession(ChatSession session) {
        this.session = session;
    }
    
    public LocalDateTime getTimestamp() {
        return sentAt;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.sentAt = timestamp;
    }
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.TEXT;
    
    private String senderName;
    
    public enum MessageType {
        TEXT,
        SYSTEM,
        FILE,
        IMAGE
    }
}
