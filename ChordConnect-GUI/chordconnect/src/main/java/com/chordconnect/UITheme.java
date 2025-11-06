package com.chordconnect;

import java.awt.Color;
import java.awt.Font;

public class UITheme {
    public static final Color BACKGROUND = new Color(18, 18, 18);
    public static final Color PANEL = new Color(30, 30, 30);
    public static final Color ACCENT = new Color(0, 150, 255);
    public static final Color TEXT = new Color(240, 240, 240);

    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 36);
    public static final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 16);

    // Network Configuration - MAKE THESE CONFIGURABLE
    private static String SERVER_HOST = "localhost"; // Change this to your IP

    public static void setServerHost(String host) {
        SERVER_HOST = host;
    }

    public static String getServerHost() {
        return SERVER_HOST;
    }

    // WebSocket Configuration
    public static String getWsBaseUrl() {
        return "ws://" + SERVER_HOST + ":8081";
    }

    public static String getBaseUrl() {
        return "http://" + SERVER_HOST + ":8080/api";
    }

    public static final String CHAT_WS_ENDPOINT = "/ws/chat";
    public static final String VIDEO_WS_ENDPOINT = "/ws/video";
}