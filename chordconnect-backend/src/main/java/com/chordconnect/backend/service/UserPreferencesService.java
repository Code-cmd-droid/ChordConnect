package com.chordconnect.backend.service;

import com.chordconnect.backend.entity.User;
import com.chordconnect.backend.entity.UserPreferences;
import com.chordconnect.backend.repository.UserPreferencesRepository;
import com.chordconnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserPreferencesService {

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    @Autowired
    private UserRepository userRepository;

    // Updated method to accept userId instead of User object
    public UserPreferences savePreferences(Long userId, String instruments, String genres,
                                           String languages, String gender, String ageGroup) {
        UserPreferences preferences = userPreferencesRepository.findByUserId(userId)
                .orElse(new UserPreferences());

        preferences.setUserId(userId);  // Set userId directly instead of User object
        preferences.setInstruments(instruments);
        preferences.setGenres(genres);
        preferences.setLanguages(languages);
        preferences.setGender(gender);
        preferences.setAgeGroup(ageGroup);

        return userPreferencesRepository.save(preferences);
    }

    // Keep existing method for backward compatibility
    public UserPreferences savePreferences(User user, String instruments, String genres,
                                           String languages, String gender, String ageGroup) {
        return savePreferences(user.getUserId(), instruments, genres, languages, gender, ageGroup);
    }

    public UserPreferences getPreferencesByUserId(Long userId) {
        return userPreferencesRepository.findByUserId(userId).orElse(null);
    }

    // New method to check if user has preferences
    public boolean hasPreferences(Long userId) {
        return userPreferencesRepository.findByUserId(userId).isPresent();
    }
}