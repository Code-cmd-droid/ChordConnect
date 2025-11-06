package com.chordconnect;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class VideoCallFrame extends JFrame {
    private JPanel videoGridPanel;
    private JPanel controlPanel;
    private List<VideoPanel> videoPanels;
    private boolean isMuted = false;
    private boolean isVideoOff = false;
    private boolean isScreenSharing = false;

    public VideoCallFrame(List<String> participants) {
        setTitle("ChordConnect - Video Call");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(32, 33, 36)); // Google Meet dark theme

        videoPanels = new ArrayList<>();

        // Header with meeting info
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Main video grid
        add(createVideoGrid(participants), BorderLayout.CENTER);

        // Controls at bottom
        add(createControlPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(32, 33, 36));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel meetingInfo = new JLabel("ChordConnect Jam Session - 4 participants");
        meetingInfo.setForeground(Color.WHITE);
        meetingInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        header.add(meetingInfo, BorderLayout.WEST);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightControls.setOpaque(false);

        JButton securityBtn = createIconButton("🔒", "Security");
        JButton participantsBtn = createIconButton("👥", "Participants (4)");
        JButton chatBtn = createIconButton("💬", "Chat");
        JButton menuBtn = createIconButton("⋯", "More options");

        rightControls.add(securityBtn);
        rightControls.add(participantsBtn);
        rightControls.add(chatBtn);
        rightControls.add(menuBtn);

        header.add(rightControls, BorderLayout.EAST);
        return header;
    }

    private JPanel createVideoGrid(List<String> participants) {
        videoGridPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // Responsive grid
        videoGridPanel.setBackground(new Color(32, 33, 36));
        videoGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        for (String participant : participants) {
            VideoPanel videoPanel = new VideoPanel(participant);
            videoPanels.add(videoPanel);
            videoGridPanel.add(videoPanel);
        }

        // Add empty panels if needed for grid layout
        while (videoPanels.size() % 2 != 0) {
            videoGridPanel.add(new VideoPanel(""));
        }

        return videoGridPanel;
    }

    private JPanel createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.setBackground(new Color(32, 33, 36));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));

        // Left spacer
        controlPanel.add(Box.createHorizontalGlue());

        // Control buttons
        JButton micBtn = createControlButton("🎤", "Mute", new Color(66, 133, 244));
        JButton videoBtn = createControlButton("📹", "Stop Video", new Color(66, 133, 244));
        JButton shareBtn = createControlButton("📺", "Present", new Color(251, 188, 5));
        JButton participantsBtn = createControlButton("👥", "People", Color.GRAY);
        JButton chatBtn = createControlButton("💬", "Chat", Color.GRAY);

        JButton leaveBtn = createControlButton("📞", "Leave", new Color(234, 67, 53));

        controlPanel.add(micBtn);
        controlPanel.add(Box.createHorizontalStrut(15));
        controlPanel.add(videoBtn);
        controlPanel.add(Box.createHorizontalStrut(15));
        controlPanel.add(shareBtn);
        controlPanel.add(Box.createHorizontalStrut(15));
        controlPanel.add(participantsBtn);
        controlPanel.add(Box.createHorizontalStrut(15));
        controlPanel.add(chatBtn);
        controlPanel.add(Box.createHorizontalStrut(30));
        controlPanel.add(leaveBtn);

        // Right spacer
        controlPanel.add(Box.createHorizontalGlue());

        return controlPanel;
    }

    private JButton createControlButton(String icon, String tooltip, Color bgColor) {
        JButton button = new JButton(icon);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        button.setToolTipText(tooltip);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> handleControlAction(tooltip, button));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private JButton createIconButton(String icon, String tooltip) {
        JButton button = new JButton(icon);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setBackground(new Color(60, 64, 67));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setToolTipText(tooltip);
        return button;
    }

    private void handleControlAction(String action, JButton button) {
        switch (action) {
            case "Mute":
                isMuted = !isMuted;
                button.setText(isMuted ? "🎤❌" : "🎤");
                button.setToolTipText(isMuted ? "Unmute" : "Mute");
                break;
            case "Stop Video":
                isVideoOff = !isVideoOff;
                button.setText(isVideoOff ? "📹❌" : "📹");
                button.setToolTipText(isVideoOff ? "Start Video" : "Stop Video");
                break;
            case "Present":
                isScreenSharing = !isScreenSharing;
                button.setText(isScreenSharing ? "📺✅" : "📺");
                button.setToolTipText(isScreenSharing ? "Stop Presenting" : "Present");
                break;
            case "Leave":
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Leave this jam session?", "Leave Session",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dispose();
                }
                break;
        }
    }
}

class VideoPanel extends JPanel {
    private String participantName;
    private JLabel videoLabel;
    private JLabel nameLabel;

    public VideoPanel(String participantName) {
        this.participantName = participantName;
        setLayout(new BorderLayout());
        setBackground(new Color(60, 64, 67));
        setBorder(BorderFactory.createLineBorder(new Color(95, 99, 104), 2));

        // Video area (placeholder for actual video stream)
        videoLabel = new JLabel("", JLabel.CENTER);
        videoLabel.setBackground(Color.BLACK);
        videoLabel.setOpaque(true);
        videoLabel.setPreferredSize(new Dimension(640, 480));
        add(videoLabel, BorderLayout.CENTER);

        // Participant name overlay
        if (!participantName.isEmpty()) {
            nameLabel = new JLabel(participantName);
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            nameLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            namePanel.setBackground(new Color(0, 0, 0, 150)); // Semi-transparent
            namePanel.add(nameLabel);
            add(namePanel, BorderLayout.NORTH);
        }
    }
}