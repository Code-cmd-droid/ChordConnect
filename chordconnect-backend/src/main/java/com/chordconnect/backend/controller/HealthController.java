package com.chordconnect.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "ChordConnect Backend");
        health.put("timestamp", System.currentTimeMillis());
        health.put("websocket", "Enabled");
        return health;
    }

    @GetMapping("/api/status")
    public Map<String, String> status() {
        Map<String, String> status = new HashMap<>();
        status.put("message", "Server is running");
        status.put("status", "OK");
        return status;
    }
}