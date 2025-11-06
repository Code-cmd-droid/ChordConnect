package com.chordconnect.backend.service;

import com.chordconnect.backend.entity.ChatMessage;
import com.chordconnect.backend.entity.User;
import com.chordconnect.backend.repository.ChatMessageRepository;
import com.chordconnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    public ChatMessage saveMessage(Long roomId, Long userId, String message) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        ChatMessage chatMessage = new ChatMessage(roomId, userOpt.get(), message);
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getMessageHistory(Long roomId) {
        return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);
    }

    public List<ChatMessage> getRecentMessages(Long roomId, int limit) {
        return chatMessageRepository.findRecentByRoomId(roomId, limit);
    }

    public Long generateRoomId(Long user1Id, Long user2Id) {
        // Generate consistent room ID for two users
        long min = Math.min(user1Id, user2Id);
        long max = Math.max(user1Id, user2Id);
        return min * 10000 + max;
    }
}