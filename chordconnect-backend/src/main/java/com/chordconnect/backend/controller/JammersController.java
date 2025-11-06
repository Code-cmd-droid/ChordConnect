package com.chordconnect.backend.controller;

import com.chordconnect.backend.entity.User;
import com.chordconnect.backend.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jammers")
@CrossOrigin(origins = "http://localhost:3000")
public class JammersController {

    @Autowired
    private FriendshipService friendshipService;

    @GetMapping("/recent/{userId}")
    public ResponseEntity<?> getRecentJammers(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<User> jammers = friendshipService.getRecentJammers(userId);
            List<Map<String, Object>> jammersList = jammers.stream()
                    .filter(user -> !user.getId().equals(userId)) // Use getId() instead of getUserId()
                    .map(user -> {
                        Map<String, Object> jammerData = new HashMap<>();
                        jammerData.put("id", user.getId());  // Use getId() instead of getUserId()
                        jammerData.put("username", user.getUsername());
                        jammerData.put("email", user.getEmail());
                        return jammerData;
                    })
                    .collect(Collectors.toList());

            response.put("status", "success");
            response.put("data", jammersList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to get recent jammers: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}