# 🔍 Debugging Guide - Call Button Not Showing

## 🐛 Issue: Call button is not visible

### **Where to check:**

1. **Open Browser Console (F12)**
   - Chrome: Right-click → Inspect → Console tab
   - Firefox: Right-click → Inspect Element → Console tab

2. **Start a chat session as student**
   - Login as student
   - Start chat (should auto-join queue)
   - Wait for agent to accept

3. **Check Console Logs:**

Look for these messages in order:

```javascript
// 1. WebSocket connected
Connected: CONNECTED ...

// 2. Notification when agent joins
Notification received: {type: 'AGENT_JOINED', agentName: '...', ...}

// 3. Agent info updated
// Should see agentUsername set
// Should see initializeVoiceCall() called

// 4. VoiceCallManager created
// Should see "VoiceCallManager initialized"

// 5. Call button shown
// Check if document.getElementById('callButton').style.display changes
```

### **Manual Check - Run in Console:**

When you're in an active chat with an agent, open the console (F12) and run:

```javascript
// 1. Check if call button element exists
console.log('Call button:', document.getElementById('callButton'));

// 2. Check if agent username is set
console.log('Agent username:', agentUsername);

// 3. Check if voice call manager is initialized
console.log('Voice call manager:', voiceCallManager);

// 4. Manually show the call button
document.getElementById('callButton').style.display = 'inline-block';

// 5. Check if webrtc-call.js is loaded
console.log('VoiceCallManager class:', typeof VoiceCallManager);
```

### **Common Issues:**

#### **Issue 1: webrtc-call.js not loaded**
**Symptom:** Console error: `VoiceCallManager is not defined`

**Fix:** Check if file exists at:
```
src/main/resources/static/js/webrtc-call.js
```

**Verify in browser:**
- Open: `http://localhost:8080/js/webrtc-call.js`
- Should download/show the file

#### **Issue 2: Agent notification not received**
**Symptom:** No `AGENT_JOINED` notification in console

**Check backend:** Is agent actually joining the chat?
- Check if ChatController is sending the notification
- Check WebSocket connection

#### **Issue 3: agentUsername is null**
**Symptom:** Console shows: `No agent connected yet`

**Check:** 
```javascript
// In console:
console.log('Agent name from updateAgentInfo:', document.getElementById('agentName').textContent);
console.log('Agent username:', agentUsername);
```

**Fix:** The agent name should be extracted from the notification

#### **Issue 4: Call button hidden by CSS**
**Symptom:** Element exists but not visible

**Check:**
```javascript
const btn = document.getElementById('callButton');
console.log('Display:', btn.style.display);
console.log('Visibility:', window.getComputedStyle(btn).visibility);
console.log('Opacity:', window.getComputedStyle(btn).opacity);
```

### **Quick Fix - Force Show Call Button:**

If you just want to test the call functionality, you can manually show the button:

1. Open console (F12)
2. Run these commands:

```javascript
// Set a dummy agent username
agentUsername = 'test_agent';

// Initialize voice call manager
initializeVoiceCall();

// Manually show call button
document.getElementById('callButton').style.display = 'inline-block';

// Now try clicking the call button
```

### **Check Agent Side:**

For the agent side, check if they have the modal HTML:

```javascript
// In agent console:
console.log('Incoming call modal:', document.getElementById('incomingCallModal'));
```

If it returns `null`, the agent-chat.html might need the modal added.

### **Expected Flow:**

1. Student joins queue → **Waiting Room shows**
2. Agent accepts chat → **WebSocket sends AGENT_JOINED**
3. Student receives notification → **handleNotification() called**
4. updateAgentInfo() called → **agentUsername set**
5. initializeVoiceCall() called → **VoiceCallManager created**
6. Call button shown → **style.display = 'inline-block'**

### **Verify Each Step:**

Add console.log to your code to track the flow:

```javascript
// In updateAgentInfo function (around line 473):
function updateAgentInfo(name, online) {
    console.log('🔍 updateAgentInfo called:', {name, online});
    
    // ... existing code ...
    
    if (online && name) {
        agentUsername = name.toLowerCase().replace(/\s+/g, '');
        console.log('🔍 Agent username set to:', agentUsername);
        initializeVoiceCall();
        console.log('🔍 initializeVoiceCall called');
    }
}

// In initializeVoiceCall function (around line 772):
function initializeVoiceCall() {
    console.log('🔍 initializeVoiceCall starting...');
    console.log('🔍 agentUsername:', agentUsername);
    console.log('🔍 VoiceCallManager:', typeof VoiceCallManager);
    
    if (!agentUsername) {
        console.warn('❌ No agent connected yet');
        return;
    }
    
    // ... existing code ...
    
    console.log('✅ Call button should now be visible');
}
```

### **Test Right Now:**

1. Make sure Spring Boot is running
2. Open browser console (F12) BEFORE logging in
3. Login as student
4. Watch console for all log messages
5. Screenshot any errors and share them

### **If Nothing Works:**

The simplest test - paste this in console when chat is active:

```javascript
document.getElementById('callButton').style.display = 'inline-block';
document.getElementById('callButton').click();
```

This will force show the button and click it. If the call starts, then the issue is just in the show/hide logic, not the call functionality itself.
