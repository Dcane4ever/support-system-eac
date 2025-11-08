package com.cusservice.bsit.repository;

import com.cusservice.bsit.model.ChatSession;
import com.cusservice.bsit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByCustomerOrderByStartedAtDesc(User customer);
    List<ChatSession> findByAgentOrderByStartedAtDesc(User agent);
    List<ChatSession> findByStatusOrderByStartedAtAsc(ChatSession.SessionStatus status);
    List<ChatSession> findByStatus(ChatSession.SessionStatus status);
    List<ChatSession> findByCustomerAndStatus(User customer, ChatSession.SessionStatus status);
    List<ChatSession> findByAgentAndStatus(User agent, ChatSession.SessionStatus status);
    List<ChatSession> findAllByOrderByStartedAtDesc();
}
