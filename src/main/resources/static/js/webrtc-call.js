/**
 * WebRTC Voice Call Module
 * Handles peer-to-peer voice calls using WebRTC and WebSocket signaling
 */

class VoiceCallManager {
    constructor(stompClient, currentUsername, remoteUsername) {
        this.stompClient = stompClient;
        this.currentUsername = currentUsername;
        this.remoteUsername = remoteUsername;
        
        // WebRTC configuration with STUN and TURN servers
        this.configuration = {
            iceServers: [
                // Google's free STUN servers
                { urls: 'stun:stun.l.google.com:19302' },
                { urls: 'stun:stun1.l.google.com:19302' },
                // OpenRelay free TURN server (for NAT traversal)
                {
                    urls: 'turn:openrelay.metered.ca:80',
                    username: 'openrelayproject',
                    credential: 'openrelayproject'
                },
                {
                    urls: 'turn:openrelay.metered.ca:443',
                    username: 'openrelayproject',
                    credential: 'openrelayproject'
                },
                {
                    urls: 'turn:openrelay.metered.ca:443?transport=tcp',
                    username: 'openrelayproject',
                    credential: 'openrelayproject'
                }
            ]
        };
        
        this.peerConnection = null;
        this.localStream = null;
        this.remoteStream = null;
        
        this.callId = null;
        this.isCallActive = false;
        this.isMuted = false;
        this.callStartTime = null;
        this.callDurationInterval = null;
        
        // Store pending offer if it arrives before peer connection is ready
        this.pendingOffer = null;
        
        // Queue for ICE candidates that arrive before peer connection is ready
        this.pendingIceCandidates = [];
        
        // Callbacks
        this.onCallStateChange = null;
        this.onCallDuration = null;
        this.onError = null;
    }
    
    /**
     * Initialize call (get microphone permission and setup)
     */
    async initializeCall() {
        try {
            console.log('🎤 Requesting microphone access...');
            
            // Request microphone access
            this.localStream = await navigator.mediaDevices.getUserMedia({
                audio: {
                    echoCancellation: true,
                    noiseSuppression: true,
                    autoGainControl: true
                },
                video: false
            });
            
            console.log('✅ Microphone access granted');
            return true;
        } catch (error) {
            console.error('❌ Error accessing microphone:', error);
            if (this.onError) {
                this.onError('Please allow microphone access to make voice calls');
            }
            return false;
        }
    }
    
    /**
     * Start a call (caller side)
     */
    async startCall() {
        try {
            // Generate call ID
            this.callId = 'call_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
            console.log('📞 Starting call with ID:', this.callId);
            
            // Initialize if not already done
            if (!this.localStream) {
                const initialized = await this.initializeCall();
                if (!initialized) return false;
            }
            
            // Create peer connection
            this.createPeerConnection();
            
            // Add local stream to peer connection
            this.localStream.getTracks().forEach(track => {
                this.peerConnection.addTrack(track, this.localStream);
                console.log('➕ Added local track:', track.kind);
            });
            
            // Create and send offer
            const offer = await this.peerConnection.createOffer();
            await this.peerConnection.setLocalDescription(offer);
            
            console.log('📤 Sending call request with SDP offer...');
            
            // Send call request via WebSocket
            this.stompClient.send("/app/call/request", {}, JSON.stringify({
                callId: this.callId,
                from: this.currentUsername,
                fromName: this.currentUsername,
                to: this.remoteUsername
            }));
            
            // Send WebRTC offer (SDP)
            this.stompClient.send("/app/call/offer", {}, JSON.stringify({
                callId: this.callId,
                from: this.currentUsername,
                to: this.remoteUsername,
                sdp: offer
            }));
            
            console.log('📤 SDP offer sent');
            
            this.isCallActive = true;
            if (this.onCallStateChange) {
                this.onCallStateChange('calling');
            }
            
            return true;
        } catch (error) {
            console.error('❌ Error starting call:', error);
            if (this.onError) {
                this.onError('Failed to start call: ' + error.message);
            }
            return false;
        }
    }
    
