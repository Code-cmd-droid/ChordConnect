package com.chordconnect;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;

public class ChatWindow extends JFrame {
    private int userId;
    private String username;
    private int targetUserId;  // ADD THIS
    private String targetUsername;  // ADD THIS
    private int currentRoomId;

    // Video and Chat panels
    private JPanel videoPanel;
    private JPanel chatPanel;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton startVideoCallBtn;
    private JButton endVideoCallBtn;

    // WebSocket client
    private WebSocketChatClient chatClient;

    // Video call state
    private boolean videoCallActive = false;
    private VideoCallClient videoClient;
    private WebRTCVideoClient videoSignalingClient;

    // Typing indicator
    private Timer typingTimer;
    private boolean isTyping = false;

    // Updated constructor with target user parameters
    public ChatWindow(int userId, String username, int targetUserId, String targetUsername) {
        this.userId = userId;
        this.username = username;
        this.targetUserId = targetUserId;
        this.targetUsername = targetUsername;
        this.currentRoomId = generateRoomId(userId, targetUserId);

        initializeUI();
        setupEventListeners();
        connectToChatServer();
    }

    // Keep the old constructor for backward compatibility
    public ChatWindow(int userId, String username) {
        this(userId, username, -1, "Demo User");
    }

    private void initializeUI() {
        setTitle("ChordConnect - Chat with " + targetUsername);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1280, 720));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());

        // ---------- HEADER ----------
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // ---------- MAIN CONTENT (Video + Chat) ----------
        JSplitPane mainSplitPane = createMainSplitPane();
        add(mainSplitPane, BorderLayout.CENTER);

        // ---------- VIDEO CONTROLS ----------
        JPanel controlsPanel = createControlsPanel();
        add(controlsPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 25));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String roomTitle = targetUserId > 0 ?
                "Chat with " + targetUsername + " - Room #" + currentRoomId :
                "Demo Chat - Room #" + currentRoomId;

        JLabel titleLabel = new JLabel(roomTitle);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.setBackground(UITheme.ACCENT);
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        backBtn.addActionListener(e -> {
            dispose();
            new DashboardFrame(userId, username);
        });

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(backBtn, BorderLayout.EAST);

        return headerPanel;
    }

    private JSplitPane createMainSplitPane() {
        // Video Panel (3/4 of space)
        videoPanel = new JPanel(new BorderLayout());
        videoPanel.setBackground(new Color(15, 15, 15));
        videoPanel.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50), 2));

        // Initial video placeholder
        String placeholderText = targetUserId > 0 ?
                "Video call not active<br>Click 'Start Video Call' to connect with " + targetUsername :
                "Video call not active<br>Click 'Start Video Call' to begin jamming!";

        JLabel videoPlaceholder = new JLabel(
                "<html><div style='text-align: center; color: #888; font-size: 16px;'>" +
                        placeholderText +
                        "</div></html>",
                JLabel.CENTER
        );
        videoPlaceholder.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        videoPlaceholder.setForeground(Color.GRAY);
        videoPanel.add(videoPlaceholder, BorderLayout.CENTER);

        // Chat Panel (1/4 of space)
        chatPanel = createChatPanel();

        // Create split pane with 75%-25% ratio
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, videoPanel, chatPanel);
        splitPane.setDividerLocation(0.75); // 75% for video, 25% for chat
        splitPane.setDividerSize(3);
        splitPane.setResizeWeight(0.75); // Prefer video panel when resizing
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        return splitPane;
    }

    private JPanel createChatPanel() {
        JPanel chatContainer = new JPanel(new BorderLayout());
        chatContainer.setBackground(new Color(30, 30, 30));
        chatContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Chat title
        String chatTitleText = targetUserId > 0 ?
                "Chat with " + targetUsername : "Session Chat";
        JLabel chatTitle = new JLabel(chatTitleText);
        chatTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        chatTitle.setForeground(Color.WHITE);
        chatTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Chat messages area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(40, 40, 40));
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);

        // Message input area
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(40, 40, 40));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        messageField = new JTextField();
        messageField.setBackground(new Color(50, 50, 50));
        messageField.setForeground(Color.WHITE);
        messageField.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        sendButton = new JButton("Send");
        sendButton.setBackground(UITheme.ACCENT);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        sendButton.setPreferredSize(new Dimension(80, 35));

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Add all components to chat container
        chatContainer.add(chatTitle, BorderLayout.NORTH);
        chatContainer.add(chatScroll, BorderLayout.CENTER);
        chatContainer.add(inputPanel, BorderLayout.SOUTH);

        return chatContainer;
    }

    private JPanel createControlsPanel() {
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlsPanel.setBackground(new Color(25, 25, 25));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Video call controls
        styleVideoButton(startVideoCallBtn, new Color(76, 175, 80)); // Green
        startVideoCallBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));

        styleVideoButton(startVideoCallBtn, new Color(76, 175, 80)); // Green
        styleVideoButton(endVideoCallBtn, new Color(244, 67, 54));   // Red

        endVideoCallBtn.setEnabled(false);

        // Audio controls
        JButton muteAudioBtn = new JButton("🔇 Mute");
        JButton unmuteAudioBtn = new JButton("🔊 Unmute");
        styleVideoButton(muteAudioBtn, new Color(33, 150, 243));    // Blue
        styleVideoButton(unmuteAudioBtn, new Color(33, 150, 243));  // Blue

        controlsPanel.add(startVideoCallBtn);
        controlsPanel.add(endVideoCallBtn);
        controlsPanel.add(Box.createHorizontalStrut(40));
        controlsPanel.add(muteAudioBtn);
        controlsPanel.add(unmuteAudioBtn);

        return controlsPanel;
    }

    private void styleVideoButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
    }

    private void setupEventListeners() {
        // Send message
        sendButton.addActionListener(e -> sendChatMessage());
        messageField.addActionListener(e -> sendChatMessage());

        // Typing detection
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!isTyping) {
                    isTyping = true;
                    if (chatClient != null) {
                        chatClient.sendTypingIndicator(true);
                    }
                }

                // Reset typing timer
                if (typingTimer != null) {
                    typingTimer.stop();
                }
                typingTimer = new Timer(2000, ev -> {
                    isTyping = false;
                    if (chatClient != null) {
                        chatClient.sendTypingIndicator(false);
                    }
                });
                typingTimer.setRepeats(false);
                typingTimer.start();
            }
        });

        // Video call controls
        startVideoCallBtn.addActionListener(e -> startVideoCall());
        endVideoCallBtn.addActionListener(e -> endVideoCall());
    }

    // ---------- CHAT FUNCTIONALITY ----------
    private void connectToChatServer() {
        try {
            chatClient = new WebSocketChatClient(
                    userId,
                    username,
                    currentRoomId,
                    this::handleIncomingMessage,
                    this::handleChatError
            );

            chatClient.connect();
            appendToChat("[" + new java.util.Date() + "] Connecting to chat server...\n");

        } catch (Exception e) {
            appendToChat("[" + new java.util.Date() + "] Failed to connect: " + e.getMessage() + "\n");
        }
    }

    private void handleIncomingMessage(String messageJson) {
        try {
            JSONObject message = new JSONObject(messageJson);
            String type = message.getString("type");

            switch (type) {
                case "CONNECTED":
                    appendToChat("[" + new java.util.Date() + "] Connected to chat server\n");
                    break;

                case "ROOM_HISTORY":
                    handleRoomHistory(message);
                    break;

                case "NEW_MESSAGE":
                    handleNewMessage(message);
                    break;

                case "USER_JOINED":
                    handleUserJoined(message);
                    break;

                case "USER_LEFT":
                    handleUserLeft(message);
                    break;

                case "USER_TYPING":
                    handleUserTyping(message);
                    break;

                case "ERROR":
                    handleChatError(message.getString("message"));
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error parsing message: " + e.getMessage());
        }
    }

    private void handleRoomHistory(JSONObject message) {
        JSONArray history = message.getJSONArray("messages");
        for (int i = 0; i < history.length(); i++) {
            JSONObject msg = history.getJSONObject(i);
            String timestamp = msg.getString("timestamp");
            String user = msg.getString("username");
            String content = msg.getString("content");
            appendToChat("[" + timestamp + "] " + user + ": " + content + "\n");
        }
    }

    private void handleNewMessage(JSONObject message) {
        JSONObject msg = message.getJSONObject("message");
        String user = msg.getString("username");
        String content = msg.getString("content");
        String timestamp = msg.getString("timestamp");
        appendToChat("[" + timestamp + "] " + user + ": " + content + "\n");
    }

    private void handleUserJoined(JSONObject message) {
        String user = message.getString("username");
        appendToChat("[" + new java.util.Date() + "] " + user + " joined the chat\n");
    }

    private void handleUserLeft(JSONObject message) {
        String user = message.getString("username");
        appendToChat("[" + new java.util.Date() + "] " + user + " left the chat\n");
    }

    private void handleUserTyping(JSONObject message) {
        String user = message.getString("username");
        boolean typing = message.getBoolean("isTyping");

        // You could show a "User is typing..." indicator in the UI
        if (typing) {
            System.out.println(user + " is typing...");
        }
    }

    private void handleChatError(String error) {
        appendToChat("[" + new java.util.Date() + "] Error: " + error + "\n");
    }

    private void sendChatMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && chatClient != null && chatClient.isConnected()) {
            chatClient.sendChatMessage(message);
            messageField.setText("");

            // Auto-scroll to bottom
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    private void appendToChat(String text) {
        chatArea.append(text);
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    // ---------- VIDEO CALL FUNCTIONALITY ----------
// In the startVideoCall method, update this line:
    // In the startVideoCall method, make sure you're using the correct constructor:
    // ---------- VIDEO CALL FUNCTIONALITY ----------
    private void startVideoCall() {
        try {
            // Open browser with video call page
            openVideoCallInBrowser();

            appendToChat("[" + new java.util.Date() + "] 🎥 Opening video call in browser...\n");
            appendToChat("[" + new java.util.Date() + "] 📍 Room ID: " + currentRoomId + "\n");
            appendToChat("[" + new java.util.Date() + "] 💡 Both users should open the same URL\n");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open video call: " + e.getMessage(),
                    "Video Call Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openVideoCallInBrowser(int roomId) {
        try {
            // Use your ngrok URL
            String serverUrl = "https://unslumbrous-glenda-proprivilege.ngrok-free.dev";

            String videoCallUrl = String.format(
                    "%s/video-call?room=%d&userId=%d&username=%s",
                    serverUrl, roomId, userId, java.net.URLEncoder.encode(username, "UTF-8")
            );

            java.awt.Desktop.getDesktop().browse(new java.net.URI(videoCallUrl));

        } catch (Exception e) {
            throw new RuntimeException("Failed to open browser: " + e.getMessage());
        }
    }


    // Add this method for video signaling
    private void setupVideoSignaling() {
        try {
            videoSignalingClient = new WebRTCVideoClient(
                    String.valueOf(userId),
                    username,
                    String.valueOf(currentRoomId),
                    this::handleVideoUserEvent,
                    this::handleVideoError
            );
            videoSignalingClient.connect();
        } catch (Exception e) {
            System.err.println("Video signaling setup failed: " + e.getMessage());
        }
    }

    private void handleVideoUserEvent(String event) {
        String[] parts = event.split(":");
        String action = parts[0];
        String user = parts[1];
        String username = parts.length > 2 ? parts[2] : "User";

        if ("JOINED".equals(action)) {
            appendToChat("[" + new java.util.Date() + "] 🎥 " + username + " joined the video call\n");
        } else if ("LEFT".equals(action)) {
            appendToChat("[" + new java.util.Date() + "] 🎥 " + username + " left the video call\n");
        } else if ("OFFER".equals(action)) {
            appendToChat("[" + new java.util.Date() + "] 🎥 Receiving video stream from " + username + "\n");
        }
    }

    private void handleVideoError(String error) {
        appendToChat("[" + new java.util.Date() + "] ❌ Video error: " + error + "\n");
    }

    private void endVideoCall() {
        // Just provide information since call is in browser
        appendToChat("[" + new java.util.Date() + "] 🎥 Video call is running in browser\n");
        appendToChat("[" + new java.util.Date() + "] 💡 Close the browser tab to end the call\n");
    }
    // Update room ID generation to include both users
    private int generateRoomId(int user1Id, int user2Id) {
        long min = Math.min(user1Id, user2Id);
        long max = Math.max(user1Id, user2Id);
        return Math.abs((int) (min * 10000 + max));
    }
    @Override
    public void dispose() {
        if (chatClient != null) {
            chatClient.disconnect();
        }
        if (videoCallActive) {
            endVideoCall();
        }
        super.dispose();
    }
    private void updateUserCount(int count) {
        if (count > 0) {
            appendToChat("[" + new java.util.Date() + "] 🎵 " + count + " musicians connected to the jam session\n");
        }
    }
}