package com.chordconnect;

import org.json.JSONObject;
import javax.swing.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class WebRTCVideoClient {
    private WebSocket webSocket;
    private final String userId;
    private final String username;
    private final String roomId;
    private final Consumer<String> onUserEvent;
    private final Consumer<String> onError;

    public WebRTCVideoClient(String userId, String username, String roomId,
                             Consumer<String> onUserEvent, Consumer<String> onError) {
        this.userId = userId;
        this.username = username;
        this.roomId = roomId;
        this.onUserEvent = onUserEvent;
        this.onError = onError;
    }

    public void connect() {
        try {
            // Use getWsBaseUrl() method
            String wsUrl = UITheme.getWsBaseUrl() + UITheme.VIDEO_WS_ENDPOINT + "?userId=" + userId + "&username=" + username + "&roomId=" + roomId;

            HttpClient client = HttpClient.newHttpClient();
            webSocket = client.newWebSocketBuilder()
                    .buildAsync(URI.create(wsUrl), new WebSocket.Listener() {
                        @Override
                        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                            String message = data.toString();
                            SwingUtilities.invokeLater(() -> handleVideoMessage(message));
                            return WebSocket.Listener.super.onText(webSocket, data, last);
                        }

                        @Override
                        public void onError(WebSocket webSocket, Throwable error) {
                            SwingUtilities.invokeLater(() -> onError.accept("Video signaling error: " + error.getMessage()));
                        }
                    })
                    .join();

            // Join the video room
            joinVideoRoom();

        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> onError.accept("Failed to connect to video signaling: " + e.getMessage()));
        }
    }

    private void joinVideoRoom() {
        JSONObject joinMessage = new JSONObject();
        joinMessage.put("type", "JOIN_ROOM");
        joinMessage.put("userId", userId);
        joinMessage.put("roomId", roomId);
        joinMessage.put("username", username);

        sendMessage(joinMessage.toString());

        System.out.println("Joined video room " + roomId + " as " + username);
    }

    private void handleVideoMessage(String messageJson) {
        try {
            JSONObject message = new JSONObject(messageJson);
            String type = message.getString("type");

            switch (type) {
                case "USER_JOINED":
                    String joinedUserId = message.getString("userId");
                    String joinedUsername = message.getString("username");
                    // Don't notify about ourselves
                    if (!joinedUserId.equals(userId)) {
                        onUserEvent.accept("JOINED:" + joinedUserId + ":" + joinedUsername);
                        System.out.println("Remote user joined: " + joinedUsername);
                    }
                    break;

                case "USER_LEFT":
                    String leftUserId = message.getString("userId");
                    // Don't notify about ourselves
                    if (!leftUserId.equals(userId)) {
                        onUserEvent.accept("LEFT:" + leftUserId);
                        System.out.println("Remote user left: " + leftUserId);
                    }
                    break;

                case "OFFER":
                    String fromUserId = message.getString("fromUserId");
                    String fromUsername = message.getString("username");
                    // Only handle offers from other users
                    if (!fromUserId.equals(userId)) {
                        onUserEvent.accept("OFFER:" + fromUserId + ":" + fromUsername);
                        System.out.println("Received offer from: " + fromUsername);
                    }
                    break;

                case "ROOM_JOINED":
                    System.out.println("Successfully joined video room");
                    break;

                case "ERROR":
                    onError.accept(message.getString("message"));
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error parsing video message: " + e.getMessage());
        }
    }

    private void sendOffer(String targetUserId) {
        JSONObject offerMessage = new JSONObject();
        offerMessage.put("type", "OFFER");
        offerMessage.put("userId", userId);
        offerMessage.put("targetUserId", targetUserId);
        offerMessage.put("username", username);
        offerMessage.put("offer", new JSONObject().put("sdp", "simulated-offer").put("type", "offer"));

        sendMessage(offerMessage.toString());
        System.out.println("Sent offer to: " + targetUserId);
    }

    private void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.sendText(message, true);
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            // Send leave message
            JSONObject leaveMessage = new JSONObject();
            leaveMessage.put("type", "LEAVE_ROOM");
            leaveMessage.put("userId", userId);
            sendMessage(leaveMessage.toString());

            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "User disconnected");
            System.out.println("Disconnected from video signaling");
        }
    }
}