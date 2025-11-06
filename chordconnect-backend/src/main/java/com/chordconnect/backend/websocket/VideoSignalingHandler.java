package com.chordconnect.backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VideoSignalingHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = extractRoomIdFromPath(session);
        sessions.put(session.getId(), session);
        sessionRoomMap.put(session.getId(), roomId);

        System.out.println("🎉 WebSocket CONNECTED - Session: " + session.getId());
        System.out.println("🎯 Room: " + roomId);
        System.out.println("📊 Total active sessions: " + sessions.size());

        // Send welcome message to the new user
        sendToSession(session, createMessage("connected", Map.of(
                "message", "Connected to room: " + roomId,
                "sessionId", session.getId(),
                "userCount", getRoomUserCount(roomId)
        )));

        // Notify other users in the room
        broadcastToRoom(roomId, createMessage("user_joined", Map.of(
                "message", "New user joined the room",
                "userCount", getRoomUserCount(roomId)
        )), session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String roomId = sessionRoomMap.get(session.getId());
            String payload = message.getPayload();

            System.out.println("📨 Received message in room " + roomId + " from session " + session.getId());
            System.out.println("   Message: " + payload);

            // Parse the message to ensure it's valid JSON
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String type = (String) data.get("type");

            System.out.println("   Type: " + type);

            // Simply relay the message to other users in the same room
            broadcastToRoom(roomId, payload, session.getId());

        } catch (Exception e) {
            System.err.println("❌ Error processing message: " + e.getMessage());
            // Send error back to the sender
            sendToSession(session, createMessage("error", Map.of(
                    "message", "Invalid message format: " + e.getMessage()
            )));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        String roomId = sessionRoomMap.remove(session.getId());
        sessions.remove(session.getId());

        System.out.println("👋 WebSocket CLOSED - Session: " + session.getId());
        System.out.println("📊 Remaining sessions: " + sessions.size());

        if (roomId != null) {
            broadcastToRoom(roomId, createMessage("user_left", Map.of(
                    "message", "User left the room",
                    "userCount", getRoomUserCount(roomId)
            )), session.getId());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("🚨 Transport error for session " + session.getId() + ": " + exception.getMessage());
    }

    private void broadcastToRoom(String roomId, String message, String excludeSessionId) {
        int sentCount = 0;

        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            String sessionId = entry.getKey();
            WebSocketSession targetSession = entry.getValue();

            // Skip if it's the excluded session, wrong room, or closed session
            if (sessionId.equals(excludeSessionId)) continue;
            if (!roomId.equals(sessionRoomMap.get(sessionId))) continue;
            if (!targetSession.isOpen()) continue;

            try {
                targetSession.sendMessage(new TextMessage(message));
                sentCount++;
                System.out.println("📤 Sent message to session: " + sessionId);
            } catch (IOException e) {
                System.err.println("❌ Failed to send to session " + sessionId + ": " + e.getMessage());
                // Remove broken session
                sessions.remove(sessionId);
                sessionRoomMap.remove(sessionId);
            }
        }

        if (sentCount > 0) {
            System.out.println("✅ Broadcast complete - Sent to " + sentCount + " users in room " + roomId);
        }
    }

    private void sendToSession(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
                System.out.println("📤 Sent message to session: " + session.getId());
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to send to session " + session.getId() + ": " + e.getMessage());
        }
    }

    private String createMessage(String type, Map<String, Object> data) {
        try {
            Map<String, Object> message = new ConcurrentHashMap<>();
            message.put("type", type);
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            System.err.println("❌ Failed to create message: " + e.getMessage());
            return "{\"type\":\"error\",\"message\":\"Failed to create message\"}";
        }
    }

    private int getRoomUserCount(String roomId) {
        return (int) sessions.values().stream()
                .filter(session -> roomId.equals(sessionRoomMap.get(session.getId())))
                .count();
    }

    private String extractRoomIdFromPath(WebSocketSession session) {
        try {
            String path = session.getUri().getPath();
            System.out.println("🔍 WebSocket connection path: " + path);

            // Expected path format: /ws/video/room123
            String[] segments = path.split("/");

            // Look for "video" in the path and get the next segment as roomId
            for (int i = 0; i < segments.length; i++) {
                if ("video".equals(segments[i]) && i + 1 < segments.length) {
                    String roomId = segments[i + 1];
                    System.out.println("🎯 Extracted room ID: " + roomId);
                    return roomId;
                }
            }

            // Fallback: use the last segment as room ID
            if (segments.length > 0) {
                String roomId = segments[segments.length - 1];
                System.out.println("🔄 Using fallback room ID: " + roomId);
                return roomId;
            }

            System.out.println("⚠️ No room ID found, using default");
            return "default-room";

        } catch (Exception e) {
            System.err.println("❌ Error extracting room ID from path: " + e.getMessage());
            return "default-room";
        }
    }

    // Utility method to get session info (for debugging)
    public void printSessionInfo() {
        System.out.println("=== WebSocket Session Info ===");
        System.out.println("Total sessions: " + sessions.size());
        sessions.forEach((sessionId, session) -> {
            String roomId = sessionRoomMap.get(sessionId);
            System.out.println("  Session: " + sessionId + " -> Room: " + roomId + " [Open: " + session.isOpen() + "]");
        });
        System.out.println("==============================");
    }
}