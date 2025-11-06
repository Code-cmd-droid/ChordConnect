package com.chordconnect.backend.websocket;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void addSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
        System.out.println("✅ WebSocket session added: " + sessionId + " (Total: " + sessions.size() + ")");
    }

    public void removeSession(String sessionId) {
        WebSocketSession session = sessions.remove(sessionId);
        if (session != null) {
            System.out.println("✅ WebSocket session removed: " + sessionId + " (Remaining: " + sessions.size() + ")");
        }
    }

    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void sendToSession(String sessionId, String message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                System.out.println("📤 Message sent to session " + sessionId + ": " + message.substring(0, Math.min(100, message.length())) + "...");
            } catch (IOException e) {
                System.err.println("❌ Error sending message to session " + sessionId + ": " + e.getMessage());
                // Remove broken session
                removeSession(sessionId);
            }
        } else {
            System.err.println("❌ Session not found or closed: " + sessionId);
        }
    }

    public void broadcastToAll(String message, String excludeSessionId) {
        sessions.forEach((sessionId, session) -> {
            if (!sessionId.equals(excludeSessionId) && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    System.err.println("❌ Error broadcasting to session " + sessionId + ": " + e.getMessage());
                    removeSession(sessionId);
                }
            }
        });
    }

    public boolean isSessionActive(String sessionId) {
        WebSocketSession session = sessions.get(sessionId);
        return session != null && session.isOpen();
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }

    public Map<String, WebSocketSession> getAllSessions() {
        return new ConcurrentHashMap<>(sessions);
    }
}