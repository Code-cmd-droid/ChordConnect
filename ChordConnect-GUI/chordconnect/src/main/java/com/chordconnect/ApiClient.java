package com.chordconnect;

import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiClient {
    private static String authToken = null;

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static String getAuthToken() {
        return authToken;
    }

    // Use the configurable base URL from UITheme
    private static String getBaseUrl() {
        return UITheme.getBaseUrl();
    }

    // Generic HTTP request method
    private static String makeRequest(String endpoint, String method, String requestBody) throws IOException {
        try {
            URL url = new URL(getBaseUrl() + endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (authToken != null && !authToken.isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + authToken);
            }

            if (requestBody != null && !requestBody.isEmpty()) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            int responseCode = connection.getResponseCode();
            System.out.println("API Response Code: " + responseCode);

            InputStream inputStream = responseCode < 400 ? connection.getInputStream() : connection.getErrorStream();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                String responseBody = response.toString();
                System.out.println("API Response: " + responseBody);
                return responseBody;
            }
        } catch (Exception e) {
            System.err.println("API Request failed: " + e.getMessage());
            throw new IOException("Failed to connect to server: " + e.getMessage());
        }
    }

    // Login method with flexible success checking
    public static JSONObject login(String username, String password) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", username);
        requestBody.put("password", password);

        String response = makeRequest("/auth/login", "POST", requestBody.toString());
        JSONObject jsonResponse = new JSONObject(response);

        // Debug: Print all fields in response
        System.out.println("Login Response Fields:");
        for (String key : jsonResponse.keySet()) {
            System.out.println("  " + key + ": " + jsonResponse.get(key));
        }

        return jsonResponse;
    }

    // Register method with flexible success checking
    public static JSONObject register(String username, String password, String email) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", username);
        requestBody.put("password", password);
        requestBody.put("email", email);

        String response = makeRequest("/auth/register", "POST", requestBody.toString());
        JSONObject jsonResponse = new JSONObject(response);

        // Debug: Print all fields in response
        System.out.println("Register Response Fields:");
        for (String key : jsonResponse.keySet()) {
            System.out.println("  " + key + ": " + jsonResponse.get(key));
        }

        return jsonResponse;
    }

    // Save user preferences
    public static JSONObject savePreferences(int userId, String instruments, String genres,
                                             String languages, String gender, String ageGroup) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("instruments", instruments);
        requestBody.put("genres", genres);
        requestBody.put("languages", languages);
        requestBody.put("gender", gender);
        requestBody.put("ageGroup", ageGroup);

        String response = makeRequest("/users/" + userId + "/preferences", "POST", requestBody.toString());
        return new JSONObject(response);
    }

    // Get user preferences
    public static JSONObject getPreferences(int userId) throws IOException {
        String response = makeRequest("/users/" + userId + "/preferences", "GET", null);
        return new JSONObject(response);
    }

    // Send chat message
    public static JSONObject sendMessage(int senderId, int receiverId, String content) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("senderId", senderId);
        requestBody.put("receiverId", receiverId);
        requestBody.put("content", content);

        String response = makeRequest("/chat/send", "POST", requestBody.toString());
        return new JSONObject(response);
    }

    // Get chat history
    public static JSONObject getChatHistory(int user1Id, int user2Id) throws IOException {
        String response = makeRequest("/chat/history/" + user1Id + "/" + user2Id, "GET", null);
        return new JSONObject(response);
    }

    // Add friend
    public static JSONObject addFriend(int userId, int friendId) throws IOException {
        String response = makeRequest("/friends/" + userId + "/add/" + friendId, "POST", null);
        return new JSONObject(response);
    }

    // Get friends list
    public static JSONObject getFriendsList(int userId) throws IOException {
        String response = makeRequest("/friends/" + userId + "/list", "GET", null);
        return new JSONObject(response);
    }

    // Get recent jammers
    public static JSONObject getRecentJammers(int userId) throws IOException {
        String response = makeRequest("/jammers/recent/" + userId, "GET", null);
        return new JSONObject(response);
    }

    // Search users by username
    public static JSONObject searchUsers(String query) throws IOException {
        String response = makeRequest("/users/search?q=" + query, "GET", null);
        return new JSONObject(response);
    }

    // Get user by username
    public static JSONObject getUserByUsername(String username) throws IOException {
        String response = makeRequest("/users/username/" + username, "GET", null);
        return new JSONObject(response);
    }

    // Create a chat room between two users
    public static JSONObject createChatRoom(int user1Id, int user2Id) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("user1Id", user1Id);
        requestBody.put("user2Id", user2Id);

        String response = makeRequest("/chat/rooms/create", "POST", requestBody.toString());
        return new JSONObject(response);
    }

    // ========== VIDEO CALL METHODS ==========

    // Create a new video call room
    public static JSONObject createVideoRoom(String hostId) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("hostId", hostId);

        String response = makeRequest("/video/create-room", "POST", requestBody.toString());
        return new JSONObject(response);
    }

    // Join an existing video call room
    public static JSONObject joinVideoRoom(String roomId, String participantId, String username) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("participantId", participantId);
        requestBody.put("username", username);

        String response = makeRequest("/video/" + roomId + "/join", "POST", requestBody.toString());
        return new JSONObject(response);
    }

    // Get participants in a video call room
    public static JSONObject getRoomParticipants(String roomId) throws IOException {
        String response = makeRequest("/video/" + roomId + "/participants", "GET", null);
        return new JSONObject(response);
    }

    // Leave a video call room
    public static JSONObject leaveVideoRoom(String roomId, String participantId) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("participantId", participantId);

        String response = makeRequest("/video/" + roomId + "/leave", "POST", requestBody.toString());
        return new JSONObject(response);
    }

    // Send WebRTC signaling data
    public static JSONObject sendWebRTCSignal(String roomId, String fromUserId, String toUserId, Object signalData) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("from", fromUserId);
        requestBody.put("to", toUserId);
        requestBody.put("data", signalData);
        requestBody.put("type", "webrtc_signal");

        String response = makeRequest("/video/" + roomId + "/signal", "POST", requestBody.toString());
        return new JSONObject(response);
    }

    // Get available video rooms (for browsing/joining)
    public static JSONObject getAvailableRooms() throws IOException {
        String response = makeRequest("/video/rooms/available", "GET", null);
        return new JSONObject(response);
    }

    // End a video call room (host only)
    public static JSONObject endVideoRoom(String roomId, String hostId) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("hostId", hostId);

        String response = makeRequest("/video/" + roomId + "/end", "POST", requestBody.toString());
        return new JSONObject(response);
    }

    // Get room information
    public static JSONObject getRoomInfo(String roomId) throws IOException {
        String response = makeRequest("/video/" + roomId + "/info", "GET", null);
        return new JSONObject(response);
    }

    // Send chat message in video call
    public static JSONObject sendVideoCallChat(String roomId, String userId, String message) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("userId", userId);
        requestBody.put("message", message);

        String response = makeRequest("/video/" + roomId + "/chat", "POST", requestBody.toString());
        return new JSONObject(response);
    }

    // Get video call chat history
    public static JSONObject getVideoCallChat(String roomId) throws IOException {
        String response = makeRequest("/video/" + roomId + "/chat", "GET", null);
        return new JSONObject(response);
    }

    // Toggle user audio in video call
    public static JSONObject toggleUserAudio(String roomId, String userId, boolean muted) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("userId", userId);
        requestBody.put("muted", muted);

        String response = makeRequest("/video/" + roomId + "/audio", "POST", requestBody.toString());
        return new JSONObject(response);
    }

    // Toggle user video in video call
    public static JSONObject toggleUserVideo(String roomId, String userId, boolean videoOff) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("userId", userId);
        requestBody.put("videoOff", videoOff);

        String response = makeRequest("/video/" + roomId + "/video", "POST", requestBody.toString());
        return new JSONObject(response);
    }

    // Raise hand in video call
    public static JSONObject raiseHand(String roomId, String userId, boolean raised) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("userId", userId);
        requestBody.put("raised", raised);

        String response = makeRequest("/video/" + roomId + "/raise-hand", "POST", requestBody.toString());
        return new JSONObject(response);
    }

    // Kick participant from room (host only)
    public static JSONObject kickParticipant(String roomId, String hostId, String participantId) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("hostId", hostId);
        requestBody.put("participantId", participantId);

        String response = makeRequest("/video/" + roomId + "/kick", "POST", requestBody.toString());
        return new JSONObject(response);
    }

    // Get video call statistics
    public static JSONObject getCallStats(String roomId) throws IOException {
        String response = makeRequest("/video/" + roomId + "/stats", "GET", null);
        return new JSONObject(response);
    }

    // Simple health check for video service
    public static JSONObject checkVideoServiceHealth() throws IOException {
        String response = makeRequest("/video/health", "GET", null);
        return new JSONObject(response);
    }

    // Get user's active video calls
    public static JSONObject getUserActiveCalls(String userId) throws IOException {
        String response = makeRequest("/video/user/" + userId + "/active-calls", "GET", null);
        return new JSONObject(response);
    }

    // Update user's video call status
    public static JSONObject updateVideoStatus(String roomId, String userId, String status) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("userId", userId);
        requestBody.put("status", status);

        String response = makeRequest("/video/" + roomId + "/status", "POST", requestBody.toString());
        return new JSONObject(response);
    }

    // Get video call recording (if implemented)
    public static JSONObject getCallRecording(String roomId) throws IOException {
        String response = makeRequest("/video/" + roomId + "/recording", "GET", null);
        return new JSONObject(response);
    }

    // Start/stop recording (host only)
    public static JSONObject toggleRecording(String roomId, String hostId, boolean recording) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("hostId", hostId);
        requestBody.put("recording", recording);

        String response = makeRequest("/video/" + roomId + "/recording", "POST", requestBody.toString());
        return new JSONObject(response);
    }
}