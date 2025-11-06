package com.chordconnect.backend.websocket;

import com.chordconnect.backend.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component  // ✅ Add this annotation
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private ChatService chatService;
    private WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();

    // ✅ Constructor injection
    public ChatWebSocketHandler(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.objectMapper = new ObjectMapper();
    }

    // ✅ Setter injection for ChatService
    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = getRoomIdFromSession(session);

        sessionManager.addSession(session.getId(), session);
        sessionRoomMap.put(session.getId(), roomId);

        System.out.println("💬 Chat connection established for room: " + roomId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomId = sessionRoomMap.get(session.getId());
        System.out.println("💬 Chat message in room " + roomId + ": " + message.getPayload());

        // Echo message back for testing
        session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionManager.removeSession(session.getId());
        sessionRoomMap.remove(session.getId());
        System.out.println("💬 Chat connection closed");
    }

    private String getRoomIdFromSession(WebSocketSession session) {
        try {
            return session.getUri().getPath().split("/")[3];
        } catch (Exception e) {
            return null;
        }
    }
}