    /**
     * Accept incoming call (receiver side)
     */
    async acceptCall(callId) {
        try {
            this.callId = callId;
            console.log('✅ Accepting call:', callId);
            
            // Initialize if not already done
            if (!this.localStream) {
                const initialized = await this.initializeCall();
                if (!initialized) return false;
            }
            
            // Create peer connection
            this.createPeerConnection();
            
            // Add local stream to peer connection
            this.localStream.getTracks().forEach(track => {
                this.peerConnection.addTrack(track, this.localStream);
                console.log('➕ Added local track:', track.kind);
            });
            
            // Process pending offer if it arrived before peer connection was ready
            if (this.pendingOffer) {
                console.log('✨ Processing pending WebRTC offer...');
                await this.handleOffer(this.pendingOffer);
                this.pendingOffer = null; // Clear pending offer
            }
            
            // Send acceptance via WebSocket
            this.stompClient.send("/app/call/accept", {}, JSON.stringify({
                callId: this.callId,
                from: this.currentUsername,
                to: this.remoteUsername
            }));
            
            this.isCallActive = true;
            if (this.onCallStateChange) {
                this.onCallStateChange('connecting');
            }
            
            return true;
        } catch (error) {
            console.error('❌ Error accepting call:', error);
            if (this.onError) {
                this.onError('Failed to accept call: ' + error.message);
            }
            return false;
        }
    }
    
    /**
     * Reject incoming call
     */
    rejectCall(callId) {
        console.log('❌ Rejecting call:', callId);
        
        this.stompClient.send("/app/call/reject", {}, JSON.stringify({
            callId: callId,
            from: this.currentUsername,
            to: this.remoteUsername,
            reason: 'User declined'
        }));
        
        this.cleanup();
    }
    
    /**
     * End active call
     */
    endCall() {
        console.log('📴 Ending call:', this.callId);
        
        if (this.callId) {
            this.stompClient.send("/app/call/end", {}, JSON.stringify({
                callId: this.callId,
                from: this.currentUsername,
                to: this.remoteUsername
            }));
        }
        
        this.cleanup();
        
        if (this.onCallStateChange) {
            this.onCallStateChange('ended');
        }
    }
    
    /**
     * Toggle mute
     */
    toggleMute() {
        if (this.localStream) {
            this.isMuted = !this.isMuted;
            this.localStream.getAudioTracks().forEach(track => {
                track.enabled = !this.isMuted;
            });
            console.log(this.isMuted ? '🔇 Muted' : '🔊 Unmuted');
            return this.isMuted;
        }
        return false;
    }
    
    /**
     * Create WebRTC peer connection
     */
    createPeerConnection() {
        console.log('🔗 Creating peer connection...');
        
        this.peerConnection = new RTCPeerConnection(this.configuration);
        
        // Handle ICE candidates
        this.peerConnection.onicecandidate = (event) => {
            if (event.candidate) {
                console.log('🧊 Sending ICE candidate');
                this.stompClient.send("/app/call/ice-candidate", {}, JSON.stringify({
                    callId: this.callId,
                    from: this.currentUsername,
                    to: this.remoteUsername,
                    candidate: event.candidate
                }));
            }
        };
        
        // Handle incoming tracks (remote audio)
        this.peerConnection.ontrack = (event) => {
            console.log('🎵 Received remote track');
            if (event.streams && event.streams[0]) {
                this.remoteStream = event.streams[0];
                
                // Play remote audio
                const remoteAudio = document.getElementById('remoteAudio');
                if (remoteAudio) {
                    remoteAudio.srcObject = this.remoteStream;
                    remoteAudio.play().catch(e => console.error('Error playing audio:', e));
                }
            }
        };
        
        // Handle connection state changes
        this.peerConnection.onconnectionstatechange = () => {
            console.log('🔌 Connection state:', this.peerConnection.connectionState);
            
            if (this.peerConnection.connectionState === 'connected') {
                console.log('✅ Call connected!');
                this.startCallTimer();
                if (this.onCallStateChange) {
                    this.onCallStateChange('connected');
                }
            } else if (this.peerConnection.connectionState === 'failed') {
                console.log('❌ Call failed');
                this.endCall();
            } else if (this.peerConnection.connectionState === 'closed') {
                console.log('❌ Call closed');
                this.endCall();
            }
            // Note: 'disconnected' state is temporary during reconnection, don't end call
        };
        
        // Handle ICE connection state
        this.peerConnection.oniceconnectionstatechange = () => {
            console.log('🧊 ICE connection state:', this.peerConnection.iceConnectionState);
            
            // Only end call if ICE completely failed after retries
            if (this.peerConnection.iceConnectionState === 'failed') {
                console.log('❌ ICE connection failed');
                this.endCall();
            }
        };
    }
    
