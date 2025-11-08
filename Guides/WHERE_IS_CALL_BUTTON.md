# 📞 WHERE TO FIND THE CALL BUTTON - Quick Reference

## 🎓 STUDENT SIDE

### **Location:**
Top-right corner of the chat interface, next to "End Chat" button

### **When it appears:**
- ✅ After agent accepts your chat
- ✅ When agent status shows "● Online"

### **What it looks like:**
```
┌──────────────────────────────────────┐
│ 👤 Agent Name ● Online               │
│                      📞 ❌ End Chat  │  ← Phone icon here!
└──────────────────────────────────────┘
```

### **HTML Element:**
```html
<button id="callButton" onclick="initiateVoiceCall()" class="btn-call">
    <span class="material-icons">phone</span>
</button>
```

### **Quick Check (Console F12):**
```javascript
// Is button there?
document.getElementById('callButton')

// Is it visible?
document.getElementById('callButton').style.display
// Should be: "inline-block" (when agent online)
// If "none", agent not connected yet

// Force show for testing
document.getElementById('callButton').style.display = 'inline-block';
```

---

## 👨‍💼 AGENT SIDE

### **Location:**
Incoming call modal (pops up in center of screen)

### **When it appears:**
- ✅ When a student/teacher clicks the call button
- ✅ Automatically pops up as modal overlay

### **What it looks like:**
```
       ╔═══════════════════════╗
       ║   📞 (animated ring)  ║
       ║                       ║
       ║ Incoming Call from    ║
       ║    student123         ║
       ║                       ║
       ║  ┌───────┐ ┌───────┐ ║
       ║  │   ✅  │ │   ❌  │ ║
       ║  │Accept │ │Reject │ ║
       ║  └───────┘ └───────┘ ║
       ╚═══════════════════════╝
```

### **HTML Element:**
```html
<div id="incomingCallModal" class="incoming-call-modal">
    <div class="incoming-call-content">
        <button onclick="acceptIncomingCall()">Accept</button>
        <button onclick="rejectIncomingCall()">Reject</button>
    </div>
</div>
```

### **Quick Check (Console F12):**
```javascript
// Is modal there?
document.getElementById('incomingCallModal')

// Show it manually for testing
document.getElementById('incomingCallModal').style.display = 'flex';

// Hide it
document.getElementById('incomingCallModal').style.display = 'none';
```

---

## 🎬 DURING ACTIVE CALL (Both Sides)

### **Location:**
Replaces the call button in header, shows call controls

### **What you see:**
```
┌──────────────────────────────────────────┐
│ 👤 Name                                   │
│           [00:45] 🎤 📞 End Chat         │
│            ↑      ↑   ↑                   │
│         Timer  Mute Hang Up              │
└──────────────────────────────────────────┘
```

### **HTML Elements:**
```html
<div id="callControls" class="call-controls">
    <span id="callDuration">00:00</span>
    <button id="muteButton" onclick="toggleMute()">
        <span class="material-icons">mic</span>
    </button>
    <button onclick="endVoiceCall()">
        <span class="material-icons">call_end</span>
    </button>
</div>
```

---

## 🔍 TROUBLESHOOTING

### **Can't find call button on student?**

1. **Check if agent is connected:**
   ```javascript
   // In console:
   document.getElementById('agentStatus').textContent
   // Should show: "● Online"
   ```

2. **Check if button exists:**
   ```javascript
   document.getElementById('callButton')
   // Should return: <button> element (not null)
   ```

3. **Force show it:**
   ```javascript
   document.getElementById('callButton').style.display = 'inline-block';
   // Button should appear now
   ```

### **Agent not seeing incoming call?**

1. **Check if modal exists:**
   ```javascript
   document.getElementById('incomingCallModal')
   // Should return: <div> element (not null)
   ```

2. **Check WebSocket connection:**
   ```javascript
   // Look in console for:
   "Connected: CONNECTED ..."
   ```

3. **Force show modal:**
   ```javascript
   document.getElementById('incomingCallModal').style.display = 'flex';
   // Modal should appear
   ```

---

## 📋 STEP-BY-STEP TEST

### **1. Setup:**
- ✅ Spring Boot running
- ✅ Open 2 browser windows

### **2. Login:**
- Window 1: Login as **agent**
- Window 2: Login as **student**

### **3. Start Chat:**
- Student: Click "Request Support"
- Student: Wait in queue
- Agent: Accept the chat from queue

### **4. Look for Button:**
- Student window: Look top-right
- Should see: 📞 phone icon button
- If not visible, check console (F12)

### **5. Make Call:**
- Student: Click 📞
- Allow microphone
- Wait for agent...

### **6. Agent Receives:**
- Modal pops up automatically
- Shows: "Incoming Call from [student]"
- Click: ✅ Accept

### **7. Call Connected:**
- Both see: Timer counting (00:01, 00:02...)
- Both see: Mute and Hang Up buttons
- Talk and listen!

---

## 💡 PRO TIPS

### **Fastest Way to Test:**

1. **Open TWO browser tabs** (same browser window)
   - Tab 1: Login as agent
   - Tab 2: Login as student

2. **Use Console Shortcuts:**
   ```javascript
   // Student tab - Force show call button
   document.getElementById('callButton').style.display = 'inline-block';
   document.getElementById('callButton').click();
   
   // Agent tab - Force show incoming call modal
   document.getElementById('incomingCallModal').style.display = 'flex';
   ```

3. **Check Console Logs:**
   - Open F12 on BOTH tabs BEFORE starting
   - Watch for: "📞 Call signal received:"
   - Watch for: "Connected: CONNECTED ..."

### **If Audio Not Working:**

1. **Check Microphone Permission:**
   - Chrome: Lock icon in address bar → Site Settings
   - Firefox: Lock icon → Permissions → Microphone

2. **Test Microphone:**
   - Visit: chrome://settings/content/microphone
   - Test your microphone first

3. **Check Volume:**
   - Make sure speakers/headphones are on
   - Make sure volume is not muted

---

## ✅ SUCCESS CHECKLIST

- [ ] Spring Boot application running
- [ ] webrtc-call.js file exists
- [ ] Agent logged in
- [ ] Student logged in
- [ ] Student in active chat with agent
- [ ] 📞 Phone button visible on student
- [ ] Clicking phone asks for microphone
- [ ] Agent sees incoming call modal
- [ ] Accepting shows call controls
- [ ] Can hear each other
- [ ] Mute works
- [ ] Hang up works

If ALL checked = **Voice calls working!** 🎉

---

## 🆘 STILL STUCK?

See these files:
- `CALL_BUTTON_FIXED.md` - Full fix documentation
- `DEBUG_CALL_BUTTON.md` - Detailed debugging
- `VOICE_CALL_QUICK_TEST.md` - Quick testing guide

Or check console logs and share any errors you see!
