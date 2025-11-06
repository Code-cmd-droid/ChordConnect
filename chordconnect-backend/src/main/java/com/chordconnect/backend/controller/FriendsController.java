package com.chordconnect.backend.controller;

import com.chordconnect.backend.entity.User;
import com.chordconnect.backend.service.FriendshipService;
import com.chordconnect.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "http://localhost:3000")
public class FriendsController {

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private UserService userService;

    @PostMapping("/{userId}/add/{friendId}")
    public ResponseEntity<?> addFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        Map<String, Object> response = new HashMap<>();
        try {
            friendshipService.sendFriendRequest(userId, friendId);
            response.put("status", "success");
            response.put("message", "Friend request sent");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to send friend request: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{userId}/list")
    public ResponseEntity<?> getFriends(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<User> friends = friendshipService.getFriends(userId);
            List<Map<String, Object>> friendsList = friends.stream()
                    .map(user -> {
                        Map<String, Object> friendData = new HashMap<>();
                        friendData.put("id", user.getId());  // Use getId() instead of getUserId()
                        friendData.put("username", user.getUsername());
                        friendData.put("email", user.getEmail());
                        return friendData;
                    })
                    .collect(Collectors.toList());

            response.put("status", "success");
            response.put("data", friendsList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to get friends: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}