    /**
     * Handle incoming WebRTC offer
     */
    async handleOffer(sdp) {
        try {
            console.log('📥 Received WebRTC offer');
            
            if (!this.peerConnection) {
                console.warn('⏳ Peer connection not ready yet, storing offer for later...');
                // Store the offer to process after acceptCall() creates peer connection
                this.pendingOffer = sdp;
                return;
            }
            
            // SDP could be either the full object or just the sdp string
            const remoteDesc = typeof sdp === 'string' 
                ? new RTCSessionDescription({ type: 'offer', sdp: sdp })
                : new RTCSessionDescription(sdp);
            
            await this.peerConnection.setRemoteDescription(remoteDesc);
            console.log('✅ Remote description set (offer)');
            
            // Process any pending ICE candidates now that remote description is set
            await this.processPendingIceCandidates();
            
            const answer = await this.peerConnection.createAnswer();
            await this.peerConnection.setLocalDescription(answer);
            
            console.log('📤 Sending WebRTC answer');
            this.stompClient.send("/app/call/answer", {}, JSON.stringify({
                callId: this.callId,
                from: this.currentUsername,
                to: this.remoteUsername,
                sdp: answer
            }));
        } catch (error) {
            console.error('❌ Error handling offer:', error);
        }
    }
    
    /**
     * Handle incoming WebRTC answer
     */
    async handleAnswer(sdp) {
        try {
            console.log('📥 Received WebRTC answer');
            
            if (!this.peerConnection) {
                console.error('❌ Peer connection not created');
                return;
            }
            
            // SDP could be either the full object or just the sdp string
            const remoteDesc = typeof sdp === 'string'
                ? new RTCSessionDescription({ type: 'answer', sdp: sdp })
                : new RTCSessionDescription(sdp);
            
            await this.peerConnection.setRemoteDescription(remoteDesc);
            console.log('✅ Remote description set (answer)');
            
            // Process any pending ICE candidates now that remote description is set
            await this.processPendingIceCandidates();
        } catch (error) {
            console.error('❌ Error handling answer:', error);
        }
    }
    
    /**
     * Handle incoming ICE candidate
     */
    async handleICECandidate(candidate) {
        try {
            console.log('🧊 Received ICE candidate');
            
            if (!this.peerConnection) {
                console.warn('⏳ Peer connection not ready yet, queuing ICE candidate...');
                this.pendingIceCandidates.push(candidate);
                return;
            }
            
            // Check if remote description is set
            if (!this.peerConnection.remoteDescription) {
                console.warn('⏳ Remote description not set yet, queuing ICE candidate...');
                this.pendingIceCandidates.push(candidate);
                return;
            }
            
            await this.peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
            console.log('✅ ICE candidate added successfully');
        } catch (error) {
            console.error('❌ Error adding ICE candidate:', error);
        }
    }
    
    /**
     * Process any pending ICE candidates
     */
    async processPendingIceCandidates() {
        if (this.pendingIceCandidates.length > 0 && this.peerConnection && this.peerConnection.remoteDescription) {
            console.log(`📦 Processing ${this.pendingIceCandidates.length} pending ICE candidates...`);
            
            for (const candidate of this.pendingIceCandidates) {
                try {
                    await this.peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
                    console.log('✅ Pending ICE candidate added');
                } catch (error) {
                    console.error('❌ Error adding pending ICE candidate:', error);
                }
            }
            
            this.pendingIceCandidates = [];
        }
    }
    
    /**
     * Start call duration timer
     */
    startCallTimer() {
        this.callStartTime = Date.now();
        this.callDurationInterval = setInterval(() => {
            const duration = Math.floor((Date.now() - this.callStartTime) / 1000);
            if (this.onCallDuration) {
                this.onCallDuration(duration);
            }
        }, 1000);
    }
    
    /**
     * Clean up resources
     */
    cleanup() {
        console.log('🧹 Cleaning up call resources...');
        
        // Stop call timer
        if (this.callDurationInterval) {
            clearInterval(this.callDurationInterval);
            this.callDurationInterval = null;
        }
        
        // Stop local stream
        if (this.localStream) {
            this.localStream.getTracks().forEach(track => track.stop());
            this.localStream = null;
        }
        
        // Close peer connection
        if (this.peerConnection) {
            this.peerConnection.close();
            this.peerConnection = null;
        }
        
        this.isCallActive = false;
        this.isMuted = false;
        this.callId = null;
        this.callStartTime = null;
    }
    
    /**
     * Format call duration
     */
    static formatDuration(seconds) {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }
}
