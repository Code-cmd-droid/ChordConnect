package com.chordconnect;

import javax.swing.*;
import java.awt.*;

public class VideoPanel extends JPanel {
    private String participantId;
    private String participantName;
    private JLabel videoLabel;
    private JLabel nameLabel;
    private JLabel statusLabel;

    public VideoPanel(String participantId, String participantName) {
        this.participantId = participantId;
        this.participantName = participantName;

        setLayout(new BorderLayout());
        setBackground(new Color(60, 64, 67));
        setBorder(BorderFactory.createLineBorder(new Color(95, 99, 104), 2));

        // Video area placeholder
        videoLabel = new JLabel("", JLabel.CENTER);
        videoLabel.setBackground(Color.BLACK);
        videoLabel.setOpaque(true);
        videoLabel.setPreferredSize(new Dimension(640, 480));

        // Add some visual indication
        JPanel placeholder = new JPanel(new BorderLayout());
        placeholder.setBackground(Color.BLACK);

        JLabel placeholderText = new JLabel("Video Stream", JLabel.CENTER);
        placeholderText.setForeground(Color.WHITE);
        placeholderText.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        placeholder.add(placeholderText, BorderLayout.CENTER);

        videoLabel.add(placeholder);
        add(videoLabel, BorderLayout.CENTER);

        // Participant info overlay
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(0, 0, 0, 180)); // Semi-transparent
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        nameLabel = new JLabel(participantName);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        statusLabel = new JLabel("● Connected");
        statusLabel.setForeground(Color.GREEN);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        infoPanel.add(nameLabel, BorderLayout.WEST);
        infoPanel.add(statusLabel, BorderLayout.EAST);

        add(infoPanel, BorderLayout.NORTH);
    }

    public String getParticipantId() { return participantId; }
    public String getParticipantName() { return participantName; }

    public void setStatus(String status, Color color) {
        statusLabel.setText("● " + status);
        statusLabel.setForeground(color);
    }

    public void setVideoAvailable(boolean available) {
        if (available) {
            videoLabel.setBackground(Color.BLACK);
        } else {
            videoLabel.setBackground(Color.DARK_GRAY);
        }
    }
}