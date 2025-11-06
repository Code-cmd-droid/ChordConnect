package com.chordconnect.backend.service;

import com.chordconnect.backend.model.User;
import com.chordconnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // ❌ Remove PasswordEncoder dependency temporarily
    // @Autowired
    // private PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        // ❌ Store plain text password temporarily (for development only)
        user.setPassword(password); // In production, use: passwordEncoder.encode(password)
        user.setEmail(email);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User updateLastLogin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            return userRepository.save(user);
        }
        return null;
    }

    public List<User> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCase(query);
    }

    public List<User> getRecentJammers(Long userId) {
        return userRepository.findRecentJammers(userId);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // ✅ Add a simple password check method (for development)
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        // Simple comparison for development
        return rawPassword.equals(encodedPassword);
    }
}