package com.cusservice.bsit.dto;

import com.cusservice.bsit.model.ChatSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueStudentDTO {
    private Long sessionId;
    private String studentName;
    private String studentId;
    private String studentEmail;
    private LocalDateTime startedAt;
    private String topic;
    
    /**
     * Create DTO from ChatSession entity
     */
    public static QueueStudentDTO fromChatSession(ChatSession session) {
        QueueStudentDTO dto = new QueueStudentDTO();
        dto.setSessionId(session.getId());
        dto.setStudentName(session.getCustomer().getFullName());
        dto.setStudentId(session.getCustomer().getStudentId());
        dto.setStudentEmail(session.getCustomer().getEmail());
        dto.setStartedAt(session.getStartedAt());
        dto.setTopic(session.getTopic());
        return dto;
    }
}
