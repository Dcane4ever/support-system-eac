package com.cusservice.bsit.controller;

import com.cusservice.bsit.model.ChatMessage;
import com.cusservice.bsit.model.ChatSession;
import com.cusservice.bsit.model.User;
import com.cusservice.bsit.service.ChatService;
import com.cusservice.bsit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Student starts a chat session - joins the queue
     */
    @MessageMapping("/chat/start")
    public void startChat(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("==========================================");
            System.out.println("RECEIVED /chat/start message");
            System.out.println("Payload: " + payload);
            System.out.println("==========================================");
            
            String username = (String) payload.get("username");
            System.out.println("Student " + username + " requesting chat");
            
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("ERROR: User not found - " + username);
                sendError(username, "User not found");
                return;
            }
            
            User student = userOpt.get();
            System.out.println("Found user: " + student.getFullName() + " (ID: " + student.getId() + ")");
            
            // Check if student already has active or waiting session
            Optional<ChatSession> existingActive = chatService.getActiveSessionForStudent(student);
            if (existingActive.isPresent()) {
                System.out.println("Student already has active session: " + existingActive.get().getId());
                sendSessionInfo(student.getUsername(), existingActive.get(), "reconnected");
                return;
            }
            
            Optional<ChatSession> existingWaiting = chatService.getWaitingSessionForStudent(student);
            if (existingWaiting.isPresent()) {
                System.out.println("Student already in queue: " + existingWaiting.get().getId());
                int position = chatService.getQueuePosition(existingWaiting.get().getId());
                sendQueuePosition(student.getUsername(), existingWaiting.get().getId(), position);
                return;
            }
            
            // Create new chat session
            System.out.println("Creating new chat session for student: " + student.getUsername());
            ChatSession session = chatService.createChatSession(student);
            int position = chatService.getQueuePosition(session.getId());
            
            System.out.println("Session created successfully - ID: " + session.getId() + ", Position: " + position);
            
            // Send queue position to student
            sendQueuePosition(student.getUsername(), session.getId(), position);
            
            System.out.println("Chat session created with ID: " + session.getId());
            
        } catch (Exception e) {
            System.err.println("ERROR in startChat: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Agent accepts a student from the queue
     */
    @MessageMapping("/chat/accept")
    public void acceptChat(@Payload Map<String, Object> payload) {
        try {
            String agentUsername = (String) payload.get("agentUsername");
            Long sessionId = Long.valueOf(payload.get("sessionId").toString());
            
            System.out.println("Agent " + agentUsername + " accepting session " + sessionId);
            
            Optional<User> agentOpt = userService.findByUsername(agentUsername);
            if (agentOpt.isEmpty()) {
                System.err.println("Agent not found: " + agentUsername);
                return;
            }
            
            User agent = agentOpt.get();
            
            // Assign agent to session
            ChatSession session = chatService.assignAgentToSession(sessionId, agent);
            
            // Send session info to both student and agent
            sendSessionInfo(session.getCustomer().getUsername(), session, "active");
            sendSessionInfo(agent.getUsername(), session, "active");
            
            System.out.println("Agent " + agentUsername + " successfully assigned to session " + sessionId);
            
        } catch (Exception e) {
            System.err.println("Error accepting chat: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send a message in a chat session
     */
    @MessageMapping("/chat/message")
    public void sendMessage(@Payload Map<String, Object> payload) {
        try {
            Long sessionId = Long.valueOf(payload.get("sessionId").toString());
            String senderUsername = (String) payload.get("senderUsername");
            String content = (String) payload.get("content");
            String typeStr = payload.get("type") != null ? (String) payload.get("type") : "TEXT";
            
            Optional<User> senderOpt = userService.findByUsername(senderUsername);
            if (senderOpt.isEmpty()) {
                System.err.println("Sender not found: " + senderUsername);
                return;
            }
            
            User sender = senderOpt.get();
            
            // Parse message type
            ChatMessage.MessageType messageType;
            try {
                messageType = ChatMessage.MessageType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                messageType = ChatMessage.MessageType.TEXT;
            }
            
            ChatMessage message = chatService.addMessage(sessionId, sender, content, messageType);
            
            // Get session to find recipient
            Optional<ChatSession> sessionOpt = chatService.getSessionById(sessionId);
            if (sessionOpt.isEmpty()) {
                System.err.println("Session not found: " + sessionId);
                return;
            }
            
            ChatSession session = sessionOpt.get();
            
            // Send message to both participants
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("sessionId", sessionId);
            messageData.put("messageId", message.getId());
            messageData.put("content", message.getContent());
            messageData.put("senderUsername", sender.getUsername());
            messageData.put("senderName", sender.getFullName());
            messageData.put("timestamp", message.getSentAt().toString());
            messageData.put("type", message.getType().toString());
            
            // Send to student
            messagingTemplate.convertAndSendToUser(
                session.getCustomer().getUsername(),
                "/queue/messages",
                messageData
            );
            
            // Send to agent if assigned
            if (session.getAgent() != null) {
                messagingTemplate.convertAndSendToUser(
                    session.getAgent().getUsername(),
                    "/queue/messages",
                    messageData
                );
            }
            
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * End a chat session
     */
    @MessageMapping("/chat/end")
    public void endChat(@Payload Map<String, Object> payload) {
        try {
            Long sessionId = Long.valueOf(payload.get("sessionId").toString());
            String username = (String) payload.get("username");
            
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                System.err.println("User not found: " + username);
                return;
            }
            
            User user = userOpt.get();
            
            // Get session before closing
            Optional<ChatSession> sessionOpt = chatService.getSessionById(sessionId);
            if (sessionOpt.isEmpty()) {
                System.err.println("Session not found: " + sessionId);
                return;
            }
            
            ChatSession session = sessionOpt.get();
            
            // End the session
            chatService.endChatSession(sessionId, user);
            
            // Notify both participants
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "SESSION_ENDED");
            notification.put("sessionId", sessionId);
            
            messagingTemplate.convertAndSendToUser(
                session.getCustomer().getUsername(),
                "/queue/notifications",
                notification
            );
            
            if (session.getAgent() != null) {
                messagingTemplate.convertAndSendToUser(
                    session.getAgent().getUsername(),
                    "/queue/notifications",
                    notification
                );
            }
            
            System.out.println("Chat session " + sessionId + " ended by " + username);
            
        } catch (Exception e) {
            System.err.println("Error ending chat: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get current queue status
     */
    @MessageMapping("/chat/queue-status")
    public void getQueueStatus(@Payload Map<String, Object> payload) {
        try {
            String username = (String) payload.get("username");
            
            Map<String, Object> status = new HashMap<>();
            status.put("queueSize", chatService.getQueueSize());
            status.put("waitingStudents", chatService.getWaitingStudents());
            
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/status",
                status
            );
            
        } catch (Exception e) {
            System.err.println("Error getting queue status: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper methods
    
    private void sendQueuePosition(String username, Long sessionId, int position) {
        Map<String, Object> queueInfo = new HashMap<>();
        queueInfo.put("type", "QUEUE_POSITION");
        queueInfo.put("sessionId", sessionId);
        queueInfo.put("position", position);
        queueInfo.put("queueSize", chatService.getQueueSize());
        
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/notifications",
            queueInfo
        );
    }
    
    private void sendSessionInfo(String username, ChatSession session, String status) {
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("type", "SESSION_INFO");
        sessionInfo.put("sessionId", session.getId());
        sessionInfo.put("status", status);
        sessionInfo.put("customerName", session.getCustomer().getFullName());
        sessionInfo.put("customerId", session.getCustomer().getStudentId());
        if (session.getAgent() != null) {
            sessionInfo.put("agentName", session.getAgent().getFullName());
            sessionInfo.put("agentUsername", session.getAgent().getUsername());
        }
        
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/notifications",
            sessionInfo
        );
    }
    
    private void sendError(String username, String errorMessage) {
        Map<String, Object> error = new HashMap<>();
        error.put("type", "ERROR");
        error.put("message", errorMessage);
        
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/notifications",
            error
        );
    }
    
    // ==================== VOICE CALL HANDLERS ====================
    
    /**
     * Handle call request from student/teacher to agent
     */
    @MessageMapping("/call/request")
    public void handleCallRequest(@Payload Map<String, Object> payload) {
        try {
            System.out.println("📞 CALL REQUEST received: " + payload);
            
            String callerUsername = (String) payload.get("from");
            String receiverUsername = (String) payload.get("to");
            Long sessionId = payload.get("sessionId") != null ? 
                            Long.valueOf(payload.get("sessionId").toString()) : null;
            
            // Forward to receiver
            Map<String, Object> callRequest = new HashMap<>();
            callRequest.put("type", "CALL_REQUEST");
            callRequest.put("callId", payload.get("callId"));
            callRequest.put("from", callerUsername);
            callRequest.put("fromName", payload.get("fromName"));
            callRequest.put("sessionId", sessionId);
            
            messagingTemplate.convertAndSendToUser(
                receiverUsername,
                "/queue/call",
                callRequest
            );
            
            System.out.println("📞 Call request forwarded to: " + receiverUsername);
        } catch (Exception e) {
            System.err.println("Error handling call request: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle call acceptance
     */
    @MessageMapping("/call/accept")
    public void handleCallAccept(@Payload Map<String, Object> payload) {
        try {
            System.out.println("✅ CALL ACCEPT received: " + payload);
            
            String accepterUsername = (String) payload.get("from");
            String callerUsername = (String) payload.get("to");
            
            // Forward acceptance to caller
            Map<String, Object> callAccept = new HashMap<>();
            callAccept.put("type", "CALL_ACCEPT");
            callAccept.put("callId", payload.get("callId"));
            callAccept.put("from", accepterUsername);
            
            messagingTemplate.convertAndSendToUser(
                callerUsername,
                "/queue/call",
                callAccept
            );
            
            System.out.println("✅ Call acceptance forwarded to: " + callerUsername);
        } catch (Exception e) {
            System.err.println("Error handling call accept: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle call rejection
     */
    @MessageMapping("/call/reject")
    public void handleCallReject(@Payload Map<String, Object> payload) {
        try {
            System.out.println("❌ CALL REJECT received: " + payload);
            
            String rejecterUsername = (String) payload.get("from");
            String callerUsername = (String) payload.get("to");
            
            // Forward rejection to caller
            Map<String, Object> callReject = new HashMap<>();
            callReject.put("type", "CALL_REJECT");
            callReject.put("callId", payload.get("callId"));
            callReject.put("from", rejecterUsername);
            callReject.put("reason", payload.get("reason"));
            
            messagingTemplate.convertAndSendToUser(
                callerUsername,
                "/queue/call",
                callReject
            );
            
            System.out.println("❌ Call rejection forwarded to: " + callerUsername);
        } catch (Exception e) {
            System.err.println("Error handling call reject: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle call end
     */
    @MessageMapping("/call/end")
    public void handleCallEnd(@Payload Map<String, Object> payload) {
        try {
            System.out.println("📴 CALL END received: " + payload);
            
            String senderUsername = (String) payload.get("from");
            String otherUsername = (String) payload.get("to");
            
            // Forward call end to other party
            Map<String, Object> callEnd = new HashMap<>();
            callEnd.put("type", "CALL_END");
            callEnd.put("callId", payload.get("callId"));
            callEnd.put("from", senderUsername);
            
            messagingTemplate.convertAndSendToUser(
                otherUsername,
                "/queue/call",
                callEnd
            );
            
            System.out.println("📴 Call end forwarded to: " + otherUsername);
        } catch (Exception e) {
            System.err.println("Error handling call end: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle WebRTC offer (SDP)
     */
    @MessageMapping("/call/offer")
    public void handleWebRTCOffer(@Payload Map<String, Object> payload) {
        try {
            System.out.println("🎯 WebRTC OFFER received");
            
            String senderUsername = (String) payload.get("from");
            String receiverUsername = (String) payload.get("to");
            
            // Forward offer to receiver
            Map<String, Object> offer = new HashMap<>();
            offer.put("type", "WEBRTC_OFFER");
            offer.put("callId", payload.get("callId"));
            offer.put("from", senderUsername);
            offer.put("sdp", payload.get("sdp"));
            
            messagingTemplate.convertAndSendToUser(
                receiverUsername,
                "/queue/call",
                offer
            );
            
            System.out.println("🎯 WebRTC offer forwarded to: " + receiverUsername);
        } catch (Exception e) {
            System.err.println("Error handling WebRTC offer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle WebRTC answer (SDP)
     */
    @MessageMapping("/call/answer")
    public void handleWebRTCAnswer(@Payload Map<String, Object> payload) {
        try {
            System.out.println("🎯 WebRTC ANSWER received");
            
            String senderUsername = (String) payload.get("from");
            String receiverUsername = (String) payload.get("to");
            
            // Forward answer to receiver
            Map<String, Object> answer = new HashMap<>();
            answer.put("type", "WEBRTC_ANSWER");
            answer.put("callId", payload.get("callId"));
            answer.put("from", senderUsername);
            answer.put("sdp", payload.get("sdp"));
            
            messagingTemplate.convertAndSendToUser(
                receiverUsername,
                "/queue/call",
                answer
            );
            
            System.out.println("🎯 WebRTC answer forwarded to: " + receiverUsername);
        } catch (Exception e) {
            System.err.println("Error handling WebRTC answer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle ICE candidate exchange
     */
    @MessageMapping("/call/ice-candidate")
    public void handleICECandidate(@Payload Map<String, Object> payload) {
        try {
            System.out.println("🧊 ICE CANDIDATE received");
            
            String senderUsername = (String) payload.get("from");
            String receiverUsername = (String) payload.get("to");
            
            // Forward ICE candidate to receiver
            Map<String, Object> iceCandidate = new HashMap<>();
            iceCandidate.put("type", "ICE_CANDIDATE");
            iceCandidate.put("callId", payload.get("callId"));
            iceCandidate.put("from", senderUsername);
            iceCandidate.put("candidate", payload.get("candidate"));
            
            messagingTemplate.convertAndSendToUser(
                receiverUsername,
                "/queue/call",
                iceCandidate
            );
            
            System.out.println("🧊 ICE candidate forwarded to: " + receiverUsername);
        } catch (Exception e) {
            System.err.println("Error handling ICE candidate: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
