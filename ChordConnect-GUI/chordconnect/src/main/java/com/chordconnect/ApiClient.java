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
}