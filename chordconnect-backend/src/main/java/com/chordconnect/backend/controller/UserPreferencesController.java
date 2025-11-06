package com.chordconnect.backend.controller;

import com.chordconnect.backend.entity.User;
import com.chordconnect.backend.entity.UserPreferences;
import com.chordconnect.backend.service.UserService;
import com.chordconnect.backend.service.UserPreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserPreferencesController {

    @Autowired
    private UserPreferencesService userPreferencesService;

    @Autowired
    private UserService userService;

    @PostMapping("/{userId}/preferences")
    public ResponseEntity<?> savePreferences(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.findById(userId);
            UserPreferences preferences = userPreferencesService.savePreferences(
                    userId,  // Use userId directly
                    request.get("instruments"),
                    request.get("genres"),
                    request.get("languages"),
                    request.get("gender"),
                    request.get("ageGroup")
            );
            response.put("status", "success");
            response.put("message", "Preferences saved successfully");
            response.put("data", Map.of(
                    "instruments", preferences.getInstruments(),
                    "genres", preferences.getGenres(),
                    "languages", preferences.getLanguages(),
                    "gender", preferences.getGender(),
                    "ageGroup", preferences.getAgeGroup()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to save preferences: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{userId}/preferences")
    public ResponseEntity<?> getPreferences(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserPreferences preferences = userPreferencesService.getPreferencesByUserId(userId);

            if (preferences != null) {
                response.put("status", "success");
                response.put("data", Map.of(
                        "instruments", preferences.getInstruments(),
                        "genres", preferences.getGenres(),
                        "languages", preferences.getLanguages(),
                        "gender", preferences.getGender(),
                        "ageGroup", preferences.getAgeGroup()
                ));
            } else {
                response.put("status", "success");
                response.put("data", Map.of());
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to get preferences: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}