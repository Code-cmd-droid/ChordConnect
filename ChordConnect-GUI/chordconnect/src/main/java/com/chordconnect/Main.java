package com.chordconnect;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Enable anti-aliasing for text
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Choose an emoji-capable font based on OS
        String emojiFont = getEmojiFont();

        Font uiFont = new Font(emojiFont, Font.PLAIN, 16);

        // Apply globally
        UIManager.put("Label.font", uiFont);
        UIManager.put("Button.font", uiFont);
        UIManager.put("TextField.font", uiFont);
        UIManager.put("TextArea.font", uiFont);
        UIManager.put("ComboBox.font", uiFont);
        UIManager.put("ToggleButton.font", uiFont);

        SwingUtilities.invokeLater(() -> new ModernLoginFrame());
    }

    private static String getEmojiFont() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return "Segoe UI Emoji";
        if (os.contains("mac")) return "Apple Color Emoji";
        return "Noto Color Emoji";
    }
}