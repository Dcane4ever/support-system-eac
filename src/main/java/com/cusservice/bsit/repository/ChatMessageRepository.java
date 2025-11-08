package com.cusservice.bsit.repository;

import com.cusservice.bsit.model.ChatMessage;
import com.cusservice.bsit.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionOrderBySentAtAsc(ChatSession session);
    List<ChatMessage> findBySessionIdOrderBySentAtAsc(Long sessionId);
}
