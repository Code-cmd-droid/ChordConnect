package com.chordconnect;

import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ModernRegisterFrame extends JFrame {
    private JTextField usernameField, emailField;
    private JPasswordField passwordField;

    public ModernRegisterFrame() {
        setTitle("ChordConnect - Register");
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

        JLabel title = new JLabel("Create Your Account", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));

        usernameField = new JTextField("Username");
        emailField = new JTextField("Email");
        passwordField = new JPasswordField("Password");

        styleField(usernameField);
        styleField(emailField);
        stylePasswordField(passwordField);

        addPlaceholderBehavior(usernameField, "Username");
        addPlaceholderBehavior(emailField, "Email");
        addPlaceholderBehavior(passwordField, "Password");

        JButton registerBtn = createButton("Register", UITheme.ACCENT);
        JButton backBtn = createButton("Back", Color.GRAY);

        registerBtn.addActionListener(e -> register());
        backBtn.addActionListener(e -> {
            dispose();
            new ModernLoginFrame();
        });

        centerPanel.add(title);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(usernameField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(emailField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(passwordField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(registerBtn);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(backBtn);

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

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String email = emailField.getText().trim();

        if (username.equals("Username") || password.equals("Password") || email.equals("Email") ||
                username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill out all fields!");
            return;
        }

        // Show loading
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<JSONObject, Void>() {
            @Override
            protected JSONObject doInBackground() throws Exception {
                // FIXED: Pass email parameter to register
                return ApiClient.register(username, password, email);
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
                        // Registration successful - EXTRACT FROM data OBJECT
                        int userId = 0;
                        String responseUsername = username; // fallback to entered username

                        // Check if response has nested data object
                        if (response.has("data")) {
                            JSONObject data = response.getJSONObject("data");

                            // Try different possible user ID fields in data object
                            if (data.has("userID")) {
                                userId = data.getInt("userID");
                                System.out.println("Found userID in data: " + userId);
                            } else if (data.has("userId")) {
                                userId = data.getInt("userId");
                                System.out.println("Found userId in data: " + userId);
                            } else if (data.has("id")) {
                                userId = data.getInt("id");
                                System.out.println("Found id in data: " + userId);
                            }

                            // Try different possible username fields in data object
                            if (data.has("username")) {
                                responseUsername = data.getString("username");
                                System.out.println("Found username in data: " + responseUsername);
                            }
                        } else {
                            // Fallback: try direct fields (old format)
                            if (response.has("userId")) {
                                userId = response.getInt("userId");
                            } else if (response.has("userID")) {
                                userId = response.getInt("userID");
                            } else if (response.has("id")) {
                                userId = response.getInt("id");
                            }

                            if (response.has("username")) {
                                responseUsername = response.getString("username");
                            }
                        }

                        if (userId == 0) {
                            // If no user ID found, show error
                            JOptionPane.showMessageDialog(ModernRegisterFrame.this,
                                    "Registration successful but user ID not found in response");
                            return;
                        }

                        // Store final variables for use in lambda
                        final int finalUserId = userId;
                        final String finalUsername = responseUsername;

                        JOptionPane.showMessageDialog(ModernRegisterFrame.this,
                                "Registration Successful!");

                        dispose();
                        // Show preferences for new user
                        new UserPreferencesFrame(finalUserId, finalUsername, () -> {
                            new DashboardFrame(finalUserId, finalUsername);
                        });
                    } else {
                        // Registration failed
                        String errorMessage = "Unknown error";
                        if (response.has("message")) {
                            errorMessage = response.getString("message");
                        } else if (response.has("error")) {
                            errorMessage = response.getString("error");
                        }
                        JOptionPane.showMessageDialog(ModernRegisterFrame.this,
                                "Registration failed: " + errorMessage);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ModernRegisterFrame.this,
                            "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }
}