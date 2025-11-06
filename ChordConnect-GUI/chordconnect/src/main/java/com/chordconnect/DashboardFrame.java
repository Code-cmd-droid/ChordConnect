package com.chordconnect;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class DashboardFrame extends JFrame {
    private int userId;
    private String username;

    public DashboardFrame(int userId, String username) {
        this.userId = userId;
        this.username = username;

        setTitle("ChordConnect - Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1280, 720));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());

        // ---------- HEADER ----------
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 25));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 80));

        JLabel appName = new JLabel("ChordConnect");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 30));
        appName.setForeground(Color.WHITE);
        appName.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 0));
        headerPanel.add(appName, BorderLayout.WEST);

        JButton profileBtn = new JButton("Profile");
        profileBtn.setBackground(UITheme.ACCENT);
        profileBtn.setForeground(Color.WHITE);
        profileBtn.setFont(UITheme.FIELD_FONT);
        profileBtn.setFocusPainted(false);
        profileBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        profileBtn.addActionListener(e -> showProfileDialog());
        headerPanel.add(profileBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ---------- MAIN AREA WITH BACKGROUND IMAGE ----------
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Create a dark overlay for better text readability
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(UITheme.BACKGROUND);

        // ---------- CENTER CONTENT ----------
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);
        centerContent.setBorder(BorderFactory.createEmptyBorder(150, 0, 0, 0));

        // Start Jamming Button
        JButton startJammingBtn = new JButton("Start Jamming");
        startJammingBtn.setFont(new Font("Segoe UI", Font.BOLD, 28));
        startJammingBtn.setFocusPainted(false);
        startJammingBtn.setBackground(UITheme.ACCENT);
        startJammingBtn.setForeground(Color.WHITE);
        startJammingBtn.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        startJammingBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startJammingBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add hover effect
        startJammingBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                startJammingBtn.setBackground(UITheme.ACCENT.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                startJammingBtn.setBackground(UITheme.ACCENT);
            }
        });

        // Redirect to Chat Window
        startJammingBtn.addActionListener(e -> {
            dispose();
            new ChatWindow(userId, username);
        });

        // Welcome Label
        JLabel welcomeLabel = new JLabel("Welcome to ChordConnect, " + username + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to center content
        centerContent.add(startJammingBtn);
        centerContent.add(Box.createRigidArea(new Dimension(0, 30)));
        centerContent.add(welcomeLabel);

        // Add center content to main panel
        mainPanel.add(centerContent, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // ---------- FRIENDS LIST ----------
        JPanel friendsPanel = createListPanel("Friends List", fetchFriends(), "Message");
        JScrollPane friendsScroll = new JScrollPane(friendsPanel);
        friendsScroll.setPreferredSize(new Dimension(250, 0));
        friendsScroll.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(50, 50, 50)));
        friendsScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(friendsScroll, BorderLayout.EAST);

        // ---------- RECENT JAMMERS ----------
        JPanel jammersPanel = createListPanel("Recent Jammers", fetchRecentJammers(), "Join");
        JScrollPane jammersScroll = new JScrollPane(jammersPanel);
        jammersScroll.setPreferredSize(new Dimension(250, 0));
        jammersScroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(50, 50, 50)));
        jammersScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(jammersScroll, BorderLayout.WEST);

        // ---------- MUSIC PLAYER ----------
        MusicPlayerPanel playerPanel = new MusicPlayerPanel();
        add(playerPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // --- Create list panel for friends/jammers ---
    private JPanel createListPanel(String title, List<String> names, String actionLabel) {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(30, 30, 30));
        listPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel listTitle = new JLabel(title);
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        listTitle.setForeground(Color.WHITE);
        listTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        listPanel.add(listTitle);
        listPanel.add(Box.createVerticalStrut(15));

        if (names.isEmpty()) {
            JLabel emptyLabel = new JLabel("No entries yet!");
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(emptyLabel);
        } else {
            for (String name : names) {
                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(new Color(45, 45, 45));
                row.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

                JLabel nameLabel = new JLabel(name);
                nameLabel.setForeground(Color.WHITE);
                nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));

                JButton actionBtn = new JButton(actionLabel);
                actionBtn.setBackground(new Color(88, 101, 242));
                actionBtn.setForeground(Color.WHITE);
                actionBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                actionBtn.setFocusPainted(false);
                actionBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                // Make buttons functional
                if ("Message".equals(actionLabel)) {
                    actionBtn.addActionListener(e -> openChatWithUser(name));
                } else if ("Join".equals(actionLabel)) {
                    actionBtn.addActionListener(e -> joinJamSession(name));
                } else if ("Add".equals(actionLabel)) {
                    actionBtn.addActionListener(e -> addUserAsFriend(name));
                }

                row.add(nameLabel, BorderLayout.WEST);
                row.add(actionBtn, BorderLayout.EAST);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
                listPanel.add(row);
                listPanel.add(Box.createVerticalStrut(8));
            }
        }
        return listPanel;
    }

    // --- Fetch data from DB ---
    private List<String> fetchFriends() {
        List<String> friends = new ArrayList<>();
        try {
            JSONObject response = ApiClient.getFriendsList(userId);
            if (response.getBoolean("success")) {
                JSONArray friendsArray = response.getJSONArray("data");
                for (int i = 0; i < friendsArray.length(); i++) {
                    JSONObject friend = friendsArray.getJSONObject(i);
                    friends.add(friend.getString("username"));
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching friends: " + e.getMessage());
            // Fallback to demo data
            friends.add("MusicLover42");
            friends.add("GuitarHero");
        }
        return friends;
    }
    // --- Profile Dialog ---
    private void showProfileDialog() {
        JDialog dialog = new JDialog(this, "Your Profile", true);
        dialog.setSize(520, 620);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);

        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBackground(new Color(20, 20, 20));
        outerPanel.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 2));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(25, 25, 25));
        content.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Profile Overview", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(25));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(25, 25, 25));

        // For now, show basic info - this will be replaced with API call
        infoPanel.add(makeProfileRow("Username", username));
        infoPanel.add(makeProfileRow("User ID", String.valueOf(userId)));
        infoPanel.add(makeProfileRow("Instruments", "Not set"));
        infoPanel.add(makeProfileRow("Genres", "Not set"));

        content.add(infoPanel);
        content.add(Box.createVerticalStrut(35));

        JButton changeBtn = new JButton("Change Preferences");
        styleButton(changeBtn, new Color(88, 101, 242));
        changeBtn.addActionListener(e -> {
            dialog.dispose();
            dispose();
            new UserPreferencesFrame(userId, username, () -> {
                new DashboardFrame(userId, username);
            });
        });

        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn, new Color(200, 60, 60));
        logoutBtn.addActionListener(e -> {
            dialog.dispose();
            dispose();
            new ModernLoginFrame();
        });

        content.add(changeBtn);
        content.add(Box.createVerticalStrut(15));
        content.add(logoutBtn);

        JButton closeBtn = new JButton("X");
        closeBtn.setFocusPainted(false);
        closeBtn.setBackground(new Color(45, 45, 45));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(closeBtn, BorderLayout.EAST);

        outerPanel.add(topBar, BorderLayout.NORTH);
        outerPanel.add(content, BorderLayout.CENTER);

        dialog.setContentPane(outerPanel);
        dialog.setVisible(true);
    }

    private JPanel makeProfileRow(String labelText, String valueText) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(25, 25, 25));
        row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(180, 180, 180));

        JLabel value = new JLabel(valueText);
        value.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        value.setForeground(Color.WHITE);

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(bg.darker()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(bg); }
        });
    }
    // Update the openChatWithUser method:
    private void openChatWithUser(String username) {
        try {
            // Look up the user from backend
            JSONObject response = ApiClient.getUserByUsername(username);
            if (response.getBoolean("success")) {
                JSONObject userData = response.getJSONObject("data");
                int targetUserId = userData.getInt("id");
                String targetUsername = userData.getString("username");

                // Open chat with the actual user using NEW constructor
                ChatWindow chatWindow = new ChatWindow(userId, this.username, targetUserId, targetUsername);
                chatWindow.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "User '" + username + "' not found!\n" +
                                "Make sure they exist in the system.");
                // Fallback to demo chat
                ChatWindow chatWindow = new ChatWindow(userId, this.username);
                chatWindow.setVisible(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error finding user: " + e.getMessage() + "\n" +
                            "Opening demo chat instead.");
            // Fallback to demo chat
            ChatWindow chatWindow = new ChatWindow(userId, this.username);
            chatWindow.setVisible(true);
        }
    }

    // Update the joinJamSession method:
    private void joinJamSession(String username) {
        try {
            // Look up the user from backend
            JSONObject response = ApiClient.getUserByUsername(username);
            if (response.getBoolean("success")) {
                JSONObject userData = response.getJSONObject("data");
                int targetUserId = userData.getInt("id");
                String targetUsername = userData.getString("username");

                // Open chat with video call auto-start using NEW constructor
                ChatWindow chatWindow = new ChatWindow(userId, this.username, targetUserId, targetUsername);
                chatWindow.setVisible(true);

                // Auto-start video call
                SwingUtilities.invokeLater(() -> {
                    try {
                        Thread.sleep(1000);
                        chatWindow.startVideoCall();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                JOptionPane.showMessageDialog(this, "User not found: " + username);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void addUserAsFriend(String username) {
        // In a real app, you'd look up the user ID by username
        JOptionPane.showMessageDialog(this,
                "Friend request sent to " + username + "\n(Backend integration needed)");
    }
    private List<String> fetchRecentJammers() {
        List<String> jammers = new ArrayList<>();
        try {
            JSONObject response = ApiClient.getRecentJammers(userId); // Use userId instead of currentUserId
            if (response.getBoolean("success")) {
                JSONArray jammersArray = response.getJSONArray("data");
                for (int i = 0; i < jammersArray.length(); i++) {
                    JSONObject jammer = jammersArray.getJSONObject(i);
                    jammers.add(jammer.getString("username"));
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching recent jammers: " + e.getMessage());
            // Fallback to demo data
            jammers.add("DrummerPro");
            jammers.add("BassMaster");
            jammers.add("PianoWizard");
        }
        return jammers;
    }

}
