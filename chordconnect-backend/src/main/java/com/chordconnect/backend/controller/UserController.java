package com.chordconnect.backend.controller;

import com.chordconnect.backend.entity.User;
import com.chordconnect.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/preferences")
    public ResponseEntity<?> saveUserPreferences(@RequestBody Map<String, String> request) {
        try {
            Long userId = Long.parseLong(request.get("userId"));
            userService.saveUserPreferences(
                    userId,
                    request.get("instrument"),
                    request.get("skillLevel"),
                    request.get("favoriteGenres"),
                    request.get("practiceGoals"),
                    request.get("learningStyle")
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Preferences saved");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.badRequest().body(response);
        }
    }
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.findByUsername(username);
            if (user != null) {
                response.put("success", true);
                response.put("data", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail()
                ));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error finding user: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String q) {
        Map<String, Object> response = new HashMap<>();
        try {
            // For now, return demo data - implement real search later
            List<User> users = userService.searchUsers(q);
            List<Map<String, Object>> userList = users.stream()
                    .map(user -> Map.of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "email", user.getEmail()
                    ))
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("data", userList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error searching users: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}