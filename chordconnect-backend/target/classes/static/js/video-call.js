class VideoCallManager {
    constructor(roomId, userId, username) {
        this.roomId = roomId;
        this.userId = userId;
        this.username = username;

        this.localStream = null;
        this.remoteStream = null;
        this.peerConnection = null;
        this.webSocket = null;

        this.isCallActive = false;
        this.remoteUserConnected = false;

        this.initializeElements();
        this.initializeEventListeners();
        this.connectSignaling();
    }

    initializeElements() {
        this.localVideo = document.getElementById('localVideo');
        this.remoteVideo = document.getElementById('remoteVideo');
        this.startCallBtn = document.getElementById('startCallBtn');
        this.endCallBtn = document.getElementById('endCallBtn');
        this.videoContainer = document.getElementById('videoContainer');
        this.callStatus = document.getElementById('callStatus');

        // Create status element if it doesn't exist
        if (!this.callStatus) {
            this.callStatus = document.createElement('div');
            this.callStatus.id = 'callStatus';
            this.callStatus.className = 'call-status';
            if (this.videoContainer) {
                this.videoContainer.parentNode.insertBefore(this.callStatus, this.videoContainer);
            }
        }
    }

    initializeEventListeners() {
        if (this.startCallBtn) {
            this.startCallBtn.addEventListener('click', () => this.startCall());
        }
        if (this.endCallBtn) {
            this.endCallBtn.addEventListener('click', () => this.endCall());
        }

        // Handle page unload
        window.addEventListener('beforeunload', () => {
            this.cleanup();
        });
    }

    connectSignaling() {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}/ws/video/${this.roomId}/${this.userId}`;

        this.webSocket = new WebSocket(wsUrl);

        this.webSocket.onopen = () => {
            console.log('✅ WebSocket connected for video signaling');
            this.updateStatus('Connected to signaling server');
        };

        this.webSocket.onmessage = async (event) => {
            const signal = JSON.parse(event.data);
            await this.handleSignal(signal);
        };

        this.webSocket.onclose = () => {
            console.log('❌ WebSocket disconnected');
            this.updateStatus('Disconnected from signaling server');
        };

        this.webSocket.onerror = (error) => {
            console.error('❌ WebSocket error:', error);
            this.updateStatus('Connection error');
        };
    }

    async startCall() {
        try {
            this.updateStatus('Starting video call...');

            // Get user media
            this.localStream = await navigator.mediaDevices.getUserMedia({
                video: {
                    width: 1280,
                    height: 720
                },
                audio: true
            });

            // Display local video
            this.localVideo.srcObject = this.localStream;
            this.showVideoUI();

            // Create peer connection
            await this.createPeerConnection();

            // Add local tracks to peer connection
            this.localStream.getTracks().forEach(track => {
                this.peerConnection.addTrack(track, this.localStream);
            });

            // Create and send offer
            const offer = await this.peerConnection.createOffer();
            await this.peerConnection.setLocalDescription(offer);

            this.sendSignal('OFFER', {
                sdp: offer.sdp,
                userId: this.userId,
                username: this.username
            });

            this.updateStatus('Call started - waiting for answer...');

        } catch (error) {
            console.error('❌ Error starting call:', error);
            this.updateStatus('Error: ' + error.message);
            alert('Cannot access camera/microphone: ' + error.message);
        }
    }

    async createPeerConnection() {
        const configuration = {
            iceServers: [
                { urls: 'stun:stun.l.google.com:19302' },
                { urls: 'stun:stun1.l.google.com:19302' }
            ]
        };

        this.peerConnection = new RTCPeerConnection(configuration);

        // Handle incoming remote stream
        this.peerConnection.ontrack = (event) => {
            console.log('✅ Received remote stream');
            this.remoteVideo.srcObject = event.streams[0];
            this.remoteUserConnected = true;
            this.updateStatus('Connected! You can now see and hear each other.');
        };

        // Handle ICE candidates
        this.peerConnection.onicecandidate = (event) => {
            if (event.candidate) {
                this.sendSignal('ICE_CANDIDATE', {
                    candidate: event.candidate.candidate,
                    sdpMid: event.candidate.sdpMid,
                    sdpMLineIndex: event.candidate.sdpMLineIndex,
                    userId: this.userId
                });
            }
        };

        // Handle connection state changes
        this.peerConnection.onconnectionstatechange = () => {
            console.log('Connection state:', this.peerConnection.connectionState);
            switch (this.peerConnection.connectionState) {
                case 'connected':
                    this.updateStatus('Connected!');
                    break;
                case 'disconnected':
                    this.updateStatus('Disconnected');
                    break;
                case 'failed':
                    this.updateStatus('Connection failed');
                    break;
            }
        };

        this.peerConnection.oniceconnectionstatechange = () => {
            console.log('ICE connection state:', this.peerConnection.iceConnectionState);
        };
    }

    async handleSignal(signal) {
        console.log('📨 Received signal:', signal.type);

        switch (signal.type) {
            case 'OFFER':
                await this.handleOffer(signal);
                break;
            case 'ANSWER':
                await this.handleAnswer(signal);
                break;
            case 'ICE_CANDIDATE':
                await this.handleIceCandidate(signal);
                break;
            case 'USER_JOINED':
                this.handleUserJoined(signal);
                break;
            case 'USER_LEFT':
                this.handleUserLeft(signal);
                break;
            case 'CALL_ENDED':
                this.handleCallEnded(signal);
                break;
            case 'ROOM_FULL':
                this.handleRoomFull(signal);
                break;
            case 'ROOM_STATUS':
                this.handleRoomStatus(signal);
                break;
            default:
                console.warn('⚠️ Unknown signal type:', signal.type);
        }
    }

    async handleOffer(signal) {
        this.updateStatus('Received call offer - connecting...');

        if (!this.localStream) {
            // Get user media if not already done
            this.localStream = await navigator.mediaDevices.getUserMedia({
                video: true,
                audio: true
            });
            this.localVideo.srcObject = this.localStream;
            this.showVideoUI();
        }

        if (!this.peerConnection) {
            await this.createPeerConnection();
            this.localStream.getTracks().forEach(track => {
                this.peerConnection.addTrack(track, this.localStream);
            });
        }

        await this.peerConnection.setRemoteDescription({
            type: 'offer',
            sdp: signal.sdp
        });

        const answer = await this.peerConnection.createAnswer();
        await this.peerConnection.setLocalDescription(answer);

        this.sendSignal('ANSWER', {
            sdp: answer.sdp,
            userId: this.userId
        });

        this.updateStatus('Call answered - connecting...');
    }

    async handleAnswer(signal) {
        await this.peerConnection.setRemoteDescription({
            type: 'answer',
            sdp: signal.sdp
        });
        this.updateStatus('Call answered - establishing connection...');
    }

    async handleIceCandidate(signal) {
        if (this.peerConnection && signal.candidate) {
            await this.peerConnection.addIceCandidate(new RTCIceCandidate({
                candidate: signal.candidate,
                sdpMid: signal.sdpMid,
                sdpMLineIndex: signal.sdpMLineIndex
            }));
        }
    }

    handleUserJoined(signal) {
        console.log(`👋 User ${signal.userId} joined the room`);
        this.updateStatus(`User ${signal.username} joined the room`);
    }

    handleUserLeft(signal) {
        console.log(`👋 User ${signal.userId} left the room`);
        this.updateStatus(`User ${signal.username} left the room`);
        this.remoteUserConnected = false;
    }

    handleCallEnded(signal) {
        console.log('📞 Call ended by remote user');
        this.updateStatus('Call ended by other user');
        this.cleanup();
        this.hideVideoUI();
    }

    handleRoomFull(signal) {
        alert('❌ Room is full! Maximum 2 participants allowed.');
        this.updateStatus('Room full - cannot join');
        this.cleanup();
    }

    handleRoomStatus(signal) {
        console.log(`📊 Room status: ${signal.currentParticipants}/2 participants`);
        this.updateStatus(`Room: ${signal.currentParticipants}/2 participants`);
    }

    sendSignal(type, data) {
        if (this.webSocket && this.webSocket.readyState === WebSocket.OPEN) {
            const signal = {
                type: type,
                ...data,
                roomId: this.roomId
            };
            this.webSocket.send(JSON.stringify(signal));
            console.log('📤 Sent signal:', type);
        } else {
            console.error('❌ WebSocket not connected');
        }
    }

    showVideoUI() {
        if (this.videoContainer) this.videoContainer.style.display = 'block';
        if (this.startCallBtn) this.startCallBtn.style.display = 'none';
        if (this.endCallBtn) this.endCallBtn.style.display = 'inline-block';
        this.isCallActive = true;
    }

    hideVideoUI() {
        if (this.videoContainer) this.videoContainer.style.display = 'none';
        if (this.startCallBtn) this.startCallBtn.style.display = 'inline-block';
        if (this.endCallBtn) this.endCallBtn.style.display = 'none';
        this.isCallActive = false;
    }

    updateStatus(message) {
        if (this.callStatus) {
            this.callStatus.textContent = message;
            this.callStatus.className = 'call-status ' + (message.includes('Error') ? 'error' : 'info');
        }
        console.log('📢 Status:', message);
    }

    endCall() {
        this.sendSignal('END_CALL', {});
        this.cleanup();
        this.hideVideoUI();
        this.updateStatus('Call ended');
    }

    cleanup() {
        if (this.localStream) {
            this.localStream.getTracks().forEach(track => track.stop());
            this.localStream = null;
        }
        if (this.peerConnection) {
            this.peerConnection.close();
            this.peerConnection = null;
        }
        if (this.webSocket) {
            this.webSocket.close();
            this.webSocket = null;
        }

        this.localVideo.srcObject = null;
        this.remoteVideo.srcObject = null;
        this.remoteUserConnected = false;
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // These variables should be set in your HTML template
    if (window.currentRoomId && window.currentUserId && window.currentUsername) {
        window.videoCallManager = new VideoCallManager(
            window.currentRoomId,
            window.currentUserId,
            window.currentUsername
        );
        console.log('✅ VideoCallManager initialized');
    } else {
        console.warn('⚠️ VideoCallManager: Missing roomId, userId, or username');
    }
});