package com.chordconnect.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class VideoCallController {

    @GetMapping("/video-call")
    public String videoCall(
            @RequestParam String room,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            Model model) {

        if (userId == null) userId = System.currentTimeMillis() % 10000L;
        if (username == null || username.trim().isEmpty()) username = "User" + userId;

        model.addAttribute("roomId", room);
        model.addAttribute("userId", userId);
        model.addAttribute("username", username);

        System.out.println("🎥 Video call - Room: " + room + ", User: " + username);
        return "video-call";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/video-call?room=default&username=Guest";
    }

    @GetMapping("/websocket-test")
    public String websocketTest() {
        return "websocket-test";
    }

    @GetMapping("/health")
    public String health() {
        return "health";
    }
}