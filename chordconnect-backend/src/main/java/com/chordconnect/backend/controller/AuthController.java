package com.chordconnect.backend.controller;

import com.chordconnect.backend.entity.User;
import com.chordconnect.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = request.get("username");
            String password = request.get("password");
            String email = request.get("email");

            // Check if user already exists
            User existingUser = userService.findByUsername(username);
            if (existingUser != null) {
                response.put("status", "error");
                response.put("message", "Username already exists");
                return response;
            }

            User user = new User(username, password, email);
            User savedUser = userService.simpleSave(user);

            // Universal success response
            response.put("status", "success");
            response.put("message", "Registration successful!");

            Map<String, Object> userData = new HashMap<>();
            userData.put("userID", savedUser.getId());
            userData.put("username", savedUser.getUsername());
            userData.put("email", savedUser.getEmail());
            response.put("data", userData);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Registration failed: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = request.get("username");
            String password = request.get("password");

            User user = userService.findByUsername(username);
            if (user == null) {
                response.put("status", "error");
                response.put("message", "User not found");
                return response;
            }

            if (user.getPassword().equals(password)) {
                response.put("status", "success");
                response.put("message", "Login successful!");

                Map<String, Object> userData = new HashMap<>();
                userData.put("userID", user.getId());
                userData.put("username", user.getUsername());
                userData.put("email", user.getEmail());
                response.put("data", userData);
            } else {
                response.put("status", "error");
                response.put("message", "Invalid password");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Login error: " + e.getMessage());
        }
        return response;
    }
}