package com.cusservice.bsit.service;

import com.cusservice.bsit.model.CallSession;
import com.cusservice.bsit.model.ChatSession;
import com.cusservice.bsit.model.User;
import com.cusservice.bsit.repository.CallSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CallSessionService {
    
    @Autowired
    private CallSessionRepository callSessionRepository;
    
    /**
     * Initiate a new call session
     */
    @Transactional
    public CallSession initiateCall(User caller, User receiver) {
        CallSession callSession = new CallSession();
        callSession.setCaller(caller);
        callSession.setReceiver(receiver);
        callSession.setStatus(CallSession.CallStatus.INITIATED);
        callSession.setStartedAt(LocalDateTime.now());
        
        return callSessionRepository.save(callSession);
    }
    
    /**
     * Update call status to RINGING
     */
    @Transactional
    public CallSession updateToRinging(Long callId) {
        Optional<CallSession> optionalCall = callSessionRepository.findById(callId);
        if (optionalCall.isPresent()) {
            CallSession call = optionalCall.get();
            call.setStatus(CallSession.CallStatus.RINGING);
            return callSessionRepository.save(call);
        }
        return null;
    }
    
    /**
     * Accept a call
     */
    @Transactional
    public CallSession acceptCall(Long callId) {
        Optional<CallSession> optionalCall = callSessionRepository.findById(callId);
        if (optionalCall.isPresent()) {
            CallSession call = optionalCall.get();
            call.setStatus(CallSession.CallStatus.ACCEPTED);
            return callSessionRepository.save(call);
        }
        return null;
    }
    
    /**
     * Mark call as connected (WebRTC established)
     */
    @Transactional
    public CallSession markConnected(Long callId) {
        Optional<CallSession> optionalCall = callSessionRepository.findById(callId);
        if (optionalCall.isPresent()) {
            CallSession call = optionalCall.get();
            call.setStatus(CallSession.CallStatus.CONNECTED);
            return callSessionRepository.save(call);
        }
        return null;
    }
    
    /**
     * Reject a call
     */
    @Transactional
    public CallSession rejectCall(Long callId) {
        Optional<CallSession> optionalCall = callSessionRepository.findById(callId);
        if (optionalCall.isPresent()) {
            CallSession call = optionalCall.get();
            call.setStatus(CallSession.CallStatus.REJECTED);
            call.setEndedAt(LocalDateTime.now());
            return callSessionRepository.save(call);
        }
        return null;
    }
    
    /**
     * End a call
     */
    @Transactional
    public CallSession endCall(Long callId) {
        Optional<CallSession> optionalCall = callSessionRepository.findById(callId);
        if (optionalCall.isPresent()) {
            CallSession call = optionalCall.get();
            call.endCall(); // This sets status to COMPLETED and calculates duration
            return callSessionRepository.save(call);
        }
        return null;
    }
    
    /**
     * Mark call as missed
     */
    @Transactional
    public CallSession markMissed(Long callId) {
        Optional<CallSession> optionalCall = callSessionRepository.findById(callId);
        if (optionalCall.isPresent()) {
            CallSession call = optionalCall.get();
            call.setStatus(CallSession.CallStatus.MISSED);
            call.setEndedAt(LocalDateTime.now());
            return callSessionRepository.save(call);
        }
        return null;
    }
    
    /**
     * Mark call as failed
     */
    @Transactional
    public CallSession markFailed(Long callId) {
        Optional<CallSession> optionalCall = callSessionRepository.findById(callId);
        if (optionalCall.isPresent()) {
            CallSession call = optionalCall.get();
            call.setStatus(CallSession.CallStatus.FAILED);
            call.setEndedAt(LocalDateTime.now());
            return callSessionRepository.save(call);
        }
        return null;
    }
    
    /**
     * Link call session to chat session
     */
    @Transactional
    public CallSession linkToChatSession(Long callId, ChatSession chatSession) {
        Optional<CallSession> optionalCall = callSessionRepository.findById(callId);
        if (optionalCall.isPresent()) {
            CallSession call = optionalCall.get();
            call.setChatSession(chatSession);
            return callSessionRepository.save(call);
        }
        return null;
    }
    
    /**
     * Get active call for a user
     */
    public Optional<CallSession> getActiveCall(User user) {
        return callSessionRepository.findActiveCallByUser(user);
    }
    
    /**
     * Get call by ID
     */
    public Optional<CallSession> getCallById(Long callId) {
        return callSessionRepository.findById(callId);
    }
    
    /**
     * Get all calls for a user
     */
    public List<CallSession> getCallHistoryForUser(User user) {
        return callSessionRepository.findAllByUser(user);
    }
    
    /**
     * Get all calls (for admin/agent)
     */
    public List<CallSession> getAllCalls() {
        return callSessionRepository.findAllOrderByStartedAtDesc();
    }
    
    /**
     * Get calls by status
     */
    public List<CallSession> getCallsByStatus(CallSession.CallStatus status) {
        return callSessionRepository.findByStatusOrderByStartedAtDesc(status);
    }
    
    /**
     * Get calls within date range
     */
    public List<CallSession> getCallsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return callSessionRepository.findByDateRange(startDate, endDate);
    }
    
    /**
     * Count active calls
     */
    public long countActiveCalls() {
        return callSessionRepository.countActiveCalls();
    }
}
