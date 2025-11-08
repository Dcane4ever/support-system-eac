# Queue System - WebSocket Flow Reference

## WebSocket Endpoints

### Connection
```
Endpoint: /ws
Protocol: SockJS + STOMP
```

### Message Destinations

#### Send (Client → Server)
- `/app/chat/start` - Student joins queue
- `/app/chat/accept` - Agent accepts student
- `/app/chat/message` - Send chat message
- `/app/chat/end` - Close chat session
- `/app/chat/queue-status` - Get queue info

#### Receive (Server → Client)
- `/topic/queue-updates` - Broadcast to all agents (new students, assignments)
- `/user/queue/notifications` - Personal notifications (queue position, agent joined, session ended)
- `/user/queue/messages` - Personal chat messages

---

## Message Formats

### 1. Student Joins Queue
**Send**: `/app/chat/start`
```json
{
  "username": "student123"
}
```

**Receive**: `/user/queue/notifications`
```json
{
  "type": "QUEUE_POSITION",
  "sessionId": 15,
  "position": 3,
  "queueSize": 5
}
```

**Broadcast**: `/topic/queue-updates` (to all agents)
```json
{
  "type": "NEW_STUDENT",
  "sessionId": 15,
  "studentName": "John Doe",
  "studentId": "2021-12345",
  "queueSize": 5
}
```

---

### 2. Agent Accepts Student
**Send**: `/app/chat/accept`
```json
{
  "agentUsername": "agent01",
  "sessionId": 15
}
```

**Receive**: `/user/queue/notifications` (both student and agent)
```json
{
  "type": "SESSION_INFO",
  "sessionId": 15,
  "status": "active",
  "customerName": "John Doe",
  "customerId": "2021-12345",
  "agentName": "Agent Smith"
}
```

**Broadcast**: `/topic/queue-updates` (to all agents)
```json
{
  "type": "STUDENT_ASSIGNED",
  "sessionId": 15,
  "queueSize": 4
}
```

---

### 3. Send Message
**Send**: `/app/chat/message`
```json
{
  "sessionId": 15,
  "senderUsername": "student123",
  "content": "Hello, I need help with my assignment"
}
```

**Receive**: `/user/queue/messages` (both participants)
```json
{
  "sessionId": 15,
  "messageId": 42,
  "content": "Hello, I need help with my assignment",
  "senderUsername": "student123",
  "senderName": "John Doe",
  "timestamp": "2024-01-15T10:30:00",
  "type": "TEXT"
}
```

---

### 4. End Chat
**Send**: `/app/chat/end`
```json
{
  "sessionId": 15,
  "username": "agent01"
}
```

**Receive**: `/user/queue/notifications` (both participants)
```json
{
  "type": "SESSION_ENDED",
  "sessionId": 15
}
```

**Broadcast**: `/topic/queue-updates` (to all agents)
```json
{
  "type": "QUEUE_UPDATE",
  "queueSize": 4
}
```

---

## Notification Types

### Student Receives:
- `QUEUE_POSITION` - Your position in queue
- `AGENT_JOINED` - Agent has joined your chat
- `SESSION_INFO` - Session details (reconnect)
- `SESSION_ENDED` - Chat has been closed
- `ERROR` - Error message

### Agent Receives:
- `NEW_STUDENT` - New student in queue
- `STUDENT_ASSIGNED` - Student was assigned (remove from queue)
- `QUEUE_UPDATE` - Queue size changed
- `SESSION_INFO` - Session details
- `SESSION_ENDED` - Chat has been closed

---

## Queue System State Flow

```
STUDENT                           AGENT
   |                                |
   |--[1] /app/chat/start---------->|
   |                                |
   |<--[2] QUEUE_POSITION-----------|
   |    (Position: 3)               |
   |                                |
   |                          [3] See in queue
   |                                |
   |<--[4] /app/chat/accept---------|
   |                                |
   |<--[5] AGENT_JOINED------------>|
   |    SESSION_INFO         SESSION_INFO
   |                                |
   |<--[6] /app/chat/message------->|
   |<--    CHAT MESSAGES         -->|
   |                                |
   |<--[7] /app/chat/end------------|
   |                                |
   |<--[8] SESSION_ENDED----------->|
   |    Agent available again       |
```

