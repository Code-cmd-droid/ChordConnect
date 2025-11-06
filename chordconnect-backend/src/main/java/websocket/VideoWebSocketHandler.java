package com.chordconnect.backend.websocket;

import com.chordconnect.backend.service.VideoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VideoWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<String, String> userToSessionMap = new ConcurrentHashMap<>();
    private final VideoService videoService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VideoWebSocketHandler(VideoService videoService) {
        this.videoService = videoService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = extractUserId(session);
        if (userId != null) {
            userSessions.put(userId, session);
            System.out.println("User connected to video: " + userId);

            // Send confirmation
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                    Map.of("type", "ROOM_JOINED", "message", "Connected to video room")
            )));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) payload.get("type");
            String userId = (String) payload.get("userId");

            switch (type) {
                case "JOIN_ROOM":
                    handleJoinRoom(userId, payload);
                    break;
                case "OFFER":
                    handleOffer(userId, payload);
                    break;
                case "ANSWER":
                    handleAnswer(userId, payload);
                    break;
                case "ICE_CANDIDATE":
                    handleIceCandidate(userId, payload);
                    break;
                case "LEAVE_ROOM":
                    handleLeaveRoom(userId);
                    break;
            }
        } catch (Exception e) {
            session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(Map.of("type", "ERROR", "message", e.getMessage()))
            ));
        }
    }

    private void handleJoinRoom(String userId, Map<String, Object> payload) throws Exception {
        String roomId = (String) payload.get("roomId");
        String username = (String) payload.get("username");
        userToSessionMap.put(userId, roomId);

        // Notify other users in the room (except self)
        broadcastToRoom(roomId, userId, Map.of(
                "type", "USER_JOINED",
                "userId", userId,
                "username", username
        ));

        // Send list of existing users in room
        sendUserList(userId, roomId);
    }

    private void handleOffer(String fromUserId, Map<String, Object> payload) throws Exception {
        String toUserId = (String) payload.get("targetUserId");
        String roomId = userToSessionMap.get(fromUserId);

        if (userSessions.containsKey(toUserId) && !fromUserId.equals(toUserId)) {
            sendToUser(toUserId, Map.of(
                    "type", "OFFER",
                    "offer", payload.get("offer"),
                    "fromUserId", fromUserId,
                    "username", payload.get("username")
            ));
        }
    }

    private void handleAnswer(String fromUserId, Map<String, Object> payload) throws Exception {
        String toUserId = (String) payload.get("targetUserId");

        if (userSessions.containsKey(toUserId) && !fromUserId.equals(toUserId)) {
            sendToUser(toUserId, Map.of(
                    "type", "ANSWER",
                    "answer", payload.get("answer"),
                    "fromUserId", fromUserId
            ));
        }
    }

    private void handleIceCandidate(String fromUserId, Map<String, Object> payload) throws Exception {
        String toUserId = (String) payload.get("targetUserId");

        if (userSessions.containsKey(toUserId) && !fromUserId.equals(toUserId)) {
            sendToUser(toUserId, Map.of(
                    "type", "ICE_CANDIDATE",
                    "candidate", payload.get("candidate"),
                    "fromUserId", fromUserId
            ));
        }
    }

    private void handleLeaveRoom(String userId) throws Exception {
        String roomId = userToSessionMap.remove(userId);
        if (roomId != null) {
            broadcastToRoom(roomId, userId, Map.of(
                    "type", "USER_LEFT",
                    "userId", userId
            ));
        }
    }

    private void sendUserList(String userId, String roomId) throws Exception {
        // Get all users in the room except the current user
        java.util.List<String> otherUsers = userToSessionMap.entrySet().stream()
                .filter(entry -> roomId.equals(entry.getValue()) && !userId.equals(entry.getKey()))
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());

        if (!otherUsers.isEmpty()) {
            sendToUser(userId, Map.of(
                    "type", "USERS_IN_ROOM",
                    "users", otherUsers
            ));
        }
    }

    private void sendToUser(String userId, Object message) throws Exception {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        }
    }

    private void broadcastToRoom(String roomId, String excludeUserId, Object message) throws Exception {
        String messageJson = objectMapper.writeValueAsString(message);

        userToSessionMap.entrySet().stream()
                .filter(entry -> roomId.equals(entry.getValue()) && !excludeUserId.equals(entry.getKey()))
                .forEach(entry -> {
                    WebSocketSession session = userSessions.get(entry.getKey());
                    if (session != null && session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage(messageJson));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private String extractUserId(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userId=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("userId=")) {
                    return param.substring(7);
                }
            }
        }
        return null;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = extractUserId(session);
        if (userId != null) {
            userSessions.remove(userId);
            handleLeaveRoom(userId);
            System.out.println("User disconnected from video: " + userId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = extractUserId(session);
        if (userId != null) {
            userSessions.remove(userId);
            handleLeaveRoom(userId);
            System.out.println("Video transport error for user: " + userId);
        }
    }
}