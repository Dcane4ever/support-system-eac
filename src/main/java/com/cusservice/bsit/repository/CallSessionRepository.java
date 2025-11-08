package com.cusservice.bsit.repository;

import com.cusservice.bsit.model.CallSession;
import com.cusservice.bsit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CallSessionRepository extends JpaRepository<CallSession, Long> {
    
    // Find active call for a user
    @Query("SELECT c FROM CallSession c WHERE (c.caller = :user OR c.receiver = :user) " +
           "AND c.status IN ('INITIATED', 'RINGING', 'ACCEPTED', 'CONNECTED') " +
           "ORDER BY c.startedAt DESC")
    Optional<CallSession> findActiveCallByUser(@Param("user") User user);
    
    // Find all calls for a user
    @Query("SELECT c FROM CallSession c WHERE c.caller = :user OR c.receiver = :user " +
           "ORDER BY c.startedAt DESC")
    List<CallSession> findAllByUser(@Param("user") User user);
    
    // Find calls by status
    List<CallSession> findByStatusOrderByStartedAtDesc(CallSession.CallStatus status);
    
    // Find calls within date range
    @Query("SELECT c FROM CallSession c WHERE c.startedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY c.startedAt DESC")
    List<CallSession> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    // Find all calls (for admin/agent view)
    @Query("SELECT c FROM CallSession c ORDER BY c.startedAt DESC")
    List<CallSession> findAllOrderByStartedAtDesc();
    
    // Count active calls
    @Query("SELECT COUNT(c) FROM CallSession c WHERE c.status IN ('INITIATED', 'RINGING', 'ACCEPTED', 'CONNECTED')")
    long countActiveCalls();
}
