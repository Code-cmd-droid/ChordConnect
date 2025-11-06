package com.chordconnect.backend.service;

import com.chordconnect.backend.entity.Friendship;
import com.chordconnect.backend.entity.User;
import com.chordconnect.backend.repository.FriendshipRepository;
import com.chordconnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    public void sendFriendRequest(Long userId, Long friendId) {
        // Check if friendship already exists
        List<Friendship> existingFriendships = friendshipRepository.findByUserIdOrFriendId(userId, friendId);
        boolean alreadyExists = existingFriendships.stream()
                .anyMatch(fs -> (fs.getUserId().equals(userId) && fs.getFriendId().equals(friendId)) ||
                        (fs.getUserId().equals(friendId) && fs.getFriendId().equals(userId)));

        if (!alreadyExists) {
            Friendship friendship = new Friendship(userId, friendId);
            friendshipRepository.save(friendship);
        }
    }

    public List<User> getFriends(Long userId) {
        List<Friendship> friendships = friendshipRepository.findByUserIdAndStatus(userId, "accepted");
        return friendships.stream()
                .map(friendship -> userRepository.findById(friendship.getFriendId()).orElse(null))
                .filter(user -> user != null)
                .collect(Collectors.toList());
    }

    public List<User> getRecentJammers(Long userId) {
        return userRepository.findTop5ByOrderByLastLoginDesc()
                .stream()
                .filter(user -> !user.getId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<Friendship> getPendingRequests(Long userId) {
        return friendshipRepository.findByFriendIdAndStatus(userId, "pending");
    }

    public void acceptFriendRequest(Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId).orElse(null);
        if (friendship != null) {
            friendship.setStatus("accepted");
            friendshipRepository.save(friendship);
        }
    }
}