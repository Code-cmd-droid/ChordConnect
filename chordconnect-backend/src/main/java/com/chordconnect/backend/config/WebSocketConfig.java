package com.chordconnect.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.chordconnect.backend.websocket.ChatWebSocketHandler;
import com.chordconnect.backend.websocket.VideoWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final VideoWebSocketHandler videoWebSocketHandler;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler,
                           VideoWebSocketHandler videoWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.videoWebSocketHandler = videoWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins("*");

        registry.addHandler(videoWebSocketHandler, "/ws/video")
                .setAllowedOrigins("*");
    }
}