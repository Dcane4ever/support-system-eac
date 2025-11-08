# WebRTC Production Setup Guide

## Current Status

✅ **HTTPS:** Enabled via Render (required for WebRTC)  
✅ **STUN Servers:** Google's free STUN servers configured  
✅ **WebSocket Signaling:** Configured via Spring WebSocket  
⚠️ **TURN Server:** Not configured (optional, improves reliability)

---

## Testing Voice Calls on Render

### 1. Basic Test
1. Open your deployed app: `https://support-system-eac.onrender.com`
2. Register two different users (use different browsers or incognito mode)
3. Start a support chat
4. Click the voice call button
5. Check if audio connects

### 2. Check Browser Console
Press `F12` and look for:
- ✅ `WebSocket connected`
- ✅ `ICE candidate received`
- ✅ `Peer connection state: connected`
- ❌ `ICE connection failed` (means you need TURN server)

---

## Adding TURN Server (Optional - For Better Reliability)

If calls don't connect for all users, you can add a TURN server.

### Free TURN Server Options:

#### Option 1: Metered TURN (Recommended - Free Tier)
1. Sign up at https://www.metered.ca/turn-server
2. Get your credentials
3. Update `webrtc-call.js`:

```javascript
this.configuration = {
    iceServers: [
        { urls: 'stun:stun.l.google.com:19302' },
        { urls: 'stun:stun1.l.google.com:19302' },
        {
            urls: 'turn:a.relay.metered.ca:80',
            username: 'YOUR_USERNAME',
            credential: 'YOUR_CREDENTIAL'
        },
        {
            urls: 'turn:a.relay.metered.ca:443',
            username: 'YOUR_USERNAME',
            credential: 'YOUR_CREDENTIAL'
        }
    ]
};
```

#### Option 2: Twilio TURN (Free Trial)
1. Sign up at https://www.twilio.com/stun-turn
2. Get credentials
3. Add to configuration:

```javascript
{
    urls: 'turn:global.turn.twilio.com:3478?transport=tcp',
    username: 'YOUR_USERNAME',
    credential: 'YOUR_CREDENTIAL'
}
```

#### Option 3: Self-Hosted TURN (Advanced)
- Install coturn on a VPS
- More control but requires server management

---

## Connection Success Rate

### Current Setup (STUN Only):
- ✅ **70-80%** of connections will work
- ❌ **20-30%** may fail (symmetric NAT, restrictive firewalls)

### With TURN Server:
- ✅ **95-99%** of connections will work
- Only fails if user blocks WebRTC entirely

---

## Troubleshooting

### Call Button Not Appearing
- Check browser console for JavaScript errors
- Verify WebSocket connection is established

### ICE Connection Failed
- Add TURN server (see above)
- Check firewall settings

### No Audio
- Check microphone permissions
- Verify `getUserMedia` is working (HTTPS required)

### WebSocket Disconnecting
- Check Render logs for errors
- Verify WebSocket timeout settings

---

## Production Checklist

- [x] HTTPS enabled (via Render)
- [x] STUN servers configured
- [x] WebSocket signaling working
- [ ] TURN server added (optional)
- [ ] Test on different networks
- [ ] Test on mobile devices
- [ ] Monitor connection success rate

---

## Recommendation

**For your professor's project:**
1. ✅ Test the current setup first
2. ✅ If 70-80% success rate is acceptable, you're done
3. ✅ If you need higher reliability, add Metered TURN (free tier)

**Current setup should work fine for:**
- Classroom demonstrations
- Same network testing
- Most home WiFi networks

**Add TURN server if:**
- You're presenting to multiple locations
- Users are on corporate networks
- You want production-grade reliability
