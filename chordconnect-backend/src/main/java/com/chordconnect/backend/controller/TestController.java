package com.chordconnect.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/test")
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Backend is running");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return response;
    }
    @GetMapping("/database-test")
    public ResponseEntity<?> testDatabase() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Try to fetch a user to test database connection
            List<User> users = userRepository.findAll();
            response.put("status", "Database connected successfully");
            response.put("userCount", users.size());
            response.put("users", users.stream()
                    .map(user -> Map.of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "email", user.getEmail()
                    ))
                    .collect(Collectors.toList()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "Database connection failed");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}