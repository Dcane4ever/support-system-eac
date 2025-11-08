# 🧪 Voice Call - Quick Testing Guide

## 🚀 How to Test (5 Minutes)

### **Setup:**
1. Start Spring Boot application
2. Open 2 browser tabs

### **Test Flow:**

**Tab 1 - Agent:**
1. Login as agent
2. Set availability to "Available"
3. Accept incoming chat

**Tab 2 - Student:**
1. Login as student
2. Start chat with agent
3. Click 📞 phone button
4. Allow microphone

**What Should Happen:**
- Student sees: "Calling..." banner
- Agent sees: Incoming call modal pop up
- Agent clicks: ✅ Accept (green button)
- Both see: "Connecting..." then call controls
- Timer starts: 00:01, 00:02, 00:03...
- **Speak and listen** - both should hear each other!

**Test Mute:**
- Click 🎤 button
- Icon changes to 🎤❌
- Other person can't hear you
- Click again to unmute

**End Call:**
- Click red 📞 button
- Call ends on both sides
- Phone button reappears

**Test Reject:**
- Student calls again
- Agent clicks: ❌ Reject (red button)
- Student sees: "Call was declined"

---

## ✅ Success Checklist

- [ ] Phone button appears for student
- [ ] Agent receives incoming call modal
- [ ] Both can hear each other
- [ ] Mute works
- [ ] Hang up works
- [ ] Reject works
- [ ] No errors in console (F12)

---

## 🐛 Quick Fixes

**No phone button?**
- Check: Agent must be in active chat with student

**No sound?**
- Check: Allow microphone permission
- Check: Try Chrome browser

**Modal doesn't show?**
- Check: WebSocket connected (see "Connected" status)

---

## 🎉 When It Works

You'll have:
- ✅ Free voice calls (no API keys!)
- ✅ Peer-to-peer audio
- ✅ Mute/unmute
- ✅ Call timer
- ✅ Accept/Reject
- ✅ All calls logged to database

**Ready to test!** 🚀📞