---

## REST API Endpoints

Use these for initial page load and non-real-time data:

### GET /api/chat/status
Get current session status for logged-in user
```json
// Student response
{
  "hasActiveSession": true,
  "sessionId": 15,
  "status": "waiting",
  "position": 3,
  "queueSize": 5
}

// Agent response
{
  "activeSessions": 2,
  "queueSize": 5,
  "waitingStudents": [...]
}
```

### GET /api/chat/session/{sessionId}/messages
Get all messages for a session
```json
[
  {
    "id": 1,
    "content": "Hello",
    "sender": {...},
    "sentAt": "2024-01-15T10:30:00",
    "type": "TEXT"
  },
  ...
]
```

### GET /api/chat/history
Get past chat sessions for current user
```json
[
  {
    "id": 15,
    "customer": {...},
    "agent": {...},
    "status": "CLOSED",
    "startedAt": "2024-01-15T10:00:00",
    "endedAt": "2024-01-15T10:45:00"
  },
  ...
]
```

### GET /api/chat/queue (Agents only)
Get current waiting students
```json
{
  "queueSize": 5,
  "waitingStudents": [
    {
      "id": 15,
      "customer": {...},
      "status": "WAITING",
      "startedAt": "2024-01-15T10:30:00"
    },
    ...
  ]
}
```

---

## Implementation Checklist

### Student Chat Page
- [ ] Include SockJS and STOMP libraries
- [ ] Connect to WebSocket on page load
- [ ] Subscribe to `/user/queue/notifications`
- [ ] Subscribe to `/user/queue/messages`
- [ ] Send `/app/chat/start` on "Start Chat" button
- [ ] Display queue position when received
- [ ] Display agent name when joined
- [ ] Send `/app/chat/message` on message submit
- [ ] Display incoming messages
- [ ] Send `/app/chat/end` on close button

### Support Chat Page
- [ ] Include SockJS and STOMP libraries
- [ ] Connect to WebSocket on page load
- [ ] Subscribe to `/topic/queue-updates`
- [ ] Subscribe to `/user/queue/messages`
- [ ] Load initial queue with `/api/chat/queue`
- [ ] Add students to UI on `NEW_STUDENT` notification
- [ ] Remove students from UI on `STUDENT_ASSIGNED`
- [ ] Send `/app/chat/accept` on accept button
- [ ] Display chat interface when assigned
- [ ] Send `/app/chat/message` on message submit
- [ ] Send `/app/chat/end` on resolve button

---

## Testing Commands

### Check if WebSocket is configured:
Look for `WebSocketConfig.java` with `/ws` endpoint

### Test queue in browser console:
```javascript
// Connect
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to notifications
    stompClient.subscribe('/user/queue/notifications', function(msg) {
        console.log('Notification:', JSON.parse(msg.body));
    });
    
    // Join queue
    stompClient.send('/app/chat/start', {}, JSON.stringify({
        username: 'your-username'
    }));
});
```

---

## Troubleshooting

### Student not entering queue
1. Check browser console for WebSocket connection errors
2. Verify `/app/chat/start` message is being sent
3. Check server logs for "Creating chat session for student"
4. Verify user is authenticated and has STUDENT role

### Agent not seeing students
1. Check if subscribed to `/topic/queue-updates`
2. Verify agent has SUPPORT_AGENT role
3. Check if `/api/chat/queue` returns waiting students
4. Look for "NEW_STUDENT" broadcast in server logs

### Messages not sending
1. Verify `sessionId` is correct in `/app/chat/message`
2. Check if both users are subscribed to `/user/queue/messages`
3. Verify session status is ACTIVE
4. Check server logs for "Error sending message"

### Session not ending
1. Verify `/app/chat/end` includes correct `sessionId`
2. Check if agent availability is updated
3. Look for "Chat session X ended" in server logs
4. Verify both users receive SESSION_ENDED notification
