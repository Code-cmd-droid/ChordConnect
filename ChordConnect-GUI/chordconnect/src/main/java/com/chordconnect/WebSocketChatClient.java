package com.chordconnect;

import org.json.JSONObject;
import javax.swing.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class WebSocketChatClient {
    private WebSocket webSocket;
    private final int userId;
    private final String username;
    private final int roomId;
    private final Consumer<String> onMessageReceived;
    private final Consumer<String> onError;
    private boolean connected = false;

    public WebSocketChatClient(int userId, String username, int roomId,
                               Consumer<String> onMessageReceived, Consumer<String> onError) {
        this.userId = userId;
        this.username = username;
        this.roomId = roomId;
        this.onMessageReceived = onMessageReceived;
        this.onError = onError;
    }

    public void connect() {
        try {
            // Use the new getWsBaseUrl() method instead of WS_BASE_URL constant
            String wsUrl = UITheme.getWsBaseUrl() + UITheme.CHAT_WS_ENDPOINT + "?roomId=" + roomId + "&userId=" + userId;

            HttpClient client = HttpClient.newHttpClient();
            webSocket = client.newWebSocketBuilder()
                    .buildAsync(URI.create(wsUrl), new WebSocket.Listener() {
                        @Override
                        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                            String message = data.toString();
                            SwingUtilities.invokeLater(() -> onMessageReceived.accept(message));
                            return WebSocket.Listener.super.onText(webSocket, data, last);
                        }

                        @Override
                        public void onError(WebSocket webSocket, Throwable error) {
                            SwingUtilities.invokeLater(() -> onError.accept("WebSocket error: " + error.getMessage()));
                            WebSocket.Listener.super.onError(webSocket, error);
                        }

                        @Override
                        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                            connected = false;
                            SwingUtilities.invokeLater(() -> onError.accept("Disconnected from chat server"));
                            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
                        }
                    })
                    .join();

            connected = true;

            // Join the room
            sendJoinMessage();

        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> onError.accept("Failed to connect: " + e.getMessage()));
        }
    }

    private void sendJoinMessage() {
        JSONObject joinMessage = new JSONObject();
        joinMessage.put("type", "JOIN_ROOM");
        joinMessage.put("roomId", roomId);
        joinMessage.put("userId", userId);
        joinMessage.put("username", username);

        sendMessage(joinMessage.toString());
    }

    public void sendChatMessage(String content) {
        JSONObject message = new JSONObject();
        message.put("type", "CHAT_MESSAGE");
        message.put("roomId", roomId);
        message.put("userId", userId);
        message.put("content", content);

        sendMessage(message.toString());
    }

    public void sendTypingIndicator(boolean isTyping) {
        JSONObject message = new JSONObject();
        message.put("type", "TYPING_INDICATOR");
        message.put("roomId", roomId);
        message.put("userId", userId);
        message.put("username", username);
        message.put("isTyping", isTyping);

        sendMessage(message.toString());
    }

    private void sendMessage(String message) {
        if (webSocket != null && connected) {
            webSocket.sendText(message, true);
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "User disconnected");
            connected = false;
        }
    }

    public boolean isConnected() {
        return connected;
    }
}