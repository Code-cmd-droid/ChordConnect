package com.chordconnect.backend.config;

import com.chordconnect.backend.websocket.ChatWebSocketHandler;
import com.chordconnect.backend.websocket.VideoWebSocketHandler;
import com.chordconnect.backend.service.ChatService;
import com.chordconnect.backend.service.VideoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketHandlerConfig {

    @Bean
    public ChatWebSocketHandler chatWebSocketHandler(ChatService chatService) {
        return new ChatWebSocketHandler(chatService);
    }

    @Bean
    public VideoWebSocketHandler videoWebSocketHandler(VideoService videoService) {
        return new VideoWebSocketHandler(videoService);
    }
}