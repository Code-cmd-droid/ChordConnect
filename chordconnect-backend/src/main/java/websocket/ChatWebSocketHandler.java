package com.chordconnect.backend.websocket;

import com.chordconnect.backend.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatWebSocketHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);

        // Send connection confirmation
        session.sendMessage(new TextMessage(
                objectMapper.writeValueAsString(Map.of("type", "CONNECTED", "sessionId", sessionId))
        ));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) payload.get("type");

            switch (type) {
                case "JOIN_ROOM":
                    handleJoinRoom(session, payload);
                    break;
                case "CHAT_MESSAGE":
                    handleChatMessage(session, payload);
                    break;
                case "TYPING_INDICATOR":
                    handleTypingIndicator(session, payload);
                    break;
            }
        } catch (Exception e) {
            session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(Map.of("type", "ERROR", "message", e.getMessage()))
            ));
        }
    }

    private void handleJoinRoom(WebSocketSession session, Map<String, Object> payload) throws Exception {
        Long roomId = ((Number) payload.get("roomId")).longValue();
        Long userId = ((Number) payload.get("userId")).longValue();

        // Store room association
        session.getAttributes().put("roomId", roomId);
        session.getAttributes().put("userId", userId);

        // Send message history
        var history = chatService.getRecentMessages(roomId, 50);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                Map.of("type", "ROOM_HISTORY", "messages", history)
        )));

        // Notify others in room
        broadcastToRoom(roomId, session, Map.of(
                "type", "USER_JOINED",
                "userId", userId,
                "username", payload.get("username")
        ));
    }

    private void handleChatMessage(WebSocketSession session, Map<String, Object> payload) throws Exception {
        Long roomId = (Long) session.getAttributes().get("roomId");
        Long userId = (Long) session.getAttributes().get("userId");
        String content = (String) payload.get("content");

        if (roomId != null && userId != null) {
            // Save to database
            var savedMessage = chatService.saveMessage(roomId, userId, content);

            // Broadcast to room
            broadcastToRoom(roomId, null, Map.of(
                    "type", "NEW_MESSAGE",
                    "message", Map.of(
                            "id", savedMessage.getId(),
                            "username", savedMessage.getUser().getUsername(),
                            "content", savedMessage.getMessage(),
                            "timestamp", savedMessage.getTimestamp().toString()
                    )
            ));
        }
    }

    private void handleTypingIndicator(WebSocketSession session, Map<String, Object> payload) throws Exception {
        Long roomId = (Long) session.getAttributes().get("roomId");
        Long userId = (Long) session.getAttributes().get("userId");
        Boolean isTyping = (Boolean) payload.get("isTyping");

        if (roomId != null && userId != null) {
            broadcastToRoomExcept(roomId, session, Map.of(
                    "type", "USER_TYPING",
                    "userId", userId,
                    "username", payload.get("username"),
                    "isTyping", isTyping
            ));
        }
    }

    private void broadcastToRoom(Long roomId, WebSocketSession excludeSession, Object message) throws Exception {
        String messageJson = objectMapper.writeValueAsString(message);

        for (WebSocketSession sess : sessions.values()) {
            if (sess.isOpen() &&
                    sess != excludeSession &&
                    roomId.equals(sess.getAttributes().get("roomId"))) {
                sess.sendMessage(new TextMessage(messageJson));
            }
        }
    }

    private void broadcastToRoomExcept(Long roomId, WebSocketSession excludeSession, Object message) throws Exception {
        broadcastToRoom(roomId, excludeSession, message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long roomId = (Long) session.getAttributes().get("roomId");
        Long userId = (Long) session.getAttributes().get("userId");

        if (roomId != null && userId != null) {
            // Notify others in room
            broadcastToRoom(roomId, session, Map.of(
                    "type", "USER_LEFT",
                    "userId", userId
            ));
        }

        sessions.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        sessions.remove(session.getId());
    }
}