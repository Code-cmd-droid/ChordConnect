package com.chordconnect;

import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UserPreferencesFrame extends JFrame {
    private int userId;
    private String username;
    private int currentQuestion = 0;
    private JPanel questionPanel;
    private List<String> answers = new ArrayList<>();
    private Runnable onPreferencesSaved;

    private final String[] questions = {
            "What kind of instruments do you play?",
            "What kind of music are you into?",
            "What languages do you speak?",
            "What is your gender?",
            "What is your age?"
    };

    private final String[][] options = {
            {"Guitar", "Drums", "Keyboard", "Vocals", "Violin", "Bass", "Harmonica", "Flute"},
            {"Rock", "Pop", "Jazz", "Classical", "Hip Hop", "Electronic", "Metal", "Country"},
            {"English", "German", "French", "Japanese", "Korean", "Other"},
            {"Male", "Female", "Other"},
            {"Below 18", "18", "18+"}
    };

    public UserPreferencesFrame(int userId, String username, Runnable onPreferencesSaved) {
        this.userId = userId;
        this.username = username;
        this.onPreferencesSaved = onPreferencesSaved;

        setTitle("Set Preferences - ChordConnect");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(30, 30, 30));

        questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
        questionPanel.setBackground(new Color(30, 30, 30));
        questionPanel.setBorder(BorderFactory.createEmptyBorder(100, 400, 100, 400));

        showQuestion();
        add(questionPanel);
        setVisible(true);
    }

    private void showQuestion() {
        questionPanel.removeAll();

        JLabel questionLabel = new JLabel(questions[currentQuestion]);
        questionLabel.setForeground(Color.WHITE);
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        questionPanel.add(questionLabel);
        questionPanel.add(Box.createVerticalStrut(30));

        ButtonGroup singleGroup = new ButtonGroup();
        java.util.List<JToggleButton> buttons = new ArrayList<>();

        for (String opt : options[currentQuestion]) {
            JToggleButton btn = new JToggleButton(opt);
            btn.setFocusPainted(false);
            btn.setBackground(new Color(45, 45, 45));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(300, 50));

            // Add selection styling
            btn.addActionListener(e -> {
                if (btn.isSelected()) {
                    btn.setBackground(UITheme.ACCENT);
                } else {
                    btn.setBackground(new Color(45, 45, 45));
                }
            });

            if (currentQuestion >= 3) singleGroup.add(btn);

            questionPanel.add(Box.createVerticalStrut(10));
            questionPanel.add(btn);
            buttons.add(btn);
        }

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        JButton nextBtn = new JButton(currentQuestion == questions.length - 1 ? "Finish" : "Next →");
        styleButton(nextBtn, new Color(88, 101, 242));
        nextBtn.addActionListener(e -> {
            List<String> selected = new ArrayList<>();
            for (JToggleButton btn : buttons)
                if (btn.isSelected()) selected.add(btn.getText());

            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select at least one option.");
                return;
            }

            answers.add(String.join(", ", selected));

            if (currentQuestion < questions.length - 1) {
                currentQuestion++;
                showQuestion();
            } else {
                savePreferences();
            }
        });

        JButton cancelBtn = new JButton("Cancel");
        styleButton(cancelBtn, Color.GRAY);
        cancelBtn.addActionListener(e -> {
            dispose();
            new DashboardFrame(userId, username);
        });

        btnPanel.add(nextBtn);
        btnPanel.add(cancelBtn);

        questionPanel.add(Box.createVerticalStrut(40));
        questionPanel.add(btnPanel);
        questionPanel.revalidate();
        questionPanel.repaint();
    }

    private void savePreferences() {
        // Show loading
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // TEMPORARY FIX: Bypass the API call and just proceed
        // Remove this SwingWorker and replace with direct success
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Simulate API call delay
                Thread.sleep(1000);
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());

                // TEMPORARY: Just show success and proceed
                System.out.println("Preferences collected (not saved to backend):");
                for (int i = 0; i < answers.size(); i++) {
                    System.out.println(questions[i] + ": " + answers.get(i));
                }

                JOptionPane.showMessageDialog(UserPreferencesFrame.this,
                        "Preferences saved successfully! (Demo mode - not saved to backend)");
                dispose();
                if (onPreferencesSaved != null) onPreferencesSaved.run();
            }
        }.execute();

        /*
        // ORIGINAL CODE (commented out for now):
        new SwingWorker<JSONObject, Void>() {
            @Override
            protected JSONObject doInBackground() throws Exception {
                return ApiClient.savePreferences(
                        userId,
                        answers.size() > 0 ? answers.get(0) : "",
                        answers.size() > 1 ? answers.get(1) : "",
                        answers.size() > 2 ? answers.get(2) : "",
                        answers.size() > 3 ? answers.get(3) : "",
                        answers.size() > 4 ? answers.get(4) : ""
                );
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    JSONObject response = get();
                    // ... rest of original code
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(UserPreferencesFrame.this,
                            "Error saving preferences: " + ex.getMessage());
                }
            }
        }.execute();
        */
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        // Add hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });
    }
}