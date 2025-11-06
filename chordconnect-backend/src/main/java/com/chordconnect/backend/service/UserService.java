package com.chordconnect.backend.service;

import com.chordconnect.backend.entity.User;
import com.chordconnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User simpleSave(User user) {
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> findRecentJammers() {
        return userRepository.findTop5ByOrderByLastLoginDesc();
    }

    // DEPRECATED: Placeholder method for saving preferences - now handled by UserPreferencesService
    public void saveUserPreferences(Long userId, String instrument, String skillLevel,
                                    String favoriteGenres, String practiceGoals, String learningStyle) {
        System.out.println("DEPRECATED: Use UserPreferencesService instead");
        System.out.println("Saving preferences for user: " + userId);
        System.out.println("Instrument: " + instrument);
        System.out.println("Skill Level: " + skillLevel);
    }

    // DEPRECATED: Placeholder method for getting preferences - now handled by UserPreferencesService
    public Map<String, Object> getUserPreferences(Long userId) {
        System.out.println("DEPRECATED: Use UserPreferencesService instead");
        // Return dummy data
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("instrument", "Guitar");
        preferences.put("skillLevel", "Beginner");
        preferences.put("favoriteGenres", "Rock, Pop");
        preferences.put("practiceGoals", "Learn chords");
        preferences.put("learningStyle", "Visual");
        return preferences;
    }


    public List<User> searchUsers(String query) {
        // Simple search - you can make this more sophisticated
        return userRepository.findByUsernameContainingIgnoreCase(query);
    }
}