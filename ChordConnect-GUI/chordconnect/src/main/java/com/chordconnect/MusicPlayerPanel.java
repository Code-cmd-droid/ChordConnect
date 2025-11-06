package com.chordconnect;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class MusicPlayerPanel extends JPanel {
    private JButton playPauseBtn, nextBtn, prevBtn;
    private JProgressBar progressBar;
    private JSlider volumeSlider;
    private JLabel songLabel;
    private Clip clip;
    private FloatControl volumeControl;

    private List<File> playlist = new ArrayList<>();
    private int currentTrack = 0;
    private boolean isPlaying = false;

    public MusicPlayerPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 20));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // --- Left: Song info ---
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        JLabel albumArt = new JLabel("Music");
        albumArt.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        albumArt.setForeground(Color.WHITE);
        infoPanel.add(albumArt, BorderLayout.WEST);

        songLabel = new JLabel("No track playing");
        songLabel.setForeground(Color.WHITE);
        songLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        infoPanel.add(songLabel, BorderLayout.CENTER);

        add(infoPanel, BorderLayout.WEST);

        // --- Center: Controls ---
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
        controlsPanel.setOpaque(false);

        prevBtn = makeButton("Previous");
        playPauseBtn = makeButton("Play");
        nextBtn = makeButton("Next");

        controlsPanel.add(prevBtn);
        controlsPanel.add(playPauseBtn);
        controlsPanel.add(nextBtn);

        add(controlsPanel, BorderLayout.CENTER);

        // --- Bottom: Progress bar ---
        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(400, 8));
        progressBar.setForeground(new Color(88, 101, 242));
        progressBar.setBackground(new Color(40, 40, 40));
        add(progressBar, BorderLayout.SOUTH);

        // --- Right: Volume control ---
        volumeSlider = new JSlider(0, 100, 70);
        volumeSlider.setOpaque(false);
        add(volumeSlider, BorderLayout.EAST);

        // --- Actions ---
        playPauseBtn.addActionListener(e -> togglePlayPause());
        nextBtn.addActionListener(e -> nextTrack());
        prevBtn.addActionListener(e -> prevTrack());

        // Load demo playlist
        loadDemoPlaylist();
    }

    private JButton makeButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(60, 60, 60));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Add hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(UITheme.ACCENT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(60, 60, 60));
            }
        });

        return btn;
    }

    private void loadDemoPlaylist() {
        // Demo files - you can replace these with actual file paths
        try {
            playlist.add(new File("demo1.wav"));
            playlist.add(new File("demo2.wav"));
        } catch (Exception e) {
            songLabel.setText("No audio files found");
        }
    }

    private void playTrack(int index) {
        if (playlist.isEmpty() || index >= playlist.size()) {
            songLabel.setText("No tracks available");
            return;
        }

        try {
            if (clip != null && clip.isRunning()) clip.stop();
            File file = playlist.get(index);

            // Check if file exists
            if (!file.exists()) {
                songLabel.setText("File not found: " + file.getName());
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            setVolume(volumeSlider.getValue());
            clip.start();
            isPlaying = true;
            playPauseBtn.setText("Pause");
            songLabel.setText("Playing: " + file.getName());

            // Progress bar updater thread
            new Thread(() -> {
                while (clip != null && clip.isRunning()) {
                    long position = clip.getMicrosecondPosition();
                    long length = clip.getMicrosecondLength();
                    if (length > 0) {
                        int progress = (int) ((position * 100) / length);
                        SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                    }
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                }
            }).start();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error playing track: " + e.getMessage());
            songLabel.setText("Error playing track");
        }
    }

    private void togglePlayPause() {
        if (playlist.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tracks in playlist");
            return;
        }

        if (clip == null) {
            playTrack(currentTrack);
        } else if (isPlaying) {
            clip.stop();
            playPauseBtn.setText("Play");
            isPlaying = false;
        } else {
            clip.start();
            playPauseBtn.setText("Pause");
            isPlaying = true;
        }
    }

    private void nextTrack() {
        if (playlist.isEmpty()) return;
        currentTrack = (currentTrack + 1) % playlist.size();
        playTrack(currentTrack);
    }

    private void prevTrack() {
        if (playlist.isEmpty()) return;
        currentTrack = (currentTrack - 1 + playlist.size()) % playlist.size();
        playTrack(currentTrack);
    }

    private void setVolume(int value) {
        if (volumeControl != null) {
            float range = volumeControl.getMaximum() - volumeControl.getMinimum();
            float gain = (range * value / 100f) + volumeControl.getMinimum();
            volumeControl.setValue(gain);
        }
    }
}