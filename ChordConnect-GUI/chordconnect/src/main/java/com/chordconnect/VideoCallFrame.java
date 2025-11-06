package com.chordconnect;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List; // ✅ Add this import
import java.util.ArrayList; // ✅ Add this import
import org.json.JSONObject;
import org.json.JSONArray;

public class VideoCallFrame extends JFrame {
    private int userId;
    private String username;
    private int roomId;

    // Video components
    private JPanel videoPanel;
    private JLabel localVideoLabel;
    private JLabel remoteVideoLabel;

    // Controls
    private JButton startCallBtn;
    private JButton endCallBtn;
    private JButton backBtn;

    // WebSocket client for video signaling
    private WebRTCVideoClient videoClient;

    public VideoCallFrame(int userId, String username, int roomId) {
        this.userId = userId;
        this.username = username;
        this.roomId = roomId;

        initializeUI();
        setupEventListeners();
    }

    private void initializeUI() {
        setTitle("ChordConnect - Video Call");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 25));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("Video Call - Room #" + roomId);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        backBtn = new JButton("← Back to Chat");
        backBtn.setBackground(new Color(76, 175, 80));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(backBtn, BorderLayout.EAST);

        // Video panel
        videoPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        videoPanel.setBackground(new Color(15, 15, 15));
        videoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Local video
        JPanel localVideoPanel = createVideoPanel("You");
        videoPanel.add(localVideoPanel);

        // Remote video
        JPanel remoteVideoPanel = createVideoPanel("Remote User");
        videoPanel.add(remoteVideoPanel);

        // Controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlsPanel.setBackground(new Color(25, 25, 25));

        startCallBtn = new JButton("🎥 Start Video Call");
        endCallBtn = new JButton("❌ End Call");

        styleVideoButton(startCallBtn, new Color(76, 175, 80));
        styleVideoButton(endCallBtn, new Color(244, 67, 54));

        endCallBtn.setEnabled(false);

        controlsPanel.add(startCallBtn);
        controlsPanel.add(endCallBtn);

        // Add all components
        add(headerPanel, BorderLayout.NORTH);
        add(videoPanel, BorderLayout.CENTER);
        add(controlsPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createVideoPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70), 2));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel videoLabel = new JLabel(
                "<html><div style='text-align: center; color: #888; font-size: 16px;'>" +
                        "Video will appear here<br>when call starts" +
                        "</div></html>",
                JLabel.CENTER
        );
        videoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        videoLabel.setForeground(Color.GRAY);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(videoLabel, BorderLayout.CENTER);

        return panel;
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
        startCallBtn.addActionListener(e -> startVideoCall());
        endCallBtn.addActionListener(e -> endVideoCall());
        backBtn.addActionListener(e -> dispose());
    }

    private void startVideoCall() {
        try {
            // Open browser for video call
            openVideoCallInBrowser();

            startCallBtn.setEnabled(false);
            endCallBtn.setEnabled(true);

            // Update video panel to show browser was opened
            updateVideoPanelStatus("Video call opened in browser");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to start video call: " + e.getMessage(),
                    "Video Call Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openVideoCallInBrowser() {
        try {
            String serverUrl = "http://localhost:8080";
            String videoCallUrl = String.format(
                    "%s/video-call?room=%d&userId=%d&username=%s",
                    serverUrl, roomId, userId, java.net.URLEncoder.encode(username, "UTF-8")
            );

            java.awt.Desktop.getDesktop().browse(new java.net.URI(videoCallUrl));

        } catch (Exception e) {
            throw new RuntimeException("Failed to open browser: " + e.getMessage(), e);
        }
    }

    private void updateVideoPanelStatus(String status) {
        Component[] comps = videoPanel.getComponents();
        if (comps.length > 1) {
            JPanel remotePanel = (JPanel) comps[1];
            remotePanel.removeAll();

            JLabel statusLabel = new JLabel(
                    "<html><div style='text-align: center; color: #4CAF50; font-size: 14px;'>" +
                            status +
                            "</div></html>",
                    JLabel.CENTER
            );
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            remotePanel.add(statusLabel, BorderLayout.CENTER);
            remotePanel.revalidate();
            remotePanel.repaint();
        }
    }

    private void endVideoCall() {
        startCallBtn.setEnabled(true);
        endCallBtn.setEnabled(false);

        updateVideoPanelStatus("Call ended - close browser tab");

        JOptionPane.showMessageDialog(this,
                "Video call is running in browser.\nClose the browser tab to end the call.",
                "Video Call Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void dispose() {
        if (videoClient != null) {
            // Clean up resources if needed
        }
        super.dispose();
    }
}