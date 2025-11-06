package com.chordconnect.backend.controller;

import com.chordconnect.backend.dto.LoginRequest;
import com.chordconnect.backend.dto.RegisterRequest;
import com.chordconnect.backend.dto.ApiResponse;
import com.chordconnect.backend.model.User;
import com.chordconnect.backend.service.UserService;
import com.chordconnect.backend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Optional<User> userOpt = userService.findByUsername(request.getUsername());

            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid username or password"));
            }

            User user = userOpt.get();

            // ✅ Simple password check (for development)
            if (!userService.checkPassword(request.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid username or password"));
            }

            // Update last login
            userService.updateLastLogin(user.getId());

            // Generate token
            String token = jwtService.generateToken(user.getUsername(), user.getId());

            // Prepare response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);
            responseData.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail()
            ));

            return ResponseEntity.ok(ApiResponse.success(responseData, "Login successful"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Check if username exists
            if (userService.usernameExists(request.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Username already exists"));
            }

            // Check if email exists
            if (userService.emailExists(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email already exists"));
            }

            // Create user (password will be stored in plain text temporarily)
            User user = userService.createUser(request.getUsername(), request.getPassword(), request.getEmail());

            // Generate token
            String token = jwtService.generateToken(user.getUsername(), user.getId());

            // Prepare response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);
            responseData.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail()
            ));

            return ResponseEntity.ok(ApiResponse.success(responseData, "Registration successful"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            String username = jwtService.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);

            if (userOpt.isPresent() && jwtService.validateToken(token, username)) {
                User user = userOpt.get();
                Map<String, Object> userData = Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail()
                );
                return ResponseEntity.ok(ApiResponse.success(userData, "Token is valid"));
            }

            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid token"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Token validation failed"));
        }
    }
}