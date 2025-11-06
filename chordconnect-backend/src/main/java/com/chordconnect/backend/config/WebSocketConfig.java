package com.chordconnect.backend.config;

import com.chordconnect.backend.websocket.VideoSignalingHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final VideoSignalingHandler videoSignalingHandler;

    public WebSocketConfig(VideoSignalingHandler videoSignalingHandler) {
        this.videoSignalingHandler = videoSignalingHandler;
        System.out.println("✅ WebSocketConfig initialized with VideoSignalingHandler");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        System.out.println("🔧 Registering WebSocket handlers...");

        // Video signaling WebSocket
        registry.addHandler(videoSignalingHandler, "/ws/video/{roomId}")
                .setAllowedOriginPatterns("*");

        System.out.println("✅ Video WebSocket handler registered at: /ws/video/{roomId}");
    }
}