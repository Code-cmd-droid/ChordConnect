package com.chordconnect;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class VideoCallClient {
    private int userId;
    private String username;
    private int roomId;
    private JPanel videoContainer;
    private javax.swing.Timer videoTimer; // Explicitly use Swing Timer
    private boolean isActive = false;

    // Track ALL connected users (except yourself)
    private Map<String, RemoteVideo> remoteVideos = new HashMap<>();
    private WebRTCVideoClient signalingClient;
    private Set<String> allowedUsers; // Optional: for private calls

    public VideoCallClient(int userId, String username, int roomId, JPanel videoContainer) {
        this.userId = userId;
        this.username = username;
        this.roomId = roomId;
        this.videoContainer = videoContainer;
        this.allowedUsers = new HashSet<>(); // Empty = allow all users
    }

    // Optional: Constructor for private calls with specific users
    public VideoCallClient(int userId, String username, int roomId, JPanel videoContainer, Set<String> allowedUsers) {
        this.userId = userId;
        this.username = username;
        this.roomId = roomId;
        this.videoContainer = videoContainer;
        this.allowedUsers = allowedUsers;
    }

    public void startCall() {
        try {
            System.out.println("Starting group video call in room: " + roomId);
            isActive = true;

            // Setup video container for multiple users
            setupVideoContainer();

            // Connect to signaling server
            connectToSignaling();

            System.out.println("Group video call started - anyone can join!");

        } catch (Exception e) {
            System.err.println("Failed to start video call: " + e.getMessage());
            JOptionPane.showMessageDialog(videoContainer, "Failed to start video: " + e.getMessage());
        }
    }

    private void setupVideoContainer() {
        videoContainer.removeAll();

        if (allowedUsers.isEmpty()) {
            // Group call layout - grid for multiple users
            videoContainer.setLayout(new GridLayout(0, 2, 10, 10));
        } else {
            // Private call layout - single user focus
            videoContainer.setLayout(new BorderLayout());
        }

        videoContainer.setBackground(new Color(15, 15, 15));

        // Show waiting message
        showWaitingForUsers();
    }

    private void showWaitingForUsers() {
        JLabel waitingLabel = new JLabel(
                "<html><div style='text-align: center; color: #888; font-size: 18px; padding: 50px;'>" +
                        "🎥 Jam Session Ready!<br>" +
                        "• Waiting for musicians to join...<br>" +
                        "• Anyone in this room can connect<br>" +
                        "• Start playing when others join!" +
                        "</div></html>",
                JLabel.CENTER
        );
        waitingLabel.setForeground(Color.GRAY);
        waitingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        videoContainer.add(waitingLabel);
        videoContainer.revalidate();
        videoContainer.repaint();
    }

    private void connectToSignaling() {
        signalingClient = new WebRTCVideoClient(
                String.valueOf(userId),
                username,
                String.valueOf(roomId),
                this::handleUserEvent,
                this::handleSignalingError
        );
        signalingClient.connect();
    }

    private void handleUserEvent(String event) {
        String[] parts = event.split(":");
        String action = parts[0];
        String remoteUserId = parts[1];
        String remoteUsername = parts.length > 2 ? parts[2] : "User";

        // Check if this user is allowed (for private calls)
        boolean isAllowed = allowedUsers.isEmpty() || allowedUsers.contains(remoteUsername);

        if (!isAllowed) {
            System.out.println("Blocked unauthorized user: " + remoteUsername);
            return;
        }

        switch (action) {
            case "JOINED":
                addRemoteVideo(remoteUserId, remoteUsername);
                break;
            case "LEFT":
                removeRemoteVideo(remoteUserId);
                break;
            case "OFFER":
                simulateVideoStream(remoteUserId, remoteUsername);
                break;
        }
    }

    private void handleSignalingError(String error) {
        System.err.println("Signaling error: " + error);
    }

    private void addRemoteVideo(String remoteUserId, String remoteUsername) {
        if (!remoteVideos.containsKey(remoteUserId)) {
            SwingUtilities.invokeLater(() -> {
                // Remove waiting message if it's the first user
                if (remoteVideos.isEmpty() && videoContainer.getComponentCount() == 1) {
                    Component firstComp = videoContainer.getComponent(0);
                    if (firstComp instanceof JLabel) {
                        videoContainer.removeAll();
                        if (allowedUsers.isEmpty()) {
                            videoContainer.setLayout(new GridLayout(0, 2, 10, 10));
                        } else {
                            videoContainer.setLayout(new BorderLayout());
                        }
                    }
                }

                // Create remote video panel
                JPanel remoteVideoPanel = createRemoteVideoPanel(remoteUsername, remoteVideos.size() + 1);
                RemoteVideo remoteVideo = new RemoteVideo(remoteUserId, remoteUsername, remoteVideoPanel);
                remoteVideos.put(remoteUserId, remoteVideo);

                // Add to container based on layout
                if (allowedUsers.isEmpty() && remoteVideos.size() == 1) {
                    // First user in group call - use center
                    videoContainer.add(remoteVideoPanel);
                } else if (allowedUsers.isEmpty()) {
                    // Additional users in group call
                    videoContainer.add(remoteVideoPanel);
                } else {
                    // Private call - single user
                    videoContainer.add(remoteVideoPanel, BorderLayout.CENTER);
                }

                videoContainer.revalidate();
                videoContainer.repaint();

                System.out.println("Now showing video from: " + remoteUsername);
                System.out.println("Total users in call: " + remoteVideos.size());

                // Update call status
                updateCallStatus();

                // Simulate video stream
                simulateVideoStream(remoteUserId, remoteUsername);
            });
        }
    }

    private void removeRemoteVideo(String remoteUserId) {
        if (remoteVideos.containsKey(remoteUserId)) {
            SwingUtilities.invokeLater(() -> {
                RemoteVideo remoteVideo = remoteVideos.remove(remoteUserId);
                videoContainer.remove(remoteVideo.getVideoPanel());

                // If no users left, show waiting message
                if (remoteVideos.isEmpty()) {
                    videoContainer.removeAll();
                    showWaitingForUsers();
                } else {
                    videoContainer.revalidate();
                    videoContainer.repaint();
                }

                System.out.println("Remote user disconnected: " + remoteVideo.getUsername());
                System.out.println("Remaining users in call: " + remoteVideos.size());

                // Update call status
                updateCallStatus();
            });
        }
    }

    private void updateCallStatus() {
        if (!remoteVideos.isEmpty()) {
            System.out.println("=== ACTIVE JAM SESSION ===");
            System.out.println("Connected musicians: " + remoteVideos.size());
            for (RemoteVideo video : remoteVideos.values()) {
                System.out.println("  • " + video.getUsername());
            }
            System.out.println("==========================");
        }
    }

    private void simulateVideoStream(String remoteUserId, String remoteUsername) {
        RemoteVideo remoteVideo = remoteVideos.get(remoteUserId);
        if (remoteVideo != null) {
            remoteVideo.startVideoSimulation();
        }
    }

    private JPanel createRemoteVideoPanel(String remoteUsername, int userNumber) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0, 0, 0));

        // Different border colors for different users
        Color[] borderColors = {Color.GREEN, Color.BLUE, Color.ORANGE, Color.MAGENTA, Color.CYAN};
        Color borderColor = borderColors[(userNumber - 1) % borderColors.length];
        panel.setBorder(BorderFactory.createLineBorder(borderColor, 3));

        // Main video content
        JPanel videoContent = new JPanel(new BorderLayout());
        videoContent.setBackground(new Color(10, 10, 10));

        // Simulated video feed
        String instrument = getRandomInstrument();
        JLabel videoFeed = new JLabel(
                "<html><div style='text-align: center; color: white; font-size: " +
                        (allowedUsers.isEmpty() ? "18" : "24") + "px; padding: 20px;'>" +
                        "🎥 " + remoteUsername + "<br>" +
                        "<div style='font-size: 16px; color: #4CAF50; margin: 10px 0;'>" +
                        "● LIVE • " + instrument + "<br>" +
                        "</div>" +
                        "<div style='font-size: 14px; color: #ccc;'>" +
                        "🔊 " + (70 + new Random().nextInt(25)) + "% • " +
                        "📹 720p • " +
                        "Ping: " + (40 + new Random().nextInt(30)) + "ms" +
                        "</div></html>",
                JLabel.CENTER
        );
        videoFeed.setForeground(Color.WHITE);
        videoFeed.setFont(new Font("Segoe UI", Font.BOLD, 14));

        videoContent.add(videoFeed, BorderLayout.CENTER);
        panel.add(videoContent, BorderLayout.CENTER);

        return panel;
    }

    private String getRandomInstrument() {
        String[] instruments = {"🎸 Guitar", "🥁 Drums", "🎹 Keyboard", "🎤 Vocals", "🎻 Violin", "🎷 Saxophone", "🎺 Trumpet"};
        return instruments[new Random().nextInt(instruments.length)];
    }

    public void endCall() {
        System.out.println("Ending video call with " + remoteVideos.size() + " users");
        isActive = false;

        if (videoTimer != null) {
            videoTimer.stop();
        }

        if (signalingClient != null) {
            signalingClient.disconnect();
        }

        remoteVideos.clear();

        if (videoContainer != null) {
            videoContainer.removeAll();
            videoContainer.revalidate();
            videoContainer.repaint();
        }
    }

    public JComponent getVideoComponent() {
        return videoContainer;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getConnectedUserCount() {
        return remoteVideos.size();
    }

    // Inner class to manage remote videos
    private class RemoteVideo {
        private String userId;
        private String username;
        private JPanel videoPanel;
        private javax.swing.Timer simulationTimer; // Explicit Swing Timer
        private Random random = new Random();

        public RemoteVideo(String userId, String username, JPanel videoPanel) {
            this.userId = userId;
            this.username = username;
            this.videoPanel = videoPanel;
        }

        public void startVideoSimulation() {
            // Simulate video activity
            simulationTimer = new javax.swing.Timer(4000, e -> {
                if (isActive) {
                    updateVideoStatus();
                }
            });
            simulationTimer.start();
        }

        private void updateVideoStatus() {
            SwingUtilities.invokeLater(() -> {
                Component videoContent = videoPanel.getComponent(0);
                if (videoContent instanceof JPanel) {
                    Component centerComp = ((JPanel) videoContent).getComponent(0);
                    if (centerComp instanceof JLabel) {
                        JLabel videoLabel = (JLabel) centerComp;

                        String instrument = getRandomInstrument();
                        int audioLevel = random.nextInt(25) + 70;
                        int latency = random.nextInt(30) + 40;

                        String newText = "<html><div style='text-align: center; color: white; font-size: " +
                                (allowedUsers.isEmpty() ? "18" : "24") + "px; padding: 20px;'>" +
                                "🎥 " + username + "<br>" +
                                "<div style='font-size: 16px; color: #4CAF50; margin: 10px 0;'>" +
                                "● LIVE • " + instrument + "<br>" +
                                "</div>" +
                                "<div style='font-size: 14px; color: #ccc;'>" +
                                "🔊 " + audioLevel + "% • " +
                                "📹 720p • " +
                                "Ping: " + latency + "ms" +
                                "</div></html>";

                        videoLabel.setText(newText);
                    }
                }
            });
        }

        public JPanel getVideoPanel() { return videoPanel; }
        public String getUsername() { return username; }
    }
}