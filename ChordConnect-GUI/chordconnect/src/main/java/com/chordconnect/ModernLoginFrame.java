package com.chordconnect;

import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ModernLoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public ModernLoginFrame() {
        setTitle("ChordConnect - Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1280, 720));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BACKGROUND);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(UITheme.BACKGROUND);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(80, 600, 80, 600));

        JLabel title = new JLabel("Welcome Back", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));

        usernameField = new JTextField("Username");
        passwordField = new JPasswordField("Password");

        styleField(usernameField);
        stylePasswordField(passwordField);

        addPlaceholderBehavior(usernameField, "Username");
        addPlaceholderBehavior(passwordField, "Password");

        JButton loginBtn = createButton("Login", UITheme.ACCENT);
        JButton registerBtn = createButton("Register", Color.GRAY);

        loginBtn.addActionListener(e -> login());
        registerBtn.addActionListener(e -> {
            dispose();
            new ModernRegisterFrame();
        });

        centerPanel.add(title);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(usernameField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(passwordField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(loginBtn);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(registerBtn);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);

        setVisible(true);
    }

    private void styleField(JTextField field) {
        field.setMaximumSize(new Dimension(400, 40));
        field.setFont(UITheme.FIELD_FONT);
        field.setBackground(UITheme.PANEL);
        field.setForeground(Color.GRAY);
        field.setCaretColor(UITheme.TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.ACCENT, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    private void stylePasswordField(JPasswordField field) {
        field.setMaximumSize(new Dimension(400, 40));
        field.setFont(UITheme.FIELD_FONT);
        field.setBackground(UITheme.PANEL);
        field.setForeground(Color.GRAY);
        field.setEchoChar((char) 0);
        field.setCaretColor(UITheme.TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.ACCENT, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    private void addPlaceholderBehavior(JTextField field, String placeholder) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(UITheme.TEXT);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void addPlaceholderBehavior(JPasswordField field, String placeholder) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String text = new String(field.getPassword());
                if (text.equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('•');
                    field.setForeground(UITheme.TEXT);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String text = new String(field.getPassword());
                if (text.isEmpty()) {
                    field.setText(placeholder);
                    field.setEchoChar((char) 0);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(UITheme.FIELD_FONT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.equals("Username") || password.equals("Password") ||
                username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password!");
            return;
        }

        // Show loading
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<JSONObject, Void>() {
            @Override
            protected JSONObject doInBackground() throws Exception {
                return ApiClient.login(username, password);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    JSONObject response = get();

                    // FLEXIBLE: Check for any success indicator
                    boolean success = false;
                    String successField = "";

                    if (response.has("success")) {
                        success = response.getBoolean("success");
                        successField = "success";
                    } else if (response.has("status") && "success".equals(response.getString("status"))) {
                        success = true;
                        successField = "status";
                    } else if (response.has("token")) {
                        success = true;
                        successField = "token";
                    }

                    System.out.println("Using success field: " + successField + " = " + success);

                    if (success) {
                        // Login successful
                        int userId = 0;
                        String responseUsername = username;

                        // Extract from data object (same as before)
                        if (response.has("data")) {
                            JSONObject data = response.getJSONObject("data");
                            if (data.has("userID")) {
                                userId = data.getInt("userID");
                            }
                            if (data.has("username")) {
                                responseUsername = data.getString("username");
                            }
                        }

                        // Store final variables
                        final int finalUserId = userId;
                        final String finalUsername = responseUsername;

                        // TEMPORARY: Skip preferences check and go directly to dashboard
                        dispose();
                        new DashboardFrame(finalUserId, finalUsername);

    /* ORIGINAL CODE (commented out):
    // Check if user has preferences
    JSONObject preferencesResponse = ApiClient.getPreferences(finalUserId);

    dispose();
    if (preferencesResponse.has("instruments") &&
            !preferencesResponse.getString("instruments").isEmpty()) {
        // User has preferences, go to dashboard
        new DashboardFrame(finalUserId, finalUsername);
    } else {
        // First-time user, show preferences
        new UserPreferencesFrame(finalUserId, finalUsername, () -> {
            new DashboardFrame(finalUserId, finalUsername);
        });
    }
    */

                    } else {
                        // Login failed
                        String errorMessage = "Unknown error";
                        if (response.has("message")) {
                            errorMessage = response.getString("message");
                        } else if (response.has("error")) {
                            errorMessage = response.getString("error");
                        }
                        JOptionPane.showMessageDialog(ModernLoginFrame.this,
                                "Login failed: " + errorMessage);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ModernLoginFrame.this,
                            "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }
}