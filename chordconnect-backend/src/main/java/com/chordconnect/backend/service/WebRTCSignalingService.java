package com.chordconnect.backend.service;

import com.chordconnect.backend.websocket.WebSocketSessionManager;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebRTCSignalingService {

    // Store room sessions: roomId -> (sessionId -> userId)
    private final Map<String, Map<String, Long>> roomSessions = new ConcurrentHashMap<>();
    // Store session to user mapping
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();
    // Store user to session mapping
    private final Map<Long, String> userSessionMap = new ConcurrentHashMap<>();

    private final WebSocketSessionManager sessionManager;

    @Autowired
    public WebRTCSignalingService(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void addSessionToRoom(String roomId, String sessionId, Long userId) {
        roomSessions.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>())
                .put(sessionId, userId);
        sessionUserMap.put(sessionId, userId);
        userSessionMap.put(userId, sessionId);

        System.out.println("✅ Added session " + sessionId + " for user " + userId + " to room " + roomId);
        System.out.println("📊 Room " + roomId + " now has " + getRoomParticipantCount(roomId) + " participant(s)");
    }

    public void removeSessionFromRoom(String roomId, String sessionId) {
        Map<String, Long> room = roomSessions.get(roomId);
        if (room != null) {
            Long userId = room.remove(sessionId);
            sessionUserMap.remove(sessionId);
            if (userId != null) {
                userSessionMap.remove(userId);
            }

            System.out.println("✅ Removed session " + sessionId + " from room " + roomId);
            System.out.println("📊 Room " + roomId + " now has " + getRoomParticipantCount(roomId) + " participant(s)");

            // Clean up empty rooms
            if (room.isEmpty()) {
                roomSessions.remove(roomId);
                System.out.println("✅ Removed empty room " + roomId);
            }
        }
    }

    public void broadcastToRoom(String roomId, String message, String excludeSessionId) {
        Map<String, Long> room = roomSessions.get(roomId);
        if (room != null) {
            System.out.println("📤 Broadcasting to room " + roomId + " (excluding: " + excludeSessionId + ")");
            room.forEach((sessionId, userId) -> {
                if (!sessionId.equals(excludeSessionId)) {
                    sessionManager.sendToSession(sessionId, message);
                }
            });
        } else {
            System.err.println("❌ Room not found for broadcasting: " + roomId);
        }
    }

    public void sendToUser(Long userId, String message) {
        String sessionId = userSessionMap.get(userId);
        if (sessionId != null) {
            sessionManager.sendToSession(sessionId, message);
        } else {
            System.err.println("❌ No active session found for user: " + userId);
        }
    }

    public String getUserIdForSession(String sessionId) {
        Long userId = sessionUserMap.get(sessionId);
        return userId != null ? userId.toString() : "unknown";
    }

    public Long getUserIdForSessionLong(String sessionId) {
        return sessionUserMap.get(sessionId);
    }

    public int getRoomParticipantCount(String roomId) {
        Map<String, Long> room = roomSessions.get(roomId);
        return room != null ? room.size() : 0;
    }

    public Map<String, Long> getRoomParticipants(String roomId) {
        Map<String, Long> room = roomSessions.get(roomId);
        return room != null ? new ConcurrentHashMap<>(room) : new ConcurrentHashMap<>();
    }

    public boolean isUserInRoom(String roomId, Long userId) {
        Map<String, Long> room = roomSessions.get(roomId);
        return room != null && room.containsValue(userId);
    }

    public String getRoomIdForSession(String sessionId) {
        for (Map.Entry<String, Map<String, Long>> roomEntry : roomSessions.entrySet()) {
            if (roomEntry.getValue().containsKey(sessionId)) {
                return roomEntry.getKey();
            }
        }
        return null;
    }

    public void cleanupUserSessions(Long userId) {
        String sessionId = userSessionMap.get(userId);
        if (sessionId != null) {
            String roomId = getRoomIdForSession(sessionId);
            if (roomId != null) {
                removeSessionFromRoom(roomId, sessionId);
            }
        }
    }

    public Map<String, Map<String, Long>> getAllRoomSessions() {
        return new ConcurrentHashMap<>(roomSessions);
    }